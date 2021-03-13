package com.jsonapi.internal.factory

import com.jsonapi.Resource
import com.jsonapi.internal.adapter.ResourceAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

internal class ResourceFactory(
  private val types: List<Type>,
  private val typeNames: List<String>,
  private val allowUnregisteredTypes: Boolean
) : FactoryDelegate {
  
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: Factory
  ): JsonAdapter<*>? {
    // this factory applies only for Resource type
    if (Types.getRawType(type) != Resource::class.java) return null
    
    // create adapter for each registered type
    val typeAdapters = mutableListOf<JsonAdapter<Any>>()
    types.forEach { typeAdapters.add(moshi.adapter(it)) }
    
    // if unregistered types are allowed request adapter for Resource type but skip past parent factory to
    // avoid infinite loop. This way adapter for type can be safely use within ResourceAdapter as default
    val defaultResourceAdapter: JsonAdapter<Any>? =
      if (allowUnregisteredTypes) moshi.nextAdapter(parent, type, annotations) else null
    
    return ResourceAdapter(types, typeNames, typeAdapters, defaultResourceAdapter)
  }
}