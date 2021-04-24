package com.jsonapi

import com.jsonapi.Link.LinkObject
import com.jsonapi.Link.LinkURI

/**
 * Links is used to represent JSON:API [links](https://jsonapi.org/format/1.1/#document-links).
 *
 * Within this object, links are accessible via [members]. Within [members] links are represented
 * as a [Link] type which can be either:
 *  - [LinkURI] - holds an uri string whose value is a URI-reference
 *  - [LinkObject] - object holding `href` as URI-reference together with other information
 *  [per specification](https://jsonapi.org/format/1.1/#document-links-link-object)
 *
 *  A link’s relation type SHOULD be inferred from the name of the link unless the link is a link object
 *  and the link object ([LinkObject]) has a `rel` member.
 *
 *  A link’s context is the top-level object, resource object, or relationship object in which it appears.
 *
 *  @see Link
 */
class Links constructor(val members: Map<String, Link>) {
  
  constructor(vararg members: Pair<String, Link>) : this(mapOf(*members))
  
  // Standard links
  val self = members["self"]
  val related = members["related"]
  
  // Standard pagination links
  val first = members["first"]
  val previous = members["previous"]
  val next = members["next"]
  val last = members["last"]
  
  // Standard errors links
  val about = members["about"]
  val type = members["type"]
  
  /** Returns [Link] with [name], or null if provided name is not present. */
  fun get(name: String): Link? {
    return members[name]
  }
  
  /** Returns [Link] with [name], or [default] if provided name is not present. */
  fun getOrDefault(name: String, default: Link): Link {
    return members.getOrDefault(name, default)
  }
  
  /** Returns [LinkURI] with [name], or null if provided name is not present or link is not instance of [LinkURI]. */
  fun linkURI(name: String): LinkURI? {
    val link = members[name]
    return if (link is LinkURI) link else null
  }
  
  /** Returns [LinkObject] with [name], or null if provided name is not present or link is not instance of [LinkObject]. */
  fun linkObject(name: String): LinkObject? {
    val link = members[name]
    return if (link is LinkObject) link else null
  }
  
  /** Returns number of [Link]s in this object. */
  fun size() = members.size
}