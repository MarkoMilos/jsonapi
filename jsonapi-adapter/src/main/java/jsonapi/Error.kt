package jsonapi

/**
 * Error objects provide additional information about problems encountered while performing an operation.
 *
 * @param id Unique identifier for this particular occurrence of the problem.
 * @param status HTTP status code applicable to this problem, expressed as a string value.
 * @param code Application-specific error code, expressed as a string value.
 * @param title Short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence
 * of the problem, except for purposes of localization.
 * @param detail Human-readable explanation specific to this occurrence of the problem that SHOULD NOT change
 * from occurrence to occurrence of the problem, except for purposes of localization.
 * @param source Contains references to the source of the error.
 * @param links Links object that MAY contain the following members:
 *  - **about**: a link that leads to further details about this particular occurrence of the problem.
 *  When dereference, this URI SHOULD return a human-readable description of the error.
 *  - **type**: a link that identifies the type of error that this particular error is an instance of.
 *  This URI SHOULD be dereferencable to a human-readable explanation of the general error.
 * @param meta Non-standard meta-information about the error.
 */
data class Error @JvmOverloads constructor(
  val id: String? = null,
  val status: String? = null,
  val code: String? = null,
  val title: String? = null,
  val detail: String? = null,
  val source: Source? = null,
  val links: Links? = null,
  val meta: Meta? = null
) {

  init {
    require(
      id != null ||
        status != null ||
        code != null ||
        title != null ||
        detail != null ||
        source != null ||
        links != null ||
        meta != null
    ) {
      "An error object MAY have the following members, and MUST contain at least one of: " +
        "id, status, code, title, detail, source, links, meta."
    }
  }

  /** Returns new [Builder] initialized from this. */
  fun newBuilder() = Builder(this)

  class Builder {
    private var id: String? = null
    private var status: String? = null
    private var code: String? = null
    private var title: String? = null
    private var detail: String? = null
    private var source: Source? = null
    private var links: Links? = null
    private var meta: Meta? = null

    constructor()

    internal constructor(error: Error) {
      this.id = error.id
      this.status = error.status
      this.code = error.code
      this.title = error.code
      this.detail = error.detail
      this.source = error.source
      this.links = error.links
      this.meta = error.meta
    }

    /** Unique identifier for this particular occurrence of the problem. */
    fun id(id: String?) = apply {
      this.id = id
    }

    /** HTTP status code applicable to this problem, expressed as a string value. */
    fun status(status: String?) = apply {
      this.status = status
    }

    /** Application-specific error code, expressed as a string value. */
    fun code(code: String?) = apply {
      this.code = code
    }

    /**
     * Short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem,
     * except for purposes of localization.
     */
    fun title(title: String?) = apply {
      this.title = title
    }

    /**
     * Human-readable explanation specific to this occurrence of the problem that SHOULD NOT change from occurrence
     * to occurrence of the problem, except for purposes of localization.
     */
    fun detail(detail: String?) = apply {
      this.detail = detail
    }

    /** Contains references to the source of the error. */
    fun source(source: Source?) = apply {
      this.source = source
    }

    /**
     * Links object that MAY contain the following members:
     *   - **about**: a link that leads to further details about this particular occurrence of the problem.
     *   When dereference, this URI SHOULD return a human-readable description of the error.
     *   - **type**: a link that identifies the type of error that this particular error is an instance of.
     *   This URI SHOULD be dereferencable to a human-readable explanation of the general error.
     */
    fun links(links: Links?) = apply {
      this.links = links
    }

    /** Non-standard meta-information about the error. */
    fun meta(meta: Meta?) = apply {
      this.meta = meta
    }

    /** Creates an instance of [Error] configured with this builder. */
    fun build(): Error {
      return Error(id, status, code, title, detail, source, links, meta)
    }
  }

  /**
   * Contains references to the source of the error.
   *
   * @param pointer JSON Pointer [RFC6901](https://tools.ietf.org/html/rfc6901) to the value in the request document
   * that caused the error (e.g. "/data" for a primary data object, or "/data/attributes/title" for a specific attribute).
   * This MUST point to a value in the request document that exists; if it doesn't, the client SHOULD simply ignore
   * the pointer.
   * @param parameter String indicating which URI query parameter caused the error.
   * @param header String indicating the name of a single request header which caused the error.
   */
  data class Source(
    val pointer: String? = null,
    val parameter: String? = null,
    val header: String? = null
  )
}
