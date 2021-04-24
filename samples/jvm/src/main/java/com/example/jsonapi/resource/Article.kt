package com.example.jsonapi.resource

import com.jsonapi.Relationship
import com.jsonapi.Resource
import com.jsonapi.Type

@Type("articles")
class Article(
  type: String?,
  id: String?,
  var title: String? = "",
  @Relationship("author") val author: Person? = null,
  @Relationship("comments") val comments: List<Comment>? = null,
) : Resource(type, id) {
  
  override fun toString(): String {
    return "" +
      "Title: $title\n" +
      "Author: $author\n" +
      "Comments:\n${comments?.joinToString("\n")}"
  }
}