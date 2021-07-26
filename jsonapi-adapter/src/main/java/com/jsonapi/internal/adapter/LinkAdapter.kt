package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.Link
import com.jsonapi.Link.LinkObject
import com.jsonapi.Link.URI
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal class LinkAdapter(moshi: Moshi) : JsonAdapter<Link>() {

  private val linkObjectAdapter = moshi.adapter(LinkObject::class.java)

  override fun fromJson(reader: JsonReader): Link? {
    return when (reader.peek()) {
      Token.NULL -> reader.nextNull()
      Token.STRING -> URI(reader.nextString())
      Token.BEGIN_OBJECT -> linkObjectAdapter.fromJson(reader)
      else -> throw JsonApiException(
        "A link MUST be represented as either:\n"
          + " * a string whose value is a URI-reference pointing to the link’s target\n"
          + " * a link object that represents a web link per specification\n"
          + "but was "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }
  }

  override fun toJson(writer: JsonWriter, value: Link?) {
    when (value) {
      is URI -> writer.value(value.value)
      is LinkObject -> linkObjectAdapter.toJson(writer, value)
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
        if (type.rawType() != Link::class.java) return null
        return LinkAdapter(moshi)
      }
    }
  }
}
