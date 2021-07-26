@file:JvmName("RelationshipsUtils")

package com.jsonapi.internal

import com.jsonapi.Relationship
import com.jsonapi.Relationships
import com.jsonapi.ResourceObject
import com.squareup.moshi.Types
import jsonapi.BindRelationship
import java.lang.reflect.ParameterizedType

/**
 * Bind all [BindRelationship] annotated fields of each resource with other resources based on
 * relationship name set with [BindRelationship] annotation and [Relationships] data from [ResourceObject].
 */
internal fun bindRelationshipFields(resources: List<Pair<ResourceObject, Any>>) {
  resources.forEach { (resourceObject, target) ->
    bindRelationshipFields(target, resourceObject, resources)
  }
}

private fun bindRelationshipFields(
  target: Any,
  resourceObject: ResourceObject,
  resources: List<Pair<ResourceObject, Any>>
) {
  // Get all relationship annotated fields for this resource
  val relationshipsFields = target.fieldsWithAnnotation(BindRelationship::class.java)
  relationshipsFields.forEach { field ->
    // Get the name from annotation
    val name = field.getAnnotation(BindRelationship::class.java).name
    // Get defined relationship with given name - skip this filed if there isn't any
    val relationship = resourceObject.relationships?.get(name) ?: return@forEach
    // Relationship with given name exists, bind depending on the relationship type
    when (relationship) {
      is Relationship.ToOne -> {
        val resourceIdentifier = relationship.data ?: return@forEach
        val matchedPair = resources.find { it.first.identifier() == resourceIdentifier }
        val matchedResource = matchedPair?.second ?: return@forEach
        try {
          field.setValue(target, matchedResource)
        } catch (e: IllegalArgumentException) {
          throw IllegalArgumentException(
            "Cannot bind"
              + " field [${target.javaClass.simpleName}.${field.name}]"
              + " of type [${field.type.simpleName}]"
              + " annotated with [@${BindRelationship::class.java.simpleName}(\"$name\")]"
              + " to matched relationship value of type [${matchedResource.javaClass.simpleName}]."
              + "\nRelationship found under name [$name]"
              + " had the resource linkage [$resourceIdentifier]"
              + " that is matched with value [$matchedResource]."
              + "\nVerify that relationships are correctly defined"
              + " and that type [${field.type.simpleName}] is registered."
          )
        }
      }
      is Relationship.ToMany -> {
        // Collect all resources that are matching relationships resource linkage data
        val matchedResources = mutableListOf<Any>()
        relationship.data.forEach { resourceIdentifier ->
          val matchedPair = resources.find { it.first.identifier() == resourceIdentifier }
          if (matchedPair?.second != null) {
            matchedResources.add(matchedPair.second)
          }
        }

        // Assert that target field is a Collection type
        val fieldType = field.genericType
        if (fieldType is ParameterizedType && fieldType.isCollection()) {
          // Assert that matched resources are assignable to field
          val fieldElementType = Types.getRawType(fieldType.actualTypeArguments.first())
          val nonMatchingResource = matchedResources.find { !fieldElementType.isAssignableFrom(it::class.java) }
          if (nonMatchingResource == null) {
            // All resources are assignable to declared field type - set field via reflection
            field.setValue(target, matchedResources)
          } else {
            // There is a resource value with type that is not assignable to declared field type
            throw IllegalArgumentException(
              "Resources matched for to-many relationship with name [$name]"
                + " contains value of type [${nonMatchingResource::class.java.simpleName}]"
                + " that is not assignable to collection of [${fieldElementType.simpleName}]"
                + " declared for the field [${target.javaClass.simpleName}.${field.name}]."
                + "\nVerify that relationships are correctly defined"
                + " and that declared type of field [${target.javaClass.simpleName}.${field.name}] is registered."
            )
          }
        } else {
          // Field type is not collection - cannot bind to-many relationship
          throw IllegalArgumentException(
            "For to-many relationship expected field type is List<>"
              + " but for to-many relationship with name [$name]"
              + " target field [${target.javaClass.simpleName}.${field.name}]"
              + " is of type [${field.type.simpleName}]."
              + "\nVerify that relationships are correctly defined."
          )
        }
      }
    }
  }
}
