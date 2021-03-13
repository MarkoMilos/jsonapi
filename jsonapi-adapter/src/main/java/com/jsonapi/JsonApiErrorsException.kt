package com.jsonapi

import com.jsonapi.model.Error

class JsonApiErrorsException(
  val errors: List<Error> = emptyList()
) : Exception()