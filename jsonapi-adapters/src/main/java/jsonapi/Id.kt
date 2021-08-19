package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Annotation for resource primary identifier field.
 * Target field should be of type [String].
 */
@JsonQualifier
@Target(FIELD, PROPERTY)
@Retention(RUNTIME)
annotation class Id
