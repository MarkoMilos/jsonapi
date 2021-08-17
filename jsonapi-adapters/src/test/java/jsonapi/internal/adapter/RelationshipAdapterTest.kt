package jsonapi.internal.adapter

import com.squareup.moshi.Moshi
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.RELATIONSHIP_TO_MANY
import jsonapi.JsonFile.RELATIONSHIP_TO_MANY_EMPTY
import jsonapi.JsonFile.RELATIONSHIP_TO_ONE
import jsonapi.JsonFile.RELATIONSHIP_TO_ONE_EMPTY
import jsonapi.JsonFormatException
import jsonapi.Links
import jsonapi.Meta
import jsonapi.Relationship
import jsonapi.Relationship.ToMany
import jsonapi.Relationship.ToOne
import jsonapi.ResourceIdentifier
import jsonapi.inlineJson
import jsonapi.read
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RelationshipAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(Relationship::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize empty to-one relation`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_ONE_EMPTY))
    assertThat(deserialized).isInstanceOfSatisfying(ToOne::class.java) {
      assertThat(deserialized).hasAllNullFieldsOrProperties()
    }
  }

  @Test
  fun `deserialize non-empty to-one relation`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_ONE))
    assertThat(deserialized).isInstanceOfSatisfying(ToOne::class.java) {
      assertThat(it.data).isNotNull
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
    }
  }

  @Test
  fun `deserialize empty to-many relation`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_MANY_EMPTY))
    assertThat(deserialized).isInstanceOfSatisfying(ToMany::class.java) {
      assertThat(it.data).isEmpty()
      assertThat(it).hasAllNullFieldsOrPropertiesExcept("data")
    }
  }

  @Test
  fun `deserialize non-empty to-many relation`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIP_TO_MANY))
    assertThat(deserialized).isInstanceOfSatisfying(ToMany::class.java) {
      assertThat(it.data).containsExactly(
        ResourceIdentifier("type", "1"),
        ResourceIdentifier("type", "2"),
        ResourceIdentifier("type", "3")
      )
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
    }
  }

  @Test
  fun `deserialize relationship without data member as to-one`() {
    val deserialized = adapter.fromJson("{}")
    assertThat(deserialized).isInstanceOfSatisfying(ToOne::class.java) {
      assertThat(it).hasAllNullFieldsOrProperties()
    }
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
  fun `serialize empty to-one relation`() {
    val relationship: Relationship = ToOne(data = null)
    val serialized = adapter.toJson(relationship)
    assertThat(serialized).isEqualTo("""{"data":null}""")
  }

  @Test
  fun `serialize non-empty to-one relation`() {
    val relationship: Relationship = ToOne(
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

  @Test
  fun `serialize empty to-many relation`() {
    val relationship: Relationship = ToMany(emptyList())
    val serialized = adapter.toJson(relationship)
    assertThat(serialized).isEqualTo("""{"data":[]}""")
  }

  @Test
  fun `serialize non-empty to-many relation`() {
    val relationship: Relationship = ToMany(
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
