package jsonapi.internal.adapter

import jsonapi.JsonFormatException
import jsonapi.JsonApiFactory
import jsonapi.JsonApiObject
import jsonapi.JsonFile.JSON_API_OBJECT
import jsonapi.Meta
import jsonapi.read
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class JsonApiObjectAdapterTest {

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(JsonApiObject::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize json api object`() {
    val deserialized = adapter.fromJson(read(JSON_API_OBJECT)) ?: fail("deserialized == null")
    assertThat(deserialized.version).isEqualTo("1.1")
    assertThat(deserialized.ext).containsExactly("ext-1")
    assertThat(deserialized.profile).containsExactly("profile-1", "profile-2")
    assertThat(deserialized.meta).isNotNull
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when deserializing non json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `ignore non standard json names`() {
    val deserialized = adapter.fromJson("""{"name":"value"}""") ?: fail("deserialized == null")
    assertThat(deserialized).hasAllNullFieldsOrProperties()
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize json api object`() {
    val jsonApi = JsonApiObject(
      version = "1.1",
      ext = listOf("ext-1"),
      profile = listOf("profile-1", "profile-2"),
      meta = Meta("name" to "value")
    )
    val serialized = adapter.toJson(jsonApi)
    val expected = read(JSON_API_OBJECT, true)
    assertThat(serialized).isEqualTo(expected)
  }
}
