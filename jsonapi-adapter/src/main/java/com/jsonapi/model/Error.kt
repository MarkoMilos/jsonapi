package com.jsonapi.model

import com.squareup.moshi.JsonClass

/**
 * Error objects provide additional information about problems encountered while performing an operation.
 */
@JsonClass(generateAdapter = true)
data class Error @JvmOverloads constructor(
  /** Unique identifier for this particular occurrence of the problem. */
  val id: String? = null,
  /**
   * Links object that MAY contain the following members:
   *   - **about**: a link that leads to further details about this particular occurrence of the problem.
   *   When dereference, this URI SHOULD return a human-readable description of the error.
   *   - **type**: a link that identifies the type of error that this particular error is an instance of.
   *   This URI SHOULD be dereferencable to a human-readable explanation of the general error.
   */
  val links: Links? = null,
  /** HTTP status code applicable to this problem, expressed as a string value. */
  val status: String? = null,
  /** Application-specific error code, expressed as a string value. */
  val code: String? = null,
  /**
   * Short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem,
   * except for purposes of localization.
   */
  val title: String? = null,
  /**
   * Human-readable explanation specific to this occurrence of the problem that SHOULD NOT change from occurrence
   * to occurrence of the problem, except for purposes of localization.
   */
  val detail: String? = null,
  /** Contains references to the source of the error. */
  val source: Source? = null,
  /** Non-standard meta-information about the error. */
  val meta: Meta? = null
) {
  /** Contains references to the source of the error. */
  @JsonClass(generateAdapter = true)
  data class Source(
    /**
     * JSON Pointer [RFC6901](https://tools.ietf.org/html/rfc6901) to the value in the request document that caused
     * the error (e.g. "/data" for a primary data object, or "/data/attributes/title" for a specific attribute).
     * This MUST point to a value in the request document that exists; if it doesn't, the client SHOULD
     * simply ignore the pointer.
     */
    val pointer: String?,
    /** String indicating which URI query parameter caused the error. */
    val parameter: String?
  )
  
  class Builder {
    private var id: String? = null
    private var links: Links? = null
    private var status: String? = null
    private var code: String? = null
    private var title: String? = null
    private var detail: String? = null
    private var source: Source? = null
    private var meta: Meta? = null
    
    fun id(id: String) = apply {
      this.id = id
    }
    
    fun links(links: Links) = apply {
      this.links = links
    }
    
    fun status(status: String) = apply {
      this.status = status
    }
    
    fun code(code: String) = apply {
      this.code = code
    }
    
    fun title(title: String) = apply {
      this.title = title
    }
    
    fun detail(detail: String) = apply {
      this.detail = detail
    }
    
    fun source(source: Source) = apply {
      this.source = source
    }
    
    fun meta(meta: Meta) = apply {
      this.meta = meta
    }
    
    fun build(): Error {
      return Error(id, links, status, code, title, detail, source, meta)
    }
  }
}