package com.jsonapi;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaTest {

  // example of resource implementation in Java
  @Type(name = "articles")
  public static class JavaArticle extends Resource {

    public String title;

    @Relationship(name = "author")
    public Person author;

    @Relationship(name = "comments")
    public List<Comment> comments;

    public JavaArticle(@Nullable String type, @Nullable String id) {
      super(type, id);
    }

    public JavaArticle(@Nullable String type, @Nullable String id, String title, Person author, List<Comment> comments) {
      super(type, id);
      this.title = title;
      this.author = author;
      this.comments = comments;
    }
  }

  private final JsonAdapter.Factory factory = new JsonApiFactory.Builder()
    .addType(JavaArticle.class)
    .addType(Person.class)
    .addType(Comment.class)
    .build();

  private final Moshi moshi = new Moshi.Builder()
    .add(factory)
    .build();

  private final JsonAdapter<Document<JavaArticle>> adapter = moshi.adapter(
    Types.newParameterizedType(Document.class, JavaArticle.class)
  );

  @Test
  public void deserializeDocumentJava() throws IOException {
    Document<JavaArticle> deserialized = adapter.fromJson(FilesKt.read(JsonFile.DOCUMENT_ARTICLE_SINGLE, false));

    assertThat(deserialized).isNotNull().isInstanceOfSatisfying(Document.class, data ->
      assertThat(data.getData()).isNotNull().isInstanceOfSatisfying(JavaArticle.class, article -> {
        assertThat(article.title).isEqualTo("Title1");
        assertThat(article.author).isNotNull();
        assertThat(article.author.getId()).isEqualTo("1");
      }));
  }

  @Test
  public void serializeDocumentJava() {
    Person author1 = new Person("people", "1", "Name1", "Surname1", "@twitter1");
    Person author2 = new Person("people", "2", "Name2", "Surname2", "@twitter2");

    Comment comment1 = new Comment("comments", "1", "Comment1", author2);
    Comment comment2 = new Comment("comments", "2", "Comment2", author1);

    JavaArticle article = new JavaArticle("articles", "1", "Title1", author1, List.of(comment1, comment2));

    Document<JavaArticle> document = new Document<>(article);

    String serialized = adapter.toJson(document);
    String expected = FilesKt.read(JsonFile.DOCUMENT_ARTICLE_SINGLE_SIMPLE, true);

    assertThat(serialized).isEqualTo(expected);
  }
}