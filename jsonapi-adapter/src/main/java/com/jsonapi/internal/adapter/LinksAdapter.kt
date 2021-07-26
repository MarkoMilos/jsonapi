package com.jsonapi.internal.adapter

import com.jsonapi.Link
import com.jsonapi.Links
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.jsonapi.internal.writeNull
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

internal class LinksAdapter(moshi: Moshi) : JsonAdapter<Links>() {

  private val linkAdapter = moshi.adapter(Link::class.java)
  private val membersAdapter: JsonAdapter<Map<String, Link?>> = moshi.adapter(
    Types.newParameterizedType(Map::class.java, String::class.java, Link::class.java)
  )

  override fun fromJson(reader: JsonReader): Links? {
    val members = membersAdapter.fromJson(reader) ?: return null
    return Links(members)
  }

  override fun toJson(writer: JsonWriter, value: Links?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    value.members.forEach {
      writer.name(it.key)
      if (it.value == null) {
        // Serialize null links (e.g. indicating no more pages for pagination)
        writer.writeNull()
      } else {
        // Serialize value using delegate adapter
        linkAdapter.toJson(writer, it.value)
      }
    }
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
        if (type.rawType() != Links::class.java) return null
        return LinksAdapter(moshi)
      }
    }
  }
}
