package jsonapi

import com.squareup.moshi.JsonQualifier

@JsonQualifier
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ToOne(val name: String)
