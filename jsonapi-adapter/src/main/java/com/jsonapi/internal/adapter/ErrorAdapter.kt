package com.jsonapi.internal.adapter

import com.jsonapi.Error
import com.jsonapi.Error.Source
import com.jsonapi.JsonApiException
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi

internal class ErrorAdapter(moshi: Moshi) : JsonAdapter<Error>() {

  private val stringAdapter = moshi.adapter(String::class.java)
  private val sourceAdapter = moshi.adapter(Source::class.java)
  private val linksAdapter = moshi.adapter(Links::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)

  override fun fromJson(reader: JsonReader): Error? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that resource object is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonApiException(
        "Error MUST be a JSON object but found "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }

    // Standard structure of Error
    var id: String? = null
    var links: Links? = null
    var status: String? = null
    var code: String? = null
    var title: String? = null
    var detail: String? = null
    var source: Source? = null
    var meta: Meta? = null

    // Read the Error
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_ID -> id = stringAdapter.fromJson(reader)
        NAME_LINKS -> links = linksAdapter.fromJson(reader)
        NAME_STATUS -> status = stringAdapter.fromJson(reader)
        NAME_CODE -> code = stringAdapter.fromJson(reader)
        NAME_TITLE -> title = stringAdapter.fromJson(reader)
        NAME_DETAIL -> detail = stringAdapter.fromJson(reader)
        NAME_SOURCE -> source = sourceAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    return Error(id, status, code, title, detail, source, links, meta)
  }

  override fun toJson(writer: JsonWriter, value: Error?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_ID).value(value.id)
    writer.name(NAME_LINKS).apply { linksAdapter.toJson(writer, value.links) }
    writer.name(NAME_STATUS).value(value.status)
    writer.name(NAME_CODE).value(value.code)
    writer.name(NAME_TITLE).value(value.title)
    writer.name(NAME_DETAIL).value(value.detail)
    writer.name(NAME_SOURCE).apply { sourceAdapter.toJson(writer, value.source) }
    writer.name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
    writer.endObject()
  }

  companion object {
    private const val NAME_ID = "id"
    private const val NAME_LINKS = "links"
    private const val NAME_STATUS = "status"
    private const val NAME_CODE = "code"
    private const val NAME_TITLE = "title"
    private const val NAME_DETAIL = "detail"
    private const val NAME_SOURCE = "source"
    private const val NAME_META = "meta"

    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == Error::class.java) ErrorAdapter(moshi) else null
    }
  }
}
