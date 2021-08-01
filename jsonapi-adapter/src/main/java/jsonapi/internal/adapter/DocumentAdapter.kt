package jsonapi.internal.adapter

import jsonapi.Document
import jsonapi.Document.IncludedSerialization.DOCUMENT
import jsonapi.Document.IncludedSerialization.NONE
import jsonapi.Document.IncludedSerialization.PROCESSED
import jsonapi.Error
import jsonapi.JsonApiObject
import jsonapi.JsonFormatException
import jsonapi.Links
import jsonapi.Meta
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.PolymorphicResource
import jsonapi.internal.bindRelationshipFields
import jsonapi.internal.collectionElementType
import jsonapi.internal.isCollection
import jsonapi.internal.isNothing
import jsonapi.internal.isResourceType
import jsonapi.internal.processIncluded
import jsonapi.internal.scan
import jsonapi.internal.forceWriteNull
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import jsonapi.Resource
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class DocumentAdapter(
  moshi: Moshi,
  private val resourceAdapter: JsonAdapter<Any>,
  private val isCollectionDocument: Boolean
) : JsonAdapter<Document<*>>() {

  private val jsonApiObjectAdapter = moshi.adapter(JsonApiObject::class.java)
  private val linksAdapter = moshi.adapter(Links::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)
  private val resourceObjectAdapter = moshi.adapter(ResourceObject::class.java)
  private val resourcePolymorphicAdapter = moshi.adapter<Any>(Any::class.java, PolymorphicResource::class.java)
  private val errorsAdapter =
    moshi.adapter<List<Error>>(Types.newParameterizedType(List::class.java, Error::class.java))

  override fun fromJson(reader: JsonReader): Document<*>? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that root of a document is a JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException("A JSON object MUST be at the root of every JSON:API document but found ${reader.peek()}")
    }

    // Standard document structure
    var data: Any? = null
    var included: List<Any>? = null
    var errors: List<Error>? = null
    var links: Links? = null
    var meta: Meta? = null
    var jsonApiObject: JsonApiObject? = null

    // All deserialized resources for this document (data and included)
    val resources = mutableListOf<Pair<ResourceObject, Any>>()

    // Deserialize document members
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_DATA -> data = readData(reader, resources)
        NAME_INCLUDED -> included = readIncluded(reader, resources)
        NAME_ERRORS -> errors = errorsAdapter.fromJson(reader)
        NAME_LINKS -> links = linksAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        NAME_JSON_API -> jsonApiObject = jsonApiObjectAdapter.fromJson(reader)
      }
    }
    reader.endObject()

    // Bind all deserialized resources mutually (primary and included)
    bindRelationshipFields(resources)

    return Document(data, included, errors, links, meta, jsonApiObject)
  }

  private fun readData(reader: JsonReader, resources: MutableList<Pair<ResourceObject, Any>>): Any? {
    // Null data documents {"data":null} are valid documents
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    if (!isCollectionDocument) {
      // Scan json and read resource object without consuming source reader
      val resourceObject = reader.scan { resourceObjectAdapter.fromJson(it) } ?: return null
      // Read the target resource
      val resource = resourceAdapter.fromJson(reader) ?: return null
      // Add to list of all deserialized resources
      resources.add(resourceObject to resource)
      return resource
    } else {
      val result = mutableListOf<Any>()
      reader.beginArray()
      // Read array of resources, skip for deserialized nulls
      while (reader.hasNext()) {
        // Scan json and read resource object without consuming source reader
        val resourceObject = reader.scan { resourceObjectAdapter.fromJson(it) } ?: continue
        // Read the target resource element
        val resource = resourceAdapter.fromJson(reader) ?: continue
        // Add to list of all deserialized resources
        resources.add(resourceObject to resource)
        result.add(resource)
      }
      reader.endArray()
      return result
    }
  }

  private fun readIncluded(reader: JsonReader, resources: MutableList<Pair<ResourceObject, Any>>): List<Any>? {
    // Included can be null, read null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Read the array and deserialize resources using polymorphic adapter that will deserialize to
    // correct instance based on the "type" member of each json object. If type (class) is not registered
    // for given type name, polymorphic adapter will deserialize element as ResourceObject type
    val included = mutableListOf<Any>()
    reader.beginArray()
    while (reader.hasNext()) {
      // Scan json and read resource object without consuming source reader
      val resourceObject = reader.scan { resourceObjectAdapter.fromJson(it) } ?: continue
      // Read the target resource element with polymorphic adapter
      val resource = resourcePolymorphicAdapter.fromJson(reader) ?: continue
      // Add to list of all deserialized resources
      resources.add(resourceObject to resource)
      included.add(resource)
    }
    reader.endArray()
    return included
  }

  override fun toJson(writer: JsonWriter, value: Document<*>?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    writer.beginObject()
    writer.name(NAME_JSON_API).apply { jsonApiObjectAdapter.toJson(writer, value.jsonapi) }
    writer.name(NAME_META).apply { metaAdapter.toJson(writer, value.meta) }
    writer.name(NAME_LINKS).apply { linksAdapter.toJson(writer, value.links) }
    writer.name(NAME_DATA).apply {
      if (value.data == null && value.errors == null && value.meta == null) {
        // Serialize data member for null document {"data":null} to produce valid json:api structure
        writer.forceWriteNull()
      } else {
        // Use delegate adapter to serialize single or collection of resources
        if (value.data is Collection<*>) {
          writer.beginArray()
          value.data.forEach { resourceAdapter.toJson(writer, it) }
          writer.endArray()
        } else {
          resourceAdapter.toJson(writer, value.data)
        }
      }
    }
    writer.name(NAME_INCLUDED).apply {
      // Determine what included should be serialized per document configuration
      val included = when (value.includedSerialization) {
        NONE -> null                        // Don't serialize included
        DOCUMENT -> value.included          // Serialize only what is defined with Document
        PROCESSED -> processIncluded(value) // Serialize what is defined with Document and all resource relationships
      }
      if (included == null) {
        nullValue()
      } else {
        beginArray()
        included.forEach { resourcePolymorphicAdapter.toJson(writer, it) }
        endArray()
      }
    }
    writer.name(NAME_ERRORS).apply { errorsAdapter.toJson(writer, value.errors) }
    writer.endObject()
  }

  companion object {
    private const val NAME_DATA = "data"
    private const val NAME_INCLUDED = "included"
    private const val NAME_ERRORS = "errors"
    private const val NAME_LINKS = "links"
    private const val NAME_META = "meta"
    private const val NAME_JSON_API = "jsonapi"

    internal val FACTORY = object : FactoryDelegate {
      override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
        parent: Factory
      ): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null
        if (type !is ParameterizedType) return null
        if (type.rawType != Document::class.java) return null

        // Type of data member for this document
        val dataType = type.actualTypeArguments.first()

        // Is document for collection of resources or single resource
        val isCollectionDocument = dataType.isCollection()

        // Assert that collection document has data member of type Collection or List
        if (isCollectionDocument) {
          require(dataType.rawType == Collection::class.java || dataType.rawType == List::class.java) {
            "Collection documents must have 'data' defined either as Collection or List but was [$dataType]."
          }
        }

        // Type of resource defined for this document (Document<[type]> or Document<List<[type]>>)
        val targetType = if (isCollectionDocument) dataType.collectionElementType(Collection::class.java) else dataType

        // Assert that target type is one of resource types or void
        require(targetType.isResourceType() || targetType.isNothing()) {
          "Resource type must be one of:" +
            " ${ResourceIdentifier::class.java.simpleName}," +
            " ${ResourceObject::class.java.simpleName}," +
            " class with annotation @${Resource::class.java.simpleName}," +
            " or Void (in case of error or meta-only documents without any data)" +
            " but was [$targetType]."
        }

        // Adapter for extracted target resource type
        val targetAdapter = moshi.adapter<Any>(targetType)

        return DocumentAdapter(moshi, targetAdapter, isCollectionDocument)
      }
    }
  }
}
