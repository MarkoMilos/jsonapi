package jsonapi.internal

import jsonapi.Document
import jsonapi.Id
import jsonapi.Resource
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.ToMany
import jsonapi.ToOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ProcessIncludedTest {

  @Resource("people")
  private data class Person(@Id val id: String?)

  @Resource("comments")
  private data class Comment(
    @Id val id: String?,
    @ToOne("author") val author: Person? = null
  )

  @Resource("articles")
  private data class Article(
    @Id val id: String?,
    @ToOne("author") val author: Person? = null,
    @ToMany("comments") val comments: List<Comment>? = null,
    @ToMany("related") val related: List<Article>? = null
  )

  @Test
  fun `process from document without primary or included resources`() {
    val document = Document.empty()
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of single resource identifier`() {
    val document = Document.from(ResourceIdentifier("articles", "1"))
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of single resource object`() {
    val document = Document.from(ResourceObject("articles", "1"))
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of single resource not having relationships`() {
    val document = Document.from(Article("1"))
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of single resource having relationships`() {
    val author = Person("1")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val article2 = Article("2")
    val article1 = Article("1", author, listOf(comment1, comment2), listOf(article2))
    val document = Document.from(article1)
    val included = processIncluded(document)
    assertThat(included).containsExactly(author, comment1, comment2, article2)
  }

  @Test
  fun `process from document of single resource and non-empty included resources`() {
    val author = Person("1")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val comment3 = Comment("3")
    val article2 = Article("2")
    val article1 = Article("1", related = listOf(article2))
    val document = Document.with(article1)
      .included(listOf(author, comment1, comment2, comment3))
      .build()
    val included = processIncluded(document)
    assertThat(included).containsExactly(author, comment1, comment2, comment3, article2)
  }

  @Test
  fun `process from document of empty collection`() {
    val document = Document.from(listOf<Article>())
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of resource identifier collection`() {
    val document = Document.from(
      ResourceIdentifier("articles", "1"),
      ResourceIdentifier("articles", "2")
    )
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of resource object collection`() {
    val document = Document.from(
      ResourceObject("articles", "1"),
      ResourceObject("articles", "2")
    )
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of resource collection not having relationships`() {
    val document = Document.from(listOf(Article("1"), Article("2")))
    val included = processIncluded(document)
    assertThat(included).isNull()
  }

  @Test
  fun `process from document of resource collection having relationships`() {
    val author = Person("1")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val article2 = Article("2", author)
    val article1 = Article("1", comments = listOf(comment1, comment2), related = listOf(article2))
    val document = Document.from(listOf(article1, article2))
    val included = processIncluded(document)
    assertThat(included).containsExactly(comment1, comment2, author)
  }

  @Test
  fun `process from document of resource collection and non-empty included resources`() {
    val author = Person("1")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val comment3 = Comment("3")
    val article3 = Article("3")
    val article2 = Article("2", author, related = listOf(article3))
    val article1 = Article("1", author, related = listOf(article2))
    val document = Document.with(article1, article2)
      .included(listOf(comment1, comment2, comment3))
      .build()
    val included = processIncluded(document)
    assertThat(included).containsExactly(comment1, comment2, comment3, author, article3)
  }

  @Test
  fun `process has no duplicated resources`() {
    val author = Person("1")
    val comment1 = Comment("1")
    val comment2 = Comment("2")
    val comment3 = Comment("3")
    val article2 = Article("2", author)
    // Duplication in relationship comments
    val article1 = Article("1", author, listOf(comment1, comment1, comment2, comment2), listOf(article2))
    val document = Document
      .with(listOf(article1, article2, article1, article2)) // Duplication for each primary resource
      .included(listOf(comment1, comment1, comment3)) // Duplication in included resources
      .build()
    val included = processIncluded(document)
    assertThat(included).containsExactly(comment1, comment3, author, comment2)
  }

  @Test
  fun `process from document of resources with circular references`() {
    // Articles with circular references via related
    val article2Related = mutableListOf<Article>()
    val article2 = Article("2", related = article2Related)
    val article1 = Article("1", related = listOf(article2))
    article2Related.add(article1)

    val document = Document.from(article1)
    val included = processIncluded(document)
    assertThat(included).containsExactly(article2)
  }

  @Test
  fun `process included with transitive relationships`() {
    // Create node tree with depth of 3 producing binary tree with 15 elements
    val node = generateNodeTree(3, "ROOT")
    val nodes = mutableListOf<Node>()
    flattenNodeTree(node, nodes)

    val included = processIncluded(Document.from(node))

    // It is expected that 14 nodes are in included - everything in tree
    // except the first node that is a primary resource
    assertThat(included).containsExactlyInAnyOrder(*nodes.subList(1, nodes.size).toTypedArray())
  }

  @Resource("node")
  private data class Node(
    @Id val id: String,
    @ToOne("left") val left: Node?,
    @ToOne("right") val right: Node?
  )

  private fun generateNodeTree(depth: Int, nodeId: String): Node {
    return if (depth == 0) {
      Node(nodeId, null, null)
    } else {
      Node(
        nodeId,
        generateNodeTree(depth - 1, "$nodeId->left"),
        generateNodeTree(depth - 1, "$nodeId->right")
      )
    }
  }

  private fun flattenNodeTree(node: Node, nodes: MutableList<Node>) {
    nodes.add(node)
    if (node.left != null) flattenNodeTree(node.left, nodes)
    if (node.right != null) flattenNodeTree(node.right, nodes)
  }
}
