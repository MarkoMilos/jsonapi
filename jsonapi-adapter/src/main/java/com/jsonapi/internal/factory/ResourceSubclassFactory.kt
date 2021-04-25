package com.jsonapi.internal.factory

import com.jsonapi.JsonApiException
import com.jsonapi.Resource
import com.jsonapi.internal.adapter.ResourceSubclassAdapter
import com.jsonapi.internal.isResource
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import com.jsonapi.Type as TypeAnnotation

internal class ResourceSubclassFactory(
  private val typeNames: List<String>,
  private val allowUnregisteredTypes: Boolean,
  private val strictTypes: Boolean
) : FactoryDelegate {

  override fun create(
    type: Type,
    annotations: MutableSet<out Annotation>,
    moshi: Moshi,
    parent: Factory
  ): JsonAdapter<*>? {
    // type needs to extend from Resource (directly or indirectly) otherwise this factory doesn't apply
    if (!type.isResource()) return null

    // assert that resource is annotated with @Type annotation
    val typeAnnotation = Types.getRawType(type).getAnnotation(TypeAnnotation::class.java)
      ?: throw JsonApiException("Type '$type' is not annotated with @Type.")

    // assert that valid type name is provided with @Type annotation
    if (typeAnnotation.name.isBlank()) {
      throw JsonApiException(
        "For type '" +
          type +
          "' value provided with @Type annotation was blank.\n" +
          "The values of type members MUST adhere to the same constraints as member names per specification."
      )
    }

    // request adapter for this type but skip past this factory to avoid infinite loop
    // this way adapter for target type can be safely used within ResourceSubclassAdapter
    val delegateAdapter: JsonAdapter<Resource> = moshi.nextAdapter(parent, type, annotations)

    return ResourceSubclassAdapter(
      moshi,
      delegateAdapter,
      type,
      typeAnnotation.name,
      strictTypes,
      typeNames,
      allowUnregisteredTypes
    )
  }
}
