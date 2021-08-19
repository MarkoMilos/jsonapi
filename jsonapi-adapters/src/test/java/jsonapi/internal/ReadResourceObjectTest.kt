package jsonapi.internal

import jsonapi.Id
import jsonapi.Lid
import jsonapi.Link
import jsonapi.Links
import jsonapi.LinksObject
import jsonapi.Meta
import jsonapi.MetaObject
import jsonapi.Relationship.ToMany
import jsonapi.Relationship.ToOne
import jsonapi.Relationships
import jsonapi.RelationshipsObject
import jsonapi.Resource
import jsonapi.ResourceIdentifier
import jsonapi.Type
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class ReadResourceObjectTest {

  @Test
  fun `read from target with basic annotations`() {
    class Foo {
      @Type val type = "foo"
      @Id val id = "1"
    }

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.type).isEqualTo("foo")
    assertThat(resourceObject.id).isEqualTo("1")
    assertThat(resourceObject).hasAllNullFieldsOrPropertiesExcept("type", "id")
  }

  @Test
  fun `read from target with all resource object annotations`() {
    class Foo {
      @Type val type = "foo"
      @Id val id = "1"
      @Lid val lid = "2"
      @RelationshipsObject val relationships = Relationships(
        "bar" to ToOne(ResourceIdentifier("bar", "1")),
        "bars" to ToMany(ResourceIdentifier("bar", "1"))
      )
      @LinksObject val links = Links.from("self" to "self")
      @MetaObject val meta = Meta("name" to "value")
    }

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.type).isEqualTo("foo")
    assertThat(resourceObject.id).isEqualTo("1")
    assertThat(resourceObject.lid).isEqualTo("2")
    assertThat(resourceObject.relationships?.members).containsExactly(
      entry("bar", ToOne(ResourceIdentifier("bar", "1"))),
      entry("bars", ToMany(ResourceIdentifier("bar", "1")))
    )
    assertThat(resourceObject.links?.members).containsExactly(entry("self", Link.URI("self")))
    assertThat(resourceObject.meta?.members).containsExactly(entry("name", "value"))
  }

  @Test
  fun `read from target with relationships`() {
    @Resource("bar")
    class Bar(@Id val id: String)

    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @jsonapi.ToOne("A") val a = Bar("1")
      @jsonapi.ToOne("B") val b = Bar("2")
      @jsonapi.ToMany("C") val c = listOf(Bar("1"), Bar("2"), Bar("3"))
    }

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.relationships?.members).containsExactly(
      entry("A", ToOne(ResourceIdentifier("bar", "1"))),
      entry("B", ToOne(ResourceIdentifier("bar", "2"))),
      entry(
        "C",
        ToMany(
          ResourceIdentifier("bar", "1"),
          ResourceIdentifier("bar", "2"),
          ResourceIdentifier("bar", "3")
        )
      )
    )
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws if multiple relationship fields are defined for the same name`() {
    @Resource("bar")
    class Bar(@Id val id: String)

    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @jsonapi.ToOne("A") val first = Bar("1")
      @jsonapi.ToOne("A") val second = Bar("1")
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws when to-many relationship annotation is set on non-collection field`() {
    @Resource("bar")
    class Bar(@Id val id: String)

    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @jsonapi.ToMany("A") val first = Bar("1") // ToMany annotation on non-collection field
    }

    readResourceObject(Foo())
  }

  @Test
  fun `read prioritizes relationships defined in relationship object`() {
    @Resource("bar")
    class Bar(@Id val id: String)

    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @jsonapi.ToOne("bar") val bar = Bar("1")
      @RelationshipsObject val relationships = Relationships(
        "bar" to ToOne(ResourceIdentifier("bar", "2"))
      )
    }

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.relationships?.members).containsExactly(
      entry("bar", ToOne(ResourceIdentifier("bar", "2")))
    )
  }

  @Test
  fun `read from target without type field`() {
    // Type field omitted and type defined with @Resource annotation
    @Resource("foo")
    class Foo(@Id val id: String = "1")

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.type).isEqualTo("foo")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `read throws for target without defined type`() {
    // Does not define type neither with class or field annotation
    class Foo(@Id val id: String = "1")
    readResourceObject(Foo())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `read throws for target with invalid type`() {
    @Resource("foo")
    class Foo {
      @Type val type = "" // Invalid and has priority
      @Id val id = "1"
    }

    readResourceObject(Foo())
  }

  @Test
  fun `read from target with local identifier`() {
    @Resource("foo")
    class Foo(@Lid val lid: String = "1")

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.id).isNull()
    assertThat(resourceObject.lid).isEqualTo("1")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `read throws for target with invalid identifier`() {
    @Resource("foo")
    class Foo {
      @Id val id = ""
      @Lid val lid = ""
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple type annotated fields`() {
    class Foo {
      @Type val field1 = "foo"
      @Type val field2 = "foo"
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple id annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val field1 = "1"
      @Id val field2 = "1"
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple lid annotated fields`() {
    @Resource("foo")
    class Foo {
      @Lid val field1 = "1"
      @Lid val field2 = "1"
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple relationships object annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @RelationshipsObject val field1: Relationships? = null
      @RelationshipsObject val field2: Relationships? = null
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple links annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @LinksObject val field1: Links? = null
      @LinksObject val field2: Links? = null
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple meta annotated fields`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @MetaObject val field1: Meta? = null
      @MetaObject val field2: Meta? = null
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with type field of incorrect type`() {
    class Foo {
      @Type val type: Int = 1
      @Id val id = "1"
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with id field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id: Int = 1
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with lid field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Lid val lid: Int = 1
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with relationships object field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @RelationshipsObject val relationships: String = ""
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with links field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @LinksObject val links: String = ""
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with meta field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @Id val id = "1"
      @MetaObject val meta: String = ""
    }

    readResourceObject(Foo())
  }
}
