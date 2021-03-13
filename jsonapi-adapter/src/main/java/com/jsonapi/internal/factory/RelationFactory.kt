package com.jsonapi.internal.factory

import com.jsonapi.Relation
import com.jsonapi.internal.adapter.RelationAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

internal class RelationFactory : FactoryDelegate {
  
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: Factory
  ): JsonAdapter<*>? {
    // type needs to be Relation otherwise this factory does not apply (returns null)
    return if (Types.getRawType(type) == Relation::class.java) RelationAdapter(moshi) else null
  }
}