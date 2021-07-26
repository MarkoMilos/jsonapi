package com.example.jsonapi.resource

@Resource("articles")
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
