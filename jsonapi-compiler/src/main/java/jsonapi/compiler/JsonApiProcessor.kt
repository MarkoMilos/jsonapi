package jsonapi.compiler

import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import com.squareup.moshi.JsonAdapter
import jsonapi.Resource
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class JsonApiProcessor : AbstractProcessor() {

  private var firstProcessingPass = true

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latestSupported()
  }

  override fun getSupportedAnnotationTypes(): MutableSet<String> {
    return mutableSetOf(Resource::class.java.canonicalName)
  }

  override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    // Process only the first round by checking and rising the flag
    if (firstProcessingPass) {
      firstProcessingPass = false
    } else {
      return false
    }

    // Collect all elements that are resource classes annotated with @Resource
    val resourceElements = mutableListOf<TypeElement>()
    roundEnv.getElementsAnnotatedWith(Resource::class.java).forEach { element ->
      // Assert that this element is class
      if (element.kind == ElementKind.CLASS) {
        resourceElements.add(element as TypeElement)
      } else {
        error("Only classes can be annotated with @Resource annotation!")
        return false
      }
    }

    // List of resources types - List<Class<*>> / List<Class<? extends Object>>
    val resourceTypes = ParameterizedTypeName.get(
      ClassName.get(List::class.java),
      ParameterizedTypeName.get(ClassName.get(Class::class.java), WildcardTypeName.subtypeOf(Any::class.java))
    )

    // Define static method that returns list of collected resource types
    val resourcesMethod = MethodSpec.methodBuilder("resources")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(resourceTypes)
      .addStatement("\$T types = new \$T<>()", resourceTypes, ClassName.get(ArrayList::class.java))
      .apply {
        resourceElements.forEach { element ->
          addStatement("types.add(\$T.class)", ClassName.get(element))
        }
      }
      .addStatement("return types")
      .build()

    // Define static method that returns default moshi factory
    val factoryMethod = MethodSpec.methodBuilder("factory")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(ClassName.get(JsonAdapter.Factory::class.java))
      .addStatement("return new JsonApiFactory.Builder().addTypes(resources()).build()")
      .build()

    // Define enclosing type (class) for static methods
    val type = TypeSpec.classBuilder("JsonApi")
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addMethod(resourcesMethod) // resources() : List<Class<*>>
      .addMethod(factoryMethod)   // factory() : JsonAdapter.Factory
      .build()

    // Create a file for defined type and write the java content
    val javaFile = JavaFile.builder("jsonapi", type).build()
    try {
      javaFile.writeTo(processingEnv.filer)
    } catch (e: IOException) {
      error("Failed to generate a JsonApi file.\n" + e.printStackTrace())
      e.printStackTrace()
    }

    return false
  }

  private fun error(message: String) {
    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
  }
}
