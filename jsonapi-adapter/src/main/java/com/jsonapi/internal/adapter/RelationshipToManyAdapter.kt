package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationship.ToMany
import com.jsonapi.ResourceIdentifier
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.NAME_DATA
import com.jsonapi.internal.NAME_LINKS
import com.jsonapi.internal.NAME_META
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

internal class RelationshipToManyAdapter(moshi: Moshi) : JsonAdapter<ToMany>() {

  private val dataAdapter: JsonAdapter<List<ResourceIdentifier>> =
    moshi.adapter(Types.newParameterizedType(List::class.java, ResourceIdentifier::class.java))
  private val linksAdapter = moshi.adapter(Links::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)

  override fun fromJson(reader: JsonReader): ToMany? {
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

    // Structure of ToMany relationship
    var data: List<ResourceIdentifier> = emptyList()
    var links: Links? = null
    var meta: Meta? = null

    // Read the ToMany relationship
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_DATA -> {
          data = if (reader.peek() == Token.BEGIN_ARRAY) {
            dataAdapter.fromJson(reader) ?: emptyList()
          } else {
            throw JsonApiException(
              "Resource linkage for to-many relationship MUST be represented as one of the following:\n"
                + " - an empty array ([]) for empty to-many relationships.\n"
                + " - an array of resource identifier objects for non-empty to-many relationships\n"
                + " but was "
                + reader.peek()
                + " on path "
                + reader.path
            )
          }
        }
        NAME_LINKS -> links = linksAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    return ToMany(data, links, meta)
  }

  override fun toJson(writer: JsonWriter, value: ToMany?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer
      .beginObject()
      .name(NAME_DATA).apply { dataAdapter.toJson(writer, value.data) }
      .name(NAME_LINKS).apply { linksAdapter.toJson(writer, value.links) }
      .name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
      .endObject()
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
        if (type.rawType() != ToMany::class.java) return null
        return RelationshipToManyAdapter(moshi)
      }
    }
  }
}
