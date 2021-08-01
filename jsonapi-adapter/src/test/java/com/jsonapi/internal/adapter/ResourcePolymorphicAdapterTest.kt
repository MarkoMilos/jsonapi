package com.jsonapi.internal.adapter

import com.jsonapi.JsonApiFactory
import com.jsonapi.JsonFile.RESOURCE_ARTICLE
import com.jsonapi.JsonFile.RESOURCE_COMMENT
import com.jsonapi.JsonFormatException
import com.jsonapi.ResourceIdentifier
import com.jsonapi.ResourceObject
import com.jsonapi.internal.PolymorphicResource
import com.jsonapi.read
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import jsonapi.Resource
import jsonapi.ResourceId
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResourcePolymorphicAdapterTest {

  @JsonClass(generateAdapter = true)
  @Resource("articles")
  class Article(
    @ResourceId val id: String? = null,
    val title: String? = null
  )

  private val factory = JsonApiFactory.Builder()
    .addType(Article::class.java)
    .build()

  private val moshi = Moshi.Builder()
    .add(factory)
    .build()

  private val adapter = moshi.adapter<Any>(Any::class.java, PolymorphicResource::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize registered resource to registered type`() {
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized).isInstanceOf(Article::class.java)
  }

  @Test
  fun `deserialize unregistered type as resource object`() {
    val deserialized = adapter.fromJson(read(RESOURCE_COMMENT))
      ?: throw AssertionError("deserialized == null")
    assertThat(deserialized).isInstanceOf(ResourceObject::class.java)
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource without type`() {
    adapter.fromJson("""{"id":"1"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when deserializing non json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize resource of registered type`() {
    val article = Article(id = "1", title = "Title")
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1","attributes":{"title":"Title"}}""")
  }

  @Test
  fun `serialize resource of resource object type`() {
    val resourceObject = ResourceObject(type = "articles", id = "1")
    val serialized = adapter.toJson(resourceObject)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }

  @Test
  fun `serialize resource of resource identifier type`() {
    val resourceIdentifier = ResourceIdentifier(type = "articles", id = "1")
    val serialized = adapter.toJson(resourceIdentifier)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when serializing resource of expected type`() {
    class UnregisteredResource

    val resource = UnregisteredResource()
    adapter.toJson(resource)
  }
}
