package jsonapi

/**
 * Defines a “Resource object” that appears in a JSON:API document ([Document]) to represent resource.
 *
 * A resource object MUST contain at least the following: `id`, `type`.
 *
 * Exception: The `id` is not required when the resource object originates at the client and represents
 * a new resource to be created. In that case, a client MAY include a `lid` member to uniquely
 * identify the resource by type locally within the document.
 *
 *  @param type The `type` member is used to describe resource objects that share common attributes and relationships.
 *  The values of type members **MUST** adhere to the same constraints as
 *  [member names](https://jsonapi.org/format/1.1/#document-member-names).
 *  @param id Unique identifier of the resource.
 *  @param lid Unique identifier of the resource (by `type` *locally*). **MAY** be included if `id` is omitted.
 *  @param relationships a [Relationships] object describing relationships between the resource and other resources.
 *  @param links a [Links] object containing links related to the resource.
 *  @param meta a [Meta] object containing non-standard meta-information about the resource.
 */
class ResourceObject @JvmOverloads constructor(
  val type: String,
  val id: String? = null,
  val lid: String? = null,
  val relationships: Relationships? = null,
  val links: Links? = null,
  val meta: Meta? = null
) {

  init {
    require(type.isNotBlank()) {
      "A resource object MUST contain a type member but was blank."
    }
    require(!id.isNullOrBlank() || !lid.isNullOrBlank()) {
      "A resource object MUST contain an 'id' or 'lid' member but both were null or blank."
    }
  }

  /** Creates [ResourceIdentifier] from this resource. */
  fun identifier(): ResourceIdentifier {
    return ResourceIdentifier(type, id, lid, meta)
  }

  override fun equals(other: Any?): Boolean {
    return (other is ResourceObject) &&
      other.type == this.type &&
      other.id == this.id &&
      other.lid == this.lid
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + (id?.hashCode() ?: 0)
    result = 31 * result + (lid?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "ResourceObject[type=$type, id=$id, lid=$lid, relationships=$relationships, links=$links, meta=$meta]"
  }

  companion object {
    fun from(identifier: ResourceIdentifier): ResourceObject {
      return ResourceObject(identifier.type, identifier.id, identifier.lid, null, null, identifier.meta)
    }
  }
}
