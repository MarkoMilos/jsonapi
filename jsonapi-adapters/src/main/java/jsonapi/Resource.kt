package jsonapi

import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Annotation used for defining JSON:API resource.
 *
 * The [type] name is used to describe resource objects that share common attributes and relationships.
 * The values of type **MUST** adhere to the same constraints as
 * [member names](https://jsonapi.org/format/1.1/#document-member-names).
 *
 * Example:
 * ```
 *  @Resource("articles")
 *  class Article()
 * ```
 */
@Inherited
@Target(CLASS)
@Retention(RUNTIME)
annotation class Resource(val type: String)
