package com.jsonapi.internal.adapter

import com.jsonapi.Document
import com.jsonapi.JsonApiException
import com.jsonapi.internal.NAME_DATA
import com.jsonapi.internal.NAME_ERRORS
import com.jsonapi.internal.NAME_META
import com.jsonapi.internal.bind
import com.jsonapi.internal.binding.Unbinder
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

internal class DocumentAdapter(
  private val delegateAdapter: JsonAdapter<Document<*>>
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
    // We need to scan json and not the deserialized object since json could have a top level member
    // with null value (e.g. data document {"data":null} which is valid per specification)
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

    val document = delegateAdapter.fromJson(reader)
    document?.bind()
    return document
  }

  override fun toJson(writer: JsonWriter, value: Document<*>?) {
    // Serialize null value as null
    if (value == null) {
      writer.nullValue()
      return
    }

    // Serialize null data document as {"data":null} since it is a valid document per specification
    if (!value.hasErrors() && !value.hasData() && !value.hasMeta()) {
      writer.beginObject()

      // Serialize data top-level member with null value (it is required to enable null serialization)
      val wasSerializeNulls = writer.serializeNulls
      writer.serializeNulls = true
      writer.name(NAME_DATA).nullValue()
      writer.serializeNulls = wasSerializeNulls

      // Serialize anything else that this document might have
      val token = writer.beginFlatten()
      delegateAdapter.toJson(writer, value)
      writer.endFlatten(token)

      writer.endObject()
      return
    }

    if (value.hasData()) {
      // Transform document with data for serialization by unbinding relationship fields
      val unbinder = Unbinder(value)
      unbinder.unbind()
      // When included needs to be omitted from serialization remove them from the document
      if (value.serializationRules?.serializeIncluded == false) {
        unbinder.removeIncluded()
      }
      // Serialize transformed document with delegate adapter
      delegateAdapter.toJson(writer, value)
      // When included were omitted for serialization assign them back to the document
      if (value.serializationRules?.serializeIncluded == false) {
        unbinder.assignIncluded()
      }
      // Bind document back so that primary resource(s) are not changed
      value.bind()
    } else {
      // There is no data, there is nothing to transform, delegate adapter can perform serialization
      delegateAdapter.toJson(writer, value)
    }
  }
}
