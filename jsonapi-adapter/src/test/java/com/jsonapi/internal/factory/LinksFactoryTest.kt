package com.jsonapi.internal.factory

import com.jsonapi.Link
import com.jsonapi.Link.LinkObject
import com.jsonapi.Links
import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.internal.adapter.LinkAdapter
import com.jsonapi.internal.adapter.LinkObjectAdapter
import com.jsonapi.internal.adapter.LinksAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LinksFactoryTest {
  
  private val linksFactory = LinksFactory()
  
  @Test
  fun `returns LinksAdapter when type is Links`() {
    val type = Links::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(LinksAdapter::class.java)
  }
  
  @Test
  fun `returns LinkAdapter when type is Link`() {
    val type = Link::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(LinkAdapter::class.java)
  }
  
  @Test
  fun `returns LinkObjectAdapter when type is LinkObject`() {
    val type = LinkObject::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(LinkObjectAdapter::class.java)
  }
  
  @Test
  fun `returns null for unrecognized type`() {
    val type = String::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}