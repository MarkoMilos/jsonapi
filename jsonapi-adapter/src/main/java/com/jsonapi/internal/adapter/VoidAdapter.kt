package com.jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

/** Adapter for [Nothing] / [Void] which is not included in Moshi built in adapters for standard types. */
internal class VoidAdapter : JsonAdapter<Nothing>() {
  override fun fromJson(reader: JsonReader): Nothing? {
    reader.skipValue()
    return null
  }
  
  override fun toJson(writer: JsonWriter, value: Nothing?) {
    writer.nullValue()
  }
}