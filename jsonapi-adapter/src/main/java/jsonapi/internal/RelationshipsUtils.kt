@file:JvmName("RelationshipsUtils")

package jsonapi.internal

import jsonapi.Relationship
import jsonapi.ResourceObject
import jsonapi.ToMany
import jsonapi.ToOne

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
  // All to-one relationship fields or properties
  val toOneRelationshipFields = target
    .annotatedFieldsOrProperties(ToOne::class.java)
    .map { it.getAnnotation(ToOne::class.java).name to it.field }

  // All to-many relationship fields or properties
  val toManyRelationshipFields = target
    .annotatedFieldsOrProperties(ToMany::class.java)
    .map { it.getAnnotation(ToMany::class.java).name to it.field }

  // Merged relationship fields
  val relationshipFields = toOneRelationshipFields + toManyRelationshipFields

  relationshipFields.forEach { (name, field) ->
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
            "Cannot bind" +
              " field [${target.javaClass.simpleName}.${field.name}]" +
              " of type [${field.type.simpleName}]" +
              " defined as relationship with name [$name]" +
              " to matched relationship value of type [${matchedResource.javaClass.simpleName}]." +
              "\nRelationship found under name [$name]" +
              " had the resource linkage [$resourceIdentifier]" +
              " that is matched with value [$matchedResource]." +
              "\nVerify that relationships are correctly defined" +
              " and that type [${field.type.simpleName}] is registered."
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

        // Assert that target field is either Collection or List
        if (field.type == Collection::class.java || field.type == List::class.java) {
          // Assert that matched resources are assignable to field
          val fieldElementType = field.genericType.collectionElementType(Collection::class.java).rawType()
          val nonMatchingResource = matchedResources.find { !fieldElementType.isAssignableFrom(it::class.java) }
          if (nonMatchingResource == null) {
            // All resources are assignable to declared field type - set field via reflection
            field.setValue(target, matchedResources)
          } else {
            // There is a resource value with type that is not assignable to declared field type
            throw IllegalArgumentException(
              "Resources matched for to-many relationship with name [$name]" +
                " contains value of type [${nonMatchingResource::class.java.simpleName}]" +
                " that is not assignable to collection of [${fieldElementType.simpleName}]" +
                " declared for the field [${target.javaClass.simpleName}.${field.name}]." +
                "\nVerify that relationships are correctly defined" +
                " and that declared type of field [${target.javaClass.simpleName}.${field.name}] is registered."
            )
          }
        } else {
          // Field type is not Collection or List - cannot bind to-many relationship
          throw IllegalArgumentException(
            "For relationship with name [$name]" +
              " resolved relationship value is to-many and expected field type is Collection or List" +
              " but target field [${target.javaClass.simpleName}.${field.name}]" +
              " is of type [${field.type.simpleName}]." +
              "\nVerify that relationships are correctly defined."
          )
        }
      }
    }
  }
}
