package com.jsonapi;

import com.jsonapi.annotation.Relationship;
import com.jsonapi.annotation.Type;
import com.jsonapi.model.Document;
import com.jsonapi.model.Resource;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaTest {

  // example of resource implementation in Java
  @Type(name = "articles")
  public static class JavaArticle extends Resource {

    public String title;

    @Relationship(name = "author")
    public Person author;

    public JavaArticle(@Nullable String type, @Nullable String id) {
      super(type, id);
    }
  }

  @Test
  public void documentDeserializationJava() throws IOException {
    JsonAdapter.Factory factory = new JsonApiFactory.Builder()
      .addType(JavaArticle.class)
      .addType(Person.class)
      .addType(Comment.class)
      .build();

    Moshi moshi = new Moshi.Builder()
      .add(factory)
      .build();

    JsonAdapter<Document<JavaArticle>> adapter = moshi.adapter(
      Types.newParameterizedType(Document.class, JavaArticle.class)
    );

    Document<JavaArticle> deserialized = adapter.fromJson(FilesKt.read(JsonFile.DOCUMENT_ARTICLE_SINGLE, false));

    assertThat(deserialized).isNotNull().isInstanceOfSatisfying(Document.Data.class, data -> {
      assertThat(data.getData()).isNotNull().isInstanceOfSatisfying(JavaArticle.class, article -> {
        assertThat(article.title).isEqualTo("Title1");
        assertThat(article.author).isNotNull();
        assertThat(article.author.getId()).isEqualTo("1");
      });
    });
  }
}