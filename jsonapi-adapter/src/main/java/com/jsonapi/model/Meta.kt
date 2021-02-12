package com.jsonapi.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Meta is used to include non-standard meta-information.
 * Any members MAY be specified within meta objects.
 */
@JsonClass(generateAdapter = true)
class Meta(
  val members: Map<String, Any?>
) {
  
  /** Returns true if this meta has members, false otherwise */
  fun isEmpty() = members.isEmpty()
  
  /** Returns true if meta has member with [name], false otherwise */
  fun has(name: String) = members.containsKey(name)
  
  /** Returns true if meta has non-null member with [name], false otherwise */
  fun hasNonNull(name: String) = members[name] != null
  
  /** Returns value of member with [name], or null if such a name is not present */
  fun get(name: String): Any? {
    return members[name]
  }
  
  /** Returns value of member with [name], or [default] if such a name is not present */
  fun getOrDefault(name: String, default: Any): Any {
    return members[name] ?: default
  }
  
  /**
   * Returns value of member with [name] as the defined type [T].
   * Returns null if such a name is not present or it is not instance of defined type [T].
   */
  inline fun <reified T> member(name: String): T? {
    val value = members[name]
    return if (value is T) value else null
  }
  
  /**
   * Returns value of member with [name] as the defined type [T].
   * Returns [default] if such a name is not present or it is not instance of defined type [T].
   */
  inline fun <reified T> member(name: String, default: T): T {
    val value = members[name]
    return if (value is T) value else default
  }
  
  /** Returns [Number] value of member with [name], or null if member is not a [Number] or it is not present */
  fun number(name: String): Number? = member(name)
  
  /** Returns [Number] value of member with [name], or [default] if member is not a [Number] or it is not present */
  fun number(name: String, default: Number): Number = member(name, default)
  
  /** Returns [String] value of member with [name], or null if member is not a [String] or it is not present */
  fun string(name: String): String? = member(name)
  
  /** Returns [String] value of member with [name], or [default] if member is not a [String] or it is not present */
  fun string(name: String, default: String): String = member(name, default)
  
  /** Returns [Boolean] value of member with [name], or null if member is not a [Boolean] or it is not present */
  fun boolean(name: String): Boolean? = member(name)
  
  /** Returns [Boolean] value of member with [name], or [default] if member is not a [Boolean] or it is not present */
  fun boolean(name: String, default: Boolean): Boolean = member(name, default)
  
  /** Returns [List] value of member with [name], or null if member is not a [List] or it is not present */
  fun <T> list(name: String): List<T>? = member(name)
  
  /** Returns [List] value of member with [name], or [default] if member is not a [List] or it is not present */
  fun <T> list(name: String, default: List<T>): List<T> = member(name, default)
  
  /**
   * Maps [Meta] to provided [targetType] ([T]).
   *
   * Mapping is performed by serializing [Meta] members to json and deserializing
   * it to instance of defined [targetType] ([T]).
   *
   * Use [configuredMoshi] if you want to use moshi instance that is configured with adapters
   * required for deserialization of defined type [T]. If [configuredMoshi] is not provided
   * default instance `Moshi.Builder().build()` is used for deserialization.
   */
  @JvmOverloads
  fun <T> map(targetType: Class<T>, configuredMoshi: Moshi? = null): T? {
    val moshi = configuredMoshi ?: Moshi.Builder().build()
    val mapAdapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(
      Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )
    return moshi.adapter(targetType).fromJson(mapAdapter.toJson(members))
  }
  
  /**
   * Maps [Meta] to defined type [T].
   *
   * Mapping is performed by serializing [Meta] members to json and deserializing
   * it to instance of defined type [T].
   *
   * Use [configuredMoshi] if you want to use moshi instance that is configured with adapters
   * required for deserialization of defined type [T]. If [configuredMoshi] is not provided
   * default instance `Moshi.Builder().build()` is used for deserialization.
   */
  inline fun <reified T> map(configuredMoshi: Moshi? = null): T? {
    return this.map(T::class.java, configuredMoshi)
  }
  
  companion object {
    /**
     * Creates [Meta] instance from provided [value] of type [T].
     *
     * Meta is created by serializing provided [value] to json and deserializing it to [Meta].
     *
     * Use [configuredMoshi] if you want to use moshi instance that is configured with adapters
     * required for serialization of defined type [T]. If [configuredMoshi] is not provided
     * default instance `Moshi.Builder().build()` is used for serialization.
     */
    @JvmStatic
    @JvmOverloads
    fun <T : Any> from(value: T, configuredMoshi: Moshi? = null): Meta {
      val moshi = configuredMoshi ?: Moshi.Builder().build()
      val mapAdapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
      )
      val valueTypeAdapter = moshi.adapter<T>(Types.getRawType(value::class.java))
      val serializedValue = valueTypeAdapter.toJson(value)
      val metaMap = mapAdapter.fromJson(serializedValue) ?: emptyMap()
      return Meta(metaMap)
    }
  }
}