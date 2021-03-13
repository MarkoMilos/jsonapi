package com.jsonapi.binding

import com.jsonapi.*
import com.jsonapi.model.Document
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BindingIntegrationTest {
  
  @Test
  fun `unbind followed by bind reverts primary resource to initial state`() {
    val author1 = Person("Name1", "Surname1", "@twitter1")
    author1.type = "people"
    author1.id = "1"
    
    val author2 = Person("Name2", "Surname2", "@twitter2")
    author2.type = "people"
    author2.id = "2"
    
    val comment1 = Comment("Comment1", author2)
    comment1.type = "comments"
    comment1.id = "1"
    
    val comment2 = Comment("Comment2", author1)
    comment2.type = "comments"
    comment2.id = "2"
    
    val article2 = Article("articles", "2", "Title2", author2, null)
    val article1 = Article("articles", "1", "Title1", author1, listOf(comment1, comment2), listOf(article2))
    
    val document = Document.data(article1)
    
    document.unbind()
    
    // primary resource
    assertThat(document.data).isNotNull
    assertThat(document.data?.author).isNull()
    assertThat(document.data?.comments).isNull()
    assertThat(document.data?.relatedArticles).isNull()
    
    // relationship resources
    assertThat(document.included).containsExactlyInAnyOrder(article2, author1, author2, comment1, comment2)
    assertThat(article2.author).isNull()
    assertThat(article2.comments).isNull()
    assertThat(comment1.author).isNull()
    assertThat(comment2.author).isNull()
    
    document.bind()
    
    // primary resource
    assertThat(document.data).isEqualTo(article1)
    assertThat(document.data?.author).isEqualTo(author1)
    assertThat(document.data?.comments).containsExactlyInAnyOrder(comment1, comment2)
    assertThat(document.data?.relatedArticles).containsExactlyInAnyOrder(article2)
    
    // relationship resources
    assertThat(article2.author).isEqualTo(author2)
    assertThat(article2.comments).isNull()
    assertThat(comment1.author).isEqualTo(author2)
    assertThat(comment2.author).isEqualTo(author1)
  }
}