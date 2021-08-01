package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.JsonFormatException
import jsonapi.Links
import jsonapi.Meta
import jsonapi.Relationship.ToMany
import jsonapi.ResourceIdentifier
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

internal class RelationshipToManyAdapter(moshi: Moshi) : JsonAdapter<ToMany>() {

  private val dataAdapter: JsonAdapter<List<ResourceIdentifier>> =
    moshi.adapter(Types.newParameterizedType(List::class.java, ResourceIdentifier::class.java))
  private val linksAdapter = moshi.adapter(Links::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)

  override fun fromJson(reader: JsonReader): ToMany? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that relationship is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException(
        "Relationship MUST be a JSON object but found "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }

    // Structure of ToMany relationship
    var data: List<ResourceIdentifier> = emptyList()
    var links: Links? = null
    var meta: Meta? = null

    // Read the ToMany relationship
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_DATA -> {
          data = if (reader.peek() == Token.BEGIN_ARRAY) {
            dataAdapter.fromJson(reader) ?: emptyList()
          } else {
            throw JsonFormatException(
              "Resource linkage for to-many relationship MUST be represented as one of the following:\n"
                + " - an empty array ([]) for empty to-many relationships.\n"
                + " - an array of resource identifier objects for non-empty to-many relationships\n"
                + " but was "
                + reader.peek()
                + " on path "
                + reader.path
            )
          }
        }
        NAME_LINKS -> links = linksAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        // Ignore non standard members (skip value)
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    return ToMany(data, links, meta)
  }

  override fun toJson(writer: JsonWriter, value: ToMany?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_DATA).apply { dataAdapter.toJson(writer, value.data) }
    writer.name(NAME_LINKS).apply { linksAdapter.toJson(writer, value.links) }
    writer.name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
    writer.endObject()
  }

  companion object {
    private const val NAME_DATA = "data"
    private const val NAME_LINKS = "links"
    private const val NAME_META = "meta"

    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == ToMany::class.java) RelationshipToManyAdapter(moshi) else null
    }
  }
}
