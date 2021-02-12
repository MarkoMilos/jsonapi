package com.jsonapi.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.JsonFile.*
import com.jsonapi.TestUtils.moshi
import com.jsonapi.model.Meta
import com.jsonapi.model.Relation
import com.jsonapi.model.Relation.ToMany
import com.jsonapi.model.Relation.ToOne
import com.jsonapi.model.ResourceIdentifier
import com.jsonapi.read
import com.squareup.moshi.JsonAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RelationAdapterTest {
  
  private val adapter: JsonAdapter<Relation> = moshi.adapter(Relation::class.java)
  
  @Test
  fun `deserialize empty ToOne relation`() {
    val deserialized = adapter.fromJson(read(RELATION_TO_ONE_EMPTY))
    assertThat(deserialized).isInstanceOfSatisfying(ToOne::class.java) {
      assertThat(it.data).isNull()
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
    }
  }
  
  @Test
  fun `deserialize non empty ToOne relation`() {
    val deserialized = adapter.fromJson(read(RELATION_TO_ONE))
    assertThat(deserialized).isInstanceOfSatisfying(ToOne::class.java) {
      assertThat(it.data).isNotNull
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
    }
  }
  
  @Test
  fun `deserialize empty ToMany relation`() {
    val deserialized = adapter.fromJson(read(RELATION_TO_MANY_EMPTY))
    assertThat(deserialized).isInstanceOfSatisfying(ToMany::class.java) {
      assertThat(it.data).isNotNull.isEmpty()
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
    }
  }
  
  @Test
  fun `deserialize non empty ToMany relation`() {
    val deserialized = adapter.fromJson(read(RELATION_TO_MANY))
    assertThat(deserialized).isInstanceOfSatisfying(ToMany::class.java) {
      assertThat(it.data).isNotNull.hasSize(3)
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
    }
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when deserializing non object`() {
    adapter.fromJson("null")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when deserializing non valid relation`() {
    adapter.fromJson(read(RELATION_INVALID))
  }
  
  @Test
  fun `serialize empty ToOne relation without links or meta`() {
    val relation = ToOne(data = null)
    val serialized = adapter.toJson(relation)
    assertThat(serialized).isEqualTo("""{"data":null}""")
  }
  
  @Test
  fun `serialize empty ToOne relation with links or meta`() {
    val relation = ToOne(data = null, meta = Meta(mapOf("name" to "value")))
    val serialized = adapter.toJson(relation)
    assertThat(serialized).isEqualTo("""{"meta":{"name":"value"}}""")
  }
  
  @Test
  fun `serialize non empty ToOne relation`() {
    val relation = ToOne(ResourceIdentifier("resource", "1"))
    val serialized = adapter.toJson(relation)
    assertThat(serialized).isEqualTo("""{"data":{"type":"resource","id":"1"}}""")
  }
  
  @Test
  fun `serialize empty ToMany relation`() {
    val relation = ToMany(emptyList())
    val serialized = adapter.toJson(relation)
    assertThat(serialized).isEqualTo("""{"data":[]}""")
  }
  
  @Test
  fun `serialize non empty ToMany relation`() {
    val relation = ToMany(
      listOf(
        ResourceIdentifier("resource", "1"),
        ResourceIdentifier("resource", "2")
      )
    )
    val serialized = adapter.toJson(relation)
    assertThat(serialized).isEqualTo("""{"data":[{"type":"resource","id":"1"},{"type":"resource","id":"2"}]}""")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when serializing null as relation`() {
    adapter.toJson(null)
  }
}