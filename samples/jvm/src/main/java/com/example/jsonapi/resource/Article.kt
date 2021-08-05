package com.example.jsonapi.resource

import jsonapi.Id
import jsonapi.Resource
import jsonapi.ToMany
import jsonapi.ToOne

@Resource("articles")
data class Article(
  @Id val id: String?,
  var title: String? = "",
  @ToOne("author") val author: Person? = null,
  @ToMany("comments") val comments: List<Comment>? = null,
)
