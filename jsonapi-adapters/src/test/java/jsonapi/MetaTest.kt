package jsonapi

import com.squareup.moshi.JsonClass
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class MetaTest {

  @JsonClass(generateAdapter = true)
  data class CustomMeta(
    val number: Double,
    val string: String,
    val boolean: Boolean,
    val array: List<String>,
    val nested: NestedMetaObject
  )

  @JsonClass(generateAdapter = true)
  data class NestedMetaObject(val foo: String)

  private val members = mapOf(
    "number" to 1.5,
    "string" to "value",
    "boolean" to true,
    "array" to listOf("one", "two", "three"),
    "nested" to mapOf("foo" to "bar")
  )

  private val meta = Meta(members)

  @Test
  fun `member getter returns value of defined type when value is present and matches type`() {
    val number = meta.member<Number>("number")
    val string = meta.member<String>("string")
    val boolean = meta.member<Boolean>("boolean")
    val array = meta.member<List<String>>("array")

    assertThat(number).isInstanceOf(Number::class.java).isEqualTo(1.5)
    assertThat(string).isInstanceOf(String::class.java).isEqualTo("value")
    assertThat(boolean).isInstanceOf(java.lang.Boolean::class.java).isEqualTo(true)
    assertThat(array).asList().containsExactly("one", "two", "three")
  }

  @Test
  fun `member getter returns null when value is not present or it does not match type`() {
    val number = meta.member<Number>("non-existing-key")
    val string = meta.member<String>("number")

    assertThat(number).isNull()
    assertThat(string).isNull()
  }

  @Test
  fun `type getter returns value of defined type when value is present and matches type`() {
    val number = meta.number("number")
    val string = meta.string("string")
    val boolean = meta.boolean("boolean")
    val array = meta.list<String>("array")

    assertThat(number).isInstanceOf(Number::class.java).isEqualTo(1.5)
    assertThat(string).isInstanceOf(String::class.java).isEqualTo("value")
    assertThat(boolean).isInstanceOf(java.lang.Boolean::class.java).isEqualTo(true)
    assertThat(array).asList().containsExactly("one", "two", "three")
  }

  @Test
  fun `type getter returns null when value is not present or it does not match type`() {
    val incorrectTypeValue1 = meta.number("string")
    val incorrectTypeValue2 = meta.string("number")
    val nonExistingValue = meta.boolean("non-existing-key")

    assertThat(incorrectTypeValue1).isNull()
    assertThat(incorrectTypeValue2).isNull()
    assertThat(nonExistingValue).isNull()
  }

  @Test
  fun `map converts meta to target type`() {
    val customMeta = meta.map<CustomMeta>() ?: fail("mapped meta == null")
    assertThat(customMeta.number).isEqualTo(1.5)
    assertThat(customMeta.string).isEqualTo("value")
    assertThat(customMeta.boolean).isEqualTo(true)
    assertThat(customMeta.array).containsExactly("one", "two", "three")
    assertThat(customMeta.nested).isEqualTo(NestedMetaObject("bar"))
  }

  @Test
  fun `static builder creates meta from target object`() {
    val customMeta = CustomMeta(
      number = 1.5,
      string = "value",
      boolean = true,
      array = listOf("one", "two", "three"),
      nested = NestedMetaObject("bar")
    )
    val meta = Meta.from(customMeta)
    assertThat(meta.members).containsExactlyEntriesOf(members)
  }
}
