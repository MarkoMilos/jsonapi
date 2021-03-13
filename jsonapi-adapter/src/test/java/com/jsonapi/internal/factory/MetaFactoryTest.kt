package com.jsonapi.internal.factory

import com.jsonapi.Meta
import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.internal.adapter.MetaAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MetaFactoryTest {
  
  private val metaFactory = MetaFactory()
  
  @Test
  fun `returns adapter when type is Meta`() {
    val type = Meta::class.java
    val adapter = metaFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(MetaAdapter::class.java)
  }
  
  @Test
  fun `returns null when type is not Meta`() {
    val type = String::class.java
    val adapter = metaFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}