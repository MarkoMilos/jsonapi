package com.example.jsonapi

import com.example.jsonapi.resource.Article
import com.example.jsonapi.resource.Comment
import com.example.jsonapi.resource.Person
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jsonapi.JsonApi
import jsonapi.JsonApiFactory
import jsonapi.retrofit.Document
import jsonapi.retrofit.DocumentConverterFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private interface Service {
  @Document
  @POST("/")
  fun createArticle(@Document @Body article: Article): Call<Article>
}

fun main() {
  val factory = JsonApiFactory.Builder()
    .addTypes(JsonApi.resources())
    .build()

  val moshi = Moshi.Builder()
    .add(factory)
    .addLast(KotlinJsonAdapterFactory())
    .build()

  val server = MockWebServer()
  server.start()
  server.enqueue(MockResponse().setBody(JSON))

  val retrofit = Retrofit.Builder()
    .baseUrl(server.url("/"))
    .addConverterFactory(DocumentConverterFactory.create())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

  val service = retrofit.create(Service::class.java)

  val author = Person("1", "Name", "Surname", "@twitter")
  val comment = Comment("1", "Comment", author)
  val article = Article("1", "Title", author, listOf(comment))

  val createdResource = service.createArticle(article).execute().body()
  val requestBody = server.takeRequest().body.readByteString().utf8()

  println("Request body sent:")
  println(requestBody)

  println("Created resource (response body):")
  println(createdResource)

  server.shutdown()
}

private val JSON = """
  {
    "data": {
      "type": "articles",
      "id": "1",
      "attributes": {
        "title": "Title"
      },
      "relationships": {
        "author": {
          "data": {
            "type": "people",
            "id": "1"
          }
        },
        "comments": {
          "data": [
            {
              "type": "comments",
              "id": "1"
            }
          ]
        }
      }
    },
    "included": [
      {
        "type": "people",
        "id": "1",
        "attributes": {
          "firstName": "Name",
          "lastName": "Surname",
          "twitter": "@twitter"
        }
      },
      {
        "type": "comments",
        "id": "1",
        "attributes": {
          "body": "Comment"
        },
        "relationships": {
          "author": {
            "data": {
              "type": "people",
              "id": "1"
            }
          }
        }
      }
    ]
  }
""".trimIndent()
