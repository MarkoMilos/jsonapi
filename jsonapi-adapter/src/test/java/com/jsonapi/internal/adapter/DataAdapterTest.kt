package com.jsonapi.internal.adapter

import com.jsonapi.*
import com.jsonapi.Document
import com.jsonapi.Document.Data
import com.jsonapi.JsonFile.*
import com.jsonapi.TestUtils.moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DataAdapterTest {
  
  private val articleAdapter: JsonAdapter<Data<Article>> = moshi.adapter(
    Types.newParameterizedTypeWithOwner(Document::class.java, Data::class.java, Article::class.java)
  )
  
  private val articleCollectionAdapter: JsonAdapter<Data<List<Article>>> = moshi.adapter(
    Types.newParameterizedTypeWithOwner(
      Document::class.java,
      Data::class.java,
      Types.newParameterizedType(List::class.java, Article::class.java)
    )
  )
  
  private val resourceIdentifierAdapter: JsonAdapter<Data<ResourceIdentifier>> = moshi.adapter(
    Types.newParameterizedTypeWithOwner(Document::class.java, Data::class.java, ResourceIdentifier::class.java)
  )
  
  private val resourceIdentifierCollectionAdapter: JsonAdapter<Data<List<ResourceIdentifier>>> = moshi.adapter(
    Types.newParameterizedTypeWithOwner(
      Document::class.java,
      Data::class.java,
      Types.newParameterizedType(List::class.java, ResourceIdentifier::class.java)
    )
  )
  
  private val nothingAdapter: JsonAdapter<Data<Nothing>> = moshi.adapter(
    Types.newParameterizedTypeWithOwner(Document::class.java, Data::class.java, Nothing::class.java)
  )
  
  private lateinit var article: Article
  private lateinit var author1: Person
  private lateinit var author2: Person
  private lateinit var comment1: Comment
  private lateinit var comment2: Comment
  
  @Before
  fun setup() {
    author1 = Person("people", "1", "Name1", "Surname1", "@twitter1")
    author2 = Person("people", "2", "Name2", "Surname2", "@twitter2")
    
    comment1 = Comment("comments", "1", "Comment1", author2)
    comment2 = Comment("comments", "2", "Comment2", author1)
    
    article = Article("articles", "1", "Title1", author1, listOf(comment1, comment2))
  }
  
  @Test
  fun `deserialize null`() {
    val deserialized = articleAdapter.fromJson("null")
    assertThat(deserialized).isNull()
  }
  
  @Test
  fun `deserialize null data document`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_NULL_DATA))
    assertThat(deserialized).hasAllNullFieldsOrProperties()
  }
  
  @Test
  fun `deserialize single resource document`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE))
    assertThat(deserialized?.data).isNotNull
    assertThat(deserialized?.included).isNotNull
    assertThat(deserialized?.links).isNotNull
    assertThat(deserialized?.meta).isNotNull
  }
  
  @Test
  fun `deserialized single resource document is bound`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE))
    assertThat(deserialized?.data?.author).isNotNull
    assertThat(deserialized?.data?.comments).asList().isNotEmpty
  }
  
  @Test
  fun `deserialize singe resource identifier document`() {
    val deserialized = resourceIdentifierAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE_SIMPLE))
    val resourceIdentifier = deserialized?.data
    assertThat(resourceIdentifier).isNotNull
    assertThat(resourceIdentifier?.type).isEqualTo("articles")
    assertThat(resourceIdentifier?.id).isEqualTo("1")
    assertThat(resourceIdentifier?.lid).isNull()
    assertThat(resourceIdentifier?.meta).isNull()
  }
  
  @Test
  fun `deserialize empty collection document`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_EMPTY_COLLECTION))
    assertThat(deserialized).isNotNull
    assertThat(deserialized?.data).asList().isEmpty()
    assertThat(deserialized?.included).isNull()
    assertThat(deserialized?.meta).isNull()
    assertThat(deserialized?.links).isNull()
  }
  
  @Test
  fun `deserialize resource collection document`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION))
    assertThat(deserialized?.data).asList().isNotEmpty
    assertThat(deserialized?.included).isNotNull
    assertThat(deserialized?.links).isNotNull
    assertThat(deserialized?.meta).isNotNull
  }
  
  @Test
  fun `deserialized resource collection document is bound`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION))
    assertThat(deserialized?.data?.first()?.author).isNotNull
    assertThat(deserialized?.data?.first()?.comments).asList().isNotEmpty
  }
  
  @Test
  fun `deserialize resource identifier collection document`() {
    val deserialized = resourceIdentifierCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION_SIMPLE))
    val resourceIdentifier = deserialized?.data?.first()
    assertThat(resourceIdentifier).isNotNull
    assertThat(resourceIdentifier?.type).isEqualTo("articles")
    assertThat(resourceIdentifier?.id).isEqualTo("1")
    assertThat(resourceIdentifier?.lid).isNull()
    assertThat(resourceIdentifier?.meta).isNull()
  }
  
  @Test
  fun `deserialize meta only document`() {
    val deserialized = nothingAdapter.fromJson(read(DOCUMENT_META))
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("meta")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when root is not a JSON object`() {
    // A JSON object MUST be at the root of every JSON:API request and response containing data.
    // This object defines a document’s “top level”.
    articleAdapter.fromJson("[]")
  }
  
  @Test
  fun `serialize null`() {
    val serialized = articleAdapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize null data document`() {
    val document = Data<Article>(null)
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":null}""")
  }
  
  @Test
  fun `serialize single resource document`() {
    val expected = read(DOCUMENT_ARTICLE_SINGLE_SIMPLE, true)
    val document = Data(article)
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `serialize single resource identifier document`() {
    val document = Data(ResourceIdentifier("articles", "1", "1"))
    val serialized = resourceIdentifierAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":{"type":"articles","id":"1","lid":"1"}}""")
  }
  
  @Test
  fun `serialize empty collection document`() {
    val document = Data(emptyList<Article>())
    val serialized = articleCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[]}""")
  }
  
  @Test
  fun `serialize resource collection document`() {
    val expected = read(DOCUMENT_ARTICLE_COLLECTION_SIMPLE, true)
    val document = Data(listOf(article))
    val serialized = articleCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `serialize resource identifier collection document`() {
    val document = Data(listOf(ResourceIdentifier("articles", "1", "1")))
    val serialized = resourceIdentifierCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[{"type":"articles","id":"1","lid":"1"}]}""")
  }
  
  @Test
  fun `serialize meta document`() {
    // Meta document has only meta, data is null
    val document = Data(
      data = null,
      meta = Meta(mapOf("name" to "value"))
    )
    val serialized = nothingAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"meta":{"name":"value"}}""")
  }
  
  @Test
  fun `resource relations are unchanged after serialization`() {
    val document = Data(article)
    articleAdapter.toJson(document)
    assertThat(article.author).isEqualTo(author1)
    assertThat(article.comments).containsExactly(comment1, comment2)
    assertThat(comment1.author).isEqualTo(author2)
    assertThat(comment2.author).isEqualTo(author1)
  }
}