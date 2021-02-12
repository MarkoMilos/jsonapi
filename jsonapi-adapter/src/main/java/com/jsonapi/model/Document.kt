package com.jsonapi.model

import com.squareup.moshi.JsonClass

sealed class Document<T> {
  
  @JsonClass(generateAdapter = true)
  data class Data<T>(
    val data: T?,
    val included: List<Resource>? = null,
    val links: Links? = null,
    val meta: Meta? = null
  ) : Document<T>()
  // TODO should we add Document.Data init {} block to check for data type and throw for invalid documents?
  
  @JsonClass(generateAdapter = true)
  data class Errors(val errors: List<Error>) : Document<Nothing>()
  
  // TODO add Document API (like Result)
  
}