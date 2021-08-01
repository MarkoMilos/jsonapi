package com.jsonapi.internal.adapter

import com.jsonapi.JsonFormatException
import com.jsonapi.JsonApiFactory
import com.jsonapi.JsonFile.RESOURCE_ARTICLE
import com.jsonapi.JsonFile.RESOURCE_ARTICLE_NON_STANDARD_NAMES
import com.jsonapi.Meta
import com.jsonapi.ResourceIdentifier
import com.jsonapi.inlineJson
import com.jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResourceIdentifierAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(ResourceIdentifier::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize basic identifier`() {
    val deserialized = adapter.fromJson("""{"type":"articles", "id":"1"}""")
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "id")
  }

  @Test
  fun `deserialize identifier with lid`() {
    val deserialized = adapter.fromJson("""{"type":"articles", "lid":"1"}""")
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.lid).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "lid")
  }

  @Test
  fun `deserialize identifier with all members`() {
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized.lid).isNull()
    assertThat(deserialized.meta).isNotNull
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing identifier without type`() {
    adapter.fromJson("""{"id":"1"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing identifier with invalid type`() {
    adapter.fromJson("""{"type":null, "id":"1"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing identifier without id or lid`() {
    adapter.fromJson("""{"type":"articles"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing identifier with invalid id`() {
    adapter.fromJson("""{"type":"articles", "id":""}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing identifier with invalid lid`() {
    adapter.fromJson("""{"type":"articles", "lid":""}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when deserializing identifier that is not an json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `ignore non standard json names`() {
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE_NON_STANDARD_NAMES))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "id")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize basic identifier`() {
    val identifier = ResourceIdentifier("articles", "1", null)
    val serialized = adapter.toJson(identifier)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }

  @Test
  fun `serialize identifier with lid`() {
    val identifier = ResourceIdentifier("articles", lid = "1")
    val serialized = adapter.toJson(identifier)
    assertThat(serialized).isEqualTo("""{"type":"articles","lid":"1"}""")
  }

  @Test
  fun `serialize identifier with all members`() {
    val identifier = ResourceIdentifier(
      type = "articles",
      id = "1",
      lid = "2",
      meta = Meta("name" to "value")
    )

    val serialized = adapter.toJson(identifier)

    assertThat(serialized).isEqualTo(
      """
      {
      "type":"articles",
      "id":"1",
      "lid":"2",
      "meta":{"name":"value"}
      }
      """.inlineJson()
    )
  }
}
