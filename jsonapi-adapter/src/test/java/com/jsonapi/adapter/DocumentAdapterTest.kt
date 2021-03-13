package com.jsonapi.adapter

import com.jsonapi.Article
import com.jsonapi.JsonApiException
import com.jsonapi.TestUtils.moshi
import com.jsonapi.model.Document
import com.jsonapi.model.Meta
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DocumentAdapterTest {
  
  private val adapter: JsonAdapter<Document<*>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Article::class.java)
  )
  
  @Test(expected = JsonApiException::class)
  fun `throw when root is not a JSON object`() {
    // A JSON object MUST be at the root of every JSON:API request and response containing data.
    // This object defines a document’s “top level”.
    adapter.fromJson("[]")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when document does not contain any standard top level member`() {
    // Trying to deserialize document without data, meta or errors
    adapter.fromJson("{}")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when document contains both data and errors`() {
    // The members data and errors MUST NOT coexist in the same document
    adapter.fromJson("""{"data":{},"errors":[]}""")
  }
  
  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }
  
  @Test
  fun `deserialize to data type when top-level member data is present`() {
    val deserialized = adapter.fromJson("""{"data":null}""")
    assertThat(deserialized).isInstanceOf(Document.Data::class.java)
  }
  
  @Test
  fun `deserialize to data type when top-level member meta is present`() {
    val deserialized = adapter.fromJson("""{"meta":null}""")
    assertThat(deserialized).isInstanceOf(Document.Data::class.java)
  }
  
  @Test
  fun `deserialize to errors type when top-level member errors is present`() {
    val deserialized = adapter.fromJson("""{"errors":[]}""")
    assertThat(deserialized).isInstanceOf(Document.Errors::class.java)
  }
  
  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize to data document when data type is provided`() {
    val document = Document.Data(Article("articles", "1", "title"))
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":{"type":"articles","id":"1","attributes":{"title":"title"}}}""")
  }
  
  @Test
  fun `serialize to data document when meta type is provided`() {
    val document = Document.meta(Meta(mapOf("name" to "value")))
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"meta":{"name":"value"}}""")
  }
  
  @Test
  fun `serialize to errors document when errors type is provided`() {
    val document = Document.Errors(emptyList())
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"errors":[]}""")
  }
}