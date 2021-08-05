package com.example.jsonapi

import com.example.jsonapi.resource.Article
import com.example.jsonapi.resource.Comment
import com.example.jsonapi.resource.Person
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jsonapi.Document
import jsonapi.JsonApi
import jsonapi.JsonApiFactory
import jsonapi.Link.URI
import jsonapi.Links
import jsonapi.Meta

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

  val author1 = Person("1", "Name1", "Surname1", "@twitter1")
  val author2 = Person("2", "Name2", "Surname2", "@twitter2")
  val comment1 = Comment("1", "Comment1", author2)
  val comment2 = Comment("2", "Comment2", author1)
  val article = Article("1", "Title1", author1, listOf(comment1, comment2))

  val meta = Meta(
    "name" to "value",
    "number" to 15
  )

  val links = Links(
    "self" to URI("http://example.com/articles"),
    "next" to URI("http://example.com/articles?page[offset]=2")
  )

  val document = Document.with(article)
    .meta(meta)
    .links(links)
    .build()

  println(adapter.toJson(document))
}
