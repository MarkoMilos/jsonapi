package com.example.jsonapi.resource

import jsonapi.Id
import jsonapi.Resource

@Resource("people")
data class Person(
  @Id val id: String?,
  val firstName: String,
  val lastName: String,
  val twitter: String
)
