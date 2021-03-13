package com.jsonapi.adapter

import com.jsonapi.*
import com.jsonapi.model.Links
import com.jsonapi.model.Meta
import com.jsonapi.model.Relation
import com.jsonapi.model.Resource
import com.squareup.moshi.*
import java.lang.reflect.Type

class ResourceSubclassAdapter<T : Resource>(
  private val moshi: Moshi,
  private val delegateAdapter: JsonAdapter<T>,
  private val resourceType: Type,
  private val annotatedTypeName: String,
  private val strictTypes: Boolean,
  private val typeNames: List<String>,
  private val allowUnregisteredTypes: Boolean
) : JsonAdapter<T>() {
  
  private val linksAdapter = moshi.adapter(Links::class.java)
  private val metaAdapter = moshi.adapter(Meta::class.java)
  private val stringAdapter = moshi.adapter(String::class.java)
  private val relationshipAdapter: JsonAdapter<Map<String, Relation>> = moshi.adapter(
    Types.newParameterizedType(Map::class.java, String::class.java, Relation::class.java)
  )
  
  override fun fromJson(reader: JsonReader): T? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == JsonReader.Token.NULL) {
      return reader.nextNull()
    }
    
    // Assert that resource is JSON object
    if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
      throw JsonApiException("Resource MUST be a JSON object but found ${reader.peek()} on path ${reader.path}")
    }
    
    // Standard structure of a resource
    var type: String? = null
    var id: String? = null
    var lid: String? = null
    var relationships: Map<String, Relation>? = null
    var links: Links? = null
    var meta: Meta? = null
    var resource: T? = null
    
    // Read the resource and delegate attributes parsing to source resource adapter
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        NAME_TYPE -> type = stringAdapter.fromJson(reader)
        NAME_ID -> id = stringAdapter.fromJson(reader)
        NAME_LID -> lid = stringAdapter.fromJson(reader)
        NAME_ATTRIBUTES -> resource = delegateAdapter.fromJson(reader)
        NAME_RELATIONSHIPS -> relationships = relationshipAdapter.fromJson(reader)
        NAME_LINKS -> links = linksAdapter.fromJson(reader)
        NAME_META -> meta = metaAdapter.fromJson(reader)
        else -> reader.skipValue() // ignore non standard names (skip value)
      }
    }
    reader.endObject()
    
    validateType(type, reader.path)
    validateIdentifier(id, lid, reader.path)
    
    // If 'attributes' key is not found for this resource
    // ensure that default instance of target class is created
    if (resource == null) {
      resource = delegateAdapter.fromJson("{}")
    }
    // Assign values from structure to deserialized resource
    resource?.type = type
    resource?.id = id
    resource?.relationships = relationships
    resource?.links = links
    resource?.meta = meta
    return resource
  }
  
  override fun toJson(writer: JsonWriter, value: T?) {
    if (value == null) {
      writer.nullValue()
      return
    }
    
    validateType(value.type, writer.path)
    validateIdentifier(value.id, value.lid, writer.path)
    
    // capture resource values
    val type = value.type
    val id = value.id
    val lid = value.lid
    val relationships = value.relationships
    val links = value.links
    val meta = value.meta
    
    // set all resource values to null to avoid serialization within attributes
    value.type = null
    value.id = null
    value.lid = null
    value.relationships = null
    value.links = null
    value.meta = null
    
    // Improvement possible:
    // this implementation requires double serialization just to check for empty objects
    // and to omit 'attributes' object from serialized resource when it is empty
    val attributes = delegateAdapter.toJson(value)
    
    // serialize values
    writer
      .beginObject()
      .name(NAME_TYPE).value(type)
      .name(NAME_ID).value(id)
      .name(NAME_LID).value(lid)
      .apply {
        if (attributes != "{}") {
          name(NAME_ATTRIBUTES)
          delegateAdapter.toJson(writer, value)
        }
      }
      .name(NAME_RELATIONSHIPS).apply { relationshipAdapter.toJson(writer, relationships) }
      .name(NAME_LINKS).apply { linksAdapter.toJson(writer, links) }
      .name(NAME_META).apply { metaAdapter.toJson(writer, meta) }
      .endObject()
    
    // assign back all resource values that were set to null
    value.type = type
    value.id = id
    value.lid = lid
    value.relationships = relationships
    value.links = links
    value.meta = meta
  }
  
  private fun validateType(type: String?, path: String = "") {
    // type is required, it should not be null or empty
    if (type.isNullOrEmpty()) {
      throw JsonApiException("A resource object MUST contain non-null, non-empty type member.")
    }
    
    // when unregistered types are not allowed type needs to be within registered types list
    if (!allowUnregisteredTypes && !typeNames.contains(type)) {
      throw JsonApiException(
        "Expected resource type name is one of $typeNames "
          + "but was '$type' "
          + "on path [$path]."
          + "\nRegister this type or use allowUnregisteredTypes(true)."
      )
    }
    
    // when strict type checking is enabled type needs to match annotated value for target class
    if (strictTypes && annotatedTypeName != type) {
      throw JsonApiException(
        "Expected type name '$annotatedTypeName' "
          + "for ${resourceType.typeName} "
          + "but was '$type' "
          + "on path [$path]."
          + "\nTo disable strict types use strictTypes(false) on JsonApiFactory.Builder."
      )
    }
  }
  
  private fun validateIdentifier(id: String?, lid: String?, path: String = "") {
    if (id.isNullOrEmpty() && lid.isNullOrEmpty()) {
      throw JsonApiException(
        "Resource MUST contain an 'id' member except when it represents a new resource to be created."
          + " In this case, a 'lid' member MUST be included that identifies the new resource."
          + " Was: id=[$id], lid=[$lid] on path [$path]."
      )
    }
  }
}