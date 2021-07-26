package com.jsonapi

import jsonapi.Resource
import org.junit.Test

class JsonApiFactoryBuilderTest {

  // TODO update this tests
  @Test(expected = JsonApiException::class)
  fun `throw when provided type is not annotated with Type annotation`() {
    class NotAnnotatedResource

    JsonApiFactory.Builder()
      .addType(NotAnnotatedResource::class.java)
      .build()
  }

  @Test(expected = JsonApiException::class)
  fun `throw when provided type is annotated with invalid type value`() {
    @Resource("")
    class InvalidTypeValueResource

    JsonApiFactory.Builder()
      .addType(InvalidTypeValueResource::class.java)
      .build()
  }

  @Test(expected = JsonApiException::class)
  fun `throw when type name is already registered for another type`() {
    @Resource("type-name")
    class A

    @Resource("type-name")
    class B

    JsonApiFactory.Builder()
      .addType(A::class.java)
      .addType(B::class.java)
      .build()
  }
}
