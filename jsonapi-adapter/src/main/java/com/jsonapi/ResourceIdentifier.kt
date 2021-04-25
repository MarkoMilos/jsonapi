package com.jsonapi

import com.squareup.moshi.JsonClass

/**
 * A “resource identifier object” is an object that identifies an individual resource.
 *
 * A “resource identifier object” **MUST** contain a type member. It **MUST** also contain an id member, except
 * when it represents a new resource to be created on the server. In this case, a lid member **MUST** be included
 * that identifies the new resource.
 *
 * The values of the `id`, `type`, and `lid` members **MUST** be strings.
 *
 * A “resource identifier object” **MAY** also include a meta member, whose value is a meta object that
 * contains non-standard meta-information.
 *
 * @param type The `type` member is used to describe resource objects that share common attributes and relationships.
 * The values of type members **MUST** adhere to the same constraints as
 * [member names](https://jsonapi.org/format/1.1/#document-member-names).
 * @param id Unique identifier of the resource.
 * @param lid Unique identifier of the resource (by `type` *locally*). **MUST** be included if `id` is omitted.
 * @param meta a [Meta] object containing non-standard meta-information about a resource identifier.
 */
@JsonClass(generateAdapter = true)
class ResourceIdentifier @JvmOverloads constructor(
  val type: String,
  val id: String? = null,
  val lid: String? = null,
  val meta: Meta? = null
) {

  init {
    check(type.isNotBlank()) {
      "A “resource identifier object” MUST contain a type member but was blank."
    }
    check(!id.isNullOrBlank() || !lid.isNullOrBlank()) {
      "A “resource identifier object” MUST contain an 'id' or 'lid' member but both were null or blank."
    }
  }

  override fun equals(other: Any?): Boolean {
    return (other is ResourceIdentifier) &&
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
}
