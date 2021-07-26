package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.Link
import com.jsonapi.Link.LinkObject
import com.jsonapi.Meta
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

internal class LinkObjectAdapter(moshi: Moshi) : JsonAdapter<LinkObject>() {

  private val linkAdapter = moshi.adapter(Link::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)
  private val stringAdapter = moshi.adapter(String::class.java)
  private val listAdapter: JsonAdapter<List<String>> =
    moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))

  override fun fromJson(reader: JsonReader): LinkObject? {
    if (reader.peek() == Token.NULL) {
      // In case of a null value deserialize to null and consume token
      return reader.nextNull()
    }

    var href: String? = null
    var rel: String? = null
    var describedby: Link? = null
    var title: String? = null
    var type: String? = null
    var hreflang: List<String>? = null
    var meta: Meta? = null

    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        "href" -> href = stringAdapter.fromJson(reader)
        "rel" -> rel = stringAdapter.fromJson(reader)
        "describedby" -> describedby = linkAdapter.fromJson(reader)
        "title" -> title = stringAdapter.fromJson(reader)
        "type" -> type = stringAdapter.fromJson(reader)
        "hreflang" -> hreflang = deserializeHrefLang(reader)
        "meta" -> meta = metaAdapter.fromJson(reader)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    if (href == null) {
      throw JsonApiException("A link object MUST contain member [href].")
    }

    return LinkObject(href, rel, describedby, title, type, hreflang, meta)
  }

  override fun toJson(writer: JsonWriter, value: LinkObject?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
      .name("href").value(value.href)
      .name("rel").value(value.rel)
      .name("describedby").apply { linkAdapter.toJson(writer, value.describedby) }
      .name("title").value(value.title)
      .name("type").value(value.type)
      .name("hreflang").apply { serializeHrefLang(writer, value.hreflang) }
      .name("meta").apply { metaAdapter.toJson(writer, value.meta) }
      .endObject()
  }

  private fun deserializeHrefLang(reader: JsonReader): List<String>? {
    return when (reader.peek()) {
      Token.NULL -> reader.nextNull()
      Token.STRING -> listOf(reader.nextString())
      Token.BEGIN_ARRAY -> listAdapter.fromJson(reader)
      else -> throw JsonApiException(
        "Member [hreflang] MUST be a string or an array of strings that indicates the language(s) of the link's target" +
          " but was ${reader.peek()} on path ${reader.path}"
      )
    }
  }

  private fun serializeHrefLang(writer: JsonWriter, hrefLang: List<String>?) {
    if (hrefLang == null) {
      writer.nullValue()
      return
    }

    // When size of 'hreflang' is exactly 1 serialize it as string to conform specification
    // When size is greater than 1 serialize it as string array
    when (hrefLang.size) {
      0 -> writer.nullValue()
      1 -> writer.value(hrefLang.first())
      else -> listAdapter.toJson(writer, hrefLang)
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
        if (type.rawType() != LinkObject::class.java) return null
        return LinkObjectAdapter(moshi)
      }
    }
  }
}
