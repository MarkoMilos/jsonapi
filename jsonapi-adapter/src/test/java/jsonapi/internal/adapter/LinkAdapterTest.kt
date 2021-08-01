package jsonapi.internal.adapter

import jsonapi.JsonFormatException
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.LINK_OBJECT_FULL
import jsonapi.Link
import jsonapi.Link.LinkObject
import jsonapi.Link.URI
import jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LinkAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(Link::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize link uri`() {
    val deserialized = adapter.fromJson("\"link\"")
    assertThat(deserialized).isInstanceOf(URI::class.java)
  }

  @Test
  fun `deserialize link object`() {
    val deserialized = adapter.fromJson(read(LINK_OBJECT_FULL))
    assertThat(deserialized).isInstanceOf(LinkObject::class.java)
  }

  @Test(expected = JsonFormatException::class)
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
    val serialized = adapter.toJson(URI("link"))
    assertThat(serialized).isEqualTo("\"link\"")
  }

  @Test
  fun `serialize link object`() {
    val serialized = adapter.toJson(LinkObject("link"))
    assertThat(serialized).isEqualTo("""{"href":"link"}""")
  }
}
