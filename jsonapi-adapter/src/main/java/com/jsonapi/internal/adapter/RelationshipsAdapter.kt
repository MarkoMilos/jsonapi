package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.Relationship
import com.jsonapi.Relationships
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

internal class RelationshipsAdapter(moshi: Moshi) : JsonAdapter<Relationships>() {

  private val membersAdapter: JsonAdapter<Map<String, Relationship?>> = moshi.adapter(
    Types.newParameterizedType(Map::class.java, String::class.java, Relationship::class.java)
  )

  override fun fromJson(reader: JsonReader): Relationships? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that relationship is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonApiException(
        "Relationships MUST be a JSON object but found "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }

    // Read original members where relationship values can be null
    val sourceMembers = membersAdapter.fromJson(reader) ?: return null

    // Create new members map ignoring all relationships with null values
    val members = mutableMapOf<String, Relationship>()
    sourceMembers.forEach { (key, value) ->
      if (value != null) {
        members[key] = value
      }
    }

    return Relationships(members)
  }

  override fun toJson(writer: JsonWriter, value: Relationships?) {
    membersAdapter.toJson(writer, value?.members)
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
        if (type.rawType() != Relationships::class.java) return null
        return RelationshipsAdapter(moshi)
      }
    }
  }
}
