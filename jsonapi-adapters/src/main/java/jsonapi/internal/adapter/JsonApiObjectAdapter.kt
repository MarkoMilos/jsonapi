package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.JsonApiObject
import jsonapi.JsonFormatException
import jsonapi.Meta
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

internal class JsonApiObjectAdapter(moshi: Moshi) : JsonAdapter<JsonApiObject>() {

  private val metaAdapter = moshi.adapter(Meta::class.java)
  private val stringAdapter = moshi.adapter(String::class.java)
  private val stringArrayAdapter = moshi.adapter<List<String>>(
    Types.newParameterizedType(List::class.java, String::class.java)
  )

  override fun fromJson(reader: JsonReader): JsonApiObject? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that root of json api object is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException(
        "Member 'jsonapi' MUST be a JSON object but found " +
          reader.peek() +
          " on path " +
          reader.path
      )
    }

    // Standard structure of json api object
    var version: String? = null
    var ext: List<String>? = null
    var profile: List<String>? = null
    var meta: Meta? = null

    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_VERSION -> version = stringAdapter.fromJson(reader)
        NAME_EXTENSIONS -> ext = stringArrayAdapter.fromJson(reader)
        NAME_PROFILE -> profile = stringArrayAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    return JsonApiObject(version, ext, profile, meta)
  }

  override fun toJson(writer: JsonWriter, value: JsonApiObject?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_VERSION).value(value.version)
    writer.name(NAME_EXTENSIONS).apply { stringArrayAdapter.toJson(writer, value.ext) }
    writer.name(NAME_PROFILE).apply { stringArrayAdapter.toJson(writer, value.profile) }
    writer.name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
    writer.endObject()
  }

  companion object {
    private const val NAME_VERSION = "version"
    private const val NAME_EXTENSIONS = "ext"
    private const val NAME_PROFILE = "profile"
    private const val NAME_META = "meta"

    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == JsonApiObject::class.java) JsonApiObjectAdapter(moshi) else null
    }
  }
}
