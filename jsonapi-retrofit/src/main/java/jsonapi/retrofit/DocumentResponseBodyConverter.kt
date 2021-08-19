package jsonapi.retrofit

import jsonapi.Document
import okhttp3.ResponseBody
import retrofit2.Converter

internal class DocumentResponseBodyConverter(
  private val delegate: Converter<ResponseBody, Document<*>>
) : Converter<ResponseBody, Any> {

  override fun convert(responseBody: ResponseBody): Any? {
    val document = delegate.convert(responseBody)
    return document?.dataOrThrow()
  }
}
