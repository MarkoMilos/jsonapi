package com.jsonapi

/**
 * A [Link] MUST be represented as either:
 *  - [LinkURI] - which holds an uri string whose value is a URI-reference
 *  - [LinkObject] - a link object which holds href as URI-reference together with other information
 * [per specification](https://jsonapi.org/format/1.1/#document-links-link-object)
 *
 * @see LinkURI
 * @see LinkObject
 */
sealed class Link {
  /**
   * Holds [uri] string whose value is a URI-reference.
   *
   * @param uri String whose value is a URI-reference
   * [RFC3986 Section 4.1](https://tools.ietf.org/html/rfc3986#section-4.1) pointing to the link’s target.
   */
  data class LinkURI(val uri: String) : Link()
  
  /**
   * A “link object” is an object that represents a [web link](https://tools.ietf.org/html/rfc8288).
   *
   * A link object MUST contain the following member `href` a string whose value is a URI-reference pointing to
   * the link’s target.
   *
   * A link object MAY also contain any of the following members: `rel`, `describedby`, `title`, `type`, `hreflang`,
   * `meta`.
   *
   * @param href String whose value is a URI-reference [RFC3986 Section 4.1](https://tools.ietf.org/html/rfc3986#section-4.1)
   * pointing to the link’s target.
   * @param rel String indicating the link’s relation type. The string MUST be a valid link relation type.
   * @param describedby Link to a description document (e.g. OpenAPI or JSON Schema) for the link target.
   * @param title String which serves as a label for the destination of a link such that it can be used
   * as a human-readable identifier (e.g., a menu entry).
   * @param type String indicating the media type of the link’s target.
   * @param hreflang An array of strings indicating the language(s) of the link’s target.
   * An array with multiple values (size > 1) indicates that the link’s target is available in multiple languages.
   * Each string MUST be a valid language tag [RFC5646](https://tools.ietf.org/html/rfc5646).
   * Note: this data structure deviates from specification. This value is specified as either string or array
   * of strings but this data structure defines `hreflang` as array of strings in both cases and it respects
   * specification by doing the following:
   *  * deserialization: single string values will be deserialized as list with 1 item
   *  * serialization: `hreflang` list with 1 item will be serialized as json string instead array
   * @param meta Meta object containing non-standard meta-information about the link.
   */
  data class LinkObject @JvmOverloads constructor(
    val href: String,
    val rel: String? = null,
    val describedby: Link? = null,
    val title: String? = null,
    val type: String? = null,
    val hreflang: List<String>? = null,
    val meta: Meta? = null
  ) : Link()
  
  /**
   * Returns string whose value is a URI-reference [RFC3986 Section 4.1](https://tools.ietf.org/html/rfc3986#section-4.1)
   * pointing to the link’s target for given link type.
   *
   * @see LinkURI
   * @see LinkObject
   */
  fun uri(): String {
    return when (this) {
      is LinkURI -> uri
      is LinkObject -> href
    }
  }
  
  /** If this is instance of [LinkURI] returns it, otherwise return null */
  fun asURI(): LinkURI? {
    return if (this is LinkURI) this else null
  }
  
  /** If this is instance of [LinkObject] returns it, otherwise return null */
  fun asObject(): LinkObject? {
    return if (this is LinkObject) this else null
  }
}