package jsonapi.retrofit

import jsonapi.Document
import okhttp3.RequestBody
import retrofit2.Converter

internal class DocumentRequestBodyConverter(
  private val delegate: Converter<Document<*>, RequestBody>
) : Converter<Any, RequestBody> {

  override fun convert(value: Any): RequestBody? {
    val document = Document.from(value)
    return delegate.convert(document)
  }
}
