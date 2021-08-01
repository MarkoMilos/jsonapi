package jsonapi.internal.adapter

import jsonapi.JsonApiFactory
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class VoidAdapterTest {

  @JsonClass(generateAdapter = true)
  class VoidTestClass<T>(
    val nonVoid: String? = null,
    val void: T? = null
  )

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val type = Types.newParameterizedTypeWithOwner(
    VoidAdapterTest::class.java,
    VoidTestClass::class.java,
    Nothing::class.java
  )

  private val adapter = moshi.adapter<VoidTestClass<Nothing>>(type)
    .failOnUnknown()
    .serializeNulls()

  @Test
  fun `deserialize class with void fields`() {
    val json = """{"nonVoid":"value","void":"value"}"""
    val deserialized = adapter.fromJson(json) ?: fail("deserialized == null")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("nonVoid")
  }

  @Test
  fun `serialize void`() {
    val value = VoidTestClass(nonVoid = "value", void = null)
    val serialized = adapter.toJson(value)
    assertThat(serialized).isEqualTo("""{"nonVoid":"value","void":null}""")
  }
}
