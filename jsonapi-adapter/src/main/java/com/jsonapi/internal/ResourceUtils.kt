@file:JvmName("ResourceUtils")

package com.jsonapi.internal

import com.jsonapi.JsonApiException
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationship
import com.jsonapi.Relationships
import com.jsonapi.ResourceIdentifier
import com.jsonapi.ResourceObject
import jsonapi.BindRelationship
import jsonapi.Resource
import jsonapi.ResourceId
import jsonapi.ResourceLid
import jsonapi.ResourceLinks
import jsonapi.ResourceMeta
import jsonapi.ResourceRelationships
import jsonapi.ResourceType

/** Reads [ResourceObject] from [target] annotated fields via reflection. */
internal fun readResourceObject(target: Any): ResourceObject {
  val identifier = resourceIdentifier(target)
  val relationships = relationships(target)
  val links = getValueOfAnnotatedField<Links>(target, ResourceLinks::class.java)
  val meta = getValueOfAnnotatedField<Meta>(target, ResourceMeta::class.java)
  return ResourceObject(identifier.type, identifier.id, identifier.lid, relationships, links, meta)
}

/** Writes [ResourceObject] values to [target] annotated fields via reflection. */
internal fun bindResourceObject(target: Any, resourceObject: ResourceObject) {
  setValueOfAnnotatedField(target, ResourceType::class.java, resourceObject.type)
  // Set value of other annotated fields but don't override if default value is set
  resourceObject.id?.let { setValueOfAnnotatedField(target, ResourceId::class.java, it) }
  resourceObject.lid?.let { setValueOfAnnotatedField(target, ResourceLid::class.java, it) }
  resourceObject.relationships?.let { setValueOfAnnotatedField(target, ResourceRelationships::class.java, it) }
  resourceObject.links?.let { setValueOfAnnotatedField(target, ResourceLinks::class.java, it) }
  resourceObject.meta?.let { setValueOfAnnotatedField(target, ResourceMeta::class.java, it) }
}

private fun resourceIdentifier(target: Any): ResourceIdentifier {
  // When no field annotations are declared for member type use the value from class annotation
  val classLevelType = target::class.java.getAnnotation(Resource::class.java)?.type
  val type = getValueOfAnnotatedField(target, ResourceType::class.java) ?: classLevelType
  if (type.isNullOrEmpty()) {
    throw JsonApiException("A resource MUST contain non-null, non-empty type.")
  }

  val id = getValueOfAnnotatedField<String>(target, ResourceId::class.java)
  val lid = getValueOfAnnotatedField<String>(target, ResourceLid::class.java)
  if (id.isNullOrBlank() && lid.isNullOrBlank()) {
    throw JsonApiException("A resource MUST contain an 'id' or 'lid' but both were null or blank.")
  }

  return ResourceIdentifier(type, id, lid)
}

private fun relationships(target: Any): Relationships? {
  val mergedRelationships = mutableMapOf<String, Relationship>()

  // Merge relationships from all non-null @BindRelationship annotated fields
  val fieldRelationships = fieldRelationships(target)
  fieldRelationships.forEach { (name, value) ->
    // Skip all fields with null values
    if (value == null) return@forEach
    val relationship = if (value is Collection<*>) {
      // Create to-many relationship by mapping all values from collection to resource identifiers
      Relationship.ToMany(value.filterNotNull().map { item -> resourceIdentifier(item) })
    } else {
      // Create to-one relationship from target non-collection object
      Relationship.ToOne(resourceIdentifier(value))
    }
    // Add relationship to merged relationships
    mergedRelationships[name] = relationship
  }

  // Merge relationship from annotated @ResourceRelationships field (if exists)
  // These will override any relationship set from @BindRelationship fields (if any)
  val relationshipsObject = getValueOfAnnotatedField<Relationships>(target, ResourceRelationships::class.java)
  if (relationshipsObject != null) {
    mergedRelationships.putAll(relationshipsObject.members)
  }

  return if (mergedRelationships.isEmpty()) null else Relationships(mergedRelationships)
}

private fun fieldRelationships(target: Any): Map<String, Any?> {
  val relationships = mutableMapOf<String, Any?>()
  target
    .fieldsWithAnnotation(BindRelationship::class.java)
    .forEach {
      val name = it.getAnnotation(BindRelationship::class.java).name
      val value = it.getValue(target)
      val replaced = relationships.put(name, value)
      if (replaced != null) {
        throw IllegalStateException(
          "Class [${target.javaClass.simpleName}]"
            + " has multiple fields annotated with [@${BindRelationship::class.java.simpleName}]"
            + " that share the same relationship name [$name]."
        )
      }
    }
  return relationships
}
