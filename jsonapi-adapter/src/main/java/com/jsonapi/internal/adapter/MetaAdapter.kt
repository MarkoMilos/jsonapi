package com.jsonapi.internal.adapter

import com.jsonapi.Meta
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

internal class MetaAdapter(moshi: Moshi) : JsonAdapter<Meta>() {

  private val mapAdapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(
    Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
  )

  override fun fromJson(reader: JsonReader): Meta? {
    if (reader.peek() == JsonReader.Token.NULL) {
      // In case of a null value deserialize to null and consume token
      return reader.nextNull()
    }
    // deserialize all values to map (nested objects will also be map)
    val members = mapAdapter.fromJson(reader) ?: emptyMap()
    return Meta(members)
  }

  override fun toJson(writer: JsonWriter, value: Meta?) {
    if (value == null) {
      writer.nullValue()
    } else {
      mapAdapter.toJson(writer, value.members)
    }
  }
}
