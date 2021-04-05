package com.jsonapi

class JsonApiErrorsException(val errors: List<Error>) : Exception()