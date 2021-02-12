package com.jsonapi.factory

import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.adapter.LinkAdapter
import com.jsonapi.adapter.LinkObjectAdapter
import com.jsonapi.adapter.LinksAdapter
import com.jsonapi.model.Link
import com.jsonapi.model.Link.LinkObject
import com.jsonapi.model.Links
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LinksFactoryTest {
  
  private val linksFactory = LinksFactory()
  
  @Test
  fun `returns LinksAdapter when type is Links`() {
    val type = Links::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull.isInstanceOf(LinksAdapter::class.java)
  }
  
  @Test
  fun `returns LinkAdapter when type is Link`() {
    val type = Link::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull.isInstanceOf(LinkAdapter::class.java)
  }
  
  @Test
  fun `returns LinkObjectAdapter when type is LinkObject`() {
    val type = LinkObject::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull.isInstanceOf(LinkObjectAdapter::class.java)
  }
  
  @Test
  fun `returns null for unrecognized type`() {
    val type = String::class.java
    val adapter = linksFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}