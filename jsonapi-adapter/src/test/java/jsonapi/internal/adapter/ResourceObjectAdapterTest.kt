package jsonapi.internal.adapter

import jsonapi.JsonFormatException
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.RESOURCE_ARTICLE
import jsonapi.JsonFile.RESOURCE_ARTICLE_NON_STANDARD_NAMES
import jsonapi.Link.URI
import jsonapi.Links
import jsonapi.Meta
import jsonapi.Relationships
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.inlineJson
import jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResourceObjectAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(ResourceObject::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize basic resource`() {
    val deserialized = adapter.fromJson("""{"type":"articles", "id":"1"}""")
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "id")
  }

  @Test
  fun `deserialize resource with local identifier`() {
    val deserialized = adapter.fromJson("""{"type":"articles", "lid":"1"}""")
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.lid).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "lid")
  }

  @Test
  fun `deserialize resource with resource object members`() {
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized.lid).isNull()
    assertThat(deserialized.relationships?.members).hasSize(2)
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource without type`() {
    adapter.fromJson("""{"id":"1"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource with invalid type`() {
    adapter.fromJson("""{"type":null, "id":"1"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource without id or lid`() {
    adapter.fromJson("""{"type":"articles"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource with invalid id`() {
    adapter.fromJson("""{"type":"articles", "id":""}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource with invalid lid`() {
    adapter.fromJson("""{"type":"articles", "lid":""}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when deserializing resource that is not an json object`() {
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
  fun `serialize basic resource`() {
    val resource = ResourceObject("articles", "1", null)
    val serialized = adapter.toJson(resource)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }

  @Test
  fun `serialize resource with local identifier`() {
    val resource = ResourceObject("articles", lid = "1")
    val serialized = adapter.toJson(resource)
    assertThat(serialized).isEqualTo("""{"type":"articles","lid":"1"}""")
  }

  @Test
  fun `serialize resource with all members`() {
    val resource = ResourceObject(
      type = "articles",
      id = "1",
      lid = "2",
      relationships = Relationships.Builder()
        .toOne("author", ResourceIdentifier("people", "1"))
        .toMany(
          "comments",
          ResourceIdentifier("comments", "1"),
          ResourceIdentifier("comments", "2")
        )
        .build(),
      links = Links("self" to URI("self")),
      meta = Meta("name" to "value")
    )

    val serialized = adapter.toJson(resource)

    assertThat(serialized).isEqualTo(
      """
      {
      "type":"articles",
      "id":"1",
      "lid":"2",
      "relationships":{
        "author":{"data":{"type":"people","id":"1"}},
        "comments":{"data":[{"type":"comments","id":"1"},{"type":"comments","id":"2"}]}},
      "links":{"self":"self"},
      "meta":{"name":"value"}
      }
      """.inlineJson()
    )
  }
}
