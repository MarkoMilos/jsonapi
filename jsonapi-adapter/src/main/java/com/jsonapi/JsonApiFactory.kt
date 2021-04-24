package com.jsonapi

import com.jsonapi.internal.factory.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import com.jsonapi.Type as TypeAnnotation

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
  allowUnregisteredTypes: Boolean,
  strictTypes: Boolean
) : JsonAdapter.Factory {
  
  private val factoryDelegates = listOf(
    DocumentFactory(),
    ResourceFactory(types, typeNames, allowUnregisteredTypes),
    ResourceSubclassFactory(typeNames, allowUnregisteredTypes, strictTypes),
    RelationFactory(),
    LinksFactory(),
    MetaFactory(),
    VoidFactory()
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
    private var allowUnregisteredTypes = true
    private var strictTypes = false
    
    /** Register JSON:API resource [type].  */
    fun addType(type: Class<out Resource>) = apply {
      types.add(type)
    }
    
    /** Register JSON:API resource [types]. */
    fun addTypes(types: List<Class<out Resource>>) = apply {
      types.forEach { addType(it) }
    }
    
    /** Register JSON:API resource [types]. */
    fun addTypes(vararg types: Class<out Resource>) = apply {
      types.forEach { addType(it) }
    }
    
    /**
     * Modify behaviour when unregistered type is found during serialization/deserialization of [Resource]
     * or it's subclasses.
     *
     * Serializing and deserializing [Resource] is polymorphic meaning that registered subclass type will be
     * serialized/deserialized based on the value of `type` member.
     * E.g. when deserializing [Resource], if `type` found in json is equal to `article` and there is an Article class
     * (which inherits from Resource) registered for that value, then Article will be deserialized instead of a base
     * [Resource] type.
     *
     * When this is set to false unregistered types won't be allowed meaning that [JsonApiException] will be thrown
     * if `type` member is not within list of registered types.
     *
     * When this is set to true unregistered types are allowed meaning that exception won't be thrown, instead
     * resource will be serialized/deserialized as base [Resource] type.
     *
     * By default unregistered types are allowed.
     */
    fun allowUnregisteredTypes(allow: Boolean) = apply {
      allowUnregisteredTypes = allow
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
        val typeAnnotation = Types.getRawType(it).getAnnotation(TypeAnnotation::class.java)
          ?: throw JsonApiException("Provided type '$it' is not annotated with @Type.")
        
        // Assert that valid type name is provided with @Type annotation
        if (typeAnnotation.name.isBlank()) {
          throw JsonApiException(
            "For type '"
              + it
              + "' value provided with @Type annotation was blank.\n"
              + "The values of type members MUST adhere to the same constraints as member names per specification."
          )
        }
        
        // Get assigned type name from annotation and add it to names list only
        // if not already in use by another type, throw if name is in use
        val typeName = typeAnnotation.name
        if (!typeNames.contains(typeName)) {
          typeNames.add(typeName)
        } else {
          throw JsonApiException(
            "Name '$typeName' "
              + "for type '$it' "
              + "is already registered for type '${types[typeNames.indexOf(typeName)]}'."
          )
        }
      }
      
      return JsonApiFactory(types, typeNames, allowUnregisteredTypes, strictTypes)
    }
  }
}