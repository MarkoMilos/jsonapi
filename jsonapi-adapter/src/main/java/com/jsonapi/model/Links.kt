package com.jsonapi.model

import com.jsonapi.model.Link.LinkObject
import com.jsonapi.model.Link.LinkURI

/**
 * Links is used to represent JSON:API [links](https://jsonapi.org/format/1.1/#document-links).
 *
 * Within this object, links are accessible via [linksMap]. Within [linksMap] links are represented
 * as [Link] which can be either:
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
class Links(
  val linksMap: Map<String, Link>
) {
  // Standard links
  val self = linksMap["self"]
  val related = linksMap["related"]
  
  // Standard pagination links
  val first = linksMap["first"]
  val previous = linksMap["previous"]
  val next = linksMap["next"]
  val last = linksMap["last"]
  
  // Standard errors links
  val about = linksMap["about"]
  val type = linksMap["type"]
  
  /** Returns [Link] with [name], or null if provided name is not present. */
  fun get(name: String): Link? {
    return linksMap[name]
  }
  
  /** Returns [Link] with [name], or [default] if provided name is not present. */
  fun getOrDefault(name: String, default: Link): Link {
    return linksMap.getOrDefault(name, default)
  }
  
  /** Returns [LinkURI] with [name], or null if provided name is not present or link is not instance of [LinkURI]. */
  fun linkURI(name: String): LinkURI? {
    val link = linksMap[name]
    return if (link is LinkURI) link else null
  }
  
  /** Returns [LinkObject] with [name], or null if provided name is not present or link is not instance of [LinkObject]. */
  fun linkObject(name: String): LinkObject? {
    val link = linksMap[name]
    return if (link is LinkObject) link else null
  }
  
  /** Returns number of [Link]s in this object. */
  fun size() = linksMap.size
}