package jsonapi

import jsonapi.Link.LinkObject
import jsonapi.Link.URI

/**
 * Links is used to represent JSON:API object [links](https://jsonapi.org/format/1.1/#document-links).
 *
 * Link values are accessible via [members] and are represented as a [Link] type which can be either:
 *  - [URI] - holds an string whose value is a URI-reference
 *  - [LinkObject] - object holding `href` as URI-reference together with other information
 *  [per specification](https://jsonapi.org/format/1.1/#document-links-link-object)
 *
 * A link’s relation type SHOULD be inferred from the name of the link unless the link is a link object
 * and the link object ([LinkObject]) has a `rel` member.
 *
 * A link’s context is the top-level object, resource object, or relationship object in which it appears.
 *
 * @see Link
 */
class Links(val members: Map<String, Link?>) {

  constructor(vararg members: Pair<String, Link?>) : this(mapOf(*members))

  // Standard links
  /** Returns [Link] with name 'self'. */
  fun self() = members["self"]

  /** Returns [Link] with name 'related'. */
  fun related() = members["related"]

  // Standard pagination links
  /** Returns [Link] with name 'first'. */
  fun first() = members["first"]

  /** Returns [Link] with name 'previous'. */
  fun previous() = members["prev"]

  /** Returns [Link] with name 'next'. */
  fun next() = members["next"]

  /** Returns [Link] with name 'last'. */
  fun last() = members["last"]

  // Standard errors links
  /** Returns [Link] with name 'about'. */
  fun about() = members["about"]

  /** Returns [Link] with name 'type'. */
  fun type() = members["type"]

  /** Returns number of [Link]s in this object. */
  fun size() = members.size

  /** Returns true if links are empty, false otherwise. */
  fun isEmpty() = members.isEmpty()

  /** Returns true if link with [name] is present, false otherwise. */
  fun has(name: String) = members.containsKey(name)

  /** Returns true if link with [name] is present and has non-null value, false otherwise. */
  fun hasNonNull(name: String) = members[name] != null

  /** Returns [Link] with [name], or null if provided name is not present. */
  fun get(name: String): Link? {
    return members[name]
  }

  /** Returns [Link] with [name], or [default] if provided name is not present. */
  fun getOrDefault(name: String, default: Link): Link {
    return members[name] ?: default
  }

  /** Returns new [Builder] initialized from this. */
  fun newBuilder() = Builder(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return (other is Links) && this.members == other.members
  }

  override fun hashCode(): Int {
    return members.hashCode()
  }

  override fun toString(): String {
    return "Links(members=$members)"
  }

  companion object {
    /** Creates [Links] from [members] map transforming value of map from string to [URI] link. */
    @JvmStatic
    fun from(members: Map<String, String?>): Links {
      return Links(members.mapValues { (_, value) -> if (value != null) URI(value) else null })
    }

    /** Creates [Links] from [members] pairs transforming second/right value of each pair from string to [URI] link. */
    @JvmStatic
    fun from(vararg members: Pair<String, String?>): Links {
      return from(members.toMap())
    }
  }

  class Builder {
    private val members: MutableMap<String, Link?>

    constructor() {
      this.members = mutableMapOf()
    }

    internal constructor(links: Links) {
      this.members = links.members.toMutableMap()
    }

    /** Add [Link] with [name]. */
    fun add(name: String, link: Link?) = apply {
      this.members[name] = link
    }

    /** Create [URI] from [link] and add it under [name]. */
    fun add(name: String, link: String?) = apply {
      this.members[name] = if (link != null) URI(link) else null
    }

    /** Add all links from [members] map. */
    fun add(members: Map<String, Link?>) = apply {
      this.members.putAll(members)
    }

    /** Add all links from [members] map by transforming map values from string to [URI]. */
    fun addURIs(members: Map<String, String?>) = apply {
      this.members.putAll(members.mapValues { (_, value) -> if (value != null) URI(value) else null })
    }

    /** Creates an instance of [Links] configured with this builder. */
    fun build(): Links {
      return Links(members)
    }
  }
}
