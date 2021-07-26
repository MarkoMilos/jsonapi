package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.JsonApiFactory
import com.jsonapi.JsonFile.RELATIONSHIP_TO_ONE
import com.jsonapi.JsonFile.RELATIONSHIP_TO_ONE_EMPTY
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationship.ToOne
import com.jsonapi.ResourceIdentifier
import com.jsonapi.inlineJson
import com.jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RelationshipToOneAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(ToOne::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize empty to-one relationship`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_ONE_EMPTY))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized).hasAllNullFieldsOrProperties()
  }

  @Test
  fun `deserialize non-empty to-one relationship`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_ONE))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.data).isEqualTo(ResourceIdentifier("type", "1"))
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
  }

  @Test(expected = JsonApiException::class)
  fun `throw when relationship is not json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize empty to-one relationship`() {
    val serialized = adapter.toJson(ToOne(data = null))
    assertThat(serialized).isEqualTo("""{"data":null}""")
  }

  @Test
  fun `serialize non-empty to-one relationship`() {
    val relationship = ToOne(
      data = ResourceIdentifier("type", "1"),
      links = Links.from("link" to "link"),
      meta = Meta("name" to "value")
    )
    val serialized = adapter.toJson(relationship)
    assertThat(serialized).isEqualTo(
      """
      {
      "data":{"type":"type","id":"1"},
      "links":{"link":"link"},
      "meta":{"name":"value"}
      }
      """.inlineJson()
    )
  }
}
