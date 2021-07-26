package com.jsonapi.internal.adapter

import com.jsonapi.Meta
import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

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
    internal val FACTORY = object : FactoryDelegate {
      override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
        parent: Factory
      ): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null
        if (type.rawType() != Meta::class.java) return null
        return MetaAdapter(moshi)
      }
    }
  }
}
