package jsonapi.retrofit

import java.io.File

enum class JsonFile(val fileName: String) {
  DOCUMENT_ARTICLE_SINGLE("document_article_single.json"),
  DOCUMENT_ARTICLE_COLLECTION("document_article_collection.json"),
  DOCUMENT_ERROR("document_error.json"),
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
