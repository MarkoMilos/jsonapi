package com.jsonapi.internal

import com.jsonapi.JsonApiException
import com.jsonapi.Link
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationship.ToMany
import com.jsonapi.Relationship.ToOne
import com.jsonapi.Relationships
import com.jsonapi.ResourceIdentifier
import jsonapi.BindRelationship
import jsonapi.Resource
import jsonapi.ResourceId
import jsonapi.ResourceLid
import jsonapi.ResourceLinks
import jsonapi.ResourceMeta
import jsonapi.ResourceRelationships
import jsonapi.ResourceType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class ReadResourceObjectTest {

  @Test
  fun `read from target with basic annotations`() {
    class Foo {
      @ResourceType val type = "foo"
      @ResourceId val id = "1"
    }

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.type).isEqualTo("foo")
    assertThat(resourceObject.id).isEqualTo("1")
    assertThat(resourceObject).hasAllNullFieldsOrPropertiesExcept("type", "id")
  }

  @Test
  fun `read from target with all resource object annotations`() {
    class Foo {
      @ResourceType val type = "foo"
      @ResourceId val id = "1"
      @ResourceLid val lid = "2"
      @ResourceRelationships val relationships = Relationships(
        "bar" to ToOne(ResourceIdentifier("bar", "1")),
        "bars" to ToMany(listOf(ResourceIdentifier("bar", "1")))
      )
      @ResourceLinks val links = Links.from("self" to "self")
      @ResourceMeta val meta = Meta("name" to "value")
    }

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.type).isEqualTo("foo")
    assertThat(resourceObject.id).isEqualTo("1")
    assertThat(resourceObject.lid).isEqualTo("2")
    assertThat(resourceObject.relationships?.members).containsExactly(
      entry("bar", ToOne(ResourceIdentifier("bar", "1"))),
      entry("bars", ToMany(listOf(ResourceIdentifier("bar", "1"))))
    )
    assertThat(resourceObject.links?.members).containsExactly(entry("self", Link.URI("self")))
    assertThat(resourceObject.meta?.members).containsExactly(entry("name", "value"))
  }

  @Test
  fun `read from target with relationships`() {
    @Resource("bar")
    class Bar(@ResourceId val id: String)

    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @BindRelationship("A") val a = Bar("1")
      @BindRelationship("B") val b = Bar("2")
      @BindRelationship("C") val c = listOf(Bar("1"), Bar("2"), Bar("3"))
    }

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.relationships?.members).containsExactly(
      entry("A", ToOne(ResourceIdentifier("bar", "1"))),
      entry("B", ToOne(ResourceIdentifier("bar", "2"))),
      entry(
        "C", ToMany(
          listOf(
            ResourceIdentifier("bar", "1"),
            ResourceIdentifier("bar", "2"),
            ResourceIdentifier("bar", "3")
          )
        )
      )
    )
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws if multiple relationship fields are annotated with the same relationship name`() {
    @Resource("bar")
    class Bar(@ResourceId val id: String)

    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @BindRelationship("A") val first = Bar("1")
      @BindRelationship("A") val second = Bar("1")
    }

    readResourceObject(Foo())
  }

  @Test
  fun `read prioritizes relationships defined in relationship object`() {
    @Resource("bar")
    class Bar(@ResourceId val id: String)

    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @BindRelationship("bar") val bar = Bar("1")
      @ResourceRelationships val relationships = Relationships(
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
    class Foo(@ResourceId val id: String = "1")

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.type).isEqualTo("foo")
  }

  @Test(expected = JsonApiException::class)
  fun `read throws for target without defined type`() {
    // Does not define type neither with class or field annotation
    class Foo(@ResourceId val id: String = "1")
    readResourceObject(Foo())
  }

  @Test(expected = JsonApiException::class)
  fun `read throws for target with invalid type`() {
    @Resource("foo")
    class Foo {
      @ResourceType val type = "" // Invalid and has priority
      @ResourceId val id = "1"
    }

    readResourceObject(Foo())
  }

  @Test
  fun `read from target with local identifier`() {
    @Resource("foo")
    class Foo(@ResourceLid val lid: String = "1")

    val resourceObject = readResourceObject(Foo())

    assertThat(resourceObject.id).isNull()
    assertThat(resourceObject.lid).isEqualTo("1")
  }


  @Test(expected = JsonApiException::class)
  fun `read throws for target with invalid identifier`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = ""
      @ResourceLid val lid = ""
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple type annotated fields`() {
    class Foo {
      @ResourceType val field1 = "foo"
      @ResourceType val field2 = "foo"
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple id annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceId val field1 = "1"
      @ResourceId val field2 = "1"
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple lid annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceLid val field1 = "1"
      @ResourceLid val field2 = "1"
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple relationships object annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceRelationships val field1: Relationships? = null
      @ResourceRelationships val field2: Relationships? = null
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple links annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceLinks val field1: Links? = null
      @ResourceLinks val field2: Links? = null
    }

    readResourceObject(Foo())
  }

  @Test(expected = IllegalStateException::class)
  fun `read throws for target with multiple meta annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceMeta val field1: Meta? = null
      @ResourceMeta val field2: Meta? = null
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with type field of incorrect type`() {
    class Foo {
      @ResourceType val type: Int = 1
      @ResourceId val id = "1"
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with id field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id: Int = 1
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with lid field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceLid val lid: Int = 1
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with relationships object field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceRelationships val relationships: String = ""
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with links field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceLinks val links: String = ""
    }

    readResourceObject(Foo())
  }

  @Test(expected = ClassCastException::class)
  fun `read throws for target with meta field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceMeta val meta: String = ""
    }

    readResourceObject(Foo())
  }
}
