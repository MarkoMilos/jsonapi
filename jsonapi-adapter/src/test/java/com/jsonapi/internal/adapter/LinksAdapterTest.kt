package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiFactory
import com.jsonapi.JsonFile.LINKS
import com.jsonapi.Link.LinkObject
import com.jsonapi.Link.URI
import com.jsonapi.Links
import com.jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class LinksAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(Links::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize links`() {
    val deserialized = adapter.fromJson(read(LINKS))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.members).containsExactly(
      entry("self", URI("self")),
      entry("related", LinkObject("href")),
      entry("null_link", null)
    )
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize links`() {
    val links = Links(
      "self" to URI("self"),
      "related" to LinkObject("href"),
      "null_link" to null
    )
    val serialized = adapter.toJson(links)
    assertThat(serialized).isEqualTo("""{"self":"self","related":{"href":"href"},"null_link":null}""")
  }
}
