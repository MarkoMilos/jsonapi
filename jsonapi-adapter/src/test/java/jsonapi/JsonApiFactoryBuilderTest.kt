package jsonapi

import org.junit.Test

class JsonApiFactoryBuilderTest {

  @Test(expected = IllegalArgumentException::class)
  fun `throw when registered type is not annotated`() {
    class NotAnnotatedResource

    JsonApiFactory.Builder()
      .addType(NotAnnotatedResource::class.java)
      .build()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `throw when registered type has invalid type value provided with annotation`() {
    @Resource("")
    class InvalidTypeValueResource

    JsonApiFactory.Builder()
      .addType(InvalidTypeValueResource::class.java)
      .build()
  }

  @Test(expected = IllegalStateException::class)
  fun `throw when multiple types are registered for the same name`() {
    @Resource("NAME")
    class A

    @Resource("NAME")
    class B

    JsonApiFactory.Builder()
      .addType(A::class.java)
      .addType(B::class.java)
      .build()
  }
}
