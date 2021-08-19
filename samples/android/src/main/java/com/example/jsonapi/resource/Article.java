package com.example.jsonapi.resource;

import jsonapi.Id;
import jsonapi.Resource;
import jsonapi.ToMany;
import jsonapi.ToOne;

import java.util.List;

@Resource(type = "articles")
public class Article {

  @Id final String id;
  final String title;
  @ToOne(name = "author") final Person author;
  @ToMany(name = "comments") final List<Comment> comments;

  public Article(String id, String title, Person author, List<Comment> comments) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.comments = comments;
  }

  public String headline() {
    return this.title + " by " + this.author.getName();
  }

  @Override
  public String toString() {
    return "Article{" +
      "id='" + id + '\'' +
      ", title='" + title + '\'' +
      ", author=" + author +
      ", comments=" + comments +
      '}';
  }
}
