package com.jsonapi

import com.jsonapi.TestUtils.factory
import com.jsonapi.TestUtils.moshi
import com.jsonapi.adapter.DocumentAdapter
import com.jsonapi.factory.DocumentFactory
import com.jsonapi.model.Document
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DocumentFactoryTest {
  
  private val documentFactory = DocumentFactory()
  
  @Test(expected = JsonApiException::class)
  fun `throws when non collection generic type does not inherit from Resource`() {
    // requesting adapter for a Document with non collection generic type that does not inherit
    // (directly or indirectly) from Resource (e.g. Document<Int>) throws exception
    val type = Types.newParameterizedType(Document::class.java, NotAResource::class.java)
    documentFactory.create(type, mutableSetOf(), moshi, factory)
  }
  
  @Test
  fun `returns adapter when non collection generic type directly inherits from Resource`() {
    val type = Types.newParameterizedType(Document::class.java, ValidResource::class.java)
    val adapter = documentFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }
  
  @Test
  fun `returns adapter when non collection generic type indirectly inherits from Resource`() {
    val type = Types.newParameterizedType(Document::class.java, ValidResourceSubclass::class.java)
    val adapter = documentFactory.create(type, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }
  
  @Test(expected = JsonApiException::class)
  fun `throws when collection generic type does not inherit from Resource`() {
    val collectionType = Types.newParameterizedType(List::class.java, NotAResource::class.java)
    val documentType = Types.newParameterizedType(Document::class.java, collectionType)
    documentFactory.create(documentType, mutableSetOf(), moshi, factory)
  }
  
  @Test
  fun `returns adapter when collection generic type directly inherits from Resource`() {
    val collectionType = Types.newParameterizedType(List::class.java, ValidResource::class.java)
    val documentType = Types.newParameterizedType(Document::class.java, collectionType)
    val adapter = documentFactory.create(documentType, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }
  
  @Test
  fun `returns adapter when collection generic type indirectly inherits from Resource`() {
    val collectionType = Types.newParameterizedType(List::class.java, ValidResourceSubclass::class.java)
    val documentType = Types.newParameterizedType(Document::class.java, collectionType)
    val adapter = documentFactory.create(documentType, mutableSetOf(), moshi, factory)
    assertThat(adapter).isNotNull
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }
}