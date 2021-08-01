package jsonapi.internal.adapter

import jsonapi.Document
import jsonapi.Document.IncludedSerialization
import jsonapi.Error
import jsonapi.JsonFormatException
import jsonapi.JsonApiFactory
import jsonapi.JsonApiObject
import jsonapi.JsonFile.DOCUMENT_ARTICLE_COLLECTION
import jsonapi.JsonFile.DOCUMENT_ARTICLE_COLLECTION_DUPLICATED_RESOURCES
import jsonapi.JsonFile.DOCUMENT_ARTICLE_COLLECTION_SERIALIZED
import jsonapi.JsonFile.DOCUMENT_ARTICLE_SINGLE
import jsonapi.JsonFile.DOCUMENT_ARTICLE_SINGLE_DUPLICATED_RESOURCES
import jsonapi.JsonFile.DOCUMENT_ARTICLE_SINGLE_SERIALIZED
import jsonapi.JsonFile.DOCUMENT_ARTICLE_SINGLE_SERIALIZED_DOCUMENT_ONLY_INCLUDED
import jsonapi.JsonFile.DOCUMENT_ARTICLE_SINGLE_SERIALIZED_NO_INCLUDED
import jsonapi.JsonFile.DOCUMENT_ERROR
import jsonapi.JsonFile.DOCUMENT_META
import jsonapi.Links
import jsonapi.Meta
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.inlineJson
import jsonapi.read
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.BindRelationship
import jsonapi.Resource
import jsonapi.Id
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test

class DocumentAdapterTest {

  @JsonClass(generateAdapter = true)
  @Resource("people")
  data class Person(
    @Id val id: String?,
    val firstName: String?,
    val lastName: String?
  )

  @JsonClass(generateAdapter = true)
  @Resource("comments")
  data class Comment(
    @Id val id: String?,
    val body: String?,
    @BindRelationship("author") val author: Person? = null
  )

  @JsonClass(generateAdapter = true)
  @Resource("articles")
  data class Article(
    @Id val id: String?,
    val title: String? = null,
    @BindRelationship("author") val author: Person? = null,
    @BindRelationship("comments") val comments: List<Comment>? = null,
    @BindRelationship("related") val related: List<Article>? = null
  )

  private val factory = JsonApiFactory.Builder()
    .addType(Person::class.java)
    .addType(Article::class.java)
    .addType(Comment::class.java)
    .build()

  private val moshi = Moshi.Builder()
    .add(factory)
    .build()

  private val nothingAdapter: JsonAdapter<Document<Nothing>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Nothing::class.java)
  )

  private val articleAdapter: JsonAdapter<Document<Article>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Article::class.java)
  )

  private val articleCollectionAdapter: JsonAdapter<Document<List<Article>>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Types.newParameterizedType(List::class.java, Article::class.java))
  )

  private lateinit var article1: Article
  private lateinit var article2: Article
  private lateinit var author1: Person
  private lateinit var author2: Person
  private lateinit var comment1: Comment
  private lateinit var comment2: Comment

  @Before
  fun setup() {
    // Setup resources to match test json sample
    author1 = Person("1", "Name1", "Surname1")
    author2 = Person("2", "Name2", "Surname2")
    comment1 = Comment("1", "Comment1")
    comment2 = Comment("2", "Comment2", author1)
    article2 = Article("2", "Title2", null, emptyList(), null)
    article1 = Article("1", "Title1", author1, listOf(comment1, comment2), listOf(article2))
  }

  @Test
  fun `deserialize null`() {
    val deserialized = articleAdapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize null data document`() {
    val deserialized = articleAdapter.fromJson("""{"data":null}""") ?: fail("deserialized == null")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("includedSerialization")
  }

  @Test
  fun `deserialize singe resource identifier document`() {
    val adapter = moshi.adapter<Document<ResourceIdentifier>>(
      Types.newParameterizedType(Document::class.java, ResourceIdentifier::class.java)
    )

    // Primary data
    val deserialized = adapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE)) ?: fail("deserialized == null")
    val resourceIdentifier = deserialized.data ?: fail("deserialized.data == null")
    assertThat(resourceIdentifier.type).isEqualTo("articles")
    assertThat(resourceIdentifier.id).isEqualTo("1")
    assertThat(resourceIdentifier.lid).isNull()
    assertThat(resourceIdentifier.meta).isNull()

    // Other Document fields
    assertThat(deserialized.included).containsExactly(article2, comment1, comment2, author1)
    assertThat(deserialized.errors).isNull()
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
    assertThat(deserialized.jsonapi).isNotNull
  }

  @Test
  fun `deserialize singe resource object document`() {
    val adapter = moshi.adapter<Document<ResourceObject>>(
      Types.newParameterizedType(Document::class.java, ResourceObject::class.java)
    )

    // Primary data
    val deserialized = adapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE)) ?: fail("deserialized == null")
    val resourceObject = deserialized.data ?: fail("deserialized.data == null")
    assertThat(resourceObject.type).isEqualTo("articles")
    assertThat(resourceObject.id).isEqualTo("1")
    assertThat(resourceObject.lid).isNull()
    assertThat(resourceObject.relationships).isNotNull
    assertThat(resourceObject.links).isNull()
    assertThat(resourceObject.meta).isNull()

    // Other Document fields
    assertThat(deserialized.included).containsExactly(article2, comment1, comment2, author1)
    assertThat(deserialized.errors).isNull()
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
    assertThat(deserialized.jsonapi).isNotNull
  }

  @Test
  fun `deserialize single resource document`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE)) ?: fail("deserialized == null")
    assertThat(deserialized.data).isEqualTo(article1)
    assertThat(deserialized.included).containsExactly(article2, comment1, comment2, author1)
    assertThat(deserialized.errors).isNull()
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
    assertThat(deserialized.jsonapi).isNotNull
  }

  @Test
  fun `deserialized single resource document is bound`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE)) ?: fail("deserialized == null")
    val included = deserialized.included ?: fail("included == null")

    val article1 = deserialized.data ?: fail("article1 == null")
    val article2 = included[0] as? Article ?: fail("article2 == null")
    val comment1 = included[1] as? Comment ?: fail("comment1 == null")
    val comment2 = included[2] as? Comment ?: fail("comment1 == null")
    val author1 = included[3] as? Person ?: fail("author1 == null")

    // Article 1
    assertThat(article1.author).isEqualTo(author1)
    assertThat(article1.comments).containsExactly(comment1, comment2)
    assertThat(article1.related).containsExactly(article2)

    // Article 2
    assertThat(article2.author).isNull()     // not in included
    assertThat(article2.comments).isEmpty()  // empty relationship
    assertThat(article2.related).isNull()    // relationship not defined

    // Comment 1
    assertThat(comment1.author).isNull()     // not in included

    // Comment 2
    assertThat(comment2.author).isEqualTo(author1)
  }

  @Test
  fun `deserialized single resource document with duplicated resources is bound`() {
    val deserialized = articleAdapter.fromJson(read(DOCUMENT_ARTICLE_SINGLE_DUPLICATED_RESOURCES))
      ?: fail("deserialized == null")
    val included = deserialized.included ?: fail("included == null")

    val article1Primary = deserialized.data ?: fail("article1 primary == null")
    val article1Included = included[0] as? Article ?: fail("article1 included == null")
    val article2 = included[1] as? Article ?: fail("article2 == null")
    val comment1 = included[2] as? Comment ?: fail("comment1 == null")
    val comment2 = included[3] as? Comment ?: fail("comment1 == null")
    val author1 = included[4] as? Person ?: fail("author1 == null")

    // Article 1 - from primary resources
    assertThat(article1Primary.author).isEqualTo(author1)
    assertThat(article1Primary.comments).containsExactly(comment1, comment2)
    assertThat(article1Primary.related).containsExactly(article2)

    // Article 1 - from included resources
    assertThat(article1Included.author).isEqualTo(author1)
    assertThat(article1Included.comments).containsExactly(comment1, comment2)
    assertThat(article1Included.related).containsExactly(article2)

    // Article 2
    assertThat(article2.author).isNull()     // not in included
    assertThat(article2.comments).isEmpty()  // empty relationship
    assertThat(article2.related).isNull()    // relationship not defined

    // Comment 1
    assertThat(comment1.author).isNull()     // not in included

    // Comment 2
    assertThat(comment2.author).isEqualTo(author1)
  }

  @Test
  fun `deserialize empty collection document`() {
    val deserialized = articleCollectionAdapter.fromJson("""{"data":[]}""") ?: fail("deserialized == null")
    assertThat(deserialized.data).isEmpty()
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("data", "includedSerialization")
  }

  @Test
  fun `deserialize resource identifier collection document`() {
    val adapter: JsonAdapter<Document<List<ResourceIdentifier>>> = moshi.adapter(
      Types.newParameterizedType(
        Document::class.java,
        Types.newParameterizedType(List::class.java, ResourceIdentifier::class.java)
      )
    )

    val deserialized = adapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION)) ?: fail("deserialized == null")

    // Primary resource 1
    val identifier1 = deserialized.data?.get(0) ?: fail("identifier1 == null")
    assertThat(identifier1.type).isEqualTo("articles")
    assertThat(identifier1.id).isEqualTo("1")
    assertThat(identifier1.lid).isNull()
    assertThat(identifier1.meta).isNull()

    // Primary resource 2
    val identifier2 = deserialized.data?.get(1) ?: fail("identifier2 == null")
    assertThat(identifier2.type).isEqualTo("articles")
    assertThat(identifier2.id).isEqualTo("2")
    assertThat(identifier2.lid).isNull()
    assertThat(identifier2.meta).isNull()

    // Other document elements
    assertThat(deserialized.included).containsExactly(comment1, comment2, author1)
    assertThat(deserialized.errors).isNull()
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
    assertThat(deserialized.jsonapi).isNotNull
  }

  @Test
  fun `deserialize resource object collection document`() {
    val adapter: JsonAdapter<Document<List<ResourceObject>>> = moshi.adapter(
      Types.newParameterizedType(
        Document::class.java,
        Types.newParameterizedType(List::class.java, ResourceObject::class.java)
      )
    )

    val deserialized = adapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION)) ?: fail("deserialized == null")

    // Primary resource 1
    val resourceObject1 = deserialized.data?.get(0) ?: fail("identifier1 == null")
    assertThat(resourceObject1.type).isEqualTo("articles")
    assertThat(resourceObject1.id).isEqualTo("1")
    assertThat(resourceObject1.lid).isNull()
    assertThat(resourceObject1.relationships).isNotNull
    assertThat(resourceObject1.links).isNull()
    assertThat(resourceObject1.meta).isNull()

    // Primary resource 2
    val resourceObject2 = deserialized.data?.get(1) ?: fail("identifier2 == null")
    assertThat(resourceObject2.type).isEqualTo("articles")
    assertThat(resourceObject2.id).isEqualTo("2")
    assertThat(resourceObject2.lid).isNull()
    assertThat(resourceObject1.relationships).isNotNull
    assertThat(resourceObject1.links).isNull()
    assertThat(resourceObject2.meta).isNull()

    // Other document elements
    assertThat(deserialized.included).containsExactly(comment1, comment2, author1)
    assertThat(deserialized.errors).isNull()
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
    assertThat(deserialized.jsonapi).isNotNull
  }

  @Test
  fun `deserialize resource collection document`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION))
      ?: fail("deserialized == null")
    assertThat(deserialized.data).containsExactly(article1, article2)
    assertThat(deserialized.included).containsExactly(comment1, comment2, author1)
    assertThat(deserialized.errors).isNull()
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
    assertThat(deserialized.jsonapi).isNotNull
  }

  @Test
  fun `deserialized resource collection document is bound`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION))
      ?: fail("deserialized == null")

    val primary = deserialized.data ?: fail("primary == null")
    val included = deserialized.included ?: fail("included == null")

    val article1 = primary[0]
    val article2 = primary[1]
    val comment1 = included[0] as? Comment ?: fail("comment1 == null")
    val comment2 = included[1] as? Comment ?: fail("comment1 == null")
    val author1 = included[2] as? Person ?: fail("author1 == null")

    // Article 1
    assertThat(article1.author).isEqualTo(author1)
    assertThat(article1.comments).containsExactly(comment1, comment2)
    assertThat(article1.related).containsExactly(article2)

    // Article 2
    assertThat(article2.author).isNull()     // not in included
    assertThat(article2.comments).isEmpty()  // empty relationship
    assertThat(article2.related).isNull()    // relationship not defined

    // Comment 1
    assertThat(comment1.author).isNull()     // not in included

    // Comment 2
    assertThat(comment2.author).isEqualTo(author1)
  }

  @Test
  fun `deserialized resource collection document with duplicated resources is bound`() {
    val deserialized = articleCollectionAdapter.fromJson(read(DOCUMENT_ARTICLE_COLLECTION_DUPLICATED_RESOURCES))
      ?: fail("deserialized == null")

    val primary = deserialized.data ?: fail("primary == null")
    val included = deserialized.included ?: fail("included == null")

    val article1Primary = primary[0]
    val article2 = primary[1]
    val article1Included = included[0] as? Article ?: fail("article1 included == null")
    val comment1 = included[1] as? Comment ?: fail("comment1 == null")
    val comment2 = included[2] as? Comment ?: fail("comment1 == null")
    val author1 = included[3] as? Person ?: fail("author1 == null")

    // Article 1 - from primary resources
    assertThat(article1Primary.author).isEqualTo(author1)
    assertThat(article1Primary.comments).containsExactly(comment1, comment2)
    assertThat(article1Primary.related).containsExactly(article2)

    // Article 1 - from included resources
    assertThat(article1Included.author).isEqualTo(author1)
    assertThat(article1Included.comments).containsExactly(comment1, comment2)
    assertThat(article1Included.related).containsExactly(article2)

    // Article 2
    assertThat(article2.author).isNull()     // not in included
    assertThat(article2.comments).isEmpty()  // empty relationship
    assertThat(article2.related).isNull()    // relationship not defined

    // Comment 1
    assertThat(comment1.author).isNull()     // not in included

    // Comment 2
    assertThat(comment2.author).isEqualTo(author1)
  }

  @Test
  fun `deserialize meta only document`() {
    val deserialized = nothingAdapter.fromJson(read(DOCUMENT_META))
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("meta", "includedSerialization")
    assertThat(deserialized?.meta?.members).containsExactly(entry("name", "value"))
  }

  @Test
  fun `deserialize empty errors`() {
    val deserialized = nothingAdapter.fromJson("""{"errors":[]}""") ?: fail("deserialized == null")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("errors", "includedSerialization")
    assertThat(deserialized.errors).isEmpty()
  }

  @Test
  fun `deserialize non empty errors`() {
    val deserialized = nothingAdapter.fromJson(read(DOCUMENT_ERROR)) ?: fail("deserialized == null")

    val errors = deserialized.errors ?: fail("errors == null")
    assertThat(errors).hasSize(3)

    val error1 = errors[0]
    val error2 = errors[1]
    val error3 = errors[2]

    assertThat(error1).hasNoNullFieldsOrProperties()
    assertThat(error1.source).hasNoNullFieldsOrProperties()
    assertThat(error2).hasAllNullFieldsOrPropertiesExcept("id")
    assertThat(error3).hasAllNullFieldsOrPropertiesExcept("id", "links", "source")
    assertThat(error3.source).hasAllNullFieldsOrProperties()
  }

  @Test
  fun `deserialize included resources of unregistered type as resource objects`() {
    // JSON with included resources with some types (A, B) that are not registered
    val json = """
      {
        "data":{"type":"articles","id":"1"},
        "included":[
          {"type":"articles","id":"1"},
          {"type":"A","id":"1"},
          {"type":"B","id":"1"}
        ]
      }
      """.inlineJson()

    val deserialized = articleAdapter.fromJson(json) ?: fail("deserialized == null")

    assertThat(deserialized.included).containsExactly(
      Article("1"),
      ResourceObject("A", "1"),
      ResourceObject("B", "1"),
    )
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when document root is not a json object`() {
    // A JSON object MUST be at the root of every JSON:API request and response containing data.
    // This object defines a document’s “top level”.
    articleAdapter.fromJson("[]")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when document contains both data and errors`() {
    // The members data and errors MUST NOT coexist in the same document
    articleAdapter.fromJson("""{"data":{"type":"articles","id":"1"},"errors":[]}""")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when document contains included but not data`() {
    // If a document does not contain a top-level data key, the included member MUST NOT be present either
    articleAdapter.fromJson("""{"included":[]}""")
  }

  @Test
  fun `serialize null`() {
    val serialized = articleAdapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize null data document`() {
    val document = Document.empty()
    val serialized = nothingAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":null}""")
  }

  @Test
  fun `serialize null data document with links`() {
    val document = Document.Builder<Article>()
      .links(Links.from("link" to "link"))
      .build()
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"links":{"link":"link"},"data":null}""")
  }

  @Test
  fun `serialize single resource identifier document`() {
    val adapter = moshi.adapter<Document<ResourceIdentifier>>(
      Types.newParameterizedType(Document::class.java, ResourceIdentifier::class.java)
    )
    val document = Document.from(ResourceIdentifier("articles", "1", "1"))
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":{"type":"articles","id":"1","lid":"1"}}""")
  }

  @Test
  fun `serialize single resource object document`() {
    val adapter = moshi.adapter<Document<ResourceObject>>(
      Types.newParameterizedType(Document::class.java, ResourceObject::class.java)
    )
    val document = Document.from(ResourceObject("articles", "1", "1"))
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":{"type":"articles","id":"1","lid":"1"}}""")
  }

  @Test
  fun `serialize single resource document`() {
    // Document with all members (except errors)
    val document = Document.with(article1)
      .included(listOf(author2)) // Additional included not linked in via primary resource
      .links(Links.from("link" to "link"))
      .meta(Meta("name" to "value"))
      .jsonapi(JsonApiObject("1"))
      .build()
    val serialized = articleAdapter.toJson(document)
    val expected = read(DOCUMENT_ARTICLE_SINGLE_SERIALIZED, true)
    assertThat(serialized).isEqualTo(expected)
  }

  @Test
  fun `serialize empty collection document`() {
    val document = Document.from(emptyList<Article>())
    val serialized = articleCollectionAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[]}""")
  }

  @Test
  fun `serialize resource identifier collection document`() {
    val adapter = moshi.adapter<Document<List<ResourceIdentifier>>>(
      Types.newParameterizedType(
        Document::class.java,
        Types.newParameterizedType(List::class.java, ResourceIdentifier::class.java)
      )
    )
    val document = Document.from(listOf(ResourceIdentifier("articles", "1", "1")))
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[{"type":"articles","id":"1","lid":"1"}]}""")
  }

  @Test
  fun `serialize resource object collection document`() {
    val adapter = moshi.adapter<Document<List<ResourceObject>>>(
      Types.newParameterizedType(
        Document::class.java,
        Types.newParameterizedType(List::class.java, ResourceObject::class.java)
      )
    )
    val document = Document.from(listOf(ResourceObject("articles", "1", "1")))
    val serialized = adapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"data":[{"type":"articles","id":"1","lid":"1"}]}""")
  }

  @Test
  fun `serialize resource collection document`() {
    // Document with all members (except errors)
    val document = Document.with(listOf(article1, article2))
      .included(listOf(author2)) // Additional included not linked in via primary resources
      .links(Links.from("link" to "link"))
      .meta(Meta("name" to "value"))
      .jsonapi(JsonApiObject("1"))
      .build()
    val serialized = articleCollectionAdapter.toJson(document)
    val expected = read(DOCUMENT_ARTICLE_COLLECTION_SERIALIZED, true)
    assertThat(serialized).isEqualTo(expected)
  }

  @Test
  fun `serialize document configured to skip included serialization`() {
    val expected = read(DOCUMENT_ARTICLE_SINGLE_SERIALIZED_NO_INCLUDED, true)
    val document = Document.with(article1)
      .included(listOf(author2))
      .includedSerialization(IncludedSerialization.NONE)
      .build()
    val serialized = articleAdapter.toJson(document)
    assertThat(serialized).isEqualTo(expected)
  }

  @Test
  fun `serialize document configured to serialize only document defined included`() {
    val author = Person("1", "A", "B") // Shouldn't be in included
    val comment1 = Comment("1", "Comment1")         // Should be in included
    val comment2 = Comment("2", "Comment2")         // Should be in included
    val article = Article("1", "Title1", author, listOf(comment1))  // Primary resource
    val document = Document.with(article)
      .included(listOf(comment1, comment2))
      .includedSerialization(IncludedSerialization.DOCUMENT)
      .build()
    val serialized = articleAdapter.toJson(document)
    val expected = read(DOCUMENT_ARTICLE_SINGLE_SERIALIZED_DOCUMENT_ONLY_INCLUDED, true)
    assertThat(serialized).isEqualTo(expected)
  }

  @Test
  fun `serialize meta document`() {
    val document = Document.from(Meta("name" to "value"))
    val serialized = nothingAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"meta":{"name":"value"}}""")
  }

  @Test
  fun `serialize empty errors`() {
    val document = Document.from(emptyList())
    val serialized = nothingAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"errors":[]}""")
  }

  @Test
  fun `serialize non empty errors`() {
    val error = Error.Builder()
      .title("Title")
      .detail("Detail")
      .build()
    val document = Document.from(error)
    val serialized = nothingAdapter.toJson(document)
    assertThat(serialized).isEqualTo("""{"errors":[{"title":"Title","detail":"Detail"}]}""")
  }
}
