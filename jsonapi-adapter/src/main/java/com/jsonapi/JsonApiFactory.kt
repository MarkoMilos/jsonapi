package com.jsonapi

import com.jsonapi.internal.adapter.DocumentAdapter
import com.jsonapi.internal.adapter.ErrorAdapter
import com.jsonapi.internal.adapter.JsonApiObjectAdapter
import com.jsonapi.internal.adapter.LinkAdapter
import com.jsonapi.internal.adapter.LinkObjectAdapter
import com.jsonapi.internal.adapter.LinksAdapter
import com.jsonapi.internal.adapter.MetaAdapter
import com.jsonapi.internal.adapter.RelationshipAdapter
import com.jsonapi.internal.adapter.RelationshipToManyAdapter
import com.jsonapi.internal.adapter.RelationshipToOneAdapter
import com.jsonapi.internal.adapter.RelationshipsAdapter
import com.jsonapi.internal.adapter.ResourceIdentifierAdapter
import com.jsonapi.internal.adapter.ResourceObjectAdapter
import com.jsonapi.internal.adapter.ResourcePolymorphicAdapter
import com.jsonapi.internal.adapter.ResourceTypeAdapter
import com.jsonapi.internal.adapter.SourceAdapter
import com.jsonapi.internal.adapter.TransientAdapter
import com.jsonapi.internal.adapter.VoidAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.Resource
import java.lang.reflect.Type

/**
 * Factory that provides adapters for converting and binding JSON:API entities.
 *
 * Use [JsonApiFactory.Builder] for creating and instance of [JsonApiFactory].
 *
 * [JsonApiFactory] extends from Moshi [JsonAdapter.Factory] so it can be used via `.add(factory)`.
 *
 * Example:
 * ```
 *  val factory = JsonApiFactory.Builder()
 *    .addType(Person::class.java)
 *    .addType(Article::class.java)
 *    .addType(Comment::class.java)
 *    .build()
 *
 *  val moshi: Moshi = Moshi.Builder()
 *   .add(factory)
 *   .build()
 * ```
 *
 * @see [JsonApiFactory.Builder]
 */
class JsonApiFactory private constructor(
  types: List<Type>,
  typeNames: List<String>,
  strictTypes: Boolean
) : JsonAdapter.Factory {

  private val factoryDelegates = listOf(
    DocumentAdapter.FACTORY,
    ResourceIdentifierAdapter.FACTORY,
    ResourceObjectAdapter.FACTORY,
    ResourceTypeAdapter.factory(strictTypes),
    ResourcePolymorphicAdapter.factory(types, typeNames),
    RelationshipsAdapter.FACTORY,
    RelationshipAdapter.FACTORY,
    RelationshipToOneAdapter.FACTORY,
    RelationshipToManyAdapter.FACTORY,
    LinksAdapter.FACTORY,
    LinkAdapter.FACTORY,
    LinkObjectAdapter.FACTORY,
    MetaAdapter.FACTORY,
    JsonApiObjectAdapter.FACTORY,
    ErrorAdapter.FACTORY,
    SourceAdapter.FACTORY,
    TransientAdapter.FACTORY,
    VoidAdapter.FACTORY
  )

  override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
    // Iterate over factory delegates until first one that resolves for [type, annotations] is found
    // In that case delegate factory will return non-null adapter
    // If none of the delegates return non-null adapter this factory does not apply (returns null)
    return factoryDelegates
      .asSequence()
      .map { it.create(type, annotations, moshi, this) }
      .firstOrNull { it != null }
  }

  class Builder {
    private val types = mutableListOf<Type>()
    private val typeNames = mutableListOf<String>()
    private var strictTypes = false

    /** Register JSON:API resource [type]. */
    fun addType(type: Class<*>) = apply {
      types.add(type)
    }

    /** Register JSON:API resource [types]. */
    fun addTypes(types: List<Class<*>>) = apply {
      types.forEach { addType(it) }
    }

    /** Register JSON:API resource [types]. */
    fun addTypes(vararg types: Class<*>) = apply {
      types.forEach { addType(it) }
    }

    /**
     * Enables strict type checking meaning that each serialization/deserialization of resource will assert if
     * `type` member matches with value provided with [TypeAnnotation] for the target class. On mismatch
     * [JsonApiException] will be thrown.
     *
     * E.g. given that class Article that is annotated with `@Type("articles")` is being deserialized, when provided
     * json for deserialization is "{"type":"dinosaur"....}", then [JsonApiException] will be thrown stating that
     * "articles" was expected but "dinosaur" was found.
     *
     * By default strict type checking is disabled.
     */
    fun strictTypes(enabled: Boolean) = apply {
      strictTypes = enabled
    }

    /**
     * Creates instance of [JsonApiFactory].
     *
     * Throws [JsonApiException] if there are multiple resources registered for the same name or if any of the
     * registered resources has invalid type value.
     *
     * @see JsonApiFactory
     */
    fun build(): JsonAdapter.Factory {
      types.forEach {
        // Get and assert that Type annotation is present on target resource type
        val resourceAnnotation = Types.getRawType(it).getAnnotation(Resource::class.java)
          ?: throw JsonApiException("Provided type '$it' is not annotated with @Type.")

        // Assert that valid type name is provided with @Type annotation
        if (resourceAnnotation.type.isBlank()) {
          throw JsonApiException(
            "For type '" +
              it +
              "' value provided with @Type annotation was blank.\n" +
              "The values of type members MUST adhere to the same constraints as member names per specification."
          )
        }

        // Get assigned type name from annotation and add it to names list only
        // if not already in use by another type, throw if name is in use
        val typeName = resourceAnnotation.type
        if (!typeNames.contains(typeName)) {
          typeNames.add(typeName)
        } else {
          throw JsonApiException(
            "Name '$typeName' " +
              "for type '$it' " +
              "is already registered for type '${types[typeNames.indexOf(typeName)]}'."
          )
        }
      }

      return JsonApiFactory(types, typeNames, strictTypes)
    }
  }
}
