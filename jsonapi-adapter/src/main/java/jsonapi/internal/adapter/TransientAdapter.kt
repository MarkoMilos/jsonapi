package jsonapi.internal.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jsonapi.BindRelationship
import jsonapi.Id
import jsonapi.Lid
import jsonapi.LinksObject
import jsonapi.MetaObject
import jsonapi.RelationshipsObject
import jsonapi.internal.FactoryDelegate
import java.lang.reflect.Type
import jsonapi.Type as ResourceType

/** Adapter that will skip serialization / deserialization for the given type. */
internal class TransientAdapter<T> : JsonAdapter<T>() {

  override fun fromJson(reader: JsonReader): T? {
    // Skip this value but don't throw if passed reader is configured to do so
    val wasFailOnUnknown = reader.failOnUnknown()
    reader.setFailOnUnknown(false)
    reader.skipValue()
    reader.setFailOnUnknown(wasFailOnUnknown)
    return null
  }

  override fun toJson(writer: JsonWriter, value: T?) {
    // Set serializeNulls to false so that this serialization
    // is completely removed from the result json
    val wasSerializeNulls = writer.serializeNulls
    writer.serializeNulls = false
    writer.nullValue()
    writer.serializeNulls = wasSerializeNulls
  }

  companion object {
    private val TRANSIENT_ANNOTATIONS = listOf(
      BindRelationship::class.java,
      ResourceType::class.java,
      Id::class.java,
      Lid::class.java,
      RelationshipsObject::class.java,
      LinksObject::class.java,
      MetaObject::class.java,
    )

    internal val FACTORY = object : FactoryDelegate {
      override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
        parent: Factory
      ): JsonAdapter<*>? {
        // If there are no annotations don't look for JsonQualifier annotations
        if (annotations.isEmpty()) return null

        // Check if type has one of the library annotations and
        // return adapter that will skip serialization/deserialization
        return TRANSIENT_ANNOTATIONS
          .asSequence()
          .mapNotNull {
            val hasAnnotation = Types.nextAnnotations(annotations, it) != null
            if (hasAnnotation) TransientAdapter<Any>() else null
          }
          .firstOrNull()
      }
    }
  }
}
