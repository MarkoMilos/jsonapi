package jsonapi

import java.io.File

enum class JsonFile(val fileName: String) {
  DOCUMENT_ARTICLE_SINGLE("document_article_single.json"),
  DOCUMENT_ARTICLE_SINGLE_DUPLICATED_RESOURCES("document_article_single_duplicated_resources.json"),
  DOCUMENT_ARTICLE_SINGLE_SERIALIZED("document_article_single_serialized.json"),
  DOCUMENT_ARTICLE_SINGLE_SERIALIZED_NO_INCLUDED("document_article_single_serialized_no_included.json"),
  DOCUMENT_ARTICLE_SINGLE_SERIALIZED_DOCUMENT_ONLY_INCLUDED("document_article_single_serialized_document_only_included.json"),
  DOCUMENT_ARTICLE_COLLECTION("document_article_collection.json"),
  DOCUMENT_ARTICLE_COLLECTION_DUPLICATED_RESOURCES("document_article_collection_duplicated_resources.json"),
  DOCUMENT_ARTICLE_COLLECTION_SERIALIZED("document_article_collection_serialized.json"),
  DOCUMENT_META("document_meta.json"),
  DOCUMENT_ERROR("document_error.json"),
  RESOURCE_ARTICLE("resource_article.json"),
  RESOURCE_ARTICLE_NON_STANDARD_NAMES("resource_article_non_standard_names.json"),
  RESOURCE_COMMENT("resource_comment.json"),
  RELATIONSHIPS("relationships.json"),
  RELATIONSHIP_TO_ONE("relationship_to_one.json"),
  RELATIONSHIP_TO_ONE_EMPTY("relationship_to_one_empty.json"),
  RELATIONSHIP_TO_MANY("relationship_to_many.json"),
  RELATIONSHIP_TO_MANY_EMPTY("relationship_to_many_empty.json"),
  LINKS("links.json"),
  LINK_OBJECT_FULL("link_object_full.json"),
  LINK_OBJECT_HREF_ONLY("link_object_href_only.json"),
  LINK_OBJECT_SINGLE_HREFLANG("link_object_single_hreflang.json"),
  META("meta.json"),
  JSON_API_OBJECT("json_api_object.json"),
  ERROR("error.json"),
  INTEGRATION_DESERIALIZE("integration_deserialize.json"),
  INTEGRATION_SERIALIZE("integration_serialize.json")
}

/** Read file defined with [fileName] from resources as UTF8 string. */
fun read(fileName: String): String {
  return File("src/test/resources/$fileName").readText()
}

/**
 * Read file defined with [jsonFile] from resources as UTF8 string.
 * Use [simplify] to remove new lines, tabs, and spaces from json string (default false).
 */
fun read(jsonFile: JsonFile, simplify: Boolean = false): String {
  val string = read(jsonFile.fileName)
  return if (simplify) string.replace(Regex("""[\n\t\s]"""), "") else string
}

/** Remove new lines, tabs and white spaces from string **/
internal fun String.inlineJson(): String {
  return this
    .replace("\n", "")
    .replace("\t", "")
    .replace(" ", "")
    .trimMargin()
}
