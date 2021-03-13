package com.jsonapi

import com.squareup.moshi.JsonClass

/** Class that does not extend from Resource */
@JsonClass(generateAdapter = true)
class NotAResource

/** Class that extends from Resource but it is not annotated with [Type] */
@JsonClass(generateAdapter = true)
class NotAnnotatedResource : Resource()

/** Class that extends from Resource but it is annotated with invalid type (blank) */
@JsonClass(generateAdapter = true)
@Type("")
class InvalidTypeResource : Resource()

/** Class that extends from Resource directly and it is annotated with valid type. */
@JsonClass(generateAdapter = true)
@Type("valid-resource-type")
open class ValidResource : Resource()

/** Class that extends from Resource indirectly. @Type is inherited. */
@JsonClass(generateAdapter = true)
class ValidResourceSubclass : ValidResource()

@JsonClass(generateAdapter = true)
@Type("articles")
open class Article(
  type: String?,
  id: String?,
  var title: String? = "",
  @Relationship("author") val author: Person? = null,
  @Relationship("comments") val comments: List<Comment>? = null,
  @Relationship("related") var relatedArticles: List<Article>? = null
) : Resource(type, id) {
  override fun toString(): String {
    return "\nType: $type\nID: $id\nTitle: $title"
  }
}

@JsonClass(generateAdapter = true)
class SpecialArticle(
  type: String?,
  id: String?,
  title: String? = "",
  val headline: String,
) : Article(type, id, title)

@JsonClass(generateAdapter = true)
@Type("people")
class Person(
  type: String?,
  id: String?,
  val firstName: String,
  val lastName: String,
  val twitter: String
) : Resource(type, id)

@JsonClass(generateAdapter = true)
@Type("comments")
class Comment(
  type: String?,
  id: String?,
  val body: String,
  @Relationship("author") val author: Person?
) : Resource(type, id)