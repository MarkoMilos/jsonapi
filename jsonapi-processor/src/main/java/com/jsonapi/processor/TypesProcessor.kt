package com.jsonapi.processor

import com.google.auto.service.AutoService
import com.jsonapi.Resource
import com.jsonapi.Type
import com.squareup.javapoet.*
import com.squareup.moshi.JsonAdapter
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

@AutoService(Processor::class)
class TypesProcessor : AbstractProcessor() {
  
  private var codeGenerated = false
  
  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latestSupported()
  }
  
  override fun getSupportedAnnotationTypes(): MutableSet<String> {
    return mutableSetOf(Type::class.java.canonicalName)
  }
  
  override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    // Process only the first round by checking and rising the flag
    if (codeGenerated) {
      return false
    } else {
      codeGenerated = true
    }
    
    // Collect all elements that are resource classes annotated with @Type
    val resourceElements = mutableListOf<TypeElement>()
    roundEnv.getElementsAnnotatedWith(Type::class.java).forEach { element ->
      if (isValidResourceElement(element)) {
        resourceElements.add(element as TypeElement)
      } else {
        // Error is already printed, abort further processing
        return false
      }
    }
    
    val listType = ClassName.get(List::class.java)
    val arrayListType = ClassName.get(ArrayList::class.java)
    // Class<? extends Resource>
    val classType = ParameterizedTypeName.get(
      ClassName.get(Class::class.java),
      WildcardTypeName.subtypeOf(Resource::class.java)
    )
    // List<Class<? extends Resource>
    val resourceTypes = ParameterizedTypeName.get(listType, classType)
    
    // Define static method that returns list of collected resource types
    val resourcesMethod = MethodSpec.methodBuilder("resources")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(resourceTypes)
      .addStatement("\$T types = new \$T<>()", resourceTypes, arrayListType)
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
      .addMethod(resourcesMethod)
      .addMethod(factoryMethod)
      .build()
    
    // Create a file for defined type and write the java content
    val javaFile = JavaFile.builder("com.jsonapi", type).build()
    try {
      javaFile.writeTo(System.out)
      javaFile.writeTo(processingEnv.filer)
    } catch (e: IOException) {
      error("Failed to generate a ResourceTypes file.\n" + e.printStackTrace())
      e.printStackTrace()
    }
    
    return false
  }
  
  private fun isValidResourceElement(element: Element): Boolean {
    // Assert that this element is class
    if (element.kind != ElementKind.CLASS) {
      error("Only classes can be annotated with JSON:API @Type annotation!")
      return false
    }
    
    // We know that it is a class we can cast it to TypeElement
    val classElement = element as TypeElement
    
    // Assert that this class extends from Resource
    var currentClass = classElement
    while (true) {
      val superclass = currentClass.superclass
      
      // Check if reached the root of inheritance tree
      if (superclass.kind == TypeKind.NONE) {
        error("$classElement annotated with @Type is not extending from ${Resource::class.simpleName}")
        return false
      }
      
      // If class extends from Resource stop the inheritance tree traversal
      if (superclass.toString() == Resource::class.qualifiedName) {
        break
      }
      
      // Moving up in inheritance tree
      currentClass = processingEnv.typeUtils.asElement(superclass) as TypeElement
    }
    
    // Element is a class that extends from Resource
    return true
  }
  
  private fun error(message: String) {
    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
  }
}