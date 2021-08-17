package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Annotation for to-many relationship fields.
 *
 * Library uses this annotation to find (or create) matching entry within relationships object and bind matching
 * resource value from included resources.
 *
 * Example:
 * ```
 *  @Resource("people")
 *  class Person
 *
 *  @Resource("comments")
 *  class Comment
 *
 *  @Resource("articles")
 *  class Article(
 *    // to-one relationship field
 *    @ToOne("author") val author: Person? = null,
 *    // to-many relationship field
 *    @ToMany("comments") val comments: List<Comment>? = null
 *  )
 * ```
 *
 * @see ToOne
 */
@JsonQualifier
@Target(FIELD, PROPERTY)
@Retention(RUNTIME)
annotation class ToMany(val name: String)
