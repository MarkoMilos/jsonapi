package com.jsonapi.binding

import com.jsonapi.*
import com.jsonapi.model.Document
import com.jsonapi.model.Relation.ToMany
import com.jsonapi.model.Relation.ToOne
import com.jsonapi.model.Resource
import com.jsonapi.model.ResourceIdentifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class BinderTest {
  
  private lateinit var article1: Article
  private lateinit var article2: Article
  private lateinit var author1: Person
  private lateinit var author2: Person
  private lateinit var comment1: Comment
  private lateinit var comment2: Comment
  
  @Before
  fun setup() {
    author1 = Person("people", "1", "Name1", "Surname1", "@twitter1")
    author2 = Person("people", "2", "Name2", "Surname2", "@twitter2")
    
    comment1 = Comment("comments", "1", "Comment1", author2)
    comment1.relationships = mapOf("author" to ToOne(ResourceIdentifier("people", "2")))
    
    comment2 = Comment("comments", "2", "Comment2", author1)
    comment2.relationships = mapOf("author" to ToOne(ResourceIdentifier("people", "1")))
    
    article1 = Article("articles", "1", "Title1")
    article1.relationships = mapOf(
      "author" to ToOne(ResourceIdentifier("people", "1")),
      "comments" to ToMany(
        listOf(
          ResourceIdentifier("comments", "1"),
          ResourceIdentifier("comments", "2")
        )
      ),
      "related" to ToMany(listOf(ResourceIdentifier("articles", "2")))
    )
    
    article2 = Article("articles", "2", "Title2")
    article2.relationships = mapOf(
      "author" to ToOne(ResourceIdentifier("people", "2")),
      "related" to ToMany(listOf(ResourceIdentifier("articles", "1")))
    )
  }
  
  @Test
  fun `bind primary resource for single resource document`() {
    val document = Document.Data(
      data = article1,
      included = listOf(author1, author2, comment1, comment2)
    )
    document.bind()
    assertThat(article1.author).isEqualTo(author1)
    assertThat(article1.comments).containsExactlyInAnyOrder(comment1, comment2)
    assertThat(article1.relatedArticles).isEmpty()
  }
  
  @Test
  fun `bind primary resource for resource collection document`() {
    val document = Document.Data(
      data = listOf(article1, article2),
      included = listOf(author1, author2, comment1, comment2)
    )
    document.bind()
    
    assertThat(article1.author).isEqualTo(author1)
    assertThat(article1.comments).containsExactlyInAnyOrder(comment1, comment2)
    assertThat(article1.relatedArticles).containsExactly(article2)
    
    assertThat(article2.author).isEqualTo(author2)
    assertThat(article2.comments).isNullOrEmpty()
    assertThat(article2.relatedArticles).containsExactly(article1)
  }
  
  @Test
  fun `bind included resources`() {
    val document = Document.Data(
      data = article1,
      included = listOf(author1, author2, comment1, comment2, article2)
    )
    document.bind()
    assertThat(comment1.author).isEqualTo(author2)
    assertThat(comment2.author).isEqualTo(author1)
    assertThat(article2.relatedArticles).containsExactly(article1)
  }
  
  @Test(expected = JsonApiException::class)
  fun `throws when incorrect type is bound to relationship field`() {
    // Within included provide Resource that will match relationship entry.
    // Doing so Binder will try to bind matched Resource value to annotated Person field.
    val document = Document.Data(
      data = article1,
      included = listOf(Resource(id = "1", type = "people"))
    )
    document.bind()
  }
}