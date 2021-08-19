package jsonapi.retrofit

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jsonapi.Error
import jsonapi.ErrorsException
import jsonapi.Id
import jsonapi.JsonApiFactory
import jsonapi.Links
import jsonapi.Resource
import jsonapi.ResourceIdentifier
import jsonapi.ResourceObject
import jsonapi.retrofit.JsonFile.DOCUMENT_ARTICLE_COLLECTION
import jsonapi.retrofit.JsonFile.DOCUMENT_ARTICLE_SINGLE
import jsonapi.retrofit.JsonFile.DOCUMENT_ERROR
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class DocumentConverterFactoryTest {

  @Resource("articles")
  data class Article(
    @Id val id: String?,
    val title: String
  )

  interface Service {
    @POST("/")
    fun any(@Document @Body body: Any): Call<ResponseBody>

    @POST("/")
    fun resourceIdentifier(@Document @Body body: ResourceIdentifier): Call<ResponseBody>

    @POST("/")
    fun resourceObject(@Document @Body body: ResourceObject): Call<ResponseBody>

    @POST("/")
    fun resource(@Document @Body body: Article): Call<ResponseBody>

    @POST("/")
    fun resourceIdentifierCollection(@Document @Body body: List<ResourceIdentifier>): Call<ResponseBody>

    @POST("/")
    fun resourceObjectCollection(@Document @Body body: List<ResourceObject>): Call<ResponseBody>

    @POST("/")
    fun resourceCollection(@Document @Body body: List<Article>): Call<ResponseBody>

    @Document
    @GET("/")
    fun resourceIdentifier(): Call<ResourceIdentifier>

    @Document
    @GET("/")
    fun resourceObject(): Call<ResourceObject>

    @Document
    @GET("/")
    fun resource(): Call<Article>

    @Document
    @GET("/")
    fun resourceIdentifiersCollection(): Call<List<ResourceIdentifier>>

    @Document
    @GET("/")
    fun resourceObjectsCollection(): Call<List<ResourceObject>>

    @Document
    @GET("/")
    fun resourcesCollection(): Call<List<Article>>

    @Document
    @GET("/")
    fun any(): Call<Any>
  }

  private lateinit var server: MockWebServer
  private lateinit var service: Service

  @Before
  fun setup() {
    val jsonApiFactory = JsonApiFactory.Builder()
      .addType(Article::class.java)
      .build()

    val moshi = Moshi.Builder()
      .add(jsonApiFactory)
      .add(KotlinJsonAdapterFactory())
      .build()

    server = MockWebServer()
    server.start()

    val retrofit = Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addConverterFactory(DocumentConverterFactory.create())
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()

    service = retrofit.create(Service::class.java)
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun `supported request types`() {
    // ResourceIdentifier
    server.enqueue(MockResponse())
    service.resourceIdentifier(ResourceIdentifier("t", "1", "2")).execute()
    var body = server.takeRequest().body.readByteString().utf8()
    assertThat(body).isEqualTo("""{"data":{"type":"t","id":"1","lid":"2"}}""")

    // ResourceObject
    server.enqueue(MockResponse())
    service.resourceObject(ResourceObject("t", "1", "2")).execute()
    body = server.takeRequest().body.readByteString().utf8()
    assertThat(body).isEqualTo("""{"data":{"type":"t","id":"1","lid":"2"}}""")

    // Resource
    server.enqueue(MockResponse())
    service.resource(Article("1", "Title")).execute()
    body = server.takeRequest().body.readByteString().utf8()
    assertThat(body).isEqualTo("""{"data":{"type":"articles","id":"1","attributes":{"title":"Title"}}}""")

    // List<ResourceIdentifier>
    server.enqueue(MockResponse())
    service.resourceIdentifierCollection(listOf(ResourceIdentifier("t", "1", "2"))).execute()
    body = server.takeRequest().body.readByteString().utf8()
    assertThat(body).isEqualTo("""{"data":[{"type":"t","id":"1","lid":"2"}]}""")

    // List<ResourceObject>
    server.enqueue(MockResponse())
    service.resourceObjectCollection(listOf(ResourceObject("t", "1", "2"))).execute()
    body = server.takeRequest().body.readByteString().utf8()
    assertThat(body).isEqualTo("""{"data":[{"type":"t","id":"1","lid":"2"}]}""")

    // List<Resource>
    server.enqueue(MockResponse())
    service.resourceCollection(listOf(Article("1", "Title"))).execute()
    body = server.takeRequest().body.readByteString().utf8()
    assertThat(body).isEqualTo("""{"data":[{"type":"articles","id":"1","attributes":{"title":"Title"}}]}""")
  }

  @Test
  fun `unsupported request type`() {
    try {
      server.enqueue(MockResponse())
      service.any(Any()).execute()
      fail()
    } catch (e: Exception) {
      assertThat(e.cause).isInstanceOf(IllegalArgumentException::class.java)
      assertThat(e.cause?.message).isEqualTo(
        "Resource type must be one of: ResourceIdentifier, ResourceObject, class with annotation @Resource, " +
          "or Void (in case of error or meta-only documents without any data) but was [class java.lang.Object]."
      )
    }
  }

  @Test
  fun `supported response types`() {
    // ResourceIdentifier
    server.enqueue(response(DOCUMENT_ARTICLE_SINGLE))
    val resourceIdentifier = service.resourceIdentifier().execute().body()
    assertThat(resourceIdentifier).isEqualTo(ResourceIdentifier("articles", "1"))

    // ResourceObject
    server.enqueue(response(DOCUMENT_ARTICLE_SINGLE))
    val resourceObject = service.resourceObject().execute().body()
    assertThat(resourceObject).isEqualTo(ResourceObject("articles", "1"))

    // Resource
    server.enqueue(response(DOCUMENT_ARTICLE_SINGLE))
    val resource = service.resource().execute().body()
    assertThat(resource).isEqualTo(Article("1", "Title1"))

    // List<ResourceIdentifier>
    server.enqueue(response(DOCUMENT_ARTICLE_COLLECTION))
    val resourceIdentifiers = service.resourceIdentifiersCollection().execute().body()
    assertThat(resourceIdentifiers).containsExactly(
      ResourceIdentifier("articles", "1"),
      ResourceIdentifier("articles", "2")
    )

    // List<ResourceObject>
    server.enqueue(response(DOCUMENT_ARTICLE_COLLECTION))
    val resourceObjects = service.resourceObjectsCollection().execute().body()
    assertThat(resourceObjects).containsExactly(
      ResourceObject("articles", "1"),
      ResourceObject("articles", "2")
    )

    // List<Resource>
    server.enqueue(response(DOCUMENT_ARTICLE_COLLECTION))
    val resources = service.resourcesCollection().execute().body()
    assertThat(resources).containsExactly(
      Article("1", "Title1"),
      Article("2", "Title2")
    )
  }

  @Test
  fun `unsupported response type`() {
    try {
      service.any().execute()
      fail()
    } catch (e: Exception) {
      assertThat(e.cause).isInstanceOf(IllegalArgumentException::class.java)
      assertThat(e.cause?.message).isEqualTo(
        "Resource type must be one of: ResourceIdentifier, ResourceObject, class with annotation @Resource, " +
          "or Void (in case of error or meta-only documents without any data) but was [class java.lang.Object]."
      )
    }
  }

  @Test
  fun `throw for error document response body`() {
    try {
      server.enqueue(response(DOCUMENT_ERROR))
      service.resource().execute()
      fail()
    } catch (e: ErrorsException) {
      assertThat(e.errors).containsExactly(
        Error("1", "500", "10", "title", "detail"),
        Error("2"),
        Error("3", links = Links.from("about" to null, "type" to null))
      )
    }
  }

  private fun response(file: JsonFile): MockResponse {
    return response(read(file))
  }

  private fun response(body: String): MockResponse {
    return MockResponse().setBody(body)
  }
}
