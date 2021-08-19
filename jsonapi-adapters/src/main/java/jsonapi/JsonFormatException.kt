package jsonapi

/** Exception indicating either unrecoverable mismatch from JSON:API specification found during processing. */
class JsonFormatException : RuntimeException {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(cause: Throwable) : super(cause)
}
