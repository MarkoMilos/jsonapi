package com.jsonapi.model

import com.squareup.moshi.JsonClass

/**
 * # Definition
 * Defines a “Resource object” that appears in a JSON:API document ([Document]) to represent resource.
 *
 * A resource object MUST contain at least the following: `id`, `type`.
 *
 * Exception: The `id` is not required when the resource object originates at the client and represents
 * a new resource to be created. In that case, a client MAY include a `lid` member to uniquely
 * identify the resource by type locally within the document.
 *
 * # Extending
 * To create your custom resource you MUST extend from this class.
 *
 * Example: creating custom resource Article with `attributes` title and `relationship` author.
 *
 * **Kotlin**
 * ```
 * @Type("articles")
 * class Article(
 *   val title: String,
 *   @Relationship("author") val author: Person?
 * ) : Resource() {
 *  // implementation
 * }
 * ```
 *
 * It is possible to pass `type` and `id` to super constructor.
 *
 * **Java**
 * ```
 * @Type(name = "articles")
 * public class Article extends Resource {
 *
 *   public String title;
 *
 *   @Relationship(name = "author")
 *   public Person author;
 *
 *   public Article(String type, String id) {
 *     super(type, id);
 *   }
 * }
 * ```
 *
 *  @param type The `type` member is used to describe resource objects that share common attributes and relationships.
 *  The values of type members **MUST** adhere to the same constraints as
 *  [member names](https://jsonapi.org/format/1.1/#document-member-names)
 *  @param id Unique identifier of the resource
 *  @param lid Unique identifier of the resource (by `type` *locally*). **MAY** be included if `id` is omitted
 *  @param relationships a relationships map describing relationships between the resource and other resources
 *  @param links a [Links] object containing links related to the resource
 *  @param meta a [Meta] object containing non-standard meta-information about the resource
 */
@JsonClass(generateAdapter = true)
open class Resource @JvmOverloads constructor(
  var type: String? = null,
  var id: String? = null,
  var lid: String? = null,
  var relationships: Map<String, Relation>? = null,
  var links: Links? = null,
  var meta: Meta? = null
) {
  
  /**
   * Creates [ResourceIdentifier] from this resource.
   *
   * Note that resource needs to have a valid type and id (or lid in case when id is omitted).
   * Otherwise this will throw.
   */
  fun identifier(): ResourceIdentifier {
    return ResourceIdentifier(type ?: "", id, lid, meta)
  }
  
  override fun equals(other: Any?): Boolean {
    return (other is Resource)
      && other.type == this.type
      && other.id == this.id
      && other.lid == this.lid
  }
  
  override fun hashCode(): Int {
    var result = type?.hashCode() ?: 0
    result = 31 * result + (id?.hashCode() ?: 0)
    result = 31 * result + (lid?.hashCode() ?: 0)
    return result
  }
}