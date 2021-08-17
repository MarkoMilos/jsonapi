package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Annotation for meta object field.
 * Target field must be of type [Meta].
 *
 * Example:
 * ```
 *  @Resource("articles")
 *  class Article() {
 *    @MetaObject val meta: Meta? = null
 *  }
 * ```
 *
 * @see Meta
 */
@JsonQualifier
@Target(FIELD, PROPERTY)
@Retention(RUNTIME)
annotation class MetaObject
