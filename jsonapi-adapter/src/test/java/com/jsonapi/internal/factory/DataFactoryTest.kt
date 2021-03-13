package com.jsonapi.internal.factory

import com.jsonapi.Document
import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.ValidResource
import com.jsonapi.internal.adapter.DataAdapter
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DataFactoryTest {
  
  private val dataFactory = DataFactory()
  
  @Test
  fun `returns adapter when type is Data`() {
    val type =
      Types.newParameterizedTypeWithOwner(Document::class.java, Document.Data::class.java, ValidResource::class.java)
    val adapter = dataFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(DataAdapter::class.java)
  }
  
  @Test
  fun `returns null when type is not Data`() {
    // this factory doesn't apply for Document<*> (or any other type) only for Document.Data<*>
    val type = Types.newParameterizedType(Document::class.java, String::class.java)
    val adapter = dataFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}