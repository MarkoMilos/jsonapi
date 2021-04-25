package com.jsonapi.internal.factory

import com.jsonapi.Document
import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.ValidResource
import com.jsonapi.internal.adapter.DocumentAdapter
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DocumentFactoryTest {

  private val documentFactory = DocumentFactory()

  @Test
  fun `returns adapter when type is Document`() {
    val type = Types.newParameterizedType(Document::class.java, ValidResource::class.java)
    val adapter = documentFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test
  fun `returns null when type is not Document`() {
    val type = ValidResource::class.java
    val adapter = documentFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}
