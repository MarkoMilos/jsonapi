package com.jsonapi

import com.jsonapi.Document.Data
import com.jsonapi.Document.Errors
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
 * [Document] has two subtypes:
 *  * [Data] for documents containing `data` member (documents with resources) and meta only documents
 *  * [Errors] for documents containing `errors` member
 *
 * While deserializing [Document] it will be deserialized as either [Data] or [Errors] according to members
 * found in json object during deserialization. Documents containing only `meta` member will be deserialized as [Data].
 *
 * For serialization you can be explicit by using either [Data] or [Errors].
 *
 * A [Document] MAY contain any of these top-level members:
 *  * `links`: a links object related to the primary data
 *  * `included`: an array of resource objects that are related to the primary data and/or each other (“included resources”)
 *
 * If a document does not contain a top-level `data` key, the `included` member MUST NOT be present either.
 * That said, `included` member is available only in [Data] subtype.
 *
 * @see Data
 * @see Errors
 */
sealed class Document<T> {
  
  /**
   * [Document] subtype for documents containing `data` member (document with resource(s)) and `meta` only documents.
   *
   * @param data The document’s “primary data” is a representation of the resource or collection of resources
   * targeted by a request. Primary data MUST be either:
   *  * a single resource object, a single resource identifier object, or null, for requests that target single resources
   *  * an array of resource objects, an array of resource identifier objects, or an empty array ([]),
   *    for requests that target resource collections
   * @param included an array of resource objects that are related to the primary data and/or each other (“included resources”)
   * @param links a links object related to the primary data
   * @param meta a meta object that contains non-standard meta-information
   *
   * @see Document
   */
  @JsonClass(generateAdapter = true)
  data class Data<T> @JvmOverloads constructor(
    val data: T? = null,
    val included: List<Resource>? = null,
    val links: Links? = null,
    val meta: Meta? = null,
  ) : Document<T>()
  
  /**
   * [Document] subtype for documents containing `errors` member.
   *
   * @param errors an array of [Error] objects
   * @param links a links object related to the primary data
   * @param meta a meta object that contains non-standard meta-information
   *
   * @see Document
   * @see Error
   */
  @JsonClass(generateAdapter = true)
  data class Errors @JvmOverloads constructor(
    val errors: List<Error>,
    val links: Links? = null,
    val meta: Meta? = null,
  ) : Document<Nothing>()
  
  /** Returns true if this is a [Data] document, false otherwise. */
  fun isData() = this is Data
  
  /** Returns true if this is a [Errors] document, false otherwise. */
  fun isErrors() = this is Errors
  
  /** Returns this document as [Data], return null if document is not [Data]. */
  fun asDataOrNull(): Data<T>? {
    return when (this) {
      is Data -> this
      is Errors -> null
    }
  }
  
  /** Returns this document as [Data], throw if document is not [Data]. */
  fun asDataOrThrow(): Data<T> {
    return when (this) {
      is Data -> this
      is Errors -> throw JsonApiErrorsException(this)
    }
  }
  
  /** Returns this document as [Errors], return null if document is not [Errors]. */
  fun asErrorsOrNull(): Errors? {
    return when (this) {
      is Data -> null
      is Errors -> this
    }
  }
  
  /** Returns this document as [Errors], throw if document is not [Errors]. */
  fun asErrorsOrThrow(): Errors {
    return when (this) {
      is Data -> throw IllegalStateException("Document was Data but Errors was expected")
      is Errors -> this
    }
  }
  
  /** Executes given [block] only if this document is [Data]. */
  fun onData(block: () -> Unit): Document<T> {
    if (this is Data) block()
    return this
  }
  
  /** Executes given [block] only if this document is [Errors]. */
  fun onErrors(block: () -> Unit): Document<T> {
    if (this is Errors) block()
    return this
  }
  
  /** Get primary resource for this document. Throw if document is not [Data] or if primary resource is null. */
  fun getNonNull(): T {
    return when (this) {
      is Data -> data ?: throw NullPointerException("Resource(s) for this document is null")
      is Errors -> throw JsonApiErrorsException(this)
    }
  }
  
  /** Get primary resource for this document. Return null if document is not [Data] or if primary resource is null. */
  fun getOrNull(): T? {
    return when (this) {
      is Data -> data
      is Errors -> null
    }
  }
  
  /** Get primary resource for this document. Throw is this document is [Errors]. */
  fun getOrThrow(): T? {
    return when (this) {
      is Data -> data
      is Errors -> throw JsonApiErrorsException(this)
    }
  }
  
  /**
   * Get primary resource for this document. Return [default] in case when primary resource is null
   * or this document is [Errors].
   */
  fun getOrDefault(default: T): T {
    return when (this) {
      is Data -> data ?: default
      is Errors -> default
    }
  }
  
  /**
   * Get primary resource for this document. Return result of [block] in case when primary resource is null
   * or this document is [Errors].
   *
   * @param block block that will be called if primary resource is null or document is [Errors].
   * If document is [Errors] list of encapsulated errors will be passed to block.
   */
  fun getOrElse(block: (List<Error>?) -> T): T {
    return when (this) {
      is Data -> data ?: block(null)
      is Errors -> block(errors)
    }
  }
  
  /** Returns errors list if document is [Errors], otherwise returns null. */
  fun errorsOrNull(): List<Error>? {
    return when (this) {
      is Data -> null
      is Errors -> errors
    }
  }
  
  /** Returns errors list if document is [Errors], otherwise returns empty list. */
  fun errorsOrEmpty(): List<Error> {
    return if (this is Errors) errors else emptyList()
  }
  
  /** Throws [JsonApiErrorsException] if this document is [Errors]. */
  fun throwIfErrors() {
    if (this is Errors) {
      throw JsonApiErrorsException(this)
    }
  }
  
  /** Get [Links] for this document. */
  fun links(): Links? {
    return when (this) {
      is Data -> links
      is Errors -> links
    }
  }
  
  /** Get [Meta] for this document. */
  fun meta(): Meta? {
    return when (this) {
      is Data -> meta
      is Errors -> meta
    }
  }
  
  companion object {
    /** Creates [Data] document with [resource]. */
    @JvmStatic
    fun <T : Resource> data(resource: T?): Data<T> {
      return Data(resource)
    }
    
    /** Creates [Data] document with [resources]. */
    @JvmStatic
    fun <T : List<Resource>> data(resources: T?): Data<T> {
      return Data(resources)
    }
    
    /** Creates [Data] document with [meta]. */
    @JvmStatic
    fun meta(meta: Meta): Data<Nothing> {
      return Data(meta = meta)
    }
    
    /** Creates [Errors] document with list containing [error]. */
    @JvmStatic
    fun error(error: Error): Errors {
      return Errors(listOf(error))
    }
    
    /** Creates [Errors] document with list of [errors]. */
    @JvmStatic
    fun error(errors: List<Error>): Errors {
      return Errors(errors)
    }
  }
}