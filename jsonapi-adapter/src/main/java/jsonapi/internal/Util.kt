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
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

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

internal fun Class<*>.isKotlinClass(): Boolean {
  return this.isAnnotationPresent(Metadata::class.java)
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

internal inline fun <reified T> getValueOfAnnotatedFieldOrProperty(target: Any, annotation: Class<out Annotation>): T? {
  val annotatedTarget = target.annotatedFieldOrProperty(annotation) ?: return null
  try {
    return annotatedTarget.field.getValue(target) as T?
  } catch (cause: ClassCastException) {
    throw ClassCastException(
      "Cannot cast annotated field." +
        " For field [${target.javaClass.simpleName}.${annotatedTarget.field.name}]" +
        " annotated with [@${annotation.simpleName}]" +
        " expected type was [${T::class.java.simpleName}]" +
        " but found [${annotatedTarget.field.type.simpleName}]"
    )
  }
}

internal inline fun <reified T> setValueOfAnnotatedFieldOrProperty(
  target: Any,
  annotation: Class<out Annotation>,
  value: T?
) {
  val annotatedTarget = target.annotatedFieldOrProperty(annotation) ?: return
  try {
    annotatedTarget.field.setValue(target, value)
  } catch (cause: IllegalArgumentException) {
    throw IllegalArgumentException(
      "Cannot set annotated field." +
        " For field [${target.javaClass.simpleName}.${annotatedTarget.field.name}]" +
        " annotated with [@${annotation.simpleName}]" +
        " expected type was [${T::class.java.simpleName}]" +
        " but found [${annotatedTarget.field.type.simpleName}]",
      cause
    )
  }
}

internal fun Any.annotatedFieldOrProperty(annotation: Class<out Annotation>): AnnotatedTarget? {
  val annotatedTargets = annotatedFieldsOrProperties(annotation)
  if (annotatedTargets.size <= 1) {
    // Non or exactly one field or property found for annotation
    return annotatedTargets.firstOrNull()
  } else {
    // Multiple fields or properties found for annotation
    throw IllegalStateException(
      "Expected single field or property annotated with @${annotation.simpleName}" +
        " but found ${annotatedTargets.size} annotated fields or properties" +
        " for class ${this.javaClass.simpleName}." +
        "\nAnnotated targets found:" +
        annotatedTargets.joinToString("\n -> ", "\n -> ") { it.field.name }
    )
  }
}

internal fun Any.annotatedFieldsOrProperties(annotation: Class<out Annotation>): List<AnnotatedTarget> {
  return if (javaClass.isKotlinClass()) {
    // For kotlin classes annotations are applied to properties instead of fields
    this::class.declaredMemberProperties
      .mapNotNull { property ->
        val targetAnnotation =
          property.annotations.firstOrNull { it.annotationClass.java == annotation } ?: return@mapNotNull null
        val javaField = property.javaField ?: return@mapNotNull null
        AnnotatedTarget(javaField, targetAnnotation)
      }
  } else {
    // For java classes annotation is applied directly on declared field
    this.javaClass.declaredFields
      .filter { it.isAnnotationPresent(annotation) }
      .map { AnnotatedTarget(it, it.getAnnotation(annotation)) }
  }
}

internal class AnnotatedTarget(
  val field: Field,
  private val annotation: Annotation
) {
  fun <T : Annotation> getAnnotation(annotation: Class<out T>): T {
    return annotation.cast(this.annotation)
  }
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
