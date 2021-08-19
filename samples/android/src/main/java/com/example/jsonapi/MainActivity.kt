package com.example.jsonapi

import android.app.Activity
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import com.example.jsonapi.resource.Article
import com.example.jsonapi.resource.Comment
import com.example.jsonapi.resource.Person
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jsonapi.JsonApi
import jsonapi.JsonApiFactory
import jsonapi.retrofit.DocumentConverterFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // !!! DON'T DO THIS IN ACTIVITY, THIS IS JUST FOR DEMO PURPOSE !!!

    val policy = ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)

    val factory = JsonApiFactory.Builder()
      .addTypes(JsonApi.resources())
      .build()

    val moshi = Moshi.Builder()
      .add(factory)
      .addLast(KotlinJsonAdapterFactory())
      .build()

    val server = MockWebServer()
    server.start()
    server.enqueue(MockResponse().setBody(json))

    val retrofit = Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addConverterFactory(DocumentConverterFactory.create())
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()

    val service = retrofit.create(Service::class.java)

    val author = Person("1", "Name")
    val comment = Comment("1", "Comment", author)
    val article = Article("1", "Title", author, listOf(comment))

    val createdResource = service.createArticle(article).execute().body()
    val requestBody = server.takeRequest().body.readByteString().utf8()

    Log.d("JSONAPI", "Request body sent:")
    Log.d("JSONAPI", requestBody)

    Log.d("JSONAPI", "Created resource (response body):")
    Log.d("JSONAPI", createdResource.toString())

    server.shutdown()
  }

  private val json = """
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
          "name": "Name"
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
}
