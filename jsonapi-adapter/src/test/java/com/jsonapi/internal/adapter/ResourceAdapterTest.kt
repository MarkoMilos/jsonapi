package com.jsonapi.internal.adapter

import com.jsonapi.*
import com.jsonapi.JsonFile.RESOURCE_ARTICLE
import com.jsonapi.JsonFile.RESOURCE_UNKNOWN_TYPE
import com.jsonapi.TestUtils.moshi
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResourceAdapterTest {
  
  private val adapter = moshi.adapter(Resource::class.java)
  
  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }
  
  @Test
  fun `deserialize registered resource to registered type`() {
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE))
    assertThat(deserialized).isNotNull
    assertThat(deserialized).isInstanceOf(Article::class.java)
  }
  
  @Test
  fun `deserialize unregistered resource to base resource type when unregistered types are allowed`() {
    val factory = JsonApiFactory.Builder()
      .allowUnregisteredTypes(true)
      .build()
    val moshi: Moshi = Moshi.Builder()
      .add(factory)
      .build()
    val adapter = moshi.adapter(Resource::class.java)
    val deserialized = adapter.fromJson(read(RESOURCE_UNKNOWN_TYPE))
    assertThat(deserialized).isInstanceOfSatisfying(Resource::class.java) {
      assertThat(it.type).isEqualTo("unknown")
      assertThat(it.id).isEqualTo("1")
      assertThat(it.relationships).hasSize(1)
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
    }
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on deserializing unregistered resource when unregistered types are not allowed`() {
    val factory = JsonApiFactory.Builder()
      .allowUnregisteredTypes(false)
      .build()
    val moshi: Moshi = Moshi.Builder()
      .add(factory)
      .build()
    val adapter = moshi.adapter(Resource::class.java)
    adapter.fromJson(read(RESOURCE_UNKNOWN_TYPE))
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on deserializing resource with invalid type`() {
    adapter.fromJson("""{"type":null}""")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on deserializing resource without top-level member type`() {
    adapter.fromJson("{}")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on deserializing resource that is not a json object`() {
    adapter.fromJson("[]")
  }
  
  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize registered resource as registered type`() {
    val resource: Resource = Article("articles", "1", "Title")
    val serialized = adapter.toJson(resource)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1","attributes":{"title":"Title"}}""")
  }
  
  @Test
  fun `serialize unregistered resource as base resource type when unregistered types are allowed`() {
    val factory = JsonApiFactory.Builder()
      .allowUnregisteredTypes(true)
      .build()
    val moshi: Moshi = Moshi.Builder()
      .add(factory)
      .build()
    val adapter = moshi.adapter(Resource::class.java)
    val resource: Resource = Article("articles", "1", "title won't be serialized")
    val serialized = adapter.toJson(resource)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on serializing unregistered type when unregistered types are not allowed`() {
    val factory = JsonApiFactory.Builder()
      .allowUnregisteredTypes(false)
      .build()
    val moshi: Moshi = Moshi.Builder()
      .add(factory)
      .build()
    val adapter = moshi.adapter(Resource::class.java)
    val resource: Resource = ValidResource()
    adapter.toJson(resource)
  }
}