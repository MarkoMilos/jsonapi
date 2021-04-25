package com.jsonapi

import com.squareup.moshi.Moshi

object TestUtils {
  val factory = JsonApiFactory.Builder()
    .addType(Person::class.java)
    .addType(Article::class.java)
    .addType(Comment::class.java)
    .build()

  val moshi: Moshi = Moshi.Builder()
    .add(factory)
    .build()
}
