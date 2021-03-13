package com.jsonapi.adapter

import com.jsonapi.JsonFile.META
import com.jsonapi.TestUtils.moshi
import com.jsonapi.model.Meta
import com.jsonapi.read
import com.squareup.moshi.JsonClass
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class MetaAdapterTest {
  
  @JsonClass(generateAdapter = true)
  data class NestedMetaObject(val foo: String)
  
  private val adapter = moshi.adapter(Meta::class.java)
  
  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }
  
  @Test
  fun `deserialize meta`() {
    val deserialized = adapter.fromJson(read(META))
    assertThat(deserialized).isNotNull
    assertThat(deserialized?.members).containsExactly(
      entry("number", 1.5),
      entry("string", "value"),
      entry("boolean", true),
      entry("array", listOf("one", "two", "three")),
      entry("null", null),
      entry("nested", mapOf("foo" to "bar"))
    )
  }
  
  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize meta`() {
    val metaMap = mapOf(
      "number" to 1.5,
      "string" to "value",
      "boolean" to true,
      "array" to listOf("one", "two", "three"),
      "null" to null,
      "nested" to NestedMetaObject(foo = "bar")
    )
    val meta = Meta(metaMap)
    val expected = read(META, true)
    val serialized = adapter.serializeNulls().toJson(meta)
    assertThat(serialized).isEqualTo(expected)
  }
}