package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

/**
 * Annotation for resource primary identifier field.
 * Target field should be of type [String].
 */
@JsonQualifier
@Target(FIELD)
@Retention(RUNTIME)
annotation class Id
