package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.JsonFormatException
import jsonapi.Link
import jsonapi.Link.LinkObject
import jsonapi.Meta
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

internal class LinkObjectAdapter(moshi: Moshi) : JsonAdapter<LinkObject>() {

  private val linkAdapter = moshi.adapter(Link::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)
  private val stringAdapter = moshi.adapter(String::class.java)
  private val listAdapter: JsonAdapter<List<String>> =
    moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))

  override fun fromJson(reader: JsonReader): LinkObject? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Standard structure of link object
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
        NAME_HREF -> href = stringAdapter.fromJson(reader)
        NAME_REL -> rel = stringAdapter.fromJson(reader)
        NAME_DESCRIBED_BY -> describedby = linkAdapter.fromJson(reader)
        NAME_TITLE -> title = stringAdapter.fromJson(reader)
        NAME_TYPE -> type = stringAdapter.fromJson(reader)
        NAME_HREF_LANG -> hreflang = deserializeHrefLang(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    if (href == null) {
      throw JsonFormatException("A link object MUST contain member [href].")
    }

    return LinkObject(href, rel, describedby, title, type, hreflang, meta)
  }

  override fun toJson(writer: JsonWriter, value: LinkObject?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_HREF).value(value.href)
    writer.name(NAME_REL).value(value.rel)
    writer.name(NAME_DESCRIBED_BY).apply { linkAdapter.toJson(writer, value.describedBy) }
    writer.name(NAME_TITLE).value(value.title)
    writer.name(NAME_TYPE).value(value.type)
    writer.name(NAME_HREF_LANG).apply { serializeHrefLang(writer, value.hreflang) }
    writer.name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
    writer.endObject()
  }

  private fun deserializeHrefLang(reader: JsonReader): List<String>? {
    return when (reader.peek()) {
      Token.NULL -> reader.nextNull()
      Token.STRING -> listOf(reader.nextString())
      Token.BEGIN_ARRAY -> listAdapter.fromJson(reader)
      else -> throw JsonFormatException(
        "Member [hreflang] MUST be a string or an array of strings that indicates the language(s) of the link's target" +
          " but was " +
          reader.peek() +
          " on path " +
          reader.path
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
    private const val NAME_HREF = "href"
    private const val NAME_REL = "rel"
    private const val NAME_DESCRIBED_BY = "describedby"
    private const val NAME_TITLE = "title"
    private const val NAME_TYPE = "type"
    private const val NAME_HREF_LANG = "hreflang"
    private const val NAME_META = "meta"

    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == LinkObject::class.java) LinkObjectAdapter(moshi) else null
    }
  }
}
