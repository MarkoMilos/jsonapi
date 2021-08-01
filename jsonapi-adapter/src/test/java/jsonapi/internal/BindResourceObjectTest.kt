package jsonapi.internal

import jsonapi.Link
import jsonapi.Links
import jsonapi.Meta
import jsonapi.Relationship
import jsonapi.Relationships
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.Resource
import jsonapi.Id
import jsonapi.Lid
import jsonapi.LinksObject
import jsonapi.MetaObject
import jsonapi.RelationshipsObject
import jsonapi.Type
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class BindResourceObjectTest {

  @Test
  fun `bind to target without annotations`() {
    @Resource("foo")
    class Foo {
      val type: String? = null
      val id: String? = null
    }

    val target = Foo()
    val resourceObject = ResourceObject("foo", "1")
    bindResourceObject(target, resourceObject)

    assertThat(target).hasAllNullFieldsOrProperties()
  }

  @Test
  fun `bind to target with all annotations`() {
    @Resource("foo")
    class Foo {
      @Type val type: String? = null
      @Id val id: String? = null
      @Lid val lid: String? = null
      @RelationshipsObject val relationships: Relationships? = null
      @LinksObject val links: Links? = null
      @MetaObject val meta: Meta? = null
    }

    val target = Foo()
    val resourceObject = ResourceObject(
      type = "foo",
      id = "1",
      lid = "2",
      relationships = Relationships.Builder()
        .toOne("bar", ResourceIdentifier("bar", "1"))
        .toMany("bars", ResourceIdentifier("bar", "1"))
        .build(),
      links = Links.from("self" to "self"),
      meta = Meta("name" to "value")
    )

    bindResourceObject(target, resourceObject)

    assertThat(target.type).isEqualTo("foo")
    assertThat(target.id).isEqualTo("1")
    assertThat(target.lid).isEqualTo("2")
    assertThat(target.relationships?.members).containsExactly(
      entry("bar", Relationship.ToOne(ResourceIdentifier("bar", "1"))),
      entry("bars", Relationship.ToMany(listOf(ResourceIdentifier("bar", "1"))))
    )
    assertThat(target.links?.members).containsExactly(entry("self", Link.URI("self")))
    assertThat(target.meta?.members).containsExactly(entry("name", "value"))
  }

  @Test
  fun `bind does not override default values`() {
    @Resource("foo")
    class Foo(
      @Id val id: String = "1",
      @Lid val lid: String = "2",
      @RelationshipsObject val relationships: Relationships =
        Relationships("bar" to Relationship.ToOne(ResourceIdentifier("bar", "1"))),
      @LinksObject val links: Links = Links.from("self" to "link"),
      @MetaObject val meta: Meta = Meta("name" to "value")
    )

    val target = Foo()
    val resourceObject = ResourceObject("foo", "1")

    bindResourceObject(target, resourceObject)

    assertThat(target.id).isEqualTo("1")
    assertThat(target.lid).isEqualTo("2")
    assertThat(target.relationships.members).containsExactly(
      entry("bar", Relationship.ToOne(ResourceIdentifier("bar", "1")))
    )
    assertThat(target.links.members).containsExactly(entry("self", Link.URI("link")))
    assertThat(target.meta.members).containsExactly(entry("name", "value"))
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple type annotated fields`() {
    class Foo {
      @Type val field1 = "foo"
      @Type val field2 = "foo"
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple id annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val field1 = "1"
      @Id val field2 = "1"
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple lid annotated fields`() {
    @Resource("foo")
    class Foo {
      @Lid val field1 = "1"
      @Lid val field2 = "1"
    }

    bindResourceObject(Foo(), ResourceObject("t", lid = "1"))
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple relationships object annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @RelationshipsObject val field1: Relationships? = null
      @RelationshipsObject val field2: Relationships? = null
    }

    bindResourceObject(
      Foo(),
      ResourceObject("t", "1", relationships = Relationships(emptyMap()))
    )
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple links annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @LinksObject val field1: Links? = null
      @LinksObject val field2: Links? = null
    }

    bindResourceObject(
      Foo(),
      ResourceObject("t", "1", links = Links.from("link" to "link"))
    )
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple meta annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @MetaObject val field1: Meta? = null
      @MetaObject val field2: Meta? = null
    }

    bindResourceObject(
      Foo(),
      ResourceObject("t", "1", meta = Meta("name" to "value"))
    )
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with type field of incorrect type`() {
    class Foo {
      @Type val type: Int = 1
      @Id val id = "1"
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with id field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id: Int = 1
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with lid field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Lid val lid: Int = 1
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", "1"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with relationships object field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @RelationshipsObject val relationships: String = ""
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", relationships = Relationships.Builder().build()))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with links field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @LinksObject val links: String = ""
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", links = Links(emptyMap())))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with meta field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @MetaObject val meta: String = ""
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", meta = Meta(emptyMap())))
  }
}
