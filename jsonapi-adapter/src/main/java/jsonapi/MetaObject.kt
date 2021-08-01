package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

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
@Target(FIELD)
@Retention(RUNTIME)
annotation class MetaObject
