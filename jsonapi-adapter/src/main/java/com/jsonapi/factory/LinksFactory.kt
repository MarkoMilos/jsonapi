package com.jsonapi.factory

import com.jsonapi.adapter.LinkAdapter
import com.jsonapi.adapter.LinkObjectAdapter
import com.jsonapi.adapter.LinksAdapter
import com.jsonapi.model.Link
import com.jsonapi.model.Links
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class LinksFactory : FactoryDelegate {
  
  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: JsonAdapter.Factory
  ): JsonAdapter<*>? {
    return when (Types.getRawType(type)) {
      Links::class.java -> LinksAdapter(moshi)
      Link::class.java -> LinkAdapter(moshi)
      Link.LinkObject::class.java -> LinkObjectAdapter(moshi)
      // if type is not exactly one of the above this factory does not apply (returns null)
      else -> return null
    }
  }
}