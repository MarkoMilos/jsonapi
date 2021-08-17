package jsonapi.integration;

import jsonapi.Id;
import jsonapi.Relationships;
import jsonapi.RelationshipsObject;
import jsonapi.Resource;
import jsonapi.ToOne;
import jsonapi.Type;
import jsonapi.integration.IntegrationTest.Person;

import java.util.Objects;

/**
 * Class for integration test defined as Java to be handled by reflection adapter (ClassJsonAdapter)
 */
@Resource(type = "comments")
public class Comment {

  @Type final String type;
  @Id final String id;
  final String body;
  @ToOne(name = "author") final Person author;
  @RelationshipsObject Relationships relationships;

  public Comment(String type, String id, String body, Person author, Relationships relationships) {
    this.type = type;
    this.id = id;
    this.body = body;
    this.author = author;
    this.relationships = relationships;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Comment comment = (Comment) o;
    return Objects.equals(type, comment.type) &&
      Objects.equals(id, comment.id) &&
      Objects.equals(body, comment.body) &&
      Objects.equals(author, comment.author) &&
      Objects.equals(relationships, comment.relationships);
  }
}
