package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import jsonapi.JsonFormatException
import jsonapi.Link
import jsonapi.Link.LinkObject
import jsonapi.Link.URI
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.rawType

internal class LinkAdapter(moshi: Moshi) : JsonAdapter<Link>() {

  private val linkObjectAdapter = moshi.adapter(LinkObject::class.java)

  override fun fromJson(reader: JsonReader): Link? {
    return when (reader.peek()) {
      Token.NULL -> reader.nextNull()
      Token.STRING -> URI(reader.nextString())
      Token.BEGIN_OBJECT -> linkObjectAdapter.fromJson(reader)
      else -> throw JsonFormatException(
        "A link MUST be represented as either:\n"
          + " * a string whose value is a URI-reference pointing to the link’s target\n"
          + " * a link object that represents a web link per specification\n"
          + "but was "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }
  }

  override fun toJson(writer: JsonWriter, link: Link?) {
    when (link) {
      is URI -> writer.value(link.value)
      is LinkObject -> linkObjectAdapter.toJson(writer, link)
      null -> writer.nullValue()
    }
  }

  companion object {
    internal val FACTORY = FactoryDelegate { type, annotations, moshi, _ ->
      if (annotations.isEmpty() && type.rawType() == Link::class.java) LinkAdapter(moshi) else null
    }
  }
}
