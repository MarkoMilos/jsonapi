package com.jsonapi

import com.jsonapi.factory.*
import com.jsonapi.model.Resource
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import com.jsonapi.annotation.Type as TypeAnnotation

class JsonApiFactory private constructor(
  types: List<Type>,
  typeNames: List<String>,
  allowUnregisteredTypes: Boolean,
  strictTypes: Boolean
) : JsonAdapter.Factory {
  
  private val factoryDelegates = listOf(
    DocumentFactory(),
    DataFactory(),
    ResourceFactory(types, typeNames, allowUnregisteredTypes),
    ResourceSubclassFactory(typeNames, allowUnregisteredTypes, strictTypes),
    RelationFactory(),
    LinksFactory(),
    MetaFactory(),
    VoidFactory()
  )
  
  override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
    // iterate over factory delegates until first one that resolves for [type, annotations] is found
    // in that case delegate factory will return non-null adapter
    // if none of the delegates return non-null adapter this factory does not apply (returns null)
    return factoryDelegates
      .asSequence()
      .map { it.create(type, annotations, moshi, this) }
      .firstOrNull { it != null }
  }
  
  class Builder {
    private val types = mutableListOf<Type>()
    private val typeNames = mutableListOf<String>()
    private var allowUnregisteredTypes = false
    private var strictTypes = true
    
    fun addType(type: Class<out Resource>) = apply {
      types.add(type)
    }
    
    fun addTypes(types: List<Class<out Resource>>) = apply {
      types.forEach { addType(it) }
    }
    
    fun addTypes(vararg types: Class<out Resource>) = apply {
      types.forEach { addType(it) }
    }
    
    fun allowUnregisteredTypes(allow: Boolean) = apply {
      allowUnregisteredTypes = allow
    }
    
    fun strictTypes(enabled: Boolean) = apply {
      strictTypes = enabled
    }
    
    fun build(): JsonAdapter.Factory {
      types.forEach {
        // get and assert that Type annotation is present on target resource type
        val typeAnnotation = Types.getRawType(it).getAnnotation(TypeAnnotation::class.java)
          ?: throw JsonApiException("Provided type '$it' is not annotated with @Type.")
        
        // assert that valid type name is provided with @Type annotation
        if (typeAnnotation.name.isBlank()) {
          throw JsonApiException(
            "For type '"
              + it
              + "' value provided with @Type annotation was blank.\n"
              + "The values of type members MUST adhere to the same constraints as member names per specification."
          )
        }
        
        // get assigned type name from annotation and add it to names list only
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