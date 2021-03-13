package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.internal.NAME_TYPE
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import java.lang.reflect.Type

internal class ResourceAdapter constructor(
  private val types: List<Type>,
  private val typeNames: List<String>,
  private val typeAdapters: List<JsonAdapter<Any>>,
  private val defaultResourceAdapter: JsonAdapter<Any>?
) : JsonAdapter<Any>() {
  
  private val memberNameOptions = JsonReader.Options.of(NAME_TYPE)
  private val typeNameOptions = JsonReader.Options.of(*typeNames.toTypedArray())
  
  override fun fromJson(reader: JsonReader): Any? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }
    
    // Assert that resource is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonApiException("Resource MUST be a JSON object but found ${reader.peek()}")
    }
    
    // Find type adapter and deserialize resource
    val peeked = reader.peekJson()
    peeked.setFailOnUnknown(false)
    val typeNameIndex = peeked.use { typeNameIndex(it) }
    return if (typeNameIndex != -1) {
      typeAdapters[typeNameIndex].fromJson(reader)
    } else {
      defaultResourceAdapter!!.fromJson(reader)
    }
  }
  
  private fun typeNameIndex(reader: JsonReader): Int {
    reader.beginObject()
    while (reader.hasNext()) {
      if (reader.selectName(memberNameOptions) == -1) {
        reader.skipName()
        reader.skipValue()
        continue
      }
      val typeNameIndex = reader.selectString(typeNameOptions)
      if (typeNameIndex == -1 && defaultResourceAdapter == null) {
        throw JsonApiException(
          "Expected one of "
            + typeNames
            + " for member 'type' but found '"
            + runCatching { reader.nextString() }.getOrDefault("null")
            + "'. Register this type or use allowUnregisteredTypes(true)."
        )
      }
      return typeNameIndex
    }
    
    throw JsonApiException(
      "Resource object MUST contain top-level member 'type' but it was not found on path ${reader.path}"
    )
  }
  
  override fun toJson(writer: JsonWriter, value: Any?) {
    // Serialize null values as null
    if (value == null) {
      writer.nullValue()
      return
    }
    
    // Find adapter for this type and delegate serialization to it
    val typeIndex = types.indexOf(value.javaClass)
    val adapter = if (typeIndex != -1) {
      typeAdapters[typeIndex]
    } else {
      defaultResourceAdapter ?: throw JsonApiException(
        "Type '${value.javaClass}' not found in registered types."
          + "\nRegister this type or use allowUnregisteredTypes(true)."
          + "\nRegistered types: "
          + types.joinToString("\n  * ", "\n  * ")
      )
    }
    adapter.toJson(writer, value)
  }
}