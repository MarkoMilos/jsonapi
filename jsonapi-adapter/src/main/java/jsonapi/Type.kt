package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

/**
 * Annotation for resource `type` member field.
 * The `type` member is used to describe resource objects that share common attributes and relationships.
 * Target field should be of type [String].
 */
@JsonQualifier
@Target(FIELD)
@Retention(RUNTIME)
annotation class Type
