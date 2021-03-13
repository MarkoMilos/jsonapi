package com.jsonapi.internal.adapter

import com.jsonapi.Document
import com.jsonapi.JsonApiException
import com.jsonapi.internal.NAME_DATA
import com.jsonapi.internal.NAME_ERRORS
import com.jsonapi.internal.NAME_META
import com.jsonapi.internal.bind
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class DocumentAdapter(
  private val dataAdapter: JsonAdapter<Document.Data<*>>,
  private val errorsAdapter: JsonAdapter<Document.Errors>
) : JsonAdapter<Document<*>>() {
  
  override fun fromJson(reader: JsonReader): Document<*>? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == JsonReader.Token.NULL) {
      return reader.nextNull()
    }
    
    // Assert that root is a JSON object
    if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
      throw JsonApiException("A JSON object MUST be at the root of every JSON:API document but found ${reader.peek()}")
    }
    
    // Top-level member flags
    var dataFound = false
    var metaFound = false
    var errorsFound = false
    
    // Peak json reader so that we can search trough name/values without consuming anything
    val peaked = reader.peekJson()
    peaked.setFailOnUnknown(false)
    peaked.beginObject()
    while (peaked.hasNext()) {
      when (peaked.nextName()) {
        NAME_DATA -> dataFound = true
        NAME_META -> metaFound = true
        NAME_ERRORS -> errorsFound = true
      }
      peaked.skipValue()
    }
    
    if (!dataFound && !metaFound && !errorsFound) {
      throw JsonApiException(
        "A document MUST contain at least one of the following top-level members:\n" +
          "  * data: the document’s “primary data”\n" +
          "  * errors: an array of error objects\n" +
          "  * meta: a meta object that contains non-standard meta-information.\n"
      )
    }
    
    if (errorsFound && dataFound) {
      throw JsonApiException("The members data and errors MUST NOT coexist in the same document.")
    }
    
    return if (errorsFound) {
      errorsAdapter.fromJson(reader)
    } else {
      // use delegated data adapter to parse the content, also bind resources of returned document value
      dataAdapter.fromJson(reader)?.apply { bind() }
    }
  }
  
  override fun toJson(writer: JsonWriter, value: Document<*>?) {
    when (value) {
      is Document.Data -> dataAdapter.toJson(writer, value)
      is Document.Errors -> errorsAdapter.toJson(writer, value)
      null -> writer.nullValue()
    }
  }
}