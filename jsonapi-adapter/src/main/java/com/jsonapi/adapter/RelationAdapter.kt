package com.jsonapi.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.KEY_DATA
import com.jsonapi.model.Relation
import com.jsonapi.model.Relation.ToMany
import com.jsonapi.model.Relation.ToOne
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi

class RelationAdapter(moshi: Moshi) : JsonAdapter<Relation>() {
  
  private val toOneAdapter = moshi.adapter(ToOne::class.java)
  private val toManyAdapter = moshi.adapter(ToMany::class.java)
  
  override fun fromJson(reader: JsonReader): Relation {
    if (reader.peek() != Token.BEGIN_OBJECT) {
      // The value of the relationships key MUST be an object
      throw JsonApiException(
        "The value of the relationships key MUST be an object (a “relationships object”) but "
          + reader.peek()
          + " was found on path: "
          + reader.path
      )
    }
    
    var relationToMany = false
    // peak json reader so that we can search trough fields without consuming anything
    val peaked = reader.peekJson()
    peaked.setFailOnUnknown(false)
    // the value of the relationships key MUST be an object (a “relationships object”)
    peaked.beginObject()
    // find if this is relation to-one or to-many
    while (peaked.hasNext()) {
      if (peaked.nextName() == KEY_DATA) {
        relationToMany = peaked.peek() == Token.BEGIN_ARRAY
      }
      peaked.skipValue()
    }
    // parse relation using proper adapter
    // if data field was not found then deserialize relation as ToOne
    val relation = if (relationToMany) {
      toManyAdapter.fromJson(reader)
    } else {
      toOneAdapter.fromJson(reader)
    }
    // check if deserialized relation is valid
    if (relation == null || (!relation.hasData() && relation.links == null && relation.meta == null)) {
      throw JsonApiException("A relationship object MUST contain at least one of the following: links, data, meta!")
    }
    return relation
  }
  
  override fun toJson(writer: JsonWriter, value: Relation?) {
    when (value) {
      is ToOne -> serializeToOneRelation(writer, value)
      is ToMany -> toManyAdapter.toJson(writer, value)
      null -> throw JsonApiException("Relation should not be 'null' but 'null' was found on path: ${writer.path}")
    }
  }
  
  private fun serializeToOneRelation(writer: JsonWriter, relation: ToOne) {
    if (relation.data == null && relation.links == null && relation.meta == null) {
      // when all Relation fields are null serialize empty relation as {"data":null}
      val wasSerializeNulls = writer.serializeNulls
      writer.serializeNulls = true
      writer
        .beginObject()
        .name(KEY_DATA)
        .nullValue()
        .endObject()
      writer.serializeNulls = wasSerializeNulls
    } else {
      // if any of Relation fields is not null delegated serialization to class adapter
      toOneAdapter.toJson(writer, relation)
    }
  }
}