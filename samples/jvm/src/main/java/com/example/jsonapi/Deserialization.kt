//package com.example.jsonapi
//
//import com.example.jsonapi.resource.Article
//import jsonapi.Document
//import com.jsonapi.JsonApi
//import jsonapi.JsonApiFactory
//import com.squareup.moshi.JsonAdapter
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.Types
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//
//fun main() {
//  val factory = JsonApiFactory.Builder()
//    .addTypes(JsonApi.resources())
//    .build()
//
//  val moshi = Moshi.Builder()
//    .add(factory)
//    .addLast(KotlinJsonAdapterFactory())
//    .build()
//
//  val adapter: JsonAdapter<Document<Article>> = moshi.adapter(
//    Types.newParameterizedType(Document::class.java, Article::class.java)
//  )
//
//  val document = adapter.fromJson(json)
//
//  println("DATA:\n${document?.data}\n")
////  println("INCLUDED:\n${document?.included?.joinToString("\n")}}\n")
//  println("ERRORS: ${document?.errors}\n")
//  println("LINKS: ${document?.links?.members}\n")
//  println("META: ${document?.meta?.members}\n")
//  println("JSONAPI: ${document?.jsonapi}\n")
//}
//
//private val json = """
//  {
//    "jsonapi": {
//      "version": "1.1",
//      "ext": [
//        "https://jsonapi.org/ext/atomic"
//      ],
//      "profile": [
//        "http://example.com/profiles/flexible-pagination",
//        "http://example.com/profiles/resource-versioning"
//      ]
//    },
//    "links": {
//      "self": "http://example.com/articles",
//      "next": "http://example.com/articles?page[offset]=2",
//      "last": "http://example.com/articles?page[offset]=10"
//    },
//    "meta": {
//      "name": "value"
//    },
//    "data": {
//      "type": "articles",
//      "id": "1",
//      "attributes": {
//        "title": "Title1",
//        "headline": "Headline1"
//      },
//      "relationships": {
//        "author": {
//          "links": {
//            "self": "http://example.com/articles/1/relationships/author",
//            "related": "http://example.com/articles/1/author"
//          },
//          "data": {
//            "type": "people",
//            "id": "1"
//          }
//        },
//        "comments": {
//          "links": {
//            "self": "http://example.com/articles/1/relationships/comments",
//            "related": "http://example.com/articles/1/comments"
//          },
//          "data": [
//            {
//              "type": "comments",
//              "id": "1"
//            },
//            {
//              "type": "comments",
//              "id": "2"
//            }
//          ]
//        }
//      },
//      "links": {
//        "self": "http://example.com/articles/1"
//      }
//    },
//    "included": [
//      {
//        "type": "people",
//        "id": "1",
//        "attributes": {
//          "firstName": "Name1",
//          "lastName": "Surname1",
//          "twitter": "@twitter1"
//        },
//        "links": {
//          "self": "http://example.com/people/1"
//        }
//      },
//      {
//        "type": "comments",
//        "id": "1",
//        "attributes": {
//          "body": "Comment1"
//        },
//        "relationships": {
//          "author": {
//            "data": {
//              "type": "people",
//              "id": "2"
//            }
//          }
//        },
//        "links": {
//          "self": "http://example.com/comments/1"
//        }
//      },
//      {
//        "type": "comments",
//        "id": "2",
//        "attributes": {
//          "body": "Comment2"
//        },
//        "relationships": {
//          "author": {
//            "data": {
//              "type": "people",
//              "id": "1"
//            }
//          }
//        },
//        "links": {
//          "self": "http://example.com/comments/2"
//        }
//      }
//    ]
//  }
//""".trimIndent()
// TODO FIX THIS
