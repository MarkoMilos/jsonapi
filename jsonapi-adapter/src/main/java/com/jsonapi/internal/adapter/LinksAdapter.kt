package com.jsonapi.internal.adapter

import com.jsonapi.Link
import com.jsonapi.Links
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi

internal class LinksAdapter(moshi: Moshi) : JsonAdapter<Links>() {

  private val linkAdapter: JsonAdapter<Link> = moshi.adapter(Link::class.java)

  override fun fromJson(reader: JsonReader): Links? {
    if (reader.peek() == Token.NULL) {
      // In case of a null value deserialize to null and consume token
      return reader.nextNull()
    }

    // deserialize all members of this object to map
    val linksMap = mutableMapOf<String, Link>()
    reader.beginObject()
    while (reader.hasNext()) {
      val name = reader.nextName()
      val value = linkAdapter.fromJson(reader)
      if (value != null) {
        linksMap[name] = value
      }
    }
    reader.endObject()
    return Links(linksMap)
  }

  override fun toJson(writer: JsonWriter, value: Links?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer
      .beginObject()
      .apply {
        // deserialize all map entries as name value pairs
        value.members.forEach { entry ->
          name(entry.key)
          linkAdapter.toJson(this, entry.value)
        }
      }
      .endObject()
  }
}
