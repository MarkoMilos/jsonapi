@file:JvmName("IncludedUtils")

package jsonapi.internal

import jsonapi.Document
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.ToMany
import jsonapi.ToOne

internal fun processIncluded(document: Document<*>): List<Any>? {
  // Transform documents primary data to map of resources
  val primaryResources = when (val data = document.data) {
    // There is no primary data
    null -> emptyMap()
    // Primary data is collection, transform the collection to map of resources
    is Collection<*> -> {
      data.filterNotNull().associateBy { element ->
        when (element) {
          is ResourceObject -> element
          is ResourceIdentifier -> ResourceObject.from(element)
          else -> readResourceObject(element)
        }
      }
    }
    is ResourceObject -> mapOf(data to data)
    is ResourceIdentifier -> mapOf(ResourceObject.from(data) to data)
    else -> mapOf(readResourceObject(data) to data)
  }

  // Transform documents included list to map of resources
  val includedResources = document.included?.associateBy { readResourceObject(it) }?.toMutableMap() ?: mutableMapOf()

  // Process all primary resources by adding relationship values to included resources
  primaryResources.forEach { (_, primaryResource) ->
    // Get all relationship resources from this primary resource
    val relationshipResources = relationshipResources(primaryResource)
    // Add each resource to included if not already there or within primary resources
    relationshipResources.forEach { (resourceObject, resource) ->
      if (!includedResources.contains(resourceObject) && !primaryResources.contains(resourceObject)) {
        includedResources[resourceObject] = resource
      }
    }
  }

  // Process included resources which might in turn expand included map and thus
  // require multiple iteration of processing. Repeat until all resources are
  // from included are processed and included map is not expanded
  var start = 0                     // Start position of processed included values collection
  var end = includedResources.size  // Last position of included values collection
  while (start < end) {
    // Copy included map values and take a sublist of unprocessed values
    val values = includedResources.values.toList().subList(start, end)
    values.forEach { includedResource ->
      // Increment index of start position indicating that this resource is processed
      start++
      // Get all relationship resources from target included resource
      val relationshipResources = relationshipResources(includedResource)
      // Add each resource to included if not already there or within primary resources
      relationshipResources.forEach { (resourceObject, resource) ->
        if (!includedResources.contains(resourceObject) && !primaryResources.contains(resourceObject)) {
          includedResources[resourceObject] = resource
        }
      }
    }
    // Assign new end position since included map might be expanded during this pass
    end = includedResources.size
  }

  // Return included resources as list or as null if there are no resources
  return includedResources.values.toList().ifEmpty { null }
}

private fun relationshipResources(target: Any): Map<ResourceObject, Any> {
  val relationships = mutableMapOf<ResourceObject, Any>()

  // ToOne field relationships
  target
    .fieldsWithAnnotation(ToOne::class.java)
    .forEach { field ->
      val value = field.getValue(target) ?: return@forEach
      relationships[readResourceObject(value)] = value
    }

  // ToMany field relationships
  target
    .fieldsWithAnnotation(ToMany::class.java)
    .forEach { field ->
      val value = field.getValue(target) ?: return@forEach
      if (value is Collection<*>) {
        value.forEach { element ->
          if (element != null) relationships[readResourceObject(element)] = element
        }
      } else {
        throw IllegalStateException(
          "Class ["
            + target.javaClass.simpleName
            + "] has field ["
            + field.name
            + "] annotated with ["
            + ToMany::class.java.simpleName
            + "] that is of non-collection type ["
            + value::class.java.simpleName
            + "].\nTo-many relationship fields should be of type Collection or List."
        )
      }
    }

  return relationships
}
