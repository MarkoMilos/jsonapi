package com.jsonapi.binding

import com.jsonapi.*
import com.jsonapi.annotation.Relationship
import com.jsonapi.model.Document
import com.jsonapi.model.Relation
import com.jsonapi.model.Resource
import com.jsonapi.model.ResourceIdentifier
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Before
import org.junit.Test
import java.util.function.Consumer

class UnbinderTest {
  
  private lateinit var article1: Article
  private lateinit var article2: Article
  private lateinit var author1: Person
  private lateinit var author2: Person
  private lateinit var comment1: Comment
  private lateinit var comment2: Comment
  
  private lateinit var article1RelationshipRequirements: Consumer<Article>
  private lateinit var article2RelationshipRequirements: Consumer<Article>
  
  @Before
  fun setup() {
    author1 = Person("people", "1", "Name1", "Surname1", "@twitter1")
    author2 = Person("people", "2", "Name2", "Surname2", "@twitter2")
    
    comment1 = Comment("comments", "1", "Comment1", author2)
    comment2 = Comment("comments", "2", "Comment2", author1)
    
    article1 = Article("articles", "1", "Title1", author1, listOf(comment1, comment2))
    article2 = Article("articles", "2", "Title2", author2, null)
    
    article1RelationshipRequirements = Consumer<Article> { article ->
      assertThat(article.relationships)
        .containsOnlyKeys("author", "comments")
        .containsExactly(
          entry("author", Relation.ToOne(ResourceIdentifier("people", "1"))),
          entry(
            "comments", Relation.ToMany(
              listOf(
                ResourceIdentifier("comments", "1"),
                ResourceIdentifier("comments", "2")
              )
            )
          )
        )
    }
    
    article2RelationshipRequirements = Consumer { article ->
      assertThat(article.relationships)
        .containsOnlyKeys("author")
        .containsExactly(entry("author", Relation.ToOne(ResourceIdentifier("people", "2"))))
    }
  }
  
  @Test
  fun `set included to null for null data document`() {
    val document = Document.Data(data = null, included = listOf(author1))
    document.unbind()
    assertThat(document.included).isNull()
  }
  
  @Test
  fun `unbind primary resource for single resource document`() {
    val document = Document.Data(article1)
    document.unbind()
    assertThat(document.data).isNotNull
    assertThat(document.data?.author).isNull()
    assertThat(document.data?.comments).isNull()
  }
  
  @Test
  fun `unbind primary resources for resource collection document`() {
    val document = Document.Data(listOf(article1, article2))
    document.unbind()
    assertThat(document.data).containsExactly(article1, article2)
    assertThat(article1.author).isNull()
    assertThat(article1.comments).isNull()
    assertThat(article2.author).isNull()
    assertThat(article2.comments).isNull()
  }
  
  @Test
  fun `set primary resource relationships map for single resource document`() {
    val document = Document.Data(article1)
    document.unbind()
    assertThat(article1).satisfies(article1RelationshipRequirements)
  }
  
  @Test
  fun `set primary resources relationships map for resource collection document`() {
    val document = Document.Data(listOf(article1, article2))
    document.unbind()
    assertThat(document.data).containsExactly(article1, article2)
    assertThat(article1).satisfies(article1RelationshipRequirements)
    assertThat(article2).satisfies(article2RelationshipRequirements)
  }
  
  @Test
  fun `set included for single resource document`() {
    val document = Document.Data(article1)
    document.unbind()
    assertThat(document.included).containsExactlyInAnyOrder(author1, author2, comment1, comment2)
  }
  
  @Test
  fun `set included for resource collection document`() {
    val document = Document.Data(listOf(article1, article2))
    document.unbind()
    assertThat(document.included).containsExactlyInAnyOrder(author1, author2, comment1, comment2)
  }
  
  @Test
  fun `unbind included for single resource document`() {
    val document = Document.Data(article1)
    document.unbind()
    assertThat(document.included).contains(comment1, comment2)
    assertThat(comment1.author).isNull()
    assertThat(comment2.author).isNull()
  }
  
  @Test
  fun `unbind included for resource collection document`() {
    val document = Document.Data(listOf(article1, article2))
    document.unbind()
    assertThat(document.included).contains(comment1, comment2)
    assertThat(comment1.author).isNull()
    assertThat(comment2.author).isNull()
  }
  
  @Test
  fun `set included relationships map for single resource document`() {
    val document = Document.Data(article1)
    document.unbind()
    assertThat(document.included).contains(comment1, comment2)
    assertThat(comment1.relationships)
      .containsExactly(entry("author", Relation.ToOne(ResourceIdentifier("people", "2"))))
    assertThat(comment2.relationships)
      .containsExactly(entry("author", Relation.ToOne(ResourceIdentifier("people", "1"))))
  }
  
  @Test
  fun `set included relationships map for resource collection document`() {
    val document = Document.Data(listOf(article1, article2))
    document.unbind()
    assertThat(document.included).contains(comment1, comment2)
    assertThat(comment1.relationships)
      .containsExactly(entry("author", Relation.ToOne(ResourceIdentifier("people", "2"))))
    assertThat(comment2.relationships)
      .containsExactly(entry("author", Relation.ToOne(ResourceIdentifier("people", "1"))))
  }
  
  @Test
  fun `set relationships map to null when resource has no relations`() {
    val article = Article("articles", "1", "title")
    article.relationships = emptyMap()
    val document = Document.Data(article)
    document.unbind()
    assertThat(document.data!!.relationships).isNull()
  }
  
  @Test
  fun `set included to null when there are no relations`() {
    val article = Article("articles", "1", "title")
    val document = Document.Data(data = article, included = emptyList())
    document.unbind()
    assertThat(document.included).isNull()
  }
  
  @Test
  fun `ensure no duplicates in included resources`() {
    val articleA = Article("articles", "1", "Article A")
    val articleB = Article("articles", "2", "Article B")
    val articleC = Article("articles", "3", "Article C")
    
    // setup resources with circular references
    // A -> B, C
    // B -> A, C
    // C -> A, B
    articleA.relatedArticles = listOf(articleB, articleC)
    articleB.relatedArticles = listOf(articleA, articleC)
    articleC.relatedArticles = listOf(articleA, articleB)
    
    val document = Document.Data(articleA)
    document.unbind()
    
    assertThat(document.data).isEqualTo(articleA)
    assertThat(document.included).containsExactly(articleB, articleC)
  }
  
  @Test(expected = IllegalArgumentException::class)
  fun `throw when ToOne relationship field is not a Resource`() {
    // valid resource class with invalid relationship type
    class Foo : Resource() {
      @Relationship("bar") var bar: String? = "bar is invalid relationship value"
    }
    
    Document.Data(Foo()).unbind()
  }
  
  @Test(expected = IllegalArgumentException::class)
  fun `throw when ToMany relationship field is not a collection of Resource`() {
    // valid resource class with invalid relationship type
    class Foo : Resource() {
      @Relationship("bar") var bar: List<Any> = listOf(ValidResource(), NotAResource(), "Dinosaur")
    }
    
    Document.Data(Foo()).unbind()
  }
}