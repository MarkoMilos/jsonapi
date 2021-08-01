package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

/**
 * Annotation for relationship object field.
 * Target field must be of type [Relationships].
 *
 * Example:
 * ```
 *  @Resource("articles")
 *  class Article() {
 *    @RelationshipsObject val relationships: Relationships? = null
 *  }
 * ```
 *
 * @see Relationships
 */
@JsonQualifier
@Target(FIELD)
@Retention(RUNTIME)
annotation class RelationshipsObject
