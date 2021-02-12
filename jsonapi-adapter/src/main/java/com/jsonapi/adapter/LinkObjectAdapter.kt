package com.jsonapi.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.model.Link
import com.jsonapi.model.Link.LinkObject
import com.jsonapi.model.Meta
import com.squareup.moshi.*
import com.squareup.moshi.JsonReader.Token

class LinkObjectAdapter(moshi: Moshi) : JsonAdapter<LinkObject>() {
  
  private val linkAdapter: JsonAdapter<Link> = moshi.adapter(Link::class.java)
  private val metaAdapter: JsonAdapter<Meta> = moshi.adapter(Meta::class.java)
  private val listAdapter: JsonAdapter<List<String>> =
    moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))
  private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java)
  
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
      throw JsonApiException("A link object MUST contain member [href]")
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
        "Member [hreflang] MUST be a string or an array of strings that indicates the language(s) of the link’s target"
      )
    }
  }
  
  private fun serializeHrefLang(writer: JsonWriter, hrefLang: List<String>?) {
    if (hrefLang == null) {
      writer.nullValue()
      return
    }
    
    // when size of hreflang is exactly 1 serialize it as string to conform specification
    // when size is greater than 1 serialize it as string array
    when (hrefLang.size) {
      0 -> writer.nullValue()
      1 -> writer.value(hrefLang.first())
      else -> listAdapter.toJson(writer, hrefLang)
    }
  }
}