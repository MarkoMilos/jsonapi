package com.jsonapi

import com.squareup.moshi.JsonClass

/**
 * Information about JSON:API implementation.
 *
 * The jsonapi object MAY contain any of the following members: `version`, `ext`, `profile`, `meta`.
 *
 * If the version member is not present, clients should assume the server implements at least version
 * 1.0 of the specification.
 *
 * @param version value is a string indicating the highest JSON:API version supported.
 * @param ext an array of URIs for all applied extensions.
 * @param profile an array of URIs for all applied profiles.
 * @param meta contains non-standard meta-information.
 */
@JsonClass(generateAdapter = true)
data class JsonApi(
  val version: String? = null,
  val ext: List<String>? = null,
  val profile: List<String>? = null,
  val meta: Meta? = null
)