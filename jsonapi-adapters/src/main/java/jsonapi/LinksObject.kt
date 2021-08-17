package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Annotation for links object field.
 * Target field must be of type [Links].
 *
 * Example:
 * ```
 *  @Resource("articles")
 *  class Article() {
 *    @LinkObject val links: Links? = null
 *  }
 * ```
 *
 * @see Links
 */
@JsonQualifier
@Target(FIELD, PROPERTY)
@Retention(RUNTIME)
annotation class LinksObject
