@file:JvmName("ResourceBinder")

package jsonapi.internal

import jsonapi.Id
import jsonapi.Lid
import jsonapi.Links
import jsonapi.LinksObject
import jsonapi.Meta
import jsonapi.MetaObject
import jsonapi.Relationship
import jsonapi.Relationships
import jsonapi.RelationshipsObject
import jsonapi.Resource
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.ToMany
import jsonapi.ToOne
import jsonapi.Type

/** Writes [ResourceObject] values to [target] annotated fields via reflection. */
internal fun bindResourceObject(target: Any, resourceObject: ResourceObject) {
  setValueOfAnnotatedFieldOrProperty(target, Type::class.java, resourceObject.type)
  // Set value of other annotated fields but don't override if default value is set
  resourceObject.id?.let { setValueOfAnnotatedFieldOrProperty(target, Id::class.java, it) }
  resourceObject.lid?.let { setValueOfAnnotatedFieldOrProperty(target, Lid::class.java, it) }
  resourceObject.relationships?.let { setValueOfAnnotatedFieldOrProperty(target, RelationshipsObject::class.java, it) }
  resourceObject.links?.let { setValueOfAnnotatedFieldOrProperty(target, LinksObject::class.java, it) }
  resourceObject.meta?.let { setValueOfAnnotatedFieldOrProperty(target, MetaObject::class.java, it) }
}

/** Reads [ResourceObject] from [target] annotated fields via reflection. */
internal fun readResourceObject(target: Any): ResourceObject {
  val type = readType(target)
  val id = getValueOfAnnotatedFieldOrProperty<String>(target, Id::class.java)
  val lid = getValueOfAnnotatedFieldOrProperty<String>(target, Lid::class.java)
  val relationships = relationships(target)
  val links = getValueOfAnnotatedFieldOrProperty<Links>(target, LinksObject::class.java)
  val meta = getValueOfAnnotatedFieldOrProperty<Meta>(target, MetaObject::class.java)
  return ResourceObject(type, id, lid, relationships, links, meta)
}

private fun resourceIdentifier(target: Any): ResourceIdentifier {
  val type = readType(target)
  val id = getValueOfAnnotatedFieldOrProperty<String>(target, Id::class.java)
  val lid = getValueOfAnnotatedFieldOrProperty<String>(target, Lid::class.java)
  return ResourceIdentifier(type, id, lid)
}

private fun readType(target: Any): String {
  // When no field annotations are declared for member type use the value from class annotation
  val classLevelType = target::class.java.getAnnotation(Resource::class.java)?.type
  val type = getValueOfAnnotatedFieldOrProperty(target, Type::class.java) ?: classLevelType
  if (type == null || type.isEmpty()) {
    throw IllegalArgumentException("A resource MUST contain non-null, non-empty type.")
  }
  return type
}

private fun relationships(target: Any): Relationships? {
  val mergedRelationships = mutableMapOf<String, Relationship>()

  // ToOne relationship fields
  target
    .annotatedFieldsOrProperties(ToOne::class.java)
    .forEach { annotatedTarget ->
      val name = annotatedTarget.getAnnotation(ToOne::class.java).name
      val value = annotatedTarget.field.getValue(target) ?: return@forEach
      val relationship = Relationship.ToOne(resourceIdentifier(value))
      val replaced = mergedRelationships.put(name, relationship)
      if (replaced != null) {
        throw IllegalStateException(
          "Class " +
            target.javaClass.simpleName +
            " has multiple relationship fields that share the same relationship name " +
            name
        )
      }
    }

  // ToMany relationship fields
  target
    .annotatedFieldsOrProperties(ToMany::class.java)
    .forEach { annotatedTarget ->
      val name = annotatedTarget.getAnnotation(ToMany::class.java).name
      val value = annotatedTarget.field.getValue(target) ?: return@forEach
      // Expected to have collection value for ToMany relationship field
      if (value is Collection<*>) {
        // Create to-many relationship by mapping all non-null values from collection to resource identifiers
        val relationship = Relationship.ToMany(value.filterNotNull().map { item -> resourceIdentifier(item) })
        val replaced = mergedRelationships.put(name, relationship)
        if (replaced != null) {
          throw IllegalStateException(
            "Class " +
              target.javaClass.simpleName +
              " has multiple relationship fields that share the same relationship name " +
              name
          )
        }
      } else {
        throw IllegalStateException(
          "Class [" +
            target.javaClass.simpleName +
            "] has field [" +
            annotatedTarget.field.name +
            "] annotated with [" +
            ToMany::class.java.simpleName +
            "] that is of non-collection type [" +
            value::class.java.simpleName +
            "].\nTo-many relationship fields should be of type Collection or List."
        )
      }
    }

  // Merge relationship from annotated relationship object field (if exists)
  // These will override field relationships with the same name (if any)
  val relationshipsObject = getValueOfAnnotatedFieldOrProperty<Relationships>(target, RelationshipsObject::class.java)
  if (relationshipsObject != null) {
    mergedRelationships.putAll(relationshipsObject.members)
  }

  return if (mergedRelationships.isEmpty()) null else Relationships(mergedRelationships)
}
