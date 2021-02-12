package com.jsonapi.adapter

import com.jsonapi.KEY_DATA
import com.jsonapi.KEY_ERRORS
import com.jsonapi.bind
import com.jsonapi.model.Document
import com.jsonapi.unbind
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class DocumentAdapter(
  private val dataAdapter: JsonAdapter<Document.Data<*>>,
  private val errorsAdapter: JsonAdapter<Document.Errors>
) : JsonAdapter<Document<*>>() {
  
  override fun fromJson(reader: JsonReader): Document<*>? {
    // peak json reader so that we can search trough name/values without consuming anything
    val peaked = reader.peekJson()
    peaked.setFailOnUnknown(false)
    // document's "top level" needs to be json object, this will throw it it isn't
    peaked.beginObject()
    // search for 'errors' name and use errors adapter if the name is found
    while (peaked.hasNext()) {
      if (peaked.nextName() == KEY_ERRORS) {
        return errorsAdapter.fromJson(reader)
      } else {
        peaked.skipValue()
      }
    }
    // errors name not found, use delegated data adapter to parse the content
    // also bind resources of returned document value
    return dataAdapter.fromJson(reader)?.apply { bind() }
  }
  
  override fun toJson(writer: JsonWriter, value: Document<*>?) {
    when (value) {
      is Document.Data -> {
        if (value.data == null && value.meta == null) {
          // serialize null data document as {"data":null} since it is a valid document per specification
          val wasSerializeNulls = writer.serializeNulls
          writer.serializeNulls = true
          writer.beginObject().name(KEY_DATA).nullValue().endObject()
          writer.serializeNulls = wasSerializeNulls
        } else {
          // unbind resources of this document and use delegated adapter for serialization
          // after serialization bind document back since it may be used by calling code
          value.unbind()
          dataAdapter.toJson(writer, value)
          value.bind()
        }
      }
      is Document.Errors -> errorsAdapter.toJson(writer, value)
      null -> writer.nullValue()
    }
  }
}