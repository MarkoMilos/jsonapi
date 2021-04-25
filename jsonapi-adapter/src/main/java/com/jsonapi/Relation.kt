package com.jsonapi

import com.jsonapi.Relation.ToMany
import com.jsonapi.Relation.ToOne
import com.squareup.moshi.JsonClass

/**
 * [Relation] represent references from the resource object in which it’s defined to other resource objects.
 * Relation can be to-one ([ToOne]) or to-many ([ToMany]).
 */
sealed class Relation {
  /**
   * Links object containing at least one of the following:
   *   - **self**: a link for the relationship itself (a “relationship link”). This link allows the client to
   *   directly manipulate the relationship. For example, removing an author through an article’s relationship
   *   URL would disconnect the person from the article without deleting the people resource itself. When fetched
   *   successfully, this link returns the linkage for the related resources as its primary data.
   *   - **related**: a related resource link
   */
  abstract val links: Links?

  /** Non-standard meta-information about the relationship. */
  abstract val meta: Meta?

  @JsonClass(generateAdapter = true)
  data class ToOne @JvmOverloads constructor(
    /**
     * Resource linkage in a compound document allows a client to link together all of the included resource
     * objects without having to GET any URLs via links.
     *
     * [ToOne] resource linkage MUST be represented as one of the following:
     *   - `null` for empty to-one relationships
     *   - a single [ResourceIdentifier] object for non-empty to-one relationships
     */
    val data: ResourceIdentifier? = null,
    override val links: Links? = null,
    override val meta: Meta? = null
  ) : Relation()

  @JsonClass(generateAdapter = true)
  data class ToMany @JvmOverloads constructor(
    /**
     * Resource linkage in a compound document allows a client to link together all of the included resource
     * objects without having to GET any URLs via links.
     *
     * [ToMany] resource linkage MUST be represented as one of the following:
     *   - an empty array (`[]`) for empty to-many relationship
     *   - an array of [ResourceIdentifier] objects for non-empty to-many relationships
     */
    val data: List<ResourceIdentifier> = emptyList(),
    override val links: Links? = null,
    override val meta: Meta? = null
  ) : Relation()

  /** Returns true if this relation has a resource linkage (data), false otherwise. */
  fun hasData() = when (this) {
    is ToOne -> data != null
    is ToMany -> data.isNotEmpty()
  }
}
