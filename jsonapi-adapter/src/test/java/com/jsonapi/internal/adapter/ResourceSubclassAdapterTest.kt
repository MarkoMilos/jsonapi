package com.jsonapi.internal.adapter

import com.jsonapi.*
import com.jsonapi.JsonFile.*
import com.jsonapi.TestUtils.moshi
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResourceSubclassAdapterTest {
  
  private val articleAdapter = moshi.adapter(Article::class.java)
  private val specialArticleAdapter = moshi.adapter(SpecialArticle::class.java)
  
  @Test
  fun `deserialize null`() {
    val deserialized = articleAdapter.fromJson("null")
    assertThat(deserialized).isNull()
  }
  
  @Test
  fun `deserialize resource without attributes`() {
    val deserialized = articleAdapter.fromJson(read(RESOURCE_NO_ATTRIBUTES))
    assertThat(deserialized).isNotNull
    assertThat(deserialized).isInstanceOfSatisfying(Article::class.java) {
      assertThat(it.type).isEqualTo("articles")
      assertThat(it.id).isEqualTo("1")
      assertThat(it.relationships).hasSize(2)
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
      assertThat(it.title).isNullOrEmpty()
    }
  }
  
  @Test
  fun `deserialize resource with attributes`() {
    val deserialized = articleAdapter.fromJson(read(RESOURCE_ARTICLE))
    assertThat(deserialized).isNotNull
    assertThat(deserialized).isInstanceOfSatisfying(Article::class.java) {
      assertThat(it.type).isEqualTo("articles")
      assertThat(it.id).isEqualTo("1")
      assertThat(it.relationships).hasSize(2)
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
      assertThat(it.title).isEqualTo("Title1")
    }
  }
  
  @Test
  fun `deserialize resource of subclass type`() {
    val deserialized = specialArticleAdapter.fromJson(read(RESOURCE_ARTICLE))
    assertThat(deserialized).isNotNull
    assertThat(deserialized).isInstanceOfSatisfying(SpecialArticle::class.java) {
      assertThat(it.type).isEqualTo("articles")
      assertThat(it.id).isEqualTo("1")
      assertThat(it.relationships).hasSize(2)
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
      assertThat(it.title).isEqualTo("Title1")
      assertThat(it.headline).isEqualTo("Headline1")
    }
  }
  
  @Test
  fun `deserialize unregistered resource when unregistered types are allowed`() {
    // unregistered types are allowed and Article type is not registered
    val factory = JsonApiFactory.Builder().allowUnregisteredTypes(true).build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    val deserialized = adapter.fromJson(read(RESOURCE_ARTICLE))
    assertThat(deserialized).isNotNull
    assertThat(deserialized).isInstanceOfSatisfying(Article::class.java) {
      assertThat(it.type).isEqualTo("articles")
      assertThat(it.id).isEqualTo("1")
      assertThat(it.relationships).hasSize(2)
      assertThat(it.links).isNotNull
      assertThat(it.meta).isNotNull
      assertThat(it.title).isEqualTo("Title1")
    }
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on deserializing unregistered resource when unregistered types are not allowed`() {
    // unregistered types are not allowed and Article type is not registered
    val factory = JsonApiFactory.Builder().allowUnregisteredTypes(false).build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    adapter.fromJson(read(RESOURCE_ARTICLE))
  }
  
  @Test
  fun `deserialize resource of non matching type when strict type checking is disabled`() {
    // strict types are disabled and both Article and Comment types are registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(false)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // use article adapter to deserialize comment json resource
    val deserialized = adapter.fromJson(read(RESOURCE_COMMENT))
    assertThat(deserialized).isNotNull
    assertThat(deserialized).isInstanceOfSatisfying(Article::class.java) {
      assertThat(it.type).isEqualTo("comments")
      assertThat(it.id).isEqualTo("1")
      assertThat(it.title).isNullOrEmpty()
    }
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on deserializing non matching type when strict type checking is enabled`() {
    // strict types are enabled and both Article and Comment types are registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(true)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // use article adapter to deserialize comment json resource
    adapter.fromJson(read(RESOURCE_COMMENT))
  }
  
  @Test
  fun `ignore non standard json names`() {
    val deserialized = articleAdapter.fromJson(read(RESOURCE_NON_STANDARD_NAMES))
    assertThat(deserialized).isNotNull
    assertThat(deserialized).isInstanceOfSatisfying(Article::class.java) {
      assertThat(it.type).isEqualTo("articles")
      assertThat(it.id).isEqualTo("1")
      assertThat(it.relationships).isNull()
      assertThat(it.links).isNull()
      assertThat(it.meta).isNull()
      assertThat(it.title).isEqualTo("Title1")
    }
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when resource does not have type`() {
    articleAdapter.fromJson("""{"id":"1"}""")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when resource does not have id or lid`() {
    articleAdapter.fromJson("""{"type":"articles"}""")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when resource is not an json object`() {
    articleAdapter.fromJson("[]")
  }
  
  @Test
  fun `serialize null`() {
    val serialized = articleAdapter.toJson(null)
    assertThat(serialized).isEqualTo("null")
  }
  
  @Test
  fun `serialize resource without attributes`() {
    val article = Article("articles", "1", null)
    val serialized = articleAdapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1"}""")
  }
  
  @Test
  fun `serialize resource with attributes`() {
    val article = Article("articles", "1", "Title")
    val serialized = articleAdapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1","attributes":{"title":"Title"}}""")
  }
  
  @Test
  fun `serialize resource of subclass type`() {
    val article = SpecialArticle("articles", "1", "T", "H")
    val serialized = specialArticleAdapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1","attributes":{"title":"T","headline":"H"}}""")
  }
  
  @Test
  fun `serialize unregistered resource when unregistered types are allowed`() {
    // unregistered types are allowed and Article type is not registered
    val factory = JsonApiFactory.Builder().allowUnregisteredTypes(true).build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    val article = Article("articles", "1", "Title")
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"articles","id":"1","attributes":{"title":"Title"}}""")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on serializing unregistered type when unregistered types are not allowed`() {
    // unregistered types are not allowed and Article type is not registered
    val factory = JsonApiFactory.Builder().allowUnregisteredTypes(false).build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    val article = Article("articles", "1", "Title")
    adapter.toJson(article)
  }
  
  @Test
  fun `serialize resource of non matching type when strict type checking is disabled`() {
    // strict types are disabled and Article and Comment types are registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(false)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // provide incorrect type value to article resource
    val article = Article("comments", "1", "Title")
    val serialized = adapter.toJson(article)
    assertThat(serialized).isEqualTo("""{"type":"comments","id":"1","attributes":{"title":"Title"}}""")
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw on serializing non matching type when strict type checking is enabled`() {
    // strict types are enabled and Article type is registered
    val factory = JsonApiFactory.Builder()
      .strictTypes(true)
      .addType(Article::class.java)
      .addType(Comment::class.java)
      .build()
    val moshi = Moshi.Builder().add(factory).build()
    val adapter = moshi.adapter(Article::class.java)
    // provide incorrect type value to article resource
    val article = Article("comments", "1", "Title")
    adapter.toJson(article)
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when serializing resource without type`() {
    val article = Article(type = null, id = "1")
    articleAdapter.toJson(article)
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when serializing resource without id or lid`() {
    val article = Article(type = "articles", id = null)
    articleAdapter.toJson(article)
  }
}