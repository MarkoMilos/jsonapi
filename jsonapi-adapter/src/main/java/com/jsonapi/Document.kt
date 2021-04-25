package com.jsonapi

import com.jsonapi.Document.Builder
import com.jsonapi.internal.serialization.SerializationRules
import com.squareup.moshi.JsonClass

/**
 * Defines top level entity for JSON:API.
 *
 * A document MUST contain at least one of the following top-level members:
 *  * `data`: the document’s “primary data”
 *  * `errors`: an array of error objects
 *  * `meta`: a meta object that contains non-standard meta-information.
 *
 * The members `data` and `errors` MUST NOT coexist in the same document.
 *
 * A [Document] MAY contain any of these top-level members:
 *  * `links`: a links object related to the primary data
 *  * `included`: an array of resource objects that are related to the primary data and/or each other (“included resources”)
 *
 * If a document does not contain a top-level `data` key, the `included` member MUST NOT be present either.
 *
 * @param data The document’s “primary data” is a representation of the resource or collection of resources
 * targeted by a request. Primary data MUST be either:
 *  * a single resource object, a single resource identifier object, or null, for requests that target single resources
 *  * an array of resource objects, an array of resource identifier objects, or an empty array ([]),
 *    for requests that target resource collections
 * @param included an array of resource objects that are related to the primary data and/or each other (“included resources”)
 * @param errors an array of [Error] objects
 * @param links a links object related to the primary data
 * @param meta a meta object that contains non-standard meta-information
 * @param jsonapi includes information about JSON:API implementation
 *
 * @see Builder
 */
@JsonClass(generateAdapter = true)
class Document<T> @JvmOverloads constructor(
  val data: T? = null,
  val included: List<Resource>? = null,
  val errors: List<Error>? = null,
  val links: Links? = null,
  val meta: Meta? = null,
  val jsonapi: JsonApiInfo? = null
) {

  @Transient internal var serializationRules: SerializationRules? = null

  init {
    if (data != null && errors != null) {
      throw JsonApiException("The members data and errors MUST NOT coexist in the same document.")
    }
  }

  /** Returns true if document has non-null `data` member, false otherwise. */
  fun hasData(): Boolean {
    return data != null
  }

  /** Returns true if document has errors, false otherwise. */
  fun hasErrors(): Boolean {
    return errors != null
  }

  /** Returns true if document has meta, false otherwise. */
  fun hasMeta(): Boolean {
    return meta != null
  }

  /**
   * Get primary resource for this document. Throw if document has errors or primary resource is null.
   *
   * @throws JsonApiErrorsException document has errors
   * @throws NullPointerException primary resource is null
   */
  fun get(): T {
    if (errors != null) {
      throw JsonApiErrorsException(errors)
    }
    return data ?: throw NullPointerException("Resource(s) for this document is null")
  }

  /** Get primary resource for this document. Return null if document has errors. */
  fun getOrNull(): T? = data

  /**
   * Get primary resource for this document. Throw is this document is has errors.
   *
   * @throws JsonApiErrorsException document has errors
   */
  fun getOrThrow(): T? {
    return if (errors == null) data else throw JsonApiErrorsException(errors)
  }

  /**
   * Get primary resource for this document. Return [default] in case when primary resource is null
   * or this document is has errors.
   */
  fun getOrDefault(default: T): T {
    return data ?: default
  }

  /**
   * Get primary resource for this document. Return result of [block] in case when primary resource is null
   * or this document is has errors.
   *
   * @param block block that will be called if primary resource is null or document is has errors.
   * Errors, if exist for this document, will be passed to block.
   */
  fun getOrElse(block: (List<Error>?) -> T): T {
    return data ?: block(errors)
  }

  /** Returns errors list if document has errors, otherwise returns empty list. */
  fun errorsOrEmpty(): List<Error> {
    return errors ?: emptyList()
  }

  /** Throws [JsonApiErrorsException] if this document is has errors. */
  fun throwIfErrors() {
    if (errors != null) {
      throw JsonApiErrorsException(errors)
    }
  }

  companion object {
    /** Creates [Document] from [resource]. */
    @JvmStatic
    fun <T> from(resource: T): Document<T> {
      return Builder<T>().data(resource).build()
    }

    /** Creates [Document] from [error]. */
    @JvmStatic
    fun <T> from(error: Error): Document<T> {
      return Builder<T>().errors(listOf(error)).build()
    }

    /** Creates [Document] from [errors]. */
    @JvmStatic
    fun <T> from(errors: List<Error>): Document<T> {
      return Builder<T>().errors(errors).build()
    }

    /** Creates [Document] from [meta]. */
    @JvmStatic
    fun <T> from(meta: Meta): Document<T> {
      return Builder<T>().meta(meta).build()
    }

    /** Creates [Builder] from [resource]. */
    @JvmStatic
    fun <T> with(resource: T): Builder<T> {
      return Builder<T>().data(resource)
    }

    /** Creates [Builder] from [error]. */
    @JvmStatic
    fun <T> with(error: Error): Builder<T> {
      return Builder<T>().errors(listOf(error))
    }

    /** Creates [Builder] from [errors]. */
    @JvmStatic
    fun <T> with(errors: List<Error>): Builder<T> {
      return Builder<T>().errors(errors)
    }

    /** Creates [Builder] from [meta]. */
    @JvmStatic
    fun <T> with(meta: Meta): Builder<T> {
      return Builder<T>().meta(meta)
    }
  }

  /**
   * Builder for creating instance of [Document].
   *
   * Example:
   * ```
   *  Document.with(resource)
   *    .links(...)
   *    .meta(...)
   *    .build()
   * ```
   */
  class Builder<T> {

    private var data: T? = null
    private var included: List<Resource>? = null
    private var errors: List<Error>? = null
    private var links: Links? = null
    private var meta: Meta? = null
    private var jsonapi: JsonApiInfo? = null
    private var serializeIncluded: Boolean = true

    fun data(data: T) = apply {
      this.data = data
    }

    fun included(resources: List<Resource>) = apply {
      this.included = resources
    }

    fun errors(errors: List<Error>) = apply {
      this.errors = errors
    }

    fun links(links: Links) = apply {
      this.links = links
    }

    fun meta(meta: Meta) = apply {
      this.meta = meta
    }

    fun jsonapi(jsonapi: JsonApiInfo) = apply {
      this.jsonapi = jsonapi
    }

    /**
     * Determines weather the included member should be serialized.
     *
     * By default included member will be created and serialized if primary resource has
     * non-null relationship fields.
     *
     * Setting this to `false` will skip serialization for included.
     */
    fun serializeIncluded(serializeIncluded: Boolean) = apply {
      this.serializeIncluded = serializeIncluded
    }

    /**
     * Build [Document] configured with this builder.
     *
     * @throws JsonApiException throw when document has both data and errors member that should not
     * coexist within the same document.
     */
    fun build(): Document<T> {
      val document = Document(data, included, errors, links, meta, jsonapi)
      document.serializationRules = SerializationRules(serializeIncluded)
      return document
    }
  }
}
