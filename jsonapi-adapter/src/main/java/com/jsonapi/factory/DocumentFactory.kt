package com.jsonapi.factory

import com.jsonapi.JsonApiException
import com.jsonapi.adapter.DocumentAdapter
import com.jsonapi.isCollection
import com.jsonapi.isResource
import com.jsonapi.model.Document
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class DocumentFactory : FactoryDelegate {
  
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: Factory
  ): JsonAdapter<*>? {
    // Document<T> is parametrized type
    if (type !is ParameterizedType) return null
    // if it is not a Document this factory doesn't apply
    if (type.rawType !== Document::class.java) return null
    
    // data type argument (T) defined for this Document<T>
    val dataType = type.actualTypeArguments.first()
    // in case of collection target resource type is collection type argument (resource collection)
    // otherwise it is equal to document data type argument (single resource)
    val targetType = if (dataType.isCollection()) {
      (dataType as ParameterizedType).actualTypeArguments.first()
    } else {
      dataType
    }
    
    // TODO should we allow Document<ResourceIdentifier> here ?
    /*
Primary data MUST be either:
- a single resource object, a single resource identifier object, or null, for requests that target single resources
- an array of resource objects, an array of resource identifier objects, or an empty array ([]), for requests that target resource collections

Because of this I think that we should figure out some sort of inheritance so that Resource actually is ResourceIdentifier
 */
    
    
    // assert that target type inherits from resource (directly or indirectly)
    // FIXME: I believe that we cannot serialize errors with this since it is Document<Nothing>
    //  - This will prevent serialization for Data.Errors
    //  - Nothing and Unit??? should be accepted as well
    if (!targetType.isResource()) {
      throw JsonApiException("Document<T> type should be [T : Resource] or [Collection<T : Resource>] but was: $type")
    }
    
    // create delegate adapters for documents of this data type
    val dataAdapter: JsonAdapter<Document.Data<*>> = moshi.adapter(
      Types.newParameterizedTypeWithOwner(Document::class.java, Document.Data::class.java, dataType)
    )
    val errorsAdapter: JsonAdapter<Document.Errors> = moshi.adapter(Document.Errors::class.java)
    return DocumentAdapter(dataAdapter, errorsAdapter)
  }
}