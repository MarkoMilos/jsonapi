package com.jsonapi

/**
 * Annotation for relationship fields.
 *
 * Library uses this annotation to find (or create) matching entry within relationships map and bind matching resource
 * value from included resources.
 *
 * Field annotated with [Relationship] should be:
 * - Resource (or subclass) for to-one relations
 * - List<T> where T is a Resource (or subclass) for to-many relations
 *
 * Example:
 * ```
 *  @Type("people")
 *  class Person : Resource()
 *
 *  @Type("comments")
 *  class Comment: Resource()
 *
 *  @Type("articles")
 *  class Article(
 *    // to-one relation
 *    @Relationship("author") val author: Person? = null,
 *    // to-many relation
 *    @Relationship("comments") val comments: List<Comment>? = null,
 *  ) : Resource()
 * ```
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Relationship(val name: String)