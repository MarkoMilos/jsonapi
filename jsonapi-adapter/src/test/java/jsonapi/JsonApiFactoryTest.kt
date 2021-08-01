package jsonapi

import jsonapi.internal.PolymorphicResource
import jsonapi.internal.adapter.DocumentAdapter
import jsonapi.internal.adapter.ErrorAdapter
import jsonapi.internal.adapter.JsonApiObjectAdapter
import jsonapi.internal.adapter.LinkAdapter
import jsonapi.internal.adapter.LinkObjectAdapter
import jsonapi.internal.adapter.LinksAdapter
import jsonapi.internal.adapter.MetaAdapter
import jsonapi.internal.adapter.RelationshipAdapter
import jsonapi.internal.adapter.RelationshipToManyAdapter
import jsonapi.internal.adapter.RelationshipToOneAdapter
import jsonapi.internal.adapter.RelationshipsAdapter
import jsonapi.internal.adapter.ResourceIdentifierAdapter
import jsonapi.internal.adapter.ResourceObjectAdapter
import jsonapi.internal.adapter.ResourcePolymorphicAdapter
import jsonapi.internal.adapter.ResourceTypeAdapter
import jsonapi.internal.adapter.SourceAdapter
import jsonapi.internal.adapter.TransientAdapter
import jsonapi.internal.adapter.VoidAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.lang.reflect.Type

class JsonApiFactoryTest {

  @JsonClass(generateAdapter = true)
  @Resource("type")
  class ValidResource

  private val factory = JsonApiFactory.Builder()
    .addType(ValidResource::class.java)
    .build()

  private val moshi = Moshi.Builder()
    .add(factory)
    .build()

