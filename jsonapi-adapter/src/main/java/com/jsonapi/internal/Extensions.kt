package com.jsonapi.internal

import com.jsonapi.Document
import com.jsonapi.Resource
import com.jsonapi.internal.binding.Binder
import com.jsonapi.internal.binding.Unbinder
import com.squareup.moshi.Types
import java.lang.reflect.Field
import java.lang.reflect.Type

internal fun Document.Data<*>.bind() {
  Binder(this).bind()
}

internal fun Document.Data<*>.unbind() {
  Unbinder(this).unbind()
}

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