package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import jsonapi.Error.Source
import jsonapi.JsonFormatException
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

internal class SourceAdapter(moshi: Moshi) : JsonAdapter<Source>() {

  private val stringAdapter = moshi.adapter(String::class.java)

  override fun fromJson(reader: JsonReader): Source? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that root of source is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException(
        "Source MUST be a JSON object but found " +
          reader.peek() +
          " on path " +
          reader.path
      )
    }

    var pointer: String? = null
    var parameter: String? = null
    var header: String? = null

    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_POINTER -> pointer = stringAdapter.fromJson(reader)
        NAME_PARAMETER -> parameter = stringAdapter.fromJson(reader)
        NAME_HEADER -> header = stringAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    return Source(pointer, parameter, header)
  }

  override fun toJson(writer: JsonWriter, value: Source?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_POINTER).value(value.pointer)
    writer.name(NAME_PARAMETER).value(value.parameter)
    writer.name(NAME_HEADER).value(value.header)
    writer.endObject()
  }

  companion object {
    private const val NAME_POINTER = "pointer"
    private const val NAME_PARAMETER = "parameter"
    private const val NAME_HEADER = "header"

    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == Source::class.java) SourceAdapter(moshi) else null
    }
  }
}
