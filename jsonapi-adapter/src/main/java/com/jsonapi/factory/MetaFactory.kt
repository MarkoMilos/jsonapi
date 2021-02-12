package com.jsonapi.factory

import com.jsonapi.adapter.MetaAdapter
import com.jsonapi.model.Meta
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class MetaFactory : FactoryDelegate {
  
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: Factory
  ): JsonAdapter<*>? {
    // type needs to be Meta otherwise this factory does not apply (returns null)
    return if (Types.getRawType(type) == Meta::class.java) MetaAdapter(moshi) else null
  }
}