  @Test
  fun `create document adapter for single resource identifier`() {
    val type = Types.newParameterizedType(Document::class.java, ResourceIdentifier::class.java)
    val adapter = moshi.adapter<Any>(type)
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test
  fun `create document adapter for single resource object`() {
    val type = Types.newParameterizedType(Document::class.java, ResourceObject::class.java)
    val adapter = moshi.adapter<Any>(type)
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test
  fun `create document adapter for single resource`() {
    val type = Types.newParameterizedType(Document::class.java, ValidResource::class.java)
    val adapter = moshi.adapter<Any>(type)
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when document adapter is requested for non resource type`() {
    val type = Types.newParameterizedType(Document::class.java, Any::class.java)
    val adapter = moshi.adapter<Any>(type)
    assertThat(adapter).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test
  fun `create document adapter for collection of resource identifiers`() {
    val collectionType = documentCollectionType(ResourceIdentifier::class.java)
    val listType = documentListType(ResourceIdentifier::class.java)
    assertThat(moshi.adapter<Any>(collectionType)).isInstanceOf(DocumentAdapter::class.java)
    assertThat(moshi.adapter<Any>(listType)).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test
  fun `create document adapter for collection of resource objects`() {
    val collectionType = documentCollectionType(ResourceObject::class.java)
    val listType = documentListType(ResourceObject::class.java)
    assertThat(moshi.adapter<Any>(collectionType)).isInstanceOf(DocumentAdapter::class.java)
    assertThat(moshi.adapter<Any>(listType)).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test
  fun `create document adapter for collection of resources`() {
    val collectionType = documentCollectionType(ValidResource::class.java)
    val listType = documentListType(ValidResource::class.java)
    assertThat(moshi.adapter<Any>(collectionType)).isInstanceOf(DocumentAdapter::class.java)
    assertThat(moshi.adapter<Any>(listType)).isInstanceOf(DocumentAdapter::class.java)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when document adapter is requested for unsupported collection type`() {
    // Unsupported collection type for supported resource type
    val unsupportedCollectionType = Types.newParameterizedType(
      Document::class.java, Types.newParameterizedType(Set::class.java, ResourceIdentifier::class.java)
    )
    moshi.adapter<Any>(unsupportedCollectionType)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when document adapter is requested for collection of unsupported resource type`() {
    // Supported collection type for unsupported resource type
    val unsupportedResourceType = Types.newParameterizedType(
      Document::class.java, Types.newParameterizedType(List::class.java, Any::class.java)
    )
    moshi.adapter<Any>(unsupportedResourceType)
  }

  private fun documentCollectionType(clazz: Class<*>): Type {
    return Types.newParameterizedType(Document::class.java, Types.newParameterizedType(Collection::class.java, clazz))
  }

  private fun documentListType(clazz: Class<*>): Type {
    return Types.newParameterizedType(Document::class.java, Types.newParameterizedType(List::class.java, clazz))
  }

  @Test
  fun `create resource identifier adapter`() {
    val adapter = moshi.adapter(ResourceIdentifier::class.java)
    assertThat(adapter).isInstanceOf(ResourceIdentifierAdapter::class.java)
  }

  @Test
  fun `create resource object adapter`() {
    val adapter = moshi.adapter(ResourceObject::class.java)
    assertThat(adapter).isInstanceOf(ResourceObjectAdapter::class.java)
  }

  @Test
  fun `create resource adapter`() {
    val adapter = moshi.adapter(ValidResource::class.java)
    assertThat(adapter).isInstanceOf(ResourceTypeAdapter::class.java)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when resource adapter is requested for resource with invalid type`() {
    @Resource("")
    class InvalidResource

    moshi.adapter(InvalidResource::class.java)
  }

  @Test
  fun `create resource polymorphic adapter`() {
    val adapter = moshi.adapter<Any>(Any::class.java, PolymorphicResource::class.java)
    assertThat(adapter).isInstanceOf(ResourcePolymorphicAdapter::class.java)
  }

  @Test
  fun `create relationships adapter`() {
    val adapter = moshi.adapter(Relationships::class.java)
    assertThat(adapter).isInstanceOf(RelationshipsAdapter::class.java)
  }

  @Test
  fun `create relationship adapter`() {
    val adapter = moshi.adapter(Relationship::class.java)
    assertThat(adapter).isInstanceOf(RelationshipAdapter::class.java)
  }

  @Test
  fun `create relationship to-one adapter`() {
    val adapter = moshi.adapter(Relationship.ToOne::class.java)
    assertThat(adapter).isInstanceOf(RelationshipToOneAdapter::class.java)
  }

  @Test
  fun `create relationship to-many adapter`() {
    val adapter = moshi.adapter(Relationship.ToMany::class.java)
    assertThat(adapter).isInstanceOf(RelationshipToManyAdapter::class.java)
  }

  @Test
  fun `create links adapter`() {
    val adapter = moshi.adapter(Links::class.java)
    assertThat(adapter).isInstanceOf(LinksAdapter::class.java)
  }

  @Test
  fun `create link adapter`() {
    val adapter = moshi.adapter(Link::class.java)
    assertThat(adapter).isInstanceOf(LinkAdapter::class.java)
  }

  @Test
  fun `create link object adapter`() {
    val adapter = moshi.adapter(Link.LinkObject::class.java)
    assertThat(adapter).isInstanceOf(LinkObjectAdapter::class.java)
  }

  @Test
  fun `create meta adapter`() {
    val adapter = moshi.adapter(Meta::class.java)
    assertThat(adapter).isInstanceOf(MetaAdapter::class.java)
  }

  @Test
  fun `create json api object adapter`() {
    val adapter = moshi.adapter(JsonApiObject::class.java)
    assertThat(adapter).isInstanceOf(JsonApiObjectAdapter::class.java)
  }

  @Test
  fun `create error adapter`() {
    val adapter = moshi.adapter(Error::class.java)
    assertThat(adapter).isInstanceOf(ErrorAdapter::class.java)
  }

  @Test
  fun `create source adapter`() {
    val adapter = moshi.adapter(Error.Source::class.java)
    assertThat(adapter).isInstanceOf(SourceAdapter::class.java)
  }

  @Test
  fun `create transient adapter`() {
    class TestResource(
      @BindRelationship("foo") val resource: ValidResource,
      @ResourceType val type: String? = null,
      @ResourceId val id: String? = null,
      @ResourceLid val lid: String? = null,
      @ResourceRelationships val relationships: Relationships? = null,
      @ResourceLinks val links: Links? = null,
      @ResourceMeta val meta: Meta? = null
    )

    val clazz = TestResource::class.java
    val adapters = clazz.declaredFields.map { field ->
      moshi.adapter<Any>(field.type, Types.getFieldJsonQualifierAnnotations(clazz, field.name))
    }

    assertThat(adapters)
      .hasSize(7)
      .hasOnlyElementsOfType(TransientAdapter::class.java)
  }

  @Test
  fun `create void adapter`() {
    assertThat(moshi.adapter(Void::class.java)).isInstanceOf(VoidAdapter::class.java)
    assertThat(moshi.adapter(Nothing::class.java)).isInstanceOf(VoidAdapter::class.java)
  }
}
