package com.jsonapi.internal.factory

import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.internal.adapter.VoidAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VoidFactoryTest {

  private val voidFactory = VoidFactory()

  @Test
  fun `return adapter when type is Void`() {
    val adapter = voidFactory.create(Void::class.java, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(VoidAdapter::class.java)
  }

  @Test
  fun `return adapter when type is Nothing`() {
    val adapter = voidFactory.create(Nothing::class.java, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(VoidAdapter::class.java)
  }

  @Test
  fun `return null when type is not Void or Nothing`() {
    val adapter = voidFactory.create(Any::class.java, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}
