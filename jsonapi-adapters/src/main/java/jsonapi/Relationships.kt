package jsonapi

import jsonapi.Relationship.ToMany
import jsonapi.Relationship.ToOne

class Relationships(val members: Map<String, Relationship>) {

  constructor(vararg members: Pair<String, Relationship>) : this(mapOf(*members))

  /** Returns the number of [Relationship]s. */
  fun size() = members.size

  /** Returns true if relationships are empty, false otherwise. */
  fun isEmpty() = members.isEmpty()

  /** Returns true if relationship with [name] is present, false otherwise. */
  fun has(name: String) = members.containsKey(name)

  /** Returns relationship with [name], or null if relationship is not present. */
  fun get(name: String): Relationship? {
    return members[name]
  }

  /** Returns relationship with [name], or [default] if relationship is not present */
  fun getOrDefault(name: String, default: Relationship): Relationship {
    return members[name] ?: default
  }

  /** Returns new [Builder] initialized from this. */
  fun newBuilder(): Builder {
    return Builder(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return (other is Relationships) && this.members == other.members
  }

  override fun hashCode(): Int {
    return members.hashCode()
  }

  override fun toString(): String {
    return "Relationships(members=$members)"
  }

  class Builder {
    private val members: MutableMap<String, Relationship>

    constructor() {
      this.members = mutableMapOf()
    }

    constructor(relationships: Relationships) {
      this.members = relationships.members.toMutableMap()
    }

    fun add(name: String, value: Relationship) = apply {
      addRelationship(name, value)
    }

    fun toOne(name: String, value: ToOne) = apply {
      addRelationship(name, value)
    }

    @JvmOverloads
    fun toOne(
      name: String,
      resourceIdentifier: ResourceIdentifier? = null,
      links: Links? = null,
      meta: Meta? = null
    ) = apply {
      addRelationship(name, ToOne(resourceIdentifier, links, meta))
    }

    fun toMany(name: String, value: ToMany) = apply {
      addRelationship(name, value)
    }

    @JvmOverloads
    fun toMany(
      name: String,
      resourceIdentifiers: List<ResourceIdentifier> = emptyList(),
      links: Links? = null,
      meta: Meta? = null
    ) = apply {
      addRelationship(name, ToMany(resourceIdentifiers, links, meta))
    }

    @JvmOverloads
    fun toMany(
      name: String,
      vararg resourceIdentifiers: ResourceIdentifier,
      links: Links? = null,
      meta: Meta? = null
    ) = apply {
      addRelationship(name, ToMany(listOf(*resourceIdentifiers), links, meta))
    }

    fun build(): Relationships {
      return Relationships(members)
    }

    private fun addRelationship(name: String, value: Relationship) {
      val replaced = members.put(name, value)
      if (replaced != null) {
        throw IllegalArgumentException(
          "Multiple relationships defined for a key: " +
            name +
            "\nFirst: " +
            value +
            "\nSecond: " +
            replaced
        )
      }
    }
  }
}
