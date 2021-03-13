package com.jsonapi.factory

import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.ValidResource
import com.jsonapi.adapter.DocumentAdapter
import com.jsonapi.model.Document
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
    // This adapter should not register for Document.Data type (or any other subtypes)
    // therefore it should return null here
    val type =
      Types.newParameterizedTypeWithOwner(Document::class.java, Document.Data::class.java, ValidResource::class.java)
    val adapter = documentFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNull()
  }
}