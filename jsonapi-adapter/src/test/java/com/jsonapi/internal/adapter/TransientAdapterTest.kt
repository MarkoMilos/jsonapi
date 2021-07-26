package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiFactory
import com.jsonapi.inlineJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import jsonapi.BindRelationship
import jsonapi.ResourceId
import jsonapi.ResourceLid
import jsonapi.ResourceLinks
import jsonapi.ResourceMeta
import jsonapi.ResourceRelationships
import jsonapi.ResourceType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class TransientAdapterTest {

  @JsonClass(generateAdapter = true)
  class TransientTestClass(
    val nonTransient: Any? = null,
    @ResourceType val transient1: Any? = null,
    @ResourceId val transient2: Any? = null,
    @ResourceLid val transient3: Any? = null,
    @ResourceRelationships val transient4: Any? = null,
    @ResourceLinks val transient5: Any? = null,
    @ResourceMeta val transient6: Any? = null,
    @BindRelationship("author") val transient7: Any? = null
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
        "transient7":"value"
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
      transient7 = "value"
    )

    val serialized = adapter.toJson(value)

    assertThat(serialized).isEqualTo("""{"nonTransient":"value"}""")
  }
}
