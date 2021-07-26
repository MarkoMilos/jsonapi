package com.example.jsonapi.resource

@Resource("comments")
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
