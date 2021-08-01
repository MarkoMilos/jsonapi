package jsonapi.internal.adapter

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import jsonapi.Id
import jsonapi.JsonApiFactory
import jsonapi.Lid
import jsonapi.LinksObject
import jsonapi.MetaObject
import jsonapi.RelationshipsObject
import jsonapi.ToMany
import jsonapi.ToOne
import jsonapi.Type
import jsonapi.inlineJson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class TransientAdapterTest {

  @JsonClass(generateAdapter = true)
  class TransientTestClass(
    val nonTransient: Any? = null,
    @Type val transient1: Any? = null,
    @Id val transient2: Any? = null,
    @Lid val transient3: Any? = null,
    @RelationshipsObject val transient4: Any? = null,
    @LinksObject val transient5: Any? = null,
    @MetaObject val transient6: Any? = null,
    @ToOne("ONE") val transient7: Any? = null,
    @ToMany("MANY") val transient8: List<Any>? = null
  )

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .build()

  private val adapter = moshi.adapter(TransientTestClass::class.java)
    .failOnUnknown()
    .serializeNulls()

  @Test
  fun `deserialize class with transient fields`() {
    val json = """
      {
        "nonTransient":"value",
        "transient1":"value",
        "transient2":"value",
        "transient3":"value",
        "transient4":"value",
        "transient5":"value",
        "transient6":"value",
        "transient7":"value",
        "transient8":["value"]
      }
    """.inlineJson()

    val deserialized = adapter.fromJson(json) ?: fail("deserialized == null")

    assertThat(deserialized)
      .hasFieldOrPropertyWithValue("nonTransient", "value")
      .hasAllNullFieldsOrPropertiesExcept("nonTransient")
  }

  @Test
  fun `serialize class with transient fields`() {
    val value = TransientTestClass(
      nonTransient = "value",
      transient1 = "value",
      transient2 = "value",
      transient3 = "value",
      transient4 = "value",
      transient5 = "value",
      transient6 = "value",
      transient7 = "value",
      transient8 = listOf("value")
    )

    val serialized = adapter.toJson(value)

    assertThat(serialized).isEqualTo("""{"nonTransient":"value"}""")
  }
}
