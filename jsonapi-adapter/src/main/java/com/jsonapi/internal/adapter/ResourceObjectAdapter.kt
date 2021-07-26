package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationships
import com.jsonapi.ResourceObject
import com.jsonapi.internal.NAME_ID
import com.jsonapi.internal.NAME_LID
import com.jsonapi.internal.NAME_LINKS
import com.jsonapi.internal.NAME_META
import com.jsonapi.internal.NAME_RELATIONSHIPS
import com.jsonapi.internal.NAME_TYPE
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal class ResourceObjectAdapter(moshi: Moshi) : JsonAdapter<ResourceObject>() {

  private val stringAdapter = moshi.adapter(String::class.java)
  private val relationshipsAdapter = moshi.adapter(Relationships::class.java)
  private val linksAdapter = moshi.adapter(Links::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)

  override fun fromJson(reader: JsonReader): ResourceObject? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == JsonReader.Token.NULL) {
      return reader.nextNull()
    }

    // Assert that resource object is JSON object
    if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
      throw JsonApiException("Resource MUST be a JSON object but found ${reader.peek()} on path ${reader.path}")
    }

    // Standard structure of a resource object
    var type: String? = null
    var id: String? = null
    var lid: String? = null
    var relationships: Relationships? = null
    var links: Links? = null
    var meta: Meta? = null

    // Read the resource object
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_TYPE -> type = stringAdapter.fromJson(reader)
        NAME_ID -> id = stringAdapter.fromJson(reader)
        NAME_LID -> lid = stringAdapter.fromJson(reader)
        NAME_RELATIONSHIPS -> relationships = relationshipsAdapter.fromJson(reader)
        NAME_LINKS -> links = linksAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    // Type is required, it should not be null or empty
    if (type.isNullOrEmpty()) {
      throw JsonApiException("A resource object MUST contain non-null, non-empty type member.")
    }

    // Identifier is required so id or lid should not be empty
    if (id.isNullOrBlank() && lid.isNullOrBlank()) {
      throw JsonApiException("A resource object MUST contain an 'id' or 'lid' member but both were null or blank.")
    }

    // Create and return resource object with deserialized members
    return ResourceObject(type, id, lid, relationships, links, meta)
  }

  override fun toJson(writer: JsonWriter, value: ResourceObject?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    // Serialize resource object using standard adapters
    writer
      .beginObject()
      .name(NAME_TYPE).value(value.type)
      .name(NAME_ID).value(value.id)
      .name(NAME_LID).value(value.lid)
      .name(NAME_RELATIONSHIPS).apply { relationshipsAdapter.toJson(writer, value.relationships) }
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
        if (type.rawType() != ResourceObject::class.java) return null
        return ResourceObjectAdapter(moshi)
      }
    }
  }
}
