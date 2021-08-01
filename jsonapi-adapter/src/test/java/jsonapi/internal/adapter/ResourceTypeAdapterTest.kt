package jsonapi.internal.adapter

import jsonapi.JsonFormatException
import jsonapi.JsonApiFactory
import jsonapi.JsonFile.RESOURCE_ARTICLE
import jsonapi.JsonFile.RESOURCE_ARTICLE_NON_STANDARD_NAMES
import jsonapi.JsonFile.RESOURCE_COMMENT
import jsonapi.Link
import jsonapi.Links
import jsonapi.Meta
import jsonapi.Relationship
import jsonapi.Relationships
import jsonapi.ResourceIdentifier
import jsonapi.inlineJson
import jsonapi.read
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import jsonapi.BindRelationship
import jsonapi.Resource
import jsonapi.Id
import jsonapi.Lid
import jsonapi.LinksObject
import jsonapi.MetaObject
import jsonapi.RelationshipsObject
import jsonapi.Type
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class ResourceTypeAdapterTest {

  private val factory = JsonApiFactory.Builder()
    .addType(Article::class.java)
    .addType(Comment::class.java)
    .addType(Person::class.java)
    .build()

  private val moshi = Moshi.Builder()
    .add(factory)
    .build()

  private val adapter = moshi.adapter(Article::class.java)

  @Test
  fun `deserialize null`() {
    val deserialized = adapter.fromJson("null")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize basic resource`() {
    val deserialized = adapter.fromJson("""{"type":"articles","id":"1"}""") ?: fail("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "id")
  }

  @Test
  fun `deserialize resource with local identifier`() {
    val deserialized = adapter.fromJson("""{"type":"articles","lid":"1"}""") ?: fail("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.lid).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "lid")
  }

  @Test
  fun `deserialize resource with attributes`() {
    val deserialized = adapter.fromJson("""{"type":"articles","id":"1","attributes":{"title":"Title"}}""")
      ?: fail("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized.title).isEqualTo("Title")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "id", "title")
  }

  @Test
  fun `deserialize resource with resource object members`() {
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE)) ?: fail("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized.lid).isNull()
    assertThat(deserialized.title).isEqualTo("Title")
    assertThat(deserialized.relationships?.members).hasSize(2)
    assertThat(deserialized.links).isNotNull
    assertThat(deserialized.meta).isNotNull
  }

  @Test
  fun `deserialize resource without resource object members`() {
    val adapter = moshi.adapter(SimpleArticle::class.java)
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE)) ?: fail("deserialized == null")
    assertThat(deserialized.title).isEqualTo("Title")
    assertThat(deserialized).hasNoNullFieldsOrPropertiesExcept("title")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource without type`() {
    adapter.fromJson("""{"id":"1"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource with invalid type`() {
    adapter.fromJson("""{"type":null,"id":"1"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource without id or lid`() {
    adapter.fromJson("""{"type":"articles"}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource with invalid id`() {
    adapter.fromJson("""{"type":"articles","id":""}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw on deserializing resource with invalid lid`() {
    adapter.fromJson("""{"type":"articles","lid":""}""")
  }

  @Test(expected = JsonFormatException::class)
  fun `throw when deserializing non json object`() {
    adapter.fromJson("[]")
  }

  @Test
  fun `ignore non standard json names`() {
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE_NON_STANDARD_NAMES)) ?: fail("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized).hasAllNullFieldsOrPropertiesExcept("type", "id")
  }

  @Test
  fun `deserialization delegates attributes member to adapter down the chain`() {
    // Custom adapter that attributes object will be delegated to
    class CustomAdapter : JsonAdapter<Article>() {
      override fun fromJson(reader: JsonReader): Article {
        // For purpose of test just create a new object without reading
        reader.skipValue()
        return Article(type = "OVERRIDDEN", id = "OVERRIDDEN", title = "CUSTOM")
      }

      override fun toJson(writer: JsonWriter, value: Article?) {
        // Ignored for this test
      }
    }

    val factory = JsonApiFactory.Builder()
      .addType(Article::class.java)
      .build()
    val moshi = Moshi.Builder()
      .add(factory)
      .add(Article::class.java, CustomAdapter())
      .build()
    val adapter = moshi.adapter(Article::class.java)
    val deserialized = adapter.fromJson("""{"type":"articles","id":"1","attributes":{}}""")
      ?: fail("deserialized == null")
    assertThat(deserialized.type).isEqualTo("articles")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized.title).isEqualTo("CUSTOM")
  }

  @Test
  fun `deserialization respects null from adapter down the chain`() {
    // Custom adapter that attributes object will be delegated to
    class CustomAdapter : JsonAdapter<Article>() {
      override fun fromJson(reader: JsonReader): Article? {
        reader.skipValue()
        return null
      }

      override fun toJson(writer: JsonWriter, value: Article?) {
        // Ignored for this test
      }
    }

    val factory = JsonApiFactory.Builder()
      .addType(Article::class.java)
      .build()
    val moshi = Moshi.Builder()
      .add(factory)
      .add(Article::class.java, CustomAdapter())
      .build()
    val adapter = moshi.adapter(Article::class.java)
    val deserialized = adapter.fromJson("""{"type":"articles","id":"1","attributes":{}}""")
    assertThat(deserialized).isNull()
  }

  @Test
  fun `deserialize resource of non matching type when strict type checking is disabled`() {
    // Strict types are disabled and both Article and Comment types are registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(false)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // Use article adapter to deserialize comment json resource
    val deserialized = adapter.fromJson(read(RESOURCE_COMMENT)) ?: fail("deserialized == null")
    assertThat(deserialized.type).isEqualTo("comments")
    assertThat(deserialized.id).isEqualTo("1")
    assertThat(deserialized.title).isNullOrEmpty()
  }

  @Test(expected = AssertionError::class)
  fun `throw on deserializing non matching type when strict type checking is enabled`() {
    // Strict types are enabled and both Article and Comment types are registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(true)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // Use article adapter to deserialize comment json resource
    adapter.fromJson(read(RESOURCE_COMMENT))
  }

  @Test
  fun `serialize null`() {
    val serialized = adapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }

  @Test
  fun `serialize basic resource`() {
    val article = Article("articles", "1", null)
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }

  @Test
  fun `serialize resource with local identifier`() {
    val article = Article("articles", lid = "1")
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","lid":"1"}""")
  }

  @Test
  fun `serialize resource with attributes`() {
    val article = Article(type = "articles", id = "1", title = "Title")
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1","attributes":{"title":"Title"}}""")
  }

  @Test
  fun `serialize resource with resource object members`() {
    val author1 = Person("people", "1", "Name1", "Surname1")
    val author2 = Person("people", "2", "Name2", "Surname2")

    val comment1 = Comment("comments", "1", "Comment1", author2)
    val comment2 = Comment("comments", "2", "Comment2", author1)

    val article = Article(
      type = "articles",
      id = "1",
      title = "Title",
      author = author1,                      // to-one non-empty
      comments = listOf(comment1, comment2), // to-many non-empty
      related = null,                        // to-many empty
      links = Links("self" to Link.URI("self")),
      meta = Meta("name" to "value")
    )

    val expected = read(RESOURCE_ARTICLE, true)
    val serialized = adapter.toJson(article)

    assertThat(serialized).isEqualTo(expected)
  }

  @Test
  fun `serialize resource with relationships defined in relationships object`() {
    val relationships = Relationships(
      "author" to Relationship.ToOne(ResourceIdentifier("people", "1")),
      "comments" to Relationship.ToMany(
        listOf(
          ResourceIdentifier("comments", "1"),
          ResourceIdentifier("comments", "2"),
        )
      )
    )

    val article = Article(
      type = "articles",
      id = "1",
      title = "Title",
      relationships = relationships,
      links = Links("self" to Link.URI("self")),
      meta = Meta("name" to "value")
    )

    val expected = read(RESOURCE_ARTICLE, true)
    val serialized = adapter.toJson(article)

    assertThat(serialized).isEqualTo(expected)
  }

  @Test
  fun `serialize resource without type field`() {
    val simpleArticle = SimpleArticle("1", "Title")
    val adapter = moshi.adapter(SimpleArticle::class.java)
    val serialized = adapter.toJson(simpleArticle)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1","attributes":{"title":"Title"}}""")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when serializing resource with invalid type`() {
    val article = Article(type = "", id = "1")
    adapter.toJson(article)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when serializing resource without id or lid`() {
    val article = Article(type = "articles", id = null, lid = null)
    adapter.toJson(article)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when serializing resource with invalid id`() {
    val article = Article(type = "articles", id = "", lid = null)
    adapter.toJson(article)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when serializing resource with invalid lid`() {
    val article = Article(type = "articles", id = null, lid = "")
    adapter.toJson(article)
  }

  @Test
  fun `serialization delegates resource value to adapter down the chain`() {
    // Custom adapter that attributes object will be delegated to
    class CustomAdapter : JsonAdapter<Article>() {
      override fun fromJson(reader: JsonReader): Article? {
        return null // Ignored for this test
      }

      override fun toJson(writer: JsonWriter, value: Article?) {
        // Custom serialization
        val title = value?.title + "-by-" + value?.author?.firstName
        writer.beginObject().name("title").value(title).endObject()
      }
    }

    val factory = JsonApiFactory.Builder()
      .addType(Article::class.java)
      .build()
    val moshi = Moshi.Builder()
      .add(factory)
      .add(Article::class.java, CustomAdapter())
      .build()
    val adapter = moshi.adapter(Article::class.java)
    val author = Person("people", "1", "Name", "Surname")
    val article = Article(type = "articles", id = "1", title = "Title", author = author)
    val serialized = adapter.toJson(article)

    assertThat(serialized).isEqualTo(
      """
      {
      "type":"articles",
      "id":"1",
      "relationships":{"author":{"data":{"type":"people","id":"1"}}},
      "attributes":{"title":"Title-by-Name"}
      }
      """.inlineJson()
    )
  }

  @Test
  fun `serialization omits null from adapter down the chain`() {
    // Custom adapter that attributes object will be delegated to
    class CustomAdapter : JsonAdapter<Article>() {
      override fun fromJson(reader: JsonReader): Article? {
        return null // Ignored for this test
      }

      override fun toJson(writer: JsonWriter, value: Article?) {
        // Custom serialization
        writer.nullValue()
      }
    }

    val factory = JsonApiFactory.Builder()
      .addType(Article::class.java)
      .build()
    val moshi = Moshi.Builder()
      .add(factory)
      .add(Article::class.java, CustomAdapter())
      .build()
    val adapter = moshi.adapter(Article::class.java)
    val article = Article(type = "articles", id = "1", title = "Title")
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }

  @Test
  fun `serialize resource of non matching type when strict type checking is disabled`() {
    // Strict types are disabled and Article and Comment types are registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(false)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // Provide incorrect type value to article resource
    val article = Article(type = "comments", id = "1", title = "Title")
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"comments","id":"1","attributes":{"title":"Title"}}""")
  }

  @Test(expected = AssertionError::class)
  fun `throw on serializing non matching type when strict type checking is enabled`() {
    // Strict types are enabled and Article type is registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(true)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // Provide incorrect type value to article resource
    val article = Article(type = "comments", id = "1", title = "Title")
    adapter.toJson(article)
  }

  @JsonClass(generateAdapter = true)
  @Resource("people")
  class Person(
    @Type val type: String? = null,
    @Id val id: String? = null,
    val firstName: String,
    val lastName: String
  ) {
    @Lid val lid: String? = null
    @RelationshipsObject val relationships: Relationships? = null
    @LinksObject val links: Links? = null
    @MetaObject val meta: Meta? = null
  }

  @JsonClass(generateAdapter = true)
  @Resource("comments")
  class Comment(
    @Type val type: String? = null,
    @Id val id: String? = null,
    val body: String,
    @BindRelationship("author") val author: Person? = null
  ) {
    @Lid val lid: String? = null
    @RelationshipsObject val relationships: Relationships? = null
    @MetaObject val meta: Meta? = null
    @LinksObject val links: Links? = null
  }

  @JsonClass(generateAdapter = true)
  @Resource("articles")
  class Article(
    @Type val type: String? = null,
    @Id val id: String? = null,
    @Lid val lid: String? = null,
    val title: String? = null,
    @BindRelationship("author") val author: Person? = null,
    @BindRelationship("comments") val comments: List<Comment>? = null,
    @BindRelationship("related") val related: List<Article>? = null,
    @RelationshipsObject val relationships: Relationships? = null,
    @LinksObject val links: Links? = null,
    @MetaObject val meta: Meta? = null
  )

  // Simplified version of article used for some adapter tests
  @JsonClass(generateAdapter = true)
  @Resource("articles")
  class SimpleArticle(
    @Id val id: String?,
    val title: String
  )
}
