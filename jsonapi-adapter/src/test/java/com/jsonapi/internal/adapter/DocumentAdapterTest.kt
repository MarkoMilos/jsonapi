package com.jsonapi.internal.adapter

import com.jsonapi.*
import com.jsonapi.JsonFile.*
import com.jsonapi.TestUtils.moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test

class DocumentAdapterTest {
  
  private val articleAdapter: JsonAdapter<Document<Article>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Article::class.java)
  )
  
  private val articleCollectionAdapter: JsonAdapter<Document<List<Article>>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Types.newParameterizedType(List::class.java, Article::class.java))
  )
  
  private val resourceIdentifierAdapter: JsonAdapter<Document<ResourceIdentifier>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, ResourceIdentifier::class.java)
  )
  
  private val resourceIdentifierCollectionAdapter: JsonAdapter<Document<List<ResourceIdentifier>>> = moshi.adapter(
    Types.newParameterizedType(
      Document::class.java,
      Types.newParameterizedType(List::class.java, ResourceIdentifier::class.java)
    )
  )
  
  private val nothingAdapter: JsonAdapter<Document<Nothing>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Nothing::class.java)
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
  
  @Test(expected = JsonApiException::class)
  fun `throw when root is not a JSON object`() {
    // A JSON object MUST be at the root of every JSON:API request and response containing data.
    // This object defines a document’s “top level”.
    articleAdapter.fromJson("[]")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when document does not contain any standard top level member`() {
    // Trying to deserialize document without data, meta or errors
    articleAdapter.fromJson("{}")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when document contains both data and errors`() {
    // The members data and errors MUST NOT coexist in the same document
    articleAdapter.fromJson("""{"data":{},"errors":[]}""")
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
    assertThat(deserialized?.errors).isNull()
  }
  
  @Test
  fun `deserialized single resource document is bound`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE))
    assertThat(deserialized?.data?.author).isEqualTo(author1)
    assertThat(deserialized?.data?.comments).asList().containsExactly(comment1, comment2)
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
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("data")
  }
  
  @Test
  fun `deserialize resource collection document`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION))
    assertThat(deserialized?.data).asList().isNotEmpty
    assertThat(deserialized?.included).isNotNull
    assertThat(deserialized?.links).isNotNull
    assertThat(deserialized?.meta).isNotNull
    assertThat(deserialized?.errors).isNull()
  }
  
  @Test
  fun `deserialized resource collection document is bound`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION))
    val primaryResource = deserialized?.data?.first() ?: fail("primary resource is null")
    assertThat(primaryResource.author).isEqualTo(author1)
    assertThat(primaryResource.comments).asList().containsExactly(comment1, comment2)
  }
  
  @Test
  fun `deserialize resource identifier collection document`() {
    val deserialized = resourceIdentifierCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION_SIMPLE))
    val resourceIdentifier = deserialized?.data?.first() ?: fail("resource identifier is null")
    assertThat(resourceIdentifier.type).isEqualTo("articles")
    assertThat(resourceIdentifier.id).isEqualTo("1")
    assertThat(resourceIdentifier.lid).isNull()
    assertThat(resourceIdentifier.meta).isNull()
  }
  
  @Test
  fun `deserialize meta only document`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_META))
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("meta")
  }
  
  @Test
  fun `deserialize empty errors`() {
    val deserialized = articleAdapter.fromJson("""{"errors":[]}""")
    assertThat(deserialized?.errors).isEmpty()
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("errors")
  }
  
  @Test
  fun `deserialize non empty errors`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_ERROR))
    
    val errors = deserialized?.errors ?: fail("errors are null")
    val error1 = errors[0]
    val error2 = errors[1]
    val error3 = errors[2]
    
    assertThat(errors).hasSize(3)
    assertThat(error1).hasNoNullFieldsOrProperties()
    assertThat(error1.source).hasNoNullFieldsOrProperties()
    assertThat(error2).hasAllNullFieldsOrProperties()
    assertThat(error3).hasAllNullFieldsOrPropertiesExcept("id", "links", "source")
    assertThat(error3.source).hasAllNullFieldsOrProperties()
  }
  
  @Test
  fun `serialize null`() {
    val serialized = articleAdapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize null data document`() {
    val document = Document<Article>(null)
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":null}""")
  }
  
  @Test
  fun `serialize null data document with links`() {
    val document = Document<Article>(null, links = Links(mapOf("related" to Link.LinkURI("uri"))))
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":null,"links":{"related":"uri"}}""")
  }
  
  @Test
  fun `serialize single resource document`() {
    val expected = read(DOCUMENT_ARTICLE_SINGLE_SIMPLE, true)
    val document = Document(article)
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `serialize single resource identifier document`() {
    val document = Document(ResourceIdentifier("articles", "1", "1"))
    val serialized = resourceIdentifierAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":{"type":"articles","id":"1","lid":"1"}}""")
  }
  
  @Test
  fun `serialize empty collection document`() {
    val document = Document(emptyList<Article>())
    val serialized = articleCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[]}""")
  }
  
  @Test
  fun `serialize resource collection document`() {
    val expected = read(DOCUMENT_ARTICLE_COLLECTION_SIMPLE, true)
    val document = Document(listOf(article))
    val serialized = articleCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `serialize resource identifier collection document`() {
    val identifiers = listOf(ResourceIdentifier("articles", "1", "1"))
    val document = Document(identifiers)
    val serialized = resourceIdentifierCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[{"type":"articles","id":"1","lid":"1"}]}""")
  }
  
  @Test
  fun `serialize document without included`() {
    val expected = read(DOCUMENT_ARTICLE_SINGLE_SIMPLE_NO_INCLUDED, true)
    val document = Document.with(article).serializeIncluded(false).build()
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `serialize meta document`() {
    // Meta document has only meta, data is null
    val document = Document(
      data = null,
      meta = Meta(mapOf("name" to "value"))
    )
    val serialized = nothingAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"meta":{"name":"value"}}""")
  }
  
  @Test
  fun `serialize empty errors`() {
    val document = Document(data = null, errors = emptyList())
    val serialized = nothingAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"errors":[]}""")
  }
  
  @Test
  fun `serialize non empty errors`() {
    val errors = listOf(Error(title = "Title", detail = "Detail"))
    val document = Document(data = null, errors = errors)
    val serialized = nothingAdapter.toJson(document)
    val expected = read(DOCUMENT_ERROR_SIMPLE, true)
    assertThat(serialized).isEqualTo(expected)
  }
  
  @Test
  fun `resource relations are unchanged after serialization`() {
    val document = Document(article)
    articleAdapter.toJson(document)
    assertThat(article.author).isEqualTo(author1)
    assertThat(article.comments).containsExactly(comment1, comment2)
    assertThat(comment1.author).isEqualTo(author2)
    assertThat(comment2.author).isEqualTo(author1)
  }
}