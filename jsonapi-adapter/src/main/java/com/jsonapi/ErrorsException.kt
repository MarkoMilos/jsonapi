package com.jsonapi

/**
 * Thrown for documents that contains [errors].
 *
 * @param errors list of [Error] from source [Document].
 *
 * @see Document
 */
class ErrorsException(val errors: List<Error>) : Exception()
