package com.jsonapi.internal

import com.jsonapi.Link
import com.jsonapi.Links
import com.jsonapi.Meta
import com.jsonapi.Relationship
import com.jsonapi.Relationships
import com.jsonapi.ResourceIdentifier
import com.jsonapi.ResourceObject
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
      @ResourceType val type: String? = null
      @ResourceId val id: String? = null
      @ResourceLid val lid: String? = null
      @ResourceRelationships val relationships: Relationships? = null
      @ResourceLinks val links: Links? = null
      @ResourceMeta val meta: Meta? = null
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
      @ResourceId val id: String = "1",
      @ResourceLid val lid: String = "2",
      @ResourceRelationships val relationships: Relationships =
        Relationships("bar" to Relationship.ToOne(ResourceIdentifier("bar", "1"))),
      @ResourceLinks val links: Links = Links.from("self" to "link"),
      @ResourceMeta val meta: Meta = Meta("name" to "value")
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
      @ResourceType val field1 = "foo"
      @ResourceType val field2 = "foo"
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple id annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceId val field1 = "1"
      @ResourceId val field2 = "1"
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple lid annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceLid val field1 = "1"
      @ResourceLid val field2 = "1"
    }

    bindResourceObject(Foo(), ResourceObject("t", lid = "1"))
  }

  @Test(expected = IllegalStateException::class)
  fun `bind throws for target with multiple relationships object annotated fields`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceRelationships val field1: Relationships? = null
      @ResourceRelationships val field2: Relationships? = null
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
      @ResourceId val id = "1"
      @ResourceLinks val field1: Links? = null
      @ResourceLinks val field2: Links? = null
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
      @ResourceId val id = "1"
      @ResourceMeta val field1: Meta? = null
      @ResourceMeta val field2: Meta? = null
    }

    bindResourceObject(
      Foo(),
      ResourceObject("t", "1", meta = Meta("name" to "value"))
    )
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with type field of incorrect type`() {
    class Foo {
      @ResourceType val type: Int = 1
      @ResourceId val id = "1"
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with id field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id: Int = 1
    }

    bindResourceObject(Foo(), ResourceObject("t", "1"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with lid field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceLid val lid: Int = 1
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", "1"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with relationships object field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceRelationships val relationships: String = ""
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", relationships = Relationships.Builder().build()))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with links field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceLinks val links: String = ""
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", links = Links(emptyMap())))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `bind throws for target with meta field of incorrect type`() {
    @Resource("foo")
    class Foo {
      @ResourceId val id = "1"
      @ResourceMeta val meta: String = ""
    }

    bindResourceObject(Foo(), ResourceObject("t", "1", meta = Meta(emptyMap())))
  }
}
