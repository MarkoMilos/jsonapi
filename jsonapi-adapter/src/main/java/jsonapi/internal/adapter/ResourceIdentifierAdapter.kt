package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import jsonapi.JsonFormatException
import jsonapi.Meta
import jsonapi.ResourceIdentifier
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

internal class ResourceIdentifierAdapter(moshi: Moshi) : JsonAdapter<ResourceIdentifier>() {

  private val stringAdapter = moshi.adapter(String::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)

  override fun fromJson(reader: JsonReader): ResourceIdentifier? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that resource identifier is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException(
        "Resource identifier MUST be a JSON object but found "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }

    // Standard structure of a resource identifier
    var type: String? = null
    var id: String? = null
    var lid: String? = null
    var meta: Meta? = null

    // Read the resource identifier
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_TYPE -> type = stringAdapter.fromJson(reader)
        NAME_ID -> id = stringAdapter.fromJson(reader)
        NAME_LID -> lid = stringAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    // Type is required, it should not be null or empty
    if (type.isNullOrEmpty()) {
      throw JsonFormatException(
        "A resource identifier MUST contain non-null/non-empty type member but it was not found on path: ${reader.path}"
      )
    }

    // Identifier is required so id or lid should not be empty
    if (id.isNullOrBlank() && lid.isNullOrBlank()) {
      throw JsonFormatException(
        "A resource identifier MUST contain an 'id' or 'lid' member but both were null or blank on path ${reader.path}"
      )
    }

    return ResourceIdentifier(type, id, lid, meta)
  }

  override fun toJson(writer: JsonWriter, value: ResourceIdentifier?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_TYPE).value(value.type)
    writer.name(NAME_ID).value(value.id)
    writer.name(NAME_LID).value(value.lid)
    writer.name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
    writer.endObject()
  }

  companion object {
    private const val NAME_TYPE = "type"
    private const val NAME_ID = "id"
    private const val NAME_LID = "lid"
    private const val NAME_META = "meta"

    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == ResourceIdentifier::class.java) ResourceIdentifierAdapter(moshi) else null
    }
  }
}
