package com.jsonapi.internal.adapter

import com.jsonapi.Document
import com.jsonapi.Error
import com.jsonapi.JsonFile.DOCUMENT_ERROR
import com.jsonapi.JsonFile.DOCUMENT_ERROR_SIMPLE
import com.jsonapi.TestUtils.moshi
import com.jsonapi.read
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ErrorAdapterTest {
  
  private val adapter = moshi.adapter(Document.Errors::class.java)
  
  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }
  
  @Test
  fun `deserialize empty errors`() {
    val deserialized = adapter.fromJson("""{"errors":[]}""")
    assertThat(deserialized?.errors).isEmpty()
  }
  
  @Test
  fun `deserialize non empty errors`() {
    val deserialized = adapter.fromJson(read(DOCUMENT_ERROR))
    
    assertThat(deserialized).isNotNull
    
    val errors = deserialized!!.errors
    val error1 = errors[0]
    val error2 = errors[1]
    val error3 = errors[2]
    
    assertThat(errors).hasSize(3)
    assertThat(error1).hasNoNullFieldsOrProperties()
    assertThat(error1.source).hasNoNullFieldsOrProperties()
    assertThat(error2).hasAllNullFieldsOrProperties()
    assertThat(error3).hasAllNullFieldsOrPropertiesExcept("id", "links", "source")
    assertThat(error3.source).hasAllNullFieldsOrProperties()
  }
  
  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize empty errors`() {
    val document = Document.Errors(emptyList())
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"errors":[]}""")
  }
  
  @Test
  fun `serialize non empty errors`() {
    val errors = listOf(Error(title = "Title", detail = "Detail"))
    val document = Document.Errors(errors)
    val serialized = adapter.toJson(document)
    val expected = read(DOCUMENT_ERROR_SIMPLE, true)
    assertThat(serialized).isEqualTo(expected)
  }
}