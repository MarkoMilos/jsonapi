package com.example.jsonapi

import com.example.jsonapi.resource.Article
import com.example.jsonapi.resource.Comment
import com.example.jsonapi.resource.Person
import com.jsonapi.Document
import com.jsonapi.JsonApi
import com.jsonapi.JsonApiFactory
import com.jsonapi.Link.LinkURI
import com.jsonapi.Links
import com.jsonapi.Meta
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

fun main() {
  val factory = JsonApiFactory.Builder()
    .addTypes(JsonApi.resources())
    .build()

  val moshi = Moshi.Builder()
    .add(factory)
    .addLast(KotlinJsonAdapterFactory())
    .build()

  val adapter: JsonAdapter<Document<Article>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Article::class.java)
  )

  val author1 = Person("people", "1", "Name1", "Surname1", "@twitter1")
  val author2 = Person("people", "2", "Name2", "Surname2", "@twitter2")
  val comment1 = Comment("comments", "1", "Comment1", author2)
  val comment2 = Comment("comments", "2", "Comment2", author1)
  val article = Article("articles", "1", "Title1", author1, listOf(comment1, comment2))

  val meta = Meta(
    "name" to "value",
    "number" to 15
  )

  val links = Links(
    "self" to LinkURI("http://example.com/articles"),
    "next" to LinkURI("http://example.com/articles?page[offset]=2")
  )

  val document = Document.with(article)
    .meta(meta)
    .links(links)
    .build()

  println(adapter.toJson(document))
}
