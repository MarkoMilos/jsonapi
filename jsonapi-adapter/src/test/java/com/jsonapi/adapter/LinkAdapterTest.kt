package com.jsonapi.adapter

import com.jsonapi.JsonApiException
import com.jsonapi.JsonFile.LINK_OBJECT_FULL
import com.jsonapi.TestUtils.moshi
import com.jsonapi.model.Link
import com.jsonapi.model.Link.LinkObject
import com.jsonapi.model.Link.LinkURI
import com.jsonapi.read
import com.squareup.moshi.JsonAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LinkAdapterTest {
  
  private val adapter: JsonAdapter<Link> = moshi.adapter(Link::class.java)
  
  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }
  
  @Test
  fun `deserialize link uri`() {
    val deserialized = adapter.fromJson("\"link\"")
    assertThat(deserialized).isInstanceOf(LinkURI::class.java)
  }
  
  @Test
  fun `deserialize link object`() {
    val deserialized = adapter.fromJson(read(LINK_OBJECT_FULL))
    assertThat(deserialized).isInstanceOf(LinkObject::class.java)
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when link is not string or object`() {
    adapter.fromJson("[]")
  }
  
  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize link uri`() {
    val serialized = adapter.toJson(LinkURI("link"))
    assertThat(serialized).isEqualTo("\"link\"")
  }
  
  @Test
  fun `serialize link object`() {
    val serialized = adapter.toJson(LinkObject("link"))
    assertThat(serialized).isEqualTo("""{"href":"link"}""")
  }
}