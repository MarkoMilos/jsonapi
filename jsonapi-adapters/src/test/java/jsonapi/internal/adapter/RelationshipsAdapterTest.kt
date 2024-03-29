package jsonapi.internal.adapter

import com.squareup.moshi.Moshi
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.RELATIONSHIPS
import jsonapi.JsonFormatException
import jsonapi.Relationship.ToMany
import jsonapi.Relationship.ToOne
import jsonapi.Relationships
import jsonapi.ResourceIdentifier
import jsonapi.inlineJson
import jsonapi.read
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class RelationshipsAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(Relationships::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize empty relationships`() {
    val deserialized = adapter.fromJson("{}") ?: fail("deserialized == null")
    assertThat(deserialized.isEmpty()).isTrue
  }

  @Test
  fun `deserialize non-empty relationships`() {
    val deserialized = adapter.fromJson(read(RELATIONSHIPS)) ?: fail("deserialized == null")

    assertThat(deserialized.members).containsOnlyKeys("to_one_empty", "to_one", "to_many_empty", "to_many")
    assertThat(deserialized.members["to_one_empty"]).isInstanceOfSatisfying(ToOne::class.java) {
      assertThat(it).hasAllNullFieldsOrProperties()
    }
    assertThat(deserialized.members["to_one"]).isInstanceOfSatisfying(ToOne::class.java) {
      assertThat(it.data).isEqualTo(ResourceIdentifier("type", "1"))
      assertThat(it).hasNoNullFieldsOrProperties()
    }
    assertThat(deserialized.members["to_many_empty"]).isInstanceOfSatisfying(ToMany::class.java) {
      assertThat(it.data).isEmpty()
      assertThat(it).hasAllNullFieldsOrPropertiesExcept("data")
    }
    assertThat(deserialized.members["to_many"]).isInstanceOfSatisfying(ToMany::class.java) {
      assertThat(it.data).containsExactly(
        ResourceIdentifier("type", "1"),
        ResourceIdentifier("type", "2")
      )
      assertThat(it).hasNoNullFieldsOrProperties()
    }
  }

  @Test
  fun `omit null relationships`() {
    val deserialized = adapter.fromJson("""{"key":null}""") ?: fail("deserialized == null")
    assertThat(deserialized.members).isEmpty()
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when relationships is not json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize empty relationships`() {
    val relationships = Relationships(emptyMap())
    val serialized = adapter.toJson(relationships)
    assertThat(serialized).isEqualTo("{}")
  }

  @Test
  fun `serialize non-empty relationships`() {
    val relationships = Relationships(
      "to_one_empty" to ToOne(),
      "to_one" to ToOne(ResourceIdentifier("type", "1")),
      "to_many_empty" to ToMany(emptyList()),
      "to_many" to ToMany(listOf(ResourceIdentifier("type", "1")))
    )
    val serialized = adapter.toJson(relationships)
    assertThat(serialized).isEqualTo(
      """
      {
        "to_one_empty":{"data":null},
        "to_one":{"data":{"type":"type","id":"1"}},
        "to_many_empty":{"data":[]},
        "to_many":{"data":[{"type":"type","id":"1"}]}
      }
      """.inlineJson()
    )
  }
}
