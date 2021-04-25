package com.jsonapi.internal.adapter

import com.jsonapi.JsonFile.LINKS
import com.jsonapi.Link.LinkObject
import com.jsonapi.Link.LinkURI
import com.jsonapi.Links
import com.jsonapi.TestUtils.moshi
import com.jsonapi.read
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class LinksAdapterTest {

  private val adapter = moshi.adapter(Links::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize links`() {
    val deserialized = adapter.fromJson(read(LINKS))
    assertThat(deserialized).isNotNull
    assertThat(deserialized?.members).containsExactly(
      entry("self", LinkURI("self")),
      entry("related", LinkObject("href"))
    )
  }

  @Test
  fun `skip entries with null values`() {
    val deserialized = adapter.fromJson(read(LINKS))
    assertThat(deserialized?.members).doesNotContainKey("null_link")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize links`() {
    val linksMap = mapOf(
      "self" to LinkURI("self"),
      "related" to LinkObject("href")
    )
    val links = Links(linksMap)
    val serialized = adapter.toJson(links)
    assertThat(serialized).isEqualTo("""{"self":"self","related":{"href":"href"}}""")
  }
}
