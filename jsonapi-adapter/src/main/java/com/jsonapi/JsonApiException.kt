package com.jsonapi

/**
 * Exception indicating either unrecoverable mismatch from JSON:API specification found during processing
 * or violation of predefined and/or configured constraints.
 */
// TODO go trough all usages and try to replace where possible with IllegalArgument, IllegalState and similar exception
class JsonApiException : RuntimeException {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(cause: Throwable) : super(cause)
}
