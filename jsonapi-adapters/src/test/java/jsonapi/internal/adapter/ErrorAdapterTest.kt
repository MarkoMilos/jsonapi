package jsonapi.internal.adapter

import com.squareup.moshi.Moshi
import jsonapi.Error
import jsonapi.Error.Source
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.ERROR
import jsonapi.JsonFormatException
import jsonapi.Links
import jsonapi.Meta
import jsonapi.read
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class ErrorAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(Error::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize error`() {
    val deserialized = adapter.fromJson(read(ERROR)) ?: fail("deserialized == null")
    assertThat(deserialized.id).isEqualTo("id")
    assertThat(deserialized.status).isEqualTo("status")
    assertThat(deserialized.code).isEqualTo("code")
    assertThat(deserialized.title).isEqualTo("title")
    assertThat(deserialized.detail).isEqualTo("detail")
    assertThat(deserialized.source).isNotNull
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when deserializing error with all null fields`() {
    adapter.fromJson("""{"id":null}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when deserializing non json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `ignore non standard json names`() {
    val deserialized = adapter.fromJson("""{"id":"1","non_standard":"value"}""") ?: fail("deserialized == null")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("id")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize error`() {
    val error = Error.Builder()
      .id("id")
      .status("status")
      .code("code")
      .title("title")
      .detail("detail")
      .source(Source("pointer", "parameter", "header"))
      .links(Links.from("link" to "link"))
      .meta(Meta("name" to "value"))
      .build()
    val serialized = adapter.toJson(error)
    val expected = read(ERROR, true)
    assertThat(serialized).isEqualTo(expected)
  }
}
