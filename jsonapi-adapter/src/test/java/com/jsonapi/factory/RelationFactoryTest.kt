package com.jsonapi.factory

import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.adapter.RelationAdapter
import com.jsonapi.model.Relation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RelationFactoryTest {
  
  private val relationFactory = RelationFactory()
  
  @Test
  fun `returns adapter when type is a Relation`() {
    val type = Relation::class.java
    val adapter = relationFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull.isInstanceOf(RelationAdapter::class.java)
  }
  
  @Test
  fun `returns null when type is not a Relation`() {
    val type = String::class.java
    val adapter = relationFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}