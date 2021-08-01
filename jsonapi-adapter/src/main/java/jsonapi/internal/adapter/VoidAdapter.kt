package jsonapi.internal.adapter

import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

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
    internal val FACTORY = FactoryDelegate { type, annotations, _, _ ->
      if (annotations.isEmpty() && type.rawType() == Void::class.java) VoidAdapter() else null
    }
  }
}
