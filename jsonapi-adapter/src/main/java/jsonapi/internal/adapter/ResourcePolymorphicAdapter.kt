package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.JsonFormatException
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.PolymorphicResource
import jsonapi.internal.rawType
import jsonapi.internal.scan
import java.lang.reflect.Type

internal class ResourcePolymorphicAdapter(
  private val moshi: Moshi,
  private val types: List<Type>,
  typeNames: List<String>
) : JsonAdapter<Any>() {

  private val memberNameOptions = JsonReader.Options.of(NAME_TYPE)
  private val typeNameOptions = JsonReader.Options.of(*typeNames.toTypedArray())

  override fun fromJson(reader: JsonReader): Any? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that resource is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException(
        "Resource MUST be a JSON object but found " +
          reader.peek() +
          " on path " +
          reader.path
      )
    }

    // Scan json to find type member and determine adapter without consuming source reader
    val adapter = reader.scan { findAdapter(it) }

    // Deserialize resource using adapter found
    return adapter.fromJson(reader)
  }

  private fun findAdapter(reader: JsonReader): JsonAdapter<Any> {
    reader.beginObject()
    while (reader.hasNext()) {
      // Find 'type' member, skip until it is found
      if (reader.selectName(memberNameOptions) == -1) {
        reader.skipName()
        reader.skipValue()
        continue
      }
      // Check if type member value is within registered options
      val typeNameIndex = reader.selectString(typeNameOptions)
      return if (typeNameIndex != -1) {
        // Type member value found within options, return corresponding registered type adapter
        moshi.adapter(types[typeNameIndex])
      } else {
        // Type not found within options, return resource object adapter to
        // serialize/deserialize unregistered types as plain resource objects
        moshi.adapter<Any>(ResourceObject::class.java)
      }
    }
    // Top level member type not found for this resource json
    throw JsonFormatException(
      "Resource object MUST contain top-level member 'type' but it was not found on path ${reader.path}"
    )
  }

  override fun toJson(writer: JsonWriter, value: Any?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    when (val type = value.javaClass) {
      in types -> moshi.adapter(type).toJson(writer, value)
      ResourceObject::class.java -> moshi.adapter<Any>(ResourceObject::class.java).toJson(writer, value)
      ResourceIdentifier::class.java -> moshi.adapter<Any>(ResourceIdentifier::class.java).toJson(writer, value)
      else -> throw IllegalArgumentException(
        "Expected type was either: " +
          "\n * one of the registered types for JsonApiFactory" +
          "\n * a ResourceObject type" +
          "\n * a ResourceIdentifier type" +
          "\nBut found: " +
          value.javaClass +
          " on path " +
          writer.path +
          "\nJsonApiFactory registered types: " +
          types.joinToString("\n  * ", "\n  * ")
      )
    }
  }

  companion object {
    private const val NAME_TYPE = "type"

    internal fun factory(types: List<Type>, typeNames: List<String>) = object : FactoryDelegate {
      override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
        parent: Factory
      ): JsonAdapter<*>? {
        if (annotations.isEmpty()) return null
        val hasAnnotation = Types.nextAnnotations(annotations, PolymorphicResource::class.java) != null
        if (type.rawType() != Any::class.java || !hasAnnotation) return null
        return ResourcePolymorphicAdapter(moshi, types, typeNames)
      }
    }
  }
}
