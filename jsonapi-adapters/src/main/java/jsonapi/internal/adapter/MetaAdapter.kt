package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.Meta
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

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
