package com.jsonapi.internal.factory

import com.jsonapi.Document
import com.jsonapi.internal.adapter.DocumentAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class DocumentFactory : FactoryDelegate {

  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: Factory
  ): JsonAdapter<*>? {
    // Document<T> is parametrized type
    if (type !is ParameterizedType) return null
    // If it is not a Document this factory doesn't apply
    if (type.rawType !== Document::class.java) return null
    // Create delegate adapter for this type of document but skip past this factory to avoid infinite loop
    val delegateAdapter = moshi.nextAdapter<Document<*>>(parent, type, annotations)
    return DocumentAdapter(delegateAdapter)
  }
}
