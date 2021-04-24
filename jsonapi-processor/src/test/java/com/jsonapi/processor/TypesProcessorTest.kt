package com.jsonapi.processor

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test

class TypesProcessorTest {
  
  @Test
  fun `processor creates output file for input resources successfully`() {
    val inputFileA = JavaFileObjects.forSourceString(
      "com.jsonapi.A",
      """
      package com.jsonapi;
  
      @Type(name = "A")
      public class A extends Resource {
      }
      """.trimIndent()
    )
    
    val inputFileB = JavaFileObjects.forSourceString(
      "com.jsonapi.B",
      """
      package com.jsonapi;
  
      @Type(name = "B")
      public class B extends Resource {
      }
      """.trimIndent()
    )
    
    val outputFile = JavaFileObjects.forSourceString(
      "com.jsonapi.JsonApi",
      """
      package com.jsonapi;
  
      import com.squareup.moshi.JsonAdapter;
      import java.lang.Class;
      import java.util.ArrayList;
      import java.util.List;
  
      public final class JsonApi {
        public static List<Class<? extends Resource>> resources() {
          List<Class<? extends Resource>> types = new ArrayList<>();
          types.add(A.class);
          types.add(B.class);
          return types;
        }
      
        public static JsonAdapter.Factory factory() {
          return new JsonApiFactory.Builder().addTypes(resources()).build();
        }
      }
      """.trimIndent()
    )
    
    Truth.assert_()
      .about(JavaSourcesSubjectFactory.javaSources())
      .that(listOf(inputFileA, inputFileB))
      .processedWith(TypesProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(outputFile)
  }
  
  @Test
  fun `processor fails when annotated element is not a class`() {
    val invalidInputFile = JavaFileObjects.forSourceString(
      "com.jsonapi.A",
      """
      package com.jsonapi;
  
      @Type(name = "A")
      public enum A {
      }
      """.trimIndent()
    )
    
    Truth.assert_()
      .about(JavaSourcesSubjectFactory.javaSources())
      .that(listOf(invalidInputFile))
      .processedWith(TypesProcessor())
      .failsToCompile()
  }
  
  @Test
  fun `processor fails when annotated element is not extending from resource`() {
    val invalidInputFile = JavaFileObjects.forSourceString(
      "com.jsonapi.A",
      """
      package com.jsonapi;
  
      @Type(name = "A")
      public class A {
      }
      """.trimIndent()
    )
    
    Truth.assert_()
      .about(JavaSourcesSubjectFactory.javaSources())
      .that(listOf(invalidInputFile))
      .processedWith(TypesProcessor())
      .failsToCompile()
  }
}