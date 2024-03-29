package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.Link
import jsonapi.Links
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.forceWriteNull
import jsonapi.internal.rawType

internal class LinksAdapter(moshi: Moshi) : JsonAdapter<Links>() {

  private val linkAdapter = moshi.adapter(Link::class.java)
  private val membersAdapter: JsonAdapter<Map<String, Link?>> = moshi.adapter(
    Types.newParameterizedType(Map::class.java, String::class.java, Link::class.java)
  )

  override fun fromJson(reader: JsonReader): Links? {
    val members = membersAdapter.fromJson(reader) ?: return null
    return Links(members)
  }

  override fun toJson(writer: JsonWriter, value: Links?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    value.members.forEach {
      writer.name(it.key)
      if (it.value == null) {
        // Null links must be serialized e.g. indicating no more pages for pagination
        // Force serialize null link avoiding writer 'serializeNulls' config
        writer.forceWriteNull()
      } else {
        // Serialize value using delegate adapter
        linkAdapter.toJson(writer, it.value)
      }
    }
    writer.endObject()
  }

  companion object {
    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == Links::class.java) LinksAdapter(moshi) else null
    }
  }
}
