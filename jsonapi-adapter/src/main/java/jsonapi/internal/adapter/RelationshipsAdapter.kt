package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.JsonFormatException
import jsonapi.Relationship
import jsonapi.Relationships
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

internal class RelationshipsAdapter(moshi: Moshi) : JsonAdapter<Relationships>() {

  private val membersAdapter: JsonAdapter<Map<String, Relationship?>> = moshi.adapter(
    Types.newParameterizedType(Map::class.java, String::class.java, Relationship::class.java)
  )

  override fun fromJson(reader: JsonReader): Relationships? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that root of relationships object is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException(
        "Relationships MUST be a JSON object but found "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }

    // Read original members where relationship values can be null
    val sourceMembers = membersAdapter.fromJson(reader) ?: return null

    // Create new members map ignoring all relationships with null values
    val members = mutableMapOf<String, Relationship>()
    sourceMembers.forEach { (key, value) ->
      if (value != null) {
        members[key] = value
      }
    }

    return Relationships(members)
  }

  override fun toJson(writer: JsonWriter, value: Relationships?) {
    membersAdapter.toJson(writer, value?.members)
  }

  companion object {
    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == Relationships::class.java) RelationshipsAdapter(moshi) else null
    }
  }
}
