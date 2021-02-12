package com.jsonapi

import com.jsonapi.JsonFile.*
import com.jsonapi.TestUtils.moshi
import com.jsonapi.model.Document
import com.jsonapi.model.Meta
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DocumentAdapterTest {
  
  private val documentAdapter: JsonAdapter<Document<ValidResource>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, ValidResource::class.java)
  )
  private val documentArticleAdapter: JsonAdapter<Document<Article>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Article::class.java)
  )
  private val documentArticleCollectionAdapter: JsonAdapter<Document<List<Article>>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Types.newParameterizedType(List::class.java, Article::class.java))
  )
  
  private lateinit var article: Article
  private lateinit var author1: Person
  private lateinit var author2: Person
  private lateinit var comment1: Comment
  private lateinit var comment2: Comment
  
  @Before
  fun setup() {
    author1 = Person("Name1", "Surname1", "@twitter1")
    author1.type = "people"
    author1.id = "1"
    
    author2 = Person("Name2", "Surname2", "@twitter2")
    author2.type = "people"
    author2.id = "2"
    
    comment1 = Comment("Comment1", author2)
    comment1.type = "comments"
    comment1.id = "1"
    
    comment2 = Comment("Comment2", author1)
    comment2.type = "comments"
    comment2.id = "2"
    
    article = Article("articles", "1", "Title1", author1, listOf(comment1, comment2))
  }
  
  // TODO should this adapter deserialize pure null ???
  
  @Test
  fun `deserialize null data document`() {
    val result = documentArticleAdapter.fromJson(read(DOCUMENT_NULL_DATA))
    assertThat(result).isInstanceOfSatisfying(Document.Data::class.java) {
      assertThat(it.data).isNull()
      assertThat(it.included).isNull()
      assertThat(it.meta).isNull()
      assertThat(it.links).isNull()
    }
  }
  
  @Test
  fun `deserialize single resource document`() {
    val result = documentArticleAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE))
    assertThat(result).isInstanceOfSatisfying(Document.Data::class.java) {
      assertThat(it.data).isNotNull
      assertThat(it.included).isNotEmpty
      assertThat(it.meta).isNotNull
      assertThat(it.links).isNotNull
    }
  }
  
  @Test
  fun `deserialize empty collection document`() {
    val result = documentArticleCollectionAdapter.fromJson(read(DOCUMENT_EMPTY_COLLECTION))
    assertThat(result).isInstanceOfSatisfying(Document.Data::class.java) {
      assertThat(it.data).asList().isEmpty()
      assertThat(it.included).isNull()
      assertThat(it.meta).isNull()
      assertThat(it.links).isNull()
    }
  }
  
  @Test
  fun `deserialize collection document`() {
    val result = documentArticleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION))
    assertThat(result).isInstanceOfSatisfying(Document.Data::class.java) {
      assertThat(it.data).asList().isNotEmpty
      assertThat(it.included).isNotEmpty
      assertThat(it.meta).isNotNull
      assertThat(it.links).isNotNull
    }
  }
  
  @Test
  fun `deserialize meta only document`() {
    val result = documentAdapter.fromJson(read(DOCUMENT_META))
    assertThat(result).isInstanceOfSatisfying(Document.Data::class.java) {
      assertThat(it.data).isNull()
      assertThat(it.included).isNull()
      assertThat(it.links).isNull()
      assertThat(it.meta).isNotNull
    }
  }
  
  @Test
  fun `deserialize errors document`() {
    val result = documentAdapter.fromJson(read(DOCUMENT_ERROR_MULTIPLE))
    assertThat(result).isInstanceOfSatisfying(Document.Errors::class.java) {
      assertThat(it.errors).isNotEmpty()
    }
  }
  
  @Test(expected = JsonDataException::class)
  fun `throw when root is not a JSON object`() {
    // A JSON object MUST be at the root of every JSON:API request and response containing data.
    // This object defines a document’s “top level”.
    documentAdapter.fromJson("[]")
  }
  
  @Test
  fun `serialize null data document`() {
    val document = Document.Data<ValidResource>(null)
    val serialized = documentAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":null}""")
  }
  
  @Test
  fun `serialize meta document`() {
    // meta document has only meta, data is null
    val document = Document.Data<ValidResource>(
      data = null,
      meta = Meta(mapOf("name" to "value"))
    )
    val serialized = documentAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"meta":{"name":"value"}}""")
  }
  
  @Test
  fun `serialize single resource document`() {
    val expected = read(DOCUMENT_ARTICLE_SINGLE_SIMPLE, true)
    val document = Document.Data(article)
    val serialized = documentArticleAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `serialize empty collection document`() {
    val document = Document.Data(emptyList<Article>())
    val serialized = documentArticleCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[]}""")
  }
  
  @Test
  fun `serialize collection document`() {
    val expected = read(DOCUMENT_ARTICLE_COLLECTION_SIMPLE, true)
    val document = Document.Data(listOf(article))
    val serialized = documentArticleCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `serialize errors document`() {
    // TODO error serialization test
  }
}