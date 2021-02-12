package com.jsonapi

import com.jsonapi.annotation.Type
import com.jsonapi.model.Resource
import org.junit.Test

class JsonApiFactoryBuilderTest {
  
  @Test(expected = JsonApiException::class)
  fun `throw when provided type is not annotated with Type annotation`() {
    class NotAnnotatedResource : Resource()
    
    JsonApiFactory.Builder()
      .addType(NotAnnotatedResource::class.java)
      .build()
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when provided type is annotated with invalid type value`() {
    @Type("")
    class InvalidTypeValueResource : Resource()
    
    JsonApiFactory.Builder()
      .addType(InvalidTypeValueResource::class.java)
      .build()
  }
  
  @Test(expected = JsonApiException::class)
  fun `throw when type name is already registered for another type`() {
    @Type("type-name")
    class A : Resource()
    
    @Type("type-name")
    class B : Resource()
    
    JsonApiFactory.Builder()
      .addType(A::class.java)
      .addType(B::class.java)
      .build()
  }
}