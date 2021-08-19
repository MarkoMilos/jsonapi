package jsonapi.retrofit

import jsonapi.Document
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class DocumentConverterFactory private constructor() : Converter.Factory() {

  companion object {
    fun create() = DocumentConverterFactory()
  }

  override fun responseBodyConverter(
    type: Type,
    annotations: Array<out Annotation>,
    retrofit: Retrofit
  ): Converter<ResponseBody, *>? {
    // If there is no annotation this type cannot be handled by this factory
    if (annotations.none { it is jsonapi.retrofit.Document }) return null

    // Create Document<T> type wrapping the source type
    val documentType = Types.newParameterizedType(Document::class.java, type)

    // Create converter that will be used to convert ResponseBody
    val delegate: Converter<ResponseBody, Document<*>> =
      retrofit.nextResponseBodyConverter(this, documentType, annotations)

    return DocumentResponseBodyConverter(delegate)
  }

  override fun requestBodyConverter(
    type: Type,
    parameterAnnotations: Array<out Annotation>,
    methodAnnotations: Array<out Annotation>,
    retrofit: Retrofit
  ): Converter<*, RequestBody>? {
    // If there is no annotation for parameters this type cannot be handled by this factory
    if (parameterAnnotations.none { it is jsonapi.retrofit.Document }) return null

    // Create Document<T> type wrapping the source type
    val documentType = Types.newParameterizedType(Document::class.java, type)

    // Create converter that will be used to convert RequestBody
    val delegate: Converter<Document<*>, RequestBody> =
      retrofit.nextRequestBodyConverter(this, documentType, parameterAnnotations, methodAnnotations)

    return DocumentRequestBodyConverter(delegate)
  }
}
