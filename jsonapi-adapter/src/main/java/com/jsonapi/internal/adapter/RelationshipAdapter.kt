package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.Relationship
import com.jsonapi.Relationship.ToMany
import com.jsonapi.Relationship.ToOne
import com.jsonapi.internal.NAME_DATA
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.jsonapi.internal.scan
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal class RelationshipAdapter(moshi: Moshi) : JsonAdapter<Relationship>() {

  private val toOneAdapter = moshi.adapter<Relationship>(ToOne::class.java)
  private val toManyAdapter = moshi.adapter<Relationship>(ToMany::class.java)

  override fun fromJson(reader: JsonReader): Relationship? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that relationship is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonApiException(
        "Relationship MUST be a JSON object but found "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }

    // Scan json to find data member and determine adapter without consuming source reader
    val adapter = reader.scan { findAdapter(it) }

    // Deserialize relationship using adapter found
    return adapter.fromJson(reader)
  }

  private fun findAdapter(reader: JsonReader): JsonAdapter<Relationship> {
    // The value of the relationships key MUST be an object (a “relationships object”)
    reader.beginObject()
    // Search trough members to find data member and determine if this to-one or to-many relationship
    while (reader.hasNext()) {
      if (reader.nextName() == NAME_DATA) {
        // If data member begins with array it is to-many relationship.
        // Everything else (object/null) is deserialized with as to-one relationship
        return if (reader.peek() == Token.BEGIN_ARRAY) toManyAdapter else toOneAdapter
      }
      reader.skipValue()
    }
    // If data member was not found then deserialize relationship as to-one
    return toOneAdapter
  }

  override fun toJson(writer: JsonWriter, value: Relationship?) {
    when (value) {
      is ToOne -> toOneAdapter.toJson(writer, value)
      is ToMany -> toManyAdapter.toJson(writer, value)
      null -> writer.nullValue()
    }
  }

  companion object {
    internal val FACTORY = object : FactoryDelegate {
      override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
        parent: Factory
      ): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null
        if (type.rawType() != Relationship::class.java) return null
        return RelationshipAdapter(moshi)
      }
    }
  }
}
