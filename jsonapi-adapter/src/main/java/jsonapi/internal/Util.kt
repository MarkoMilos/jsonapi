@file:JvmName("Util")

package jsonapi.internal

import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Types
import jsonapi.Resource
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import java.lang.reflect.Field
import java.lang.reflect.Type

internal fun <T> JsonReader.scan(block: (reader: JsonReader) -> T): T {
  val peeked = peekJson()
  peeked.setFailOnUnknown(false)
  return peeked.use { block(it) }
}

internal fun JsonWriter.forceWriteNull() {
  val wasSerializeNulls = serializeNulls
  serializeNulls = true
  nullValue()
  serializeNulls = wasSerializeNulls
}

internal fun Type.rawType(): Class<*> {
  return Types.getRawType(this)
}

internal fun Type.isCollection(): Boolean {
  return Collection::class.java.isAssignableFrom(Types.getRawType(this))
}

internal fun Type.collectionElementType(contextRawType: Class<*>): Type {
  return Types.collectionElementType(this, contextRawType)
}

internal fun Type.isResourceType(): Boolean {
  return when (val rawType = Types.getRawType(this)) {
    ResourceIdentifier::class.java -> true
    ResourceObject::class.java -> true
    else -> rawType.isAnnotationPresent(Resource::class.java)
  }
}

internal fun Type.isNothing(): Boolean {
  return Types.getRawType(this) == Void::class.java
}

internal inline fun <reified T> getValueOfAnnotatedField(target: Any, annotation: Class<out Annotation>): T? {
  val field = target.fieldWithAnnotation(annotation) ?: return null
  try {
    return field.getValue(target) as T?
  } catch (cause: ClassCastException) {
    throw ClassCastException(
      "Cannot cast annotated field." +
        " For field [${target.javaClass.simpleName}.${field.name}]" +
        " annotated with [@${annotation.simpleName}]" +
        " expected type was [${T::class.java.simpleName}]" +
        " but found [${field.type.simpleName}]"
    )
  }
}

internal inline fun <reified T> setValueOfAnnotatedField(target: Any, annotation: Class<out Annotation>, value: T?) {
  val field = target.fieldWithAnnotation(annotation) ?: return
  try {
    field.setValue(target, value)
  } catch (cause: IllegalArgumentException) {
    throw IllegalArgumentException(
      "Cannot set annotated field." +
        " For field [${target.javaClass.simpleName}.${field.name}]" +
        " annotated with [@${annotation.simpleName}]" +
        " expected type was [${T::class.java.simpleName}]" +
        " but found [${field.type.simpleName}]",
      cause
    )
  }
}

internal fun Any.fieldWithAnnotation(annotation: Class<out Annotation>): Field? {
  val fields = fieldsWithAnnotation(annotation)
  return when (fields.size) {
    0 -> null // No fields found for given annotation
    1 -> fields.first() // Exactly one field found with given annotation
    else -> { // Multiple fields found for annotation
      throw IllegalStateException(
        "Expected single field annotated with @" +
          annotation.simpleName +
          " but found " +
          fields.size +
          " annotated fields for class " +
          this.javaClass.simpleName +
          "." +
          "\nFields found:" +
          fields.joinToString("\n -> ", "\n -> ") { it.name }
      )
    }
  }
}

internal fun Any.fieldsWithAnnotation(annotation: Class<out Annotation>): List<Field> {
  return javaClass.declaredFields.filter { it.isAnnotationPresent(annotation) }
}

internal fun Field.getValue(target: Any): Any? {
  isAccessible = true
  return get(target)
}

internal fun Field.setValue(target: Any, value: Any?) {
  try {
    isAccessible = true
    set(target, value)
  } catch (cause: IllegalArgumentException) {
    throw IllegalArgumentException(
      "Cannot set" +
        " field [${target.javaClass.simpleName}.$name]" +
        " of type [${type.simpleName}]" +
        " to value of type [${value?.javaClass?.name}].",
      cause
    )
  }
}
