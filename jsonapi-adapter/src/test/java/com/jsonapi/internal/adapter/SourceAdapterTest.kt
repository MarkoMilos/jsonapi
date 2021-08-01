package com.jsonapi.internal.adapter

import com.jsonapi.Error.Source
import com.jsonapi.JsonFormatException
import com.jsonapi.JsonApiFactory
import com.jsonapi.inlineJson
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class SourceAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(Source::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize source`() {
    val json = """
      {
        "pointer":"pointer",
        "parameter":"parameter",
        "header":"header"
      }
      """.inlineJson()
    val deserialized = adapter.fromJson(json) ?: fail("deserialized == null")
    assertThat(deserialized.pointer).isEqualTo("pointer")
    assertThat(deserialized.parameter).isEqualTo("parameter")
    assertThat(deserialized.header).isEqualTo("header")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when deserializing non json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `ignore non standard json names`() {
    val deserialized = adapter.fromJson("""{"name":"value"}""") ?: fail("deserialized == null")
    assertThat(deserialized).hasAllNullFieldsOrProperties()
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize source`() {
    val source = Source("pointer", "parameter", "header")
    val serialized = adapter.toJson(source)
    val expected = """
      {
        "pointer":"pointer",
        "parameter":"parameter",
        "header":"header"
      }
    """.inlineJson()
    assertThat(serialized).isEqualTo(expected)
  }
}
