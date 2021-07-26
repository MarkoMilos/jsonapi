package jsonapi

import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

// TODO update documentation
/**
 * Annotation used for defining JSON:API resource `type` for the given class.
 *
 * The `type` member is used to describe resource objects that share common attributes and relationships.
 * The values of type members **MUST** adhere to the same constraints as
 * [member names](https://jsonapi.org/format/1.1/#document-member-names).
 *
 * Example:
 * ```
 *  @Type("articles")
 *  class Article() : Resource()
 * ```
 */
@Inherited
@Target(CLASS)
@Retention(RUNTIME)
annotation class Resource(val type: String)
