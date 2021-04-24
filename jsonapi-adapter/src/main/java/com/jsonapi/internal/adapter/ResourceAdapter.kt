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
    val adapter = peeked.use { findAdapter(it) }
    return adapter.fromJson(reader)
  }
  
  private fun findAdapter(reader: JsonReader): JsonAdapter<Any> {
    reader.beginObject()
    while (reader.hasNext()) {
      // Find 'type' member
      if (reader.selectName(memberNameOptions) == -1) {
        reader.skipName()
        reader.skipValue()
        continue
      }
      // Check if type member value is within options
      val typeNameIndex = reader.selectString(typeNameOptions)
      return if (typeNameIndex != -1) {
        // Type member value found withing options, return corresponding registered type adapter
        typeAdapters[typeNameIndex]
      } else if (typeNameIndex == -1 && defaultResourceAdapter != null) {
        if (reader.peek() == Token.STRING) {
          // Type member value is a string but not found within options.
          // Configured default adapter can be used.
          defaultResourceAdapter
        } else {
          // Type member value is not a string
          throw JsonApiException("The value of top level member 'type' MUST be string.")
        }
      } else {
        // Type not found within options and default adapter is not configured
        throw JsonApiException(
          "Expected one of "
            + typeNames
            + " for member 'type' but found '"
            + runCatching { reader.nextString() }.getOrDefault("null")
            + "'. Register this type or use allowUnregisteredTypes(true)."
        )
      }
    }
    // Top level member type not found for this resource json
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