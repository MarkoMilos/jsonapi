package com.jsonapi.internal.factory

import com.jsonapi.*
import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.internal.adapter.ResourceSubclassAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResourceSubclassFactoryTest {
  
  private val resourceFactory = ResourceSubclassFactory(
    typeNames = emptyList(),
    allowUnregisteredTypes = false,
    strictTypes = false
  )
  
  @Test
  fun `returns adapter when type inherits directly from Resource`() {
    val type = ValidResource::class.java
    val adapter = resourceFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(ResourceSubclassAdapter::class.java)
  }
  
  @Test
  fun `returns adapter when type inherits indirectly from Resource`() {
    val type = ValidResourceSubclass::class.java
    val adapter = resourceFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(ResourceSubclassAdapter::class.java)
  }
  
  @Test
  fun `returns null when type does not inherit from Resource`() {
    val type = NotAResource::class.java
    val adapter = resourceFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when resource is not annotated with Type annotation`() {
    resourceFactory.create(NotAnnotatedResource::class.java, mutableSetOf(), moshi, factory)
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when annotated type value is invalid`() {
    resourceFactory.create(InvalidTypeResource::class.java, mutableSetOf(), moshi, factory)
  }
}