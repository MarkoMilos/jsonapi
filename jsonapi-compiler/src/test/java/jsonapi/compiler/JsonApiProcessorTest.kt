package jsonapi.compiler

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test

class JsonApiProcessorTest {

  @Test
  fun `processor creates output file for input resources successfully`() {
    val inputFileA = JavaFileObjects.forSourceString(
      "jsonapi.A",
      """
      package jsonapi;

      @Resource(type = "A")
      public class A {
      }
      """.trimIndent()
    )

    val inputFileB = JavaFileObjects.forSourceString(
      "jsonapi.B",
      """
      package jsonapi;

      @Resource(type = "B")
      public class B {
      }
      """.trimIndent()
    )

    val outputFile = JavaFileObjects.forSourceString(
      "jsonapi.JsonApi",
      """
      package jsonapi;

      import com.squareup.moshi.JsonAdapter;
      import java.lang.Class;
      import java.util.ArrayList;
      import java.util.List;

      public final class JsonApi {
        public static List<Class<?>> resources() {
          List<Class<?>> types = new ArrayList<>();
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
      .processedWith(JsonApiProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(outputFile)
  }

  @Test
  fun `processor fails when annotated element is not a class`() {
    val invalidInputFile = JavaFileObjects.forSourceString(
      "jsonapi.A",
      """
      package jsonapi;

      @Resource(type = "A")
      public enum A {
      }
      """.trimIndent()
    )

    Truth.assert_()
      .about(JavaSourcesSubjectFactory.javaSources())
      .that(listOf(invalidInputFile))
      .processedWith(JsonApiProcessor())
      .failsToCompile()
  }
}
