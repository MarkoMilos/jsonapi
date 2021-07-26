package com.jsonapi.internal

import com.squareup.moshi.JsonQualifier

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class PolymorphicResource
