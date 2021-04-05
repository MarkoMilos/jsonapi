package com.jsonapi

import java.io.File

enum class JsonFile(val fileName: String) {
  DOCUMENT_NULL_DATA("document_null_data.json"),
  DOCUMENT_EMPTY_COLLECTION("document_empty_collection.json"),
  DOCUMENT_ARTICLE_SINGLE("document_article_single.json"),
  DOCUMENT_ARTICLE_SINGLE_SIMPLE("document_article_single_simple.json"),
  DOCUMENT_ARTICLE_SINGLE_SIMPLE_NO_INCLUDED("document_article_single_simple_no_included.json"),
  DOCUMENT_ARTICLE_COLLECTION("document_article_collection.json"),
  DOCUMENT_ARTICLE_COLLECTION_SIMPLE("document_article_collection_simple.json"),
  DOCUMENT_META("document_meta.json"),
  DOCUMENT_ERROR("document_error.json"),
  DOCUMENT_ERROR_SIMPLE("document_error_simple.json"),
  RESOURCE_ARTICLE("resource_article.json"),
  RESOURCE_COMMENT("resource_comment.json"),
  RESOURCE_NO_ATTRIBUTES("resource_no_attributes.json"),
  RESOURCE_NON_STANDARD_NAMES("resource_non_standard_names.json"),
  RESOURCE_UNKNOWN_TYPE("resource_unknown_type.json"),
  RELATION_TO_ONE("relation_to_one.json"),
  RELATION_TO_ONE_EMPTY("relation_to_one_empty.json"),
  RELATION_TO_MANY("relation_to_many.json"),
  RELATION_TO_MANY_EMPTY("relation_to_many_empty.json"),
  RELATION_INVALID("relation_invalid.json"),
  LINKS("links.json"),
  LINK_OBJECT_FULL("link_object_full.json"),
  LINK_OBJECT_HREF_ONLY("link_object_href_only.json"),
  LINK_OBJECT_SINGLE_HREFLANG("link_object_single_hreflang.json"),
  META("meta.json")
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