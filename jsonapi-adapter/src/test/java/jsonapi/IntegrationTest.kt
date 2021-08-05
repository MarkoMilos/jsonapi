package jsonapi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import jsonapi.Document.IncludedSerialization.PROCESSED
import jsonapi.JsonFile.INTEGRATION_DESERIALIZE
import jsonapi.JsonFile.INTEGRATION_SERIALIZE
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test

class IntegrationTest {

  @JsonClass(generateAdapter = true)
  @Resource("people")
  data class Person(
    @Type val type: String? = null,
    @Id val id: String? = null,
    val name: String,
    val surname: String,
    val fullName: String
  )

  @JsonClass(generateAdapter = true)
  data class PersonAttributes(
    val firstName: String,
    val lastName: String
  )

  // Example custom adapter that will receive attributes and build full name field for Person object
  class CustomPersonAdapter {
    @FromJson
    fun fromJson(attributes: PersonAttributes): Person {
      return Person(
        name = attributes.firstName,
        surname = attributes.lastName,
        fullName = "${attributes.firstName}, ${attributes.lastName}"
      )
    }

    @ToJson
    fun toJson(person: Person): PersonAttributes {
      return PersonAttributes(person.name, person.surname)
    }
  }

  @JsonClass(generateAdapter = true)
  @Resource("comments")
  data class Comment(
    @Type val type: String?,
    @Id val id: String?,
    val body: String?,
    @ToOne("author") val author: Person?,
    @RelationshipsObject var relationships: Relationships?
  )

  @JsonClass(generateAdapter = true)
  @Resource("articles")
  data class Article(
    @Type val type: String?,
    @Id val id: String?,
    @Lid val lid: String?,
    val title: String?,
    val promoted: Boolean,
    val tags: List<String>?,
    val price: Double?,
    val source: Source?,
    @ToOne("author") val author: Person?,
    @ToMany("comments") val comments: List<Comment>?,
    @ToMany("related") val related: List<Article>?,
    @RelationshipsObject var relationships: Relationships?,
    @LinksObject val links: Links?,
    @MetaObject val meta: Meta?
  )

  @JsonClass(generateAdapter = true)
  data class Source(val name: String)

  private val factory = JsonApiFactory.Builder()
    .addType(Person::class.java)
    .addType(Comment::class.java)
    .addType(Article::class.java)
    .build()

  private val moshi = Moshi.Builder()
    .add(factory)
    .add(CustomPersonAdapter())
    .build()

  private val adapter: JsonAdapter<Document<List<Article>>> = moshi.adapter(
    Types.newParameterizedType(Document::class.java, Types.newParameterizedType(List::class.java, Article::class.java))
  )

  private lateinit var person1: Person
  private lateinit var person2: Person
  private lateinit var person3: Person
  private lateinit var person4: Person

  private lateinit var comment1: Comment
  private lateinit var comment2: Comment
  private lateinit var comment3: Comment
  private lateinit var comment4: Comment
  private lateinit var comment5: Comment

  private lateinit var article1: Article
  private lateinit var article2: Article
  private lateinit var article3: Article
  private lateinit var article4: Article
  private lateinit var article5: Article

  private lateinit var links: Links
  private lateinit var meta: Meta
  private lateinit var jsonApiObject: JsonApiObject

  @Before
  fun setup() {
    // SETUP OBJECT TO MATCH JSON REPRESENTATION IN ORDER TO USE EQUAL OPERATOR FOR ASSERTIONS

    // Persons
    person1 = Person("people", "1", "Name1", "Surname1", "Name1, Surname1")
    person2 = Person("people", "2", "Name2", "Surname2", "Name2, Surname2")
    person3 = Person("people", "3", "Name3", "Surname3", "Name3, Surname3")
    person4 = Person("people", "4", "Name4", "Surname4", "Name4, Surname4")

    // Comments
    val c1Relationships = Relationships("author" to Relationship.ToOne("people", "1"))
    val c2Relationships = Relationships("author" to Relationship.ToOne("people", "2"))
    val c3Relationships = Relationships("author" to Relationship.ToOne("people", "3"))
    val c4Relationships = Relationships("author" to Relationship.ToOne("people", "4"))
    val c5Relationships = Relationships("author" to Relationship.ToOne("people", "5"))

    comment1 = Comment("comments", "1", "Comment1", person1, c1Relationships)
    comment2 = Comment("comments", "2", "Comment2", person2, c2Relationships)
    comment3 = Comment("comments", "3", "Comment3", person3, c3Relationships)
    comment4 = Comment("comments", "4", "Comment4", person4, c4Relationships)
    comment5 = Comment("comments", "5", null, null, c5Relationships)

    // Articles
    article5 = Article(
      type = "articles",
      id = "5",
      lid = null,
      title = "Title5",
      promoted = false,
      tags = null,
      price = null,
      source = null,
      author = null,
      comments = null,
      related = null,
      relationships = Relationships(
        "author" to Relationship.ToOne("people", "5"),
      ),
      links = null,
      meta = null
    )

    article4 = Article(
      type = "articles",
      id = "4",
      lid = null,
      title = "Title4",
      promoted = false,
      tags = null,
      price = 1.5,
      source = null,
      author = person4,
      comments = null,
      related = listOf(article5),
      relationships = Relationships(
        "author" to Relationship.ToOne("people", "4"),
        "related" to Relationship.ToMany(
          ResourceIdentifier("articles", "5"),
          ResourceIdentifier("articles", "6")
        )
      ),
      links = null,
      meta = null
    )

    article3 = Article(
      type = "articles",
      id = "3",
      lid = null,
      title = "Title3",
      promoted = false,
      tags = null,
      price = null,
      source = null,
      author = person3,
      comments = emptyList(),
      related = listOf(article4, article5),
      relationships = Relationships(
        "author" to Relationship.ToOne("people", "3"),
        "comments" to Relationship.ToMany(emptyList()),
        "related" to Relationship.ToMany(
          ResourceIdentifier("articles", "4"),
          ResourceIdentifier("articles", "5")
        )
      ),
      links = null,
      meta = null
    )

    article2 = Article(
      type = "articles",
      id = "2",
      lid = null,
      title = "Title2",
      promoted = false,
      tags = emptyList(),
      price = 2.00,
      source = null,
      author = person2,
      comments = listOf(comment4, comment5),
      related = listOf(article3),
      relationships = Relationships(
        "author" to Relationship.ToOne("people", "2"),
        "comments" to Relationship.ToMany(
          ResourceIdentifier("comments", "4"),
          ResourceIdentifier("comments", "5"),
          ResourceIdentifier("comments", "6")
        ),
        "related" to Relationship.ToMany(ResourceIdentifier("articles", "3"))
      ),
      links = null,
      meta = null
    )

    article1 = Article(
      type = "articles",
      id = "1",
      lid = null,
      title = "Title1",
      promoted = true,
      tags = listOf("new", "hot"),
      price = 10.00,
      source = Source("value"),
      author = person1,
      comments = listOf(comment1, comment2, comment3),
      related = listOf(article2, article3),
      relationships = Relationships(
        // Relationship with links and meta
        "author" to Relationship.ToOne(
          // Resource identifier with meta
          ResourceIdentifier("people", "1", null, Meta("name" to "value")),
          Links("related" to Link.LinkObject("href")),
          Meta(
            "number" to 1.5,
            "string" to "value"
          )
        ),
        "comments" to Relationship.ToMany(
          ResourceIdentifier("comments", "1"),
          ResourceIdentifier("comments", "2"),
          ResourceIdentifier("comments", "3")
        ),
        "related" to Relationship.ToMany(
          ResourceIdentifier("articles", "2"),
          ResourceIdentifier("articles", "3")
        )
      ),
      links = Links(
        "prev" to null,
        "next" to Link.URI("next"),
        "related" to Link.LinkObject(
          href = "href",
          rel = "rel",
          title = "title",
          type = "type",
          hreflang = listOf("en")
        )
      ),
      meta = Meta(
        "number" to 1.5,
        "string" to "value"
      )
    )

    // Document level links
    links = Links(
      "self" to Link.URI("self"),
      "related" to Link.LinkObject(
        href = "href",
        rel = "rel",
        describedBy = Link.LinkObject("href"),
        title = "title",
        type = "type",
        hreflang = listOf("en", "es"),
        meta = Meta("name" to "value")
      ),
      "next" to null,
      "prev" to null,
    )

    // Document level meta
    meta = Meta(
      "number" to 1.5,
      "string" to "value",
      "boolean" to true,
      "array" to listOf("one", "two", "three"),
      "null" to null,
      "nested" to mapOf("foo" to "bar")
    )

    // Document level json api info
    jsonApiObject = JsonApiObject("1", listOf("ext-1"), listOf("profile-1", "profile-2"), null)
  }

  @Test
  fun `deserialization integration test`() {
    val document = adapter.fromJson(read(INTEGRATION_DESERIALIZE)) ?: fail("deserialized document == null")

    // Document methods
    assertThat(document.hasData()).isTrue
    assertThat(document.hasErrors()).isFalse
    assertThat(document.hasMeta()).isTrue
    assertThat(document.dataOrNull()).isNotNull
    assertThat(document.dataOrThrow()).isNotNull
    assertThat(document.requireData()).isNotNull
    assertThat(document.dataOrDefault(listOf())).isNotEmpty
    assertThat(document.dataOrElse { listOf() }).isNotEmpty
    assertThat(document.errorsOrEmpty()).isEmpty()
    document.throwIfErrors() // Should not throw

    // Document primary data
    assertThat(document.data).containsExactly(article1, article2)

    // Document included data
    assertThat(document.included).containsExactly(
      article3,
      article4,
      article5,
      comment1,
      comment2,
      comment3,
      comment4,
      comment5,
      person1,
      person2,
      person3,
      person4
    )

    // Document errors (not present)
    assertThat(document.errors).isNull()

    // Document Links
    assertThat(document.links?.isEmpty()).isFalse
    assertThat(document.links?.size()).isEqualTo(4)
    assertThat(document.links?.has("self")).isTrue
    assertThat(document.links?.hasNonNull("related")).isTrue
    assertThat(document.links?.previous()).isNull()
    assertThat(document.links?.next()).isNull()
    assertThat(document.links).isEqualTo(links)

    // Document Meta
    assertThat(document.meta?.members).containsOnlyKeys("number", "string", "boolean", "array", "null", "nested")
    assertThat(document.meta?.number("number")).isEqualTo(1.5)
    assertThat(document.meta?.string("string")).isEqualTo("value")
    assertThat(document.meta?.boolean("boolean")).isTrue
    assertThat(document.meta?.list<String>("array")).containsExactly("one", "two", "three")
    assertThat(document.meta?.get("null")).isNull()
    assertThat(document.meta?.get("nested")).isEqualTo(mapOf("foo" to "bar"))
    assertThat(document.meta).isEqualTo(meta)

    // Document json api info
    assertThat(document.jsonapi).isEqualTo(jsonApiObject)
  }

  @Test
  fun `serialization integration test`() {
    // Set relationships of Comment resources to null since these will be created during serialization
    comment1.relationships = null
    comment2.relationships = null
    comment3.relationships = null
    comment4.relationships = null
    comment5.relationships = null

    // Set single (author) relationship for Article1 having links and meta both on relationship level
    // and resource identifier level. This will override relationship processed from fields.
    article1.relationships = Relationships(
      "author" to Relationship.ToOne(
        // Resource identifier with meta
        ResourceIdentifier("people", "1", null, Meta("name" to "value")),
        Links("related" to Link.LinkObject("href")),
        Meta(
          "number" to 1.5,
          "string" to "value"
        )
      ),
    )

    // Set relationships of other Article resources to null since these will be created during serialization
    article2.relationships = null
    article3.relationships = null
    article4.relationships = null
    article5.relationships = null

    val document = Document.Builder<List<Article>>()
      .data(listOf(article1, article2))
      .links(links)
      .meta(meta)
      .jsonapi(jsonApiObject)
      .includedSerialization(PROCESSED)
      .build()
    val serialized = adapter.toJson(document)
    val expected = read(INTEGRATION_SERIALIZE, true)

    assertThat(serialized).isEqualTo(expected)
  }
}
