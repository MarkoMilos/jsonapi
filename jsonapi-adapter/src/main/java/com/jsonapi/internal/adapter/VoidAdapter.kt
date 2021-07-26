package com.jsonapi.internal.adapter

import com.jsonapi.internal.FactoryDelegate
import com.jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/** Adapter for [Nothing] / [Void] which is not included in Moshi built in adapters for standard types. */
internal class VoidAdapter : JsonAdapter<Nothing>() {
  override fun fromJson(reader: JsonReader): Nothing? {
    // Skip this value but don't throw if passed reader is configured to do so
    val wasFailOnUnknown = reader.failOnUnknown()
    reader.setFailOnUnknown(false)
    reader.skipValue()
    reader.setFailOnUnknown(wasFailOnUnknown)
    return null
  }

  override fun toJson(writer: JsonWriter, value: Nothing?) {
    writer.nullValue()
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
        if (type.rawType() != Void::class.java) return null
        return VoidAdapter()
      }
    }
  }
}
