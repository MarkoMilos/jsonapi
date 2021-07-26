package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationship.ToOne
import com.jsonapi.ResourceIdentifier
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.NAME_DATA
import com.jsonapi.internal.NAME_LINKS
import com.jsonapi.internal.NAME_META
import com.jsonapi.internal.rawType
import com.jsonapi.internal.writeNull
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal class RelationshipToOneAdapter(moshi: Moshi) : JsonAdapter<ToOne>() {

  private val dataAdapter = moshi.adapter(ResourceIdentifier::class.java)
  private val linksAdapter = moshi.adapter(Links::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)

  override fun fromJson(reader: JsonReader): ToOne? {
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

    // Structure of ToOne relationship
    var data: ResourceIdentifier? = null
    var links: Links? = null
    var meta: Meta? = null

    // Read the ToOne relationship
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_DATA -> data = dataAdapter.fromJson(reader)
        NAME_LINKS -> links = linksAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    return ToOne(data, links, meta)
  }

  override fun toJson(writer: JsonWriter, value: ToOne?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_DATA).apply {
      if (value.data == null) {
        // Serialize data member for empty ToOne relationship
        writer.writeNull()
      } else {
        // Serialize non-empty ToOne relationship with delegate adapter
        dataAdapter.toJson(writer, value.data)
      }
    }
    writer.name(NAME_LINKS).apply { linksAdapter.toJson(writer, value.links) }
    writer.name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
    writer.endObject()
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
        if (type.rawType() != ToOne::class.java) return null
        return RelationshipToOneAdapter(moshi)
      }
    }
  }
}
