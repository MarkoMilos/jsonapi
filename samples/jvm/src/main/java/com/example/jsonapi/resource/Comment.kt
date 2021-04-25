package com.example.jsonapi.resource

import com.jsonapi.Relationship
import com.jsonapi.Resource
import com.jsonapi.Type

@Type("comments")
class Comment(
  type: String?,
  id: String?,
  val body: String,
  @Relationship("author") val author: Person?
) : Resource(type, id) {

  override fun toString(): String {
    return "$body from $author"
  }
}
