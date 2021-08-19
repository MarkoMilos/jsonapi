package com.example.jsonapi.resource

import com.squareup.moshi.JsonClass
import jsonapi.Id
import jsonapi.Resource

@JsonClass(generateAdapter = true)
@Resource("people")
data class Person(
  @Id val id: String?,
  val name: String
)
