package com.jsonapi.factory

import com.jsonapi.adapter.VoidAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class VoidFactory : FactoryDelegate {
  
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: JsonAdapter.Factory
  ): JsonAdapter<*>? {
    return if (type == Nothing::class.java || type == Void::class.java) {
      VoidAdapter()
    } else {
      null
    }
  }
}