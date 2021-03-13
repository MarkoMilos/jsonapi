package com.jsonapi.internal.factory

import com.jsonapi.Document
import com.jsonapi.internal.adapter.DataAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class DataFactory : FactoryDelegate {
  
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: JsonAdapter.Factory
  ): JsonAdapter<*>? {
    // Document.Data<T> is parametrized type
    if (type !is ParameterizedType) return null
    // if it is not a Document.Data this factory doesn't apply
    if (type.rawType !== Document.Data::class.java) return null
    // create delegate adapter for this type but skip past this factory to avoid infinite loop
    val delegateAdapter = moshi.nextAdapter<Document.Data<*>>(parent, type, annotations)
    return DataAdapter(delegateAdapter)
  }
}