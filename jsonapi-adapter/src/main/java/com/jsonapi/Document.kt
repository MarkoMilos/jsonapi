package com.jsonapi

import com.jsonapi.Document.Builder
import com.jsonapi.Document.IncludedSerialization.PROCESSED

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
 * @param includedSerialization defines strategy for serializing included resources
 *
 * @see Builder
 */
class Document<T> internal constructor(
  val data: T? = null,
  val included: List<Any>? = null,
  val errors: List<Error>? = null,
  val links: Links? = null,
  val meta: Meta? = null,
  val jsonapi: JsonApiObject? = null,
  internal val includedSerialization: IncludedSerialization = PROCESSED
) {

  init {
    if (data != null && errors != null) {
      throw IllegalArgumentException("The members data and errors MUST NOT coexist in the same document.")
    }

    if (included != null && data == null) {
      throw IllegalArgumentException("If data member is null, the included member MUST NOT be present either.")
    }
  }

  /** Returns true if document has non-null `data` member, false otherwise. */
  fun hasData(): Boolean {
    return data != null
  }

  /**
   * Returns true if document has errors, false otherwise.
   * Note this will return true even if errors are empty.
   */
  fun hasErrors(): Boolean {
    return errors != null
  }

  /** Returns true if document has meta, false otherwise. */
  fun hasMeta(): Boolean {
    return meta != null
  }

  /** Get primary resource for this document ignoring existence of errors. */
  fun dataOrNull(): T? = data

  /**
   * Get primary resource for this document. Throw is this document is has errors.
   *
   * @throws ErrorsException document has errors
   */
  fun dataOrThrow(): T? {
    return if (errors != null) throw ErrorsException(errors) else data
  }

  /**
   * Get primary resource for this document. Throw if document has errors or primary resource is null.
   *
   * @throws ErrorsException document has errors
   * @throws NullPointerException primary resource is null
   */
  fun requireData(): T {
    if (errors != null) {
      throw ErrorsException(errors)
    }
    return data ?: throw NullPointerException("Resource(s) for this document is null")
  }

  /**
   * Get primary resource for this document. Return [default] in case when primary resource is null
   * or this document is has errors.
   */
  fun dataOrDefault(default: T): T {
    return data ?: default
  }

  /**
   * Get primary resource for this document. Return result of [block] in case when primary resource is null
   * or this document is has errors.
   *
   * @param block block that will be called if primary resource is null or document is has errors.
   * Errors, if exist for this document, will be passed to block.
   */
  fun dataOrElse(block: (List<Error>?) -> T): T {
    return data ?: block(errors)
  }

  /** Returns errors list if document has errors, otherwise returns empty list. */
  fun errorsOrEmpty(): List<Error> {
    return errors ?: emptyList()
  }

  /** Throws [ErrorsException] if this document is has errors. */
  fun throwIfErrors() {
    if (errors != null) {
      throw ErrorsException(errors)
    }
  }

  /** Returns new [Builder] initialized from this. */
  fun newBuilder() = Builder(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Document<*>

    if (data != other.data) return false
    if (included != other.included) return false
    if (errors != other.errors) return false
    if (links != other.links) return false
    if (meta != other.meta) return false
    if (jsonapi != other.jsonapi) return false

    return true
  }

  override fun hashCode(): Int {
    var result = data?.hashCode() ?: 0
    result = 31 * result + (included?.hashCode() ?: 0)
    result = 31 * result + (errors?.hashCode() ?: 0)
    result = 31 * result + (links?.hashCode() ?: 0)
    result = 31 * result + (meta?.hashCode() ?: 0)
    result = 31 * result + (jsonapi?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "Document(" +
      "\n\tdata=$data," +
      "\n\tincluded=$included " +
      "\n\terrors=$errors " +
      "\n\tlinks=$links " +
      "\n\tmeta=$meta " +
      "\n\tjsonapi=$jsonapi " +
      "\n)"
  }

  companion object {
    /** Creates empty [Document]. */
    @JvmStatic
    fun empty(): Document<Nothing> {
      return Builder<Nothing>().build()
    }

    /** Creates [Document] from [data]. */
    @JvmStatic
    fun <T> from(data: T): Document<T> {
      return Builder<T>().data(data).build()
    }

    /** Creates [Document] from [resources]. */
    @JvmStatic
    fun <T> from(vararg resources: T): Document<List<T>> {
      return Builder<List<T>>().data(listOf(*resources)).build()
    }

    /** Creates [Document] from [errors]. */
    @JvmStatic
    fun from(vararg errors: Error): Document<Nothing> {
      return from(listOf(*errors))
    }

    /** Creates [Document] from [errors]. */
    @JvmStatic
    fun from(errors: List<Error>): Document<Nothing> {
      return Builder<Nothing>().errors(errors).build()
    }

    /** Creates [Document] from [meta]. */
    @JvmStatic
    fun from(meta: Meta): Document<Nothing> {
      return Builder<Nothing>().meta(meta).build()
    }

    /** Creates [Builder] from [data]. */
    @JvmStatic
    fun <T> with(data: T): Builder<T> {
      return Builder<T>().data(data)
    }

    /** Creates [Builder] from [resources]. */
    @JvmStatic
    fun <T> with(vararg resources: T): Builder<List<T>> {
      return Builder<List<T>>().data(listOf(*resources))
    }

    /** Creates [Builder] from [errors]. */
    @JvmStatic
    fun <T> with(vararg errors: Error): Builder<T> {
      return Builder<T>().errors(listOf(*errors))
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
   *  Document.Builder()
   *    .data(resource)
   *    .links(...)
   *    .meta(...)
   *    .build()
   * ```
   */
  class Builder<T> {
    private var data: T? = null
    private var included: List<Any>? = null
    private var errors: List<Error>? = null
    private var links: Links? = null
    private var meta: Meta? = null
    private var jsonapi: JsonApiObject? = null
    private var includedSerialization: IncludedSerialization

    constructor() {
      this.includedSerialization = PROCESSED
    }

    internal constructor(document: Document<T>) {
      this.data = document.data
      this.included = document.included
      this.errors = document.errors
      this.links = document.links
      this.meta = document.meta
      this.jsonapi = document.jsonapi
      this.includedSerialization = document.includedSerialization
    }

    /**
     * The document’s “primary data” is a representation of the resource or collection of resources.
     * Primary data MUST be either:
     *  * a single resource object, a single resource identifier object, or null, for requests that target
     *  single resources
     *  * an array of resource objects, an array of resource identifier objects, or an empty array ([]),
     *  for requests that target resource collections
     */
    fun data(data: T?) = apply {
      this.data = data
    }

    /** An array of resource objects that are related to the primary data and/or each other (“included resources”). */
    fun included(resources: List<Any>?) = apply {
      this.included = resources
    }

    /** An array of [Error] objects. */
    fun errors(errors: List<Error>?) = apply {
      this.errors = errors
    }

    /** An array of [Error] objects. */
    fun errors(vararg errors: Error) = apply {
      this.errors = listOf(*errors)
    }

    /** A links object related to the primary data. */
    fun links(links: Links?) = apply {
      this.links = links
    }

    /** A meta object that contains non-standard meta-information. */
    fun meta(meta: Meta?) = apply {
      this.meta = meta
    }

    /** Includes information about JSON:API implementation. */
    fun jsonapi(jsonapi: JsonApiObject?) = apply {
      this.jsonapi = jsonapi
    }

    /**
     * Defines strategy for serialization of included resources.
     * Default value is [PROCESSED].
     *
     * @see IncludedSerialization
     */
    fun includedSerialization(includedSerialization: IncludedSerialization) = apply {
      this.includedSerialization = includedSerialization
    }

    /** Creates an instance of [Document] configured with this builder. */
    fun build(): Document<T> {
      return Document(data, included, errors, links, meta, jsonapi, includedSerialization)
    }
  }

  /** Defines strategy for serializing included resources. */
  enum class IncludedSerialization {
    /** Included resources won't be serialized. */
    NONE,

    /**
     * Only included resources defined for document field `included` will be serialized.
     * Relationships of resources within document will be ignored.
     */
    DOCUMENT,

    /**
     * Included resources will be processed from each resources within this document (primary data and included)
     * by searching for non-null relationships and adding them to included resources list.
     * Processed resources are then merged with ones defined for the document `included` field.
     * During this process duplicates are omitted ensuring that only the same resources is not present within primary
     * and included.
     */
    PROCESSED
  }
}
