package com.jsonapi

import com.jsonapi.binding.Binder
import com.jsonapi.binding.Unbinder
import com.jsonapi.model.Document
import com.jsonapi.model.Resource
import com.squareup.moshi.Types
import java.lang.reflect.Field
import java.lang.reflect.Type

// JSON:API standard names
// TODO should we rename this to NAME_ instead KEY_
internal const val KEY_TYPE = "type"
internal const val KEY_ID = "id"
internal const val KEY_LID = "lid"
internal const val KEY_DATA = "data"
internal const val KEY_ERRORS = "errors"
internal const val KEY_ATTRIBUTES = "attributes"
internal const val KEY_RELATIONSHIPS = "relationships"
internal const val KEY_META = "meta"
internal const val KEY_LINKS = "links"

internal const val EXCEPTION_MSG_INVALID_DOCUMENT = """
  For Document.Data<T> type T should be one of the following:
    - Subclass of Resource
    - Collection<K> where each collection item K is a subclass of Resource
    - Void/Nothing for null data documents (e.g. meta only documents)
"""

internal const val EXCEPTION_MSG_INVALID_RELATIONSHIP = """
  Field annotated with @Relationship needs to be:
    - Subclass of Resource for to-one relations
    - Collection<T> where each collection item T is a subclass of Resource for to-many relations
"""

internal fun Document.Data<*>.bind() {
  Binder(this).bind()
}

internal fun Document.Data<*>.unbind() {
  Unbinder(this).unbind()
}

@Suppress("UNCHECKED_CAST")
internal fun Any.isResourceCollection(): Boolean {
  // check if this is a collection and assert that each element is instance of Resource
  return (this is Collection<*>) && this.all { it is Resource }
}

internal fun Any.isSingleOrCollectionResource(): Boolean {
  return (this is Resource) || this.isResourceCollection()
}

internal fun Type.isCollection(): Boolean {
  return Collection::class.java.isAssignableFrom(Types.getRawType(this))
}

internal fun Type.isResource(): Boolean {
  return Resource::class.java.isAssignableFrom(Types.getRawType(this))
}

internal fun Class<*>.fieldsAnnotatedWith(annotation: Class<out Annotation>): List<Field> {
  return declaredFields.filter { it.isAnnotationPresent(annotation) }
}

internal fun Resource.fieldsAnnotatedWith(annotation: Class<out Annotation>): List<Field> {
  return javaClass.fieldsAnnotatedWith(annotation)
}