package com.example.jsonapi.resource

import com.jsonapi.Resource
import com.jsonapi.Type

@Type("people")
class Person(
  type: String?,
  id: String?,
  val firstName: String,
  val lastName: String,
  val twitter: String
) : Resource(type, id) {
  
  override fun toString(): String {
    return "$firstName $lastName (on Twitter: $twitter)"
  }
}