package com.jsonapi.internal.adapter

import com.jsonapi.Meta
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
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
    val members = mapAdapter.fromJson(reader) ?: return null
    return Meta(members)
  }

  override fun toJson(writer: JsonWriter, value: Meta?) {
    mapAdapter.toJson(writer, value?.members)
  }

  companion object {
    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == Meta::class.java) MetaAdapter(moshi) else null
    }
  }
}
