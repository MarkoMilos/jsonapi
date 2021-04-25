package com.jsonapi.internal.factory

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal interface FactoryDelegate {

  fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi, parent: Factory): JsonAdapter<*>?
}
