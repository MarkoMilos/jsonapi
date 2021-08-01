package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import jsonapi.JsonFormatException
import jsonapi.Resource
import jsonapi.ResourceObject
import jsonapi.internal.FactoryDelegate
import jsonapi.internal.bindResourceObject
import jsonapi.internal.rawType
import jsonapi.internal.readResourceObject
import jsonapi.internal.scan
import java.lang.reflect.Type

internal class ResourceTypeAdapter(
  moshi: Moshi,
  private val delegateAdapter: JsonAdapter<Any>,
  private val annotatedType: String,
  private val strictTypes: Boolean
) : JsonAdapter<Any>() {

  private val resourceObjectAdapter = moshi.adapter(ResourceObject::class.java)

  override fun fromJson(reader: JsonReader): Any? {
    // In case of a null value deserialize to null and consume token
    if (reader.peek() == Token.NULL) {
      return reader.nextNull()
    }

    // Assert that resource is JSON object
    if (reader.peek() != Token.BEGIN_OBJECT) {
      throw JsonFormatException(
        "Resource MUST be a JSON object but found "
          + reader.peek()
          + " on path "
          + reader.path
      )
    }

    // Scan json and read resource object without consuming source reader
    val resourceObject = reader.scan { resourceObjectAdapter.fromJson(it) } ?: return null

    // Target class that this adapter need to deserialize
    var target: Any? = null
    var hasAttributesMember = false

    // Look for attributes member and skip the rest
    val wasFailOnUnknown = reader.failOnUnknown()
    reader.setFailOnUnknown(false)
    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        // Attributes member is used to deserialize target class with delegate adapter
        NAME_ATTRIBUTES -> {
          reader.setFailOnUnknown(wasFailOnUnknown)
          target = delegateAdapter.fromJson(reader)
          hasAttributesMember = true
        }
        // Ignore other members
        else -> reader.skipValue()
      }
    }
    reader.endObject()
    reader.setFailOnUnknown(wasFailOnUnknown)

    // When strict types are enabled assert that deserialized type matches value provided with annotation
    if (strictTypes && resourceObject.type != annotatedType) {
      throw AssertionError(
        "Expected type "
          + annotatedType
          + " but found "
          + resourceObject.type
          + " on path "
          + reader.path
      )
    }

    // If attributes member is not found delegate empty json object to delegate adapter
    // in order to create default instance of the target class
    if (!hasAttributesMember) {
      target = delegateAdapter.fromJson("{}")
    }

    // If delegate adapter has deserialized target class as null respect it
    if (target == null) {
      return null
    }

    // Bind annotated fields of the target class instance with values from resource object
    bindResourceObject(target, resourceObject)

    return target
  }

  override fun toJson(writer: JsonWriter, value: Any?) {
    if (value == null) {
      writer.nullValue()
      return
    }

    // Read the resource object from value via reflection
    val resourceObject = readResourceObject(value)

    // When strict types are enabled assert that type to be serialized matches value provided with @Resource annotation
    if (strictTypes && resourceObject.type != annotatedType) {
      throw AssertionError(
        "Expected type "
          + annotatedType
          + " but found "
          + resourceObject.type
          + " on path "
          + writer.path
      )
    }

    writer.beginObject()
    // Write resource object members
    val token = writer.beginFlatten()
    resourceObjectAdapter.toJson(writer, resourceObject)
    writer.endFlatten(token)
    // Serialize target 'value' under attributes name with delegate adapter down the chain
    val attributes = delegateAdapter.toJson(value)
    // Write attributes only if not empty json {} or null
    if (attributes != "{}" && attributes != "null") {
      writer.name(NAME_ATTRIBUTES)
      writer.valueSink().use { it.writeUtf8(attributes) }
    }
    writer.endObject()
  }

  companion object {
    private const val NAME_ATTRIBUTES = "attributes"

    internal fun factory(strictTypes: Boolean) = object : FactoryDelegate {
      override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
        parent: Factory
      ): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null

        // This adapter applies only for types annotated with @Resource annotation
        val annotation = type.rawType().getAnnotation(Resource::class.java) ?: return null

        // Assert that valid type name is provided with annotation
        require(annotation.type.isNotBlank()) {
          "For [$type] type name provided with @Resource annotation was blank.\n" +
            "The values of type members MUST adhere to the same constraints as member names per specification."
        }

        // Request next adapter down the chain for this type by skipping this factory
        val delegateAdapter = moshi.nextAdapter<Any>(parent, type, annotations)

        return ResourceTypeAdapter(moshi, delegateAdapter, annotation.type, strictTypes)
      }
    }
  }
}
