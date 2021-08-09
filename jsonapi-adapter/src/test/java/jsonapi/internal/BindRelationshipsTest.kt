package jsonapi.internal

import jsonapi.Id
import jsonapi.Relationship.ToMany
import jsonapi.Relationship.ToOne
import jsonapi.Relationships
import jsonapi.Resource
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BindRelationshipsTest {

  @Resource("people")
  private data class Person(@Id val id: String?)

  @Resource("comments")
  private data class Comment(
    @Id val id: String?,
    @jsonapi.ToOne("author") val author: Person? = null
  )

  @Resource("articles")
  private data class Article(
    @Id val id: String?,
    @jsonapi.ToOne("author") val author: Person? = null,
    @jsonapi.ToMany("comments") val comments: List<Comment>? = null,
    @jsonapi.ToMany("related") val related: List<Article>? = null
  )

  @Test
  fun `bind relationship fields for resources without defined relationships`() {
    val author1 = Person("1")
    val author2 = Person("2")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val article = Article("1")

    val resources = listOf(
      ResourceObject("people", "1") to author1,
      ResourceObject("people", "2") to author2,
      ResourceObject("comments", "1", relationships = null) to comment1,
      ResourceObject("comments", "2", relationships = null) to comment2,
      ResourceObject("articles", "1", relationships = null) to article
    )

    bindRelationshipFields(resources)

    assertThat(article.author).isNull()
    assertThat(article.comments).isNull()
    assertThat(article.related).isNull()
    assertThat(comment1.author).isNull()
    assertThat(comment2.author).isNull()
  }

  @Test
  fun `bind relationship fields for resources with empty relationships`() {
    val author1 = Person("1")
    val author2 = Person("2")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val article = Article("1")

    val comment1Relationships = Relationships("author" to ToOne(data = null))
    val comment2Relationships = Relationships("author" to ToOne(data = null))

    val articleRelationships = Relationships(
      "author" to ToOne(data = null),
      "comments" to ToMany(data = emptyList()),
      "related" to ToMany(data = emptyList())
    )

    val resources = listOf(
      ResourceObject("people", "1") to author1,
      ResourceObject("people", "2") to author2,
      ResourceObject("comments", "1", relationships = comment1Relationships) to comment1,
      ResourceObject("comments", "2", relationships = comment2Relationships) to comment2,
      ResourceObject("articles", "1", relationships = articleRelationships) to article
    )

    bindRelationshipFields(resources)

    assertThat(article.author).isNull()
    assertThat(article.comments).isEmpty()
    assertThat(article.related).isEmpty()
    assertThat(comment1.author).isNull()
    assertThat(comment2.author).isNull()
  }

  @Test
  fun `bind relationship fields for resources with non-empty relationships`() {
    val author1 = Person("1")
    val author2 = Person("2")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val article = Article("1")

    val comment1Relationships = Relationships("author" to ToOne(ResourceIdentifier("people", "1")))
    val comment2Relationships = Relationships("author" to ToOne(ResourceIdentifier("people", "2")))

    val articleRelationships = Relationships(
      "author" to ToOne(ResourceIdentifier("people", "1")),
      "comments" to ToMany(
        ResourceIdentifier("comments", "1"),
        ResourceIdentifier("comments", "2")
      ),
      "related" to ToMany(ResourceIdentifier("articles", "1"))
    )

    val resources = listOf(
      ResourceObject("people", "1") to author1,
      ResourceObject("people", "2") to author2,
      ResourceObject("comments", "1", relationships = comment1Relationships) to comment1,
      ResourceObject("comments", "2", relationships = comment2Relationships) to comment2,
      ResourceObject("articles", "1", relationships = articleRelationships) to article
    )

    bindRelationshipFields(resources)

    assertThat(article.author).isEqualTo(author1)
    assertThat(article.comments).containsExactly(comment1, comment2)
    assertThat(article.related).containsExactly(article)
    assertThat(comment1.author).isEqualTo(author1)
    assertThat(comment2.author).isEqualTo(author2)
  }

  @Test
  fun `bind relationship fields for resources with duplicated values`() {
    val author1A = Person("1A")
    val author1B = Person("1B")
    val author2 = Person("2")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val articleA = Article("1A")
    val articleB = Article("1B")

    val comment1Relationships = Relationships("author" to ToOne(ResourceIdentifier("people", "1")))
    val comment2Relationships = Relationships("author" to ToOne(ResourceIdentifier("people", "2")))

    val articleRelationships = Relationships(
      "author" to ToOne(ResourceIdentifier("people", "1")),
      "comments" to ToMany(
        ResourceIdentifier("comments", "1"),
        ResourceIdentifier("comments", "2")
      ),
      "related" to ToMany(ResourceIdentifier("articles", "1"))
    )

    val resources = listOf(
      // Add duplicated authors
      ResourceObject("people", "1") to author1A,
      ResourceObject("people", "1") to author1B,
      ResourceObject("people", "2") to author2,
      ResourceObject("comments", "1", relationships = comment1Relationships) to comment1,
      ResourceObject("comments", "2", relationships = comment2Relationships) to comment2,
      // Add duplicated values for articles
      ResourceObject("articles", "1", relationships = articleRelationships) to articleA,
      ResourceObject("articles", "1", relationships = articleRelationships) to articleB
    )

    bindRelationshipFields(resources)

    // Article A
    assertThat(articleA.author).isEqualTo(author1A) // First from the list is bound
    assertThat(articleA.comments).containsExactly(comment1, comment2)
    assertThat(articleA.related).containsExactly(articleA) // First from the list is bound

    // Article B
    assertThat(articleB.author).isEqualTo(author1A) // First from the list is bound
    assertThat(articleB.comments).containsExactly(comment1, comment2)
    assertThat(articleB.related).containsExactly(articleA) // First from the list is bound

    // Comments
    assertThat(comment1.author).isEqualTo(author1A) // First from the list is bound
    assertThat(comment2.author).isEqualTo(author2)
  }

  @Test
  fun `bind relationship fields for resources without matching relationship values`() {
    val author1 = Person("1")
    val author2 = Person("2")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val article = Article("1")

    val comment1Relationships = Relationships("author" to ToOne(ResourceIdentifier("people", "1")))
    val comment2Relationships = Relationships("author" to ToOne(ResourceIdentifier("people", "2")))

    val articleRelationships = Relationships(
      // Set author id to 5 in order not to be matched with any resource
      "author" to ToOne(ResourceIdentifier("people", "5")),
      // Set id of one comment to 5 in order have non-empty to-many relationship that is partially matched
      "comments" to ToMany(
        ResourceIdentifier("comments", "1"),
        ResourceIdentifier("comments", "5")
      ),
      // Set id of related resource to 5 in order to have non-empty to-many relationship
      // where no resources matches
      "related" to ToMany(ResourceIdentifier("articles", "5"))
    )

    val resources = listOf(
      ResourceObject("people", "1") to author1,
      ResourceObject("people", "2") to author2,
      ResourceObject("comments", "1", relationships = comment1Relationships) to comment1,
      ResourceObject("comments", "2", relationships = comment2Relationships) to comment2,
      ResourceObject("articles", "1", relationships = articleRelationships) to article
    )

    bindRelationshipFields(resources)

    assertThat(article.author).isNull() // no person matches relationship
    assertThat(article.comments).containsExactly(comment1) // there is only one comment that matches
    assertThat(article.related).isEmpty() // no articles matches
    assertThat(comment1.author).isEqualTo(author1)
    assertThat(comment2.author).isEqualTo(author2)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw on field and value type mismatch for to-one relationship`() {
    val article = Article("1")
    val comment = Comment("1")

    // Set author relationship (expected type Person) to point on resource of type Comment
    val articleRelationships = Relationships(
      "author" to ToOne(ResourceIdentifier("comments", "1")),
    )

    val resources = listOf(
      ResourceObject("articles", "1", relationships = articleRelationships) to article,
      ResourceObject("comments", "1", relationships = null) to comment
    )

    // Will fail since there is a matched relationship value of type that
    // cannot be assigned to defined field type
    bindRelationshipFields(resources)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw on field and value type mismatch for to-many relationship`() {
    val article = Article("1")
    val person1 = Person("1")
    val person2 = Person("2")

    // Set comments relationship (expected type List<Comment>) to point on resources of type Person
    val articleRelationships = Relationships(
      "comments" to ToMany(
        ResourceIdentifier("people", "1"),
        ResourceIdentifier("people", "2")
      )
    )

    val resources = listOf(
      ResourceObject("articles", "1", relationships = articleRelationships) to article,
      ResourceObject("people", "1") to person1,
      ResourceObject("people", "2") to person2,
    )

    // Will fail since there is a matched relationship value (list of resources) of type that
    // cannot be assigned to defined field type
    bindRelationshipFields(resources)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when to-many relationship is defined for field of non-collection type`() {
    val article = Article("1")
    val person1 = Person("1")
    val person2 = Person("2")

    val articleRelationships = Relationships(
      // Define to-many relationship for field of non-collection type
      "author" to ToMany(
        ResourceIdentifier("people", "1"),
        ResourceIdentifier("people", "2")
      )
    )

    val resources = listOf(
      ResourceObject("articles", "1", relationships = articleRelationships) to article,
      ResourceObject("people", "1") to person1,
      ResourceObject("people", "2") to person2,
    )

    // Will fail since there is a non-collection field that is matched with to-many relationship
    // and target collection type cannot be inferred from that field
    bindRelationshipFields(resources)
  }

  // TODO add test when RO and RI API is added for field binding e.g @ToOne("foo") val foo: ResourceObject
  // assign resource object when field type is RO
  // assign resource identifier when field type is RI
  // -||- above tests but for to-many relationship
}
