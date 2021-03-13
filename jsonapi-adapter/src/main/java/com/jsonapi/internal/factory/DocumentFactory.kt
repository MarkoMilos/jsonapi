package com.jsonapi.internal.factory

import com.jsonapi.Document
import com.jsonapi.internal.adapter.DocumentAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
    // Data type argument (T) defined for this Document<T>
    val dataType = type.actualTypeArguments.first()
    // Delegate data adapter used when Document is Data
    val dataAdapter: JsonAdapter<Document.Data<*>> = moshi.adapter(
      Types.newParameterizedTypeWithOwner(Document::class.java, Document.Data::class.java, dataType)
    )
    // Delegate errors adapter used when Document is Errors
    val errorsAdapter: JsonAdapter<Document.Errors> = moshi.adapter(Document.Errors::class.java)
    return DocumentAdapter(dataAdapter, errorsAdapter)
  }
}