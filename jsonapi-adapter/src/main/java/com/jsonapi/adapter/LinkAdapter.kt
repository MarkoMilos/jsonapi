package com.jsonapi.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.model.Link
import com.jsonapi.model.Link.LinkObject
import com.jsonapi.model.Link.LinkURI
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi

class LinkAdapter(moshi: Moshi) : JsonAdapter<Link>() {
  
  private val linkObjectAdapter: JsonAdapter<LinkObject> = moshi.adapter(LinkObject::class.java)
  
  override fun fromJson(reader: JsonReader): Link? {
    return when (reader.peek()) {
      Token.NULL -> reader.nextNull()
      Token.STRING -> LinkURI(reader.nextString())
      Token.BEGIN_OBJECT -> linkObjectAdapter.fromJson(reader)
      else -> throw JsonApiException(
        "A link MUST be represented as either:\n"
          + " * a string whose value is a URI-reference pointing to the link’s target\n"
          + " * a link object that represents a web link per specification\n"
          + "but was ${reader.peek()} on path ${reader.path}"
      )
    }
  }
  
  override fun toJson(writer: JsonWriter, value: Link?) {
    when (value) {
      is LinkURI -> writer.value(value.uri)
      is LinkObject -> linkObjectAdapter.toJson(writer, value)
      null -> writer.nullValue()
    }
  }
}