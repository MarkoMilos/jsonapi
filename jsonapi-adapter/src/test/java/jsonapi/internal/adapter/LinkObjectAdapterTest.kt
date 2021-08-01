package jsonapi.internal.adapter

import jsonapi.JsonFormatException
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.LINK_OBJECT_FULL
import jsonapi.JsonFile.LINK_OBJECT_HREF_ONLY
import jsonapi.JsonFile.LINK_OBJECT_SINGLE_HREFLANG
import jsonapi.Link.LinkObject
import jsonapi.inlineJson
import jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LinkObjectAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(LinkObject::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize full link object`() {
    val deserialized = adapter.fromJson(read(LINK_OBJECT_FULL))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.href).isEqualTo("href")
    assertThat(deserialized.rel).isEqualTo("rel")
    assertThat(deserialized.describedby).isInstanceOfSatisfying(LinkObject::class.java) {
      assertThat(it)
        .hasFieldOrPropertyWithValue("href", "href")
        .hasAllNullFieldsOrPropertiesExcept("href")
    }
    assertThat(deserialized.title).isEqualTo("title")
    assertThat(deserialized.type).isEqualTo("type")
    assertThat(deserialized.hreflang).containsExactly("en", "es")
    assertThat(deserialized.meta).isNotNull
  }

  @Test
  fun `deserialize href only link object`() {
    val deserialized = adapter.fromJson(read(LINK_OBJECT_HREF_ONLY))
    assertThat(deserialized)
      .hasFieldOrPropertyWithValue("href", "href")
      .hasAllNullFieldsOrPropertiesExcept("href")
  }

  @Test
  fun `deserialize link object with hreflang string value as list of string`() {
    val deserialized = adapter.fromJson(read(LINK_OBJECT_SINGLE_HREFLANG))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.hreflang).asList().containsExactly("en")
  }

  @Test
  fun `deserialize link object with hreflang array value as list of string`() {
    val deserialized = adapter.fromJson(read(LINK_OBJECT_FULL))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized.hreflang).asList().containsExactly("en", "es")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw for invalid link object`() {
    // Link object MUST contain member [href]
    adapter.fromJson("{}")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize full link object`() {
    val linkObject = LinkObject(
      href = "href",
      rel = "rel",
      describedby = LinkObject("href"),
      title = "title",
      type = "type",
      hreflang = listOf("en", "es"),
      meta = null
    )
    val serialized = adapter.toJson(linkObject)
    assertThat(serialized).isEqualTo(
      """
        {
        "href":"href",
        "rel":"rel",
        "describedby":{"href":"href"},
        "title":"title",
        "type":"type",
        "hreflang":["en","es"]
        }""".inlineJson()
    )
  }

  @Test
  fun `serialize href only link object`() {
    val linkObject = LinkObject("href")
    val serialized = adapter.toJson(linkObject)
    assertThat(serialized).isEqualTo("""{"href":"href"}""")
  }

  @Test
  fun `serialize link object hreflang value as string when there is only one`() {
    val linkObject = LinkObject(
      href = "href",
      hreflang = listOf("en"),
    )
    val serialized = adapter.toJson(linkObject)
    assertThat(serialized).isEqualTo("""{"href":"href","hreflang":"en"}""")
  }

  @Test
  fun `serialize link object hreflang value as array when there are multiple`() {
    val linkObject = LinkObject(
      href = "href",
      hreflang = listOf("en", "es"),
    )
    val serialized = adapter.toJson(linkObject)
    assertThat(serialized).isEqualTo("""{"href":"href","hreflang":["en","es"]}""")
  }
}
