package jsonapi

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

@JsonQualifier
@Target(FIELD)
@Retention(RUNTIME)
annotation class LinksObject
