package com.jsonapi.internal.binding

import com.jsonapi.*
import com.jsonapi.Relation.ToMany
import com.jsonapi.Relation.ToOne
import com.jsonapi.internal.fieldsAnnotatedWith
import java.lang.reflect.Field

/**
 * Bind `@Relationship` annotated fields of each [Resource] (both primary and included) found in document.
 * Relationship name set in `@Relationship` annotation is used to identify [Relation] in relationship map of the resource.
 * [Relation] holds resource identifier(s) that are used to match correct resource value. If resource value is
 * found within this document it is assigned to field via reflection.
 */
internal class Binder(document: Document<*>) {
  
  /** All resources of this document (primary and included) */
  private val resources: List<Resource>
  
  init {
    // transform data (primary resource) to list of resources
    val primaryResources = when (val data = document.data) {
      is Resource -> listOf(data)
      is Collection<*> -> data.filterIsInstance<Resource>()
      else -> emptyList()
    }
    
    // included resources from document defaulting to empty list if there are none
    val includedResources = document.included ?: emptyList()
    
    // all resources contained by this document that will be bound upon bind() call
    resources = listOf(primaryResources, includedResources).flatten()
  }
  
  /**
   * Bind `@Relationship` annotated fields of each [Resource] (both primary and included) found in this [Document.Data]
   * to their values.
   * @see Binder
   */
  fun bind() {
    resources.forEach { bindResource(it) }
  }
  
  /**  Bind `@Relationship` annotated fields of [resource]. */
  private fun bindResource(resource: Resource) {
    // get all @Relationship annotated fields for this resource
    val relationshipFields = resource.fieldsAnnotatedWith(Relationship::class.java)
    relationshipFields.forEach { field ->
      // name set in annotation
      val name = field.getAnnotation(Relationship::class.java).name
      
      // search for this name in relationship map
      // if not found we cannot match this field, skip it
      val relation = resource.relationships?.get(name) ?: return@forEach
      
      // relation exist in relationship map, bind depending on relation type
      when (relation) {
        is ToOne -> {
          // find resource in list of all resources where type and id matches
          // if identifier does not exist for this relation or resource is not found
          // we cannot match this field so skip it
          val resourceIdentifier = relation.data ?: return@forEach
          // find resource that matches this identifier, skip binding if non is found
          val matchedResource = resources.find { it.identifier() == resourceIdentifier } ?: return@forEach
          // resource is found, set field via reflection
          bindField(field, resource, matchedResource)
        }
        is ToMany -> {
          val matchedResources = mutableListOf<Resource>()
          // go trough each item in relation and fill array of resources
          relation.data.forEach { resourceIdentifier ->
            // find resource in list of included resources where type and id matches
            val matchedResource = resources.find { it.identifier() == resourceIdentifier }
            if (matchedResource != null) {
              matchedResources.add(matchedResource)
            }
          }
          // resources are collected, set field via reflection
          bindField(field, resource, matchedResources)
        }
      }
    }
  }
  
  /** Assign [value] to [field] via reflection. */
  private fun bindField(field: Field, targetObject: Any, value: Any) {
    try {
      field.isAccessible = true
      field.set(targetObject, value)
    } catch (e: IllegalArgumentException) {
      // Binding of incorrect type can occur in cases when:
      // 1. Included is parsed as Resource type instead Resource subclass (when unregistered types are allowed)
      // 2. Definition error - wrong value is set in @Relationship annotation for given field
      // 3. JSON error - wrong type (resource linkage) is returned under relationship
      // 4. Type registration error - type is registered under wrong type name
      throw JsonApiException(
        "Cannot bind field ${targetObject.javaClass.simpleName}.${field.name}"
          + " of ${field.type} "
          + " to value of type ${value.javaClass.name}."
          + "\nVerify that type is registered and relationships are set correctly."
      )
    }
  }
}