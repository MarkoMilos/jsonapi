package jsonapi

import com.squareup.moshi.JsonQualifier

/**
 * Annotation for to-one relationship fields.
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
 * @see ToMany
 */
@JsonQualifier
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ToOne(val name: String)
