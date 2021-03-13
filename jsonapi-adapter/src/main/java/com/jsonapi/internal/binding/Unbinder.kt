package com.jsonapi.internal.binding

import com.jsonapi.*
import com.jsonapi.internal.fieldsAnnotatedWith
import com.jsonapi.internal.isResourceCollection

/**
 * Unbinds all `@Relationship` annotated fields of every resource in [document] by assigning null values.
 * Unbound values/references are stored in included resources list if not already there or in primary resources.
 * For each unbound value [Relation] is created ans stored in relationship map of that resource.
 */
internal class Unbinder(
  private val document: Document.Data<*>
) {
  
  private val primaryResources: List<Resource>
  private val includedResources: MutableList<Resource>
  
  init {
    // primary resource(s) from document
    val data = document.data
    
    // transform data (primary resource) to list of resources
    primaryResources = when (data) {
      is Resource -> listOf(data)
      is Collection<*> -> data.filterIsInstance<Resource>()
      else -> emptyList()
    }
    
    // transform included to mutable list or create new one if there are no included
    includedResources = document.included?.toMutableList() ?: mutableListOf()
  }
  
  /**
   * Unbinds all `@Relationship` annotated fields of every resource in document.
   * @see Unbinder
   */
  fun unbind() {
    // if there is no data (primary resource) there should not be any
    // included resource either hence there is noting to unbind
    if (document.data == null) {
      assignIncluded(null)
      return
    }
    
    // first unbind primary resources which is a fixed list resource of items that
    // cannot be extended by unbinding process
    primaryResources.forEach { unbindResource(it) }
    // unbind included resources which are set by unbinding primary resources
    // unbinding included resource can set new resources to included list in case of transitive resource
    // references (A->B->C) thus this is recursive function that is called until unbind is performed on each resource
    unbindIncluded()
    
    // if any included is present use that list but in case of an
    // empty list assign null value to avoid serialization
    val includedToAssign = if (includedResources.isNotEmpty()) includedResources else null
    assignIncluded(includedToAssign)
  }
  
  private fun unbindIncluded() {
    val previousSize = includedResources.size
    // create a copy of list because we are iteration on collection
    // that can be modified within lambda
    includedResources.toList().forEach { unbindResource(it) }
    // check if included resources list is extended by previous operation and if so call
    // unbind included again to ensure that transitive resource are also unbound
    if (previousSize != includedResources.size) {
      unbindIncluded()
    }
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun unbindResource(resource: Resource) {
    // map of all relationships for this resource
    val relationshipsMap = resource.relationships?.toMutableMap() ?: mutableMapOf()
    
    // all fields that are annotated with @Relationship for this resource
    val relationshipFields = resource
      .fieldsAnnotatedWith(Relationship::class.java)
      .onEach { it.isAccessible = true }
    
    // iterate trough and unbind each relationship field
    relationshipFields.forEach { field ->
      val name = field.getAnnotation(Relationship::class.java).name
      val value = field.get(resource) ?: return@forEach
      
      when (value) {
        is Resource -> {
          // add Relation.ToOne to relationships map
          relationshipsMap[name] = Relation.ToOne(value.identifier())
          // add resource to included list if not already there or in primary resources
          addToIncluded(value)
          // set this field to null
          field.set(resource, null)
        }
        is Collection<*> -> {
          // assert that all collection items are Resource and cast this value
          // throw if there are non Resource items
          if (value.isResourceCollection()) {
            value as Collection<Resource>
          } else {
            throw IllegalArgumentException(
              "Collection annotated with @Relationship(\"$name\") contains non Resource values.\n"
                + "Field type for to-many relations should be List<T> where T is a Resource (or subclass)."
            )
          }
          
          // this is to many relation, there will be multiple resource identifiers
          val resourceIdentifiers = mutableListOf<ResourceIdentifier>()
          value.forEach { resourceItem ->
            // add identifier to list of ResourceIdentifiers that will be set for ToMany relation
            resourceIdentifiers.add(resourceItem.identifier())
            // add resource to included list if not already there or in primary resources
            addToIncluded(resourceItem)
          }
          // if there are identifiers create relationship map entry for ToMany relation
          // otherwise remove relation entry for this name from map
          if (resourceIdentifiers.isNotEmpty()) {
            relationshipsMap[name] = Relation.ToMany(resourceIdentifiers)
          } else {
            relationshipsMap.remove(name)
          }
          // set this field to null
          field.set(resource, null)
        }
        else -> throw IllegalArgumentException(
          "Unsupported type [${value.javaClass.simpleName}] annotated with @Relationship(\"$name\").\n"
            + "Field annotated with @Relationship should be:\n"
            + " - Resource (or subclass) for to-one relations\n"
            + " - List<T> where T is a Resource (or subclass) for to-many relations"
        )
      }
    }
    
    // assign created relationships map to this resource or set it
    // to null if there are no relationships to avoid serialization
    resource.relationships = if (relationshipsMap.isNotEmpty()) relationshipsMap else null
  }
  
  /** Add [resource] to list of included resources if not already there or in list of primary resources */
  private fun addToIncluded(resource: Resource) {
    if (includedResources.none { it.identifier() == resource.identifier() }
      && primaryResources.none { it.identifier() == resource.identifier() }) {
      includedResources.add(resource)
    }
  }
  
  /** Set [Document.Data<T>] `included` field via reflection since it is defined as immutable */
  private fun assignIncluded(included: List<Resource>?) {
    val field = document.javaClass.getDeclaredField("included")
    field.isAccessible = true
    field.set(document, included)
  }
}