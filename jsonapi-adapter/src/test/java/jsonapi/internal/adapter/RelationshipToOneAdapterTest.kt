package jsonapi.internal.adapter

import jsonapi.JsonFormatException
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.RELATIONSHIP_TO_ONE
import jsonapi.JsonFile.RELATIONSHIP_TO_ONE_EMPTY
import jsonapi.Links
import jsonapi.Meta
import jsonapi.Relationship.ToOne
import jsonapi.ResourceIdentifier
import jsonapi.inlineJson
import jsonapi.read
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
