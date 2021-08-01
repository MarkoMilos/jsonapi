package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

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
@Target(FIELD)
@Retention(RUNTIME)
annotation class LinksObject
