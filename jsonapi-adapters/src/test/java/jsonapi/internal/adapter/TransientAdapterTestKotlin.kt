package jsonapi.internal.adapter

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

class TransientAdapterTestKotlin {

  // Class that have all transient annotations of this library and
  // will use generated adapter from moshi.codegen module
  @JsonClass(generateAdapter = true)
  class CodeGenClass(
    val nonTransient: Any?,
    @Type val transient1: Any?,
    @Id val transient2: Any?,
    @Lid val transient3: Any?,
    @RelationshipsObject val transient4: Any?,
    @LinksObject val transient5: Any?,
    @MetaObject val transient6: Any?,
    @ToOne("ONE") val transient7: Any?,
    @ToMany("MANY") val transient8: List<Any>?
  ) {
    @ToOne("ONE") var transient9: Any? = null
    @ToMany("MANY") var transient10: List<Any>? = null
  }

  // Class that have all transient annotations of this library and
  // will use reflection adapter from moshi.reflect module (KotlinJsonAdapter)
  class ReflectClass(
    val nonTransient: Any?,
    @Type val transient1: Any?,
    @Id val transient2: Any?,
    @Lid val transient3: Any?,
    @RelationshipsObject val transient4: Any?,
    @LinksObject val transient5: Any?,
    @MetaObject val transient6: Any?,
    @ToOne("ONE") val transient7: Any?,
    @ToMany("MANY") val transient8: List<Any>?
  ) {
    @ToOne("ONE") var transient9: Any? = null
    @ToMany("MANY") var transient10: List<Any>? = null
  }

  private val json = """
      {
        "nonTransient":"value",
        "transient1":"value",
        "transient2":"value",
        "transient3":"value",
        "transient4":"value",
        "transient5":"value",
        "transient6":"value",
        "transient7":"value",
        "transient8":["value"],
        "transient9":"value",
        "transient10":["value"]
      }
    """.inlineJson()

  private val moshi = Moshi.Builder()
    .add(JsonApiFactory.Builder().build())
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val codeGenAdapter = moshi.adapter(CodeGenClass::class.java)
    .failOnUnknown()
    .serializeNulls()

  private val reflectAdapter = moshi.adapter(ReflectClass::class.java)
    .failOnUnknown()
    .serializeNulls()

  @Test
  fun `deserialize class with transient annotations using generated adapter`() {
    val deserialized = codeGenAdapter.fromJson(json) ?: fail("deserialized == null")

    assertThat(deserialized)
      .hasFieldOrPropertyWithValue("nonTransient", "value")
      .hasAllNullFieldsOrPropertiesExcept("nonTransient")
  }

  @Test
  fun `deserialize class with transient annotations using reflection adapter`() {
    val deserialized = reflectAdapter.fromJson(json) ?: fail("deserialized == null")

    assertThat(deserialized)
      .hasFieldOrPropertyWithValue("nonTransient", "value")
      .hasAllNullFieldsOrPropertiesExcept("nonTransient")
  }

  @Test
  fun `serialize class with transient annotations using generated adapter`() {
    val value = CodeGenClass(
      nonTransient = "value",
      transient1 = "value",
      transient2 = "value",
      transient3 = "value",
      transient4 = "value",
      transient5 = "value",
      transient6 = "value",
      transient7 = "value",
      transient8 = listOf("value")
    ).apply {
      transient9 = "value"
      transient10 = listOf("value")
    }

    val serialized = codeGenAdapter.toJson(value)

    assertThat(serialized).isEqualTo("""{"nonTransient":"value"}""")
  }

  @Test
  fun `serialize class with transient annotations using reflection adapter`() {
    val value = ReflectClass(
      nonTransient = "value",
      transient1 = "value",
      transient2 = "value",
      transient3 = "value",
      transient4 = "value",
      transient5 = "value",
      transient6 = "value",
      transient7 = "value",
      transient8 = listOf("value")
    ).apply {
      transient9 = "value"
      transient10 = listOf("value")
    }

    val serialized = reflectAdapter.toJson(value)

    assertThat(serialized).isEqualTo("""{"nonTransient":"value"}""")
  }
}
