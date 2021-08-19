package com.example.jsonapi.resource

import jsonapi.Id
import jsonapi.Resource
import jsonapi.ToOne

@Resource("comments")
data class Comment(
  @Id val id: String?,
  val body: String,
  @ToOne("author") val author: Person?
)
