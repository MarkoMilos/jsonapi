package com.jsonapi

import com.jsonapi.Document.Errors

class JsonApiErrorsException(val errors: Errors) : Exception()