package com.jsonapi

import com.squareup.moshi.JsonClass

/**
 * Error objects provide additional information about problems encountered while performing an operation.
 *
 * @param id Unique identifier for this particular occurrence of the problem.
 * @param links Links object that MAY contain the following members:
 *   - **about**: a link that leads to further details about this particular occurrence of the problem.
 *   When dereference, this URI SHOULD return a human-readable description of the error.
 *   - **type**: a link that identifies the type of error that this particular error is an instance of.
 *   This URI SHOULD be dereferencable to a human-readable explanation of the general error.
 * @param status HTTP status code applicable to this problem, expressed as a string value.
 * @param code Application-specific error code, expressed as a string value.
 * @param title Short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence
 * of the problem, except for purposes of localization.
 * @param detail Human-readable explanation specific to this occurrence of the problem that SHOULD NOT change
 * from occurrence to occurrence of the problem, except for purposes of localization.
 * @param source Contains references to the source of the error.
 * @param meta Non-standard meta-information about the error.
 */
@JsonClass(generateAdapter = true)
data class Error @JvmOverloads constructor(
  val id: String? = null,
  val links: Links? = null,
  val status: String? = null,
  val code: String? = null,
  val title: String? = null,
  val detail: String? = null,
  val source: Source? = null,
  val meta: Meta? = null
) {
  /**
   * Contains references to the source of the error.
   *
   * @param pointer JSON Pointer [RFC6901](https://tools.ietf.org/html/rfc6901) to the value in the request document
   * that caused the error (e.g. "/data" for a primary data object, or "/data/attributes/title" for a specific attribute).
   * This MUST point to a value in the request document that exists; if it doesn't, the client SHOULD simply ignore
   * the pointer.
   * @param parameter String indicating which URI query parameter caused the error.
   */
  @JsonClass(generateAdapter = true)
  data class Source(
    val pointer: String?,
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
    
    /** Unique identifier for this particular occurrence of the problem. */
    fun id(id: String) = apply {
      this.id = id
    }
    
    /**
     * Links object that MAY contain the following members:
     *   - **about**: a link that leads to further details about this particular occurrence of the problem.
     *   When dereference, this URI SHOULD return a human-readable description of the error.
     *   - **type**: a link that identifies the type of error that this particular error is an instance of.
     *   This URI SHOULD be dereferencable to a human-readable explanation of the general error.
     */
    fun links(links: Links) = apply {
      this.links = links
    }
    
    /** HTTP status code applicable to this problem, expressed as a string value. */
    fun status(status: String) = apply {
      this.status = status
    }
    
    /** Application-specific error code, expressed as a string value. */
    fun code(code: String) = apply {
      this.code = code
    }
    
    /**
     * Short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem,
     * except for purposes of localization.
     */
    fun title(title: String) = apply {
      this.title = title
    }
    
    /**
     * Human-readable explanation specific to this occurrence of the problem that SHOULD NOT change from occurrence
     * to occurrence of the problem, except for purposes of localization.
     */
    fun detail(detail: String) = apply {
      this.detail = detail
    }
    
    /** Contains references to the source of the error. */
    fun source(source: Source) = apply {
      this.source = source
    }
    
    /** Non-standard meta-information about the error. */
    fun meta(meta: Meta) = apply {
      this.meta = meta
    }
    
    /** Creates an [Error] */
    fun build(): Error {
      return Error(id, links, status, code, title, detail, source, meta)
    }
  }
}