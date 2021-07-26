package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

// TODO update this docs
/**
 * Annotation for relationship fields.
 *
 * Library uses this annotation to find (or create) matching entry within relationships map and bind matching resource
 * value from included resources.
 *
 * Field annotated with [BindRelationship] should be:
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
@JsonQualifier
@Target(FIELD)
@Retention(RUNTIME)
annotation class BindRelationship(val name: String)
