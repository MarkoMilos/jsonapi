package com.jsonapi.internal.adapter

import com.jsonapi.JsonFormatException
import com.jsonapi.JsonApiFactory
import com.jsonapi.JsonFile.RELATIONSHIP_TO_MANY
import com.jsonapi.JsonFile.RELATIONSHIP_TO_MANY_EMPTY
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationship.ToMany
import com.jsonapi.ResourceIdentifier
import com.jsonapi.inlineJson
import com.jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RelationshipToManyAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(ToMany::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize empty to-many relationship`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_MANY_EMPTY))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.data).isEmpty()
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("data")
  }

  @Test
  fun `deserialize non-empty to-many relationship`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_MANY))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.data).containsExactly(
      ResourceIdentifier("type", "1"),
      ResourceIdentifier("type", "2"),
      ResourceIdentifier("type", "3")
    )
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when relationship is not json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize empty to-many relationship`() {
    val serialized = adapter.toJson(ToMany(data = emptyList()))
    assertThat(serialized).isEqualTo("""{"data":[]}""")
  }

  @Test
  fun `serialize non-empty to-many relationship`() {
    val relationship = ToMany(
      data = listOf(
        ResourceIdentifier("type", "1"),
        ResourceIdentifier("type", "2"),
        ResourceIdentifier("type", "3")
      ),
      links = Links.from("link" to "link"),
      meta = Meta("name" to "value")
    )
    val serialized = adapter.toJson(relationship)
    assertThat(serialized).isEqualTo(
      """
      {
      "data":[
        {"type":"type","id":"1"},
        {"type":"type","id":"2"},
        {"type":"type","id":"3"}
      ],
      "links":{"link":"link"},
      "meta":{"name":"value"}
      }
      """.inlineJson()
    )
  }
}
