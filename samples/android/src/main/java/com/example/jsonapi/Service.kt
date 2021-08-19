package com.example.jsonapi

import com.example.jsonapi.resource.Article
import jsonapi.retrofit.Document
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Service {
  @Document
  @POST("/")
  fun createArticle(@Document @Body article: Article): Call<Article>
}
