# JSON:API for Java & Kotlin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.markomilos.jsonapi/jsonapi-adapters/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.markomilos.jsonapi/jsonapi-adapters)

Library for streamlined use of JSON:API using Kotlin and Java built on top of a modern json
library [Moshi](https://github.com/square/moshi).

The library contains both models defined per JSON:API specification and adapters for converting these models to/from
json.

### About JSON:API

JSON:API is a specification for how a client should request that resources be fetched or modified, and how a server
should respond to those requests.

JSON:API is designed to minimize both the number of requests and the amount of data transmitted between clients and
servers. This efficiency is achieved without compromising readability, flexibility, or discoverability.

Read more about JSON:API specification [here](https://jsonapi.org/).

# Adapters

## Usage

Define resources:

```kotlin
@Resource("people")
class Person(
  @Id val id: String?,
  val name: String
)
```

```kotlin
@Resource("comments")
class Comment(
  @Id val id: String?,
  val body: String,
  @ToOne("author") val author: Person?
)
```

```kotlin
@Resource("articles")
class Article(
  @Id val id: String?,
  val title: String,
  @ToOne("author") val author: Person?,
  @ToMany("comments") val comments: List<Comment>?
)
```

Create `JsonApiFactory` with registered resources:

```kotlin
val factory = JsonApiFactory.Builder()
  .addType(Person::class.java)
  .addType(Comment::class.java)
  .addType(Article::class.java)
  .build()
```

*Alternatively you can use the annotation processor to avoid manually registering each resource (see the section below).*

Add created `JsonApiFactory` as first in factory chain to `Moshi`:

```kotlin
val moshi = Moshi.Builder()
  .add(factory)
  .build()
```

Create adapter for `Document` type:

```kotlin
val adapter = moshi.adapter<Document<Article>>(type)
```

Deserialization:

```kotlin
// Deserialize document from json
val document = adapter.fromJson("{...}")

// Get primary resource(s) from document
val article = document.dataOrNull()

// Relationship values from included are bound to field
val author = article.author  
```

Serialization:

```kotlin
// Create resource
val article = Article("1", "They're taking the hobbits to Isengard!", author, comments)

// Create document with resource, also add links and meta
val document = Document.with(article)
  .links(Links("self" to "http://example.com/articles/1"))
  .meta(Meta("copyright" to "Copyright 2015 Example Corp."))
  .build()

// Serialize document to json string
val json = adapter.toJson(document)
```

## Document
`Document` defines top level entity for JSON:API. It is a generic class accepting type of primary resource e.g. `Document<Article>`.

Create document with resource
```kotlin
Document.with(resource)
  .included(resource1, resource2)
  .links(links)
  .meta(meta)
  .build()
```

Configure how included are serialized (*by default included are processed from primary resource relationships*):
```kotlin
Document.with(resource)
  .includedSerialization(NONE) // Don't serialize included
  .build()
```

Create error document
```kotlin
Document.from(errors)
```

Document api
```
data: T?
included: List<Any>?
errors: List<Error>?
links: Links?
meta: Meta?
jsonapi: JsonApiObject?

hasData(): Boolean
hasErrors(): Boolean
hasMeta(): Boolean

dataOrNull(): T?
dataOrThrow(): T?
requireData(): T
dataOrDefault(data: T) : T
dataOrElse(block: (List<Error>?) -> T): T

errorsOrEmpty(): List<Error>
throwIfErrors()
```

Allowed document types are one of:
- single resource (e.g. `Article`) or collection of resources (e.g. `List<Article>`)
- single resource object (`ResourceObject`) or collection of resource objects (`List<ResourceObject>`)
- single resource identifier (`ResourceIdentifire`) or collection of resource identifiers (`List<ResourceIdentifier>`)
- `Void`/`Nothing` for empty documents without primary resource such are error documents or meta only documents

## Resource relationships

To define related resource fields use `ToOne` and `ToMany` annotations.

- `ToOne` - defines to-one relationship and should target field of resource type (e.g. `Article`)
- `ToMany` - defines to-many relationship and should target field whose type is a `List` or `Collection` of resources (
  e.g. `List<Article>`)

Example:

```kotlin
@Resource("articles")
class Article(
  @ToOne("author") val author: Person?,
  @ToMany("comments") val comments: List<Comment>?
)
```

Library uses this annotation info during serialization/deserialization to:

- **Deserialization** - perform a lookup on resource relationships object for relationships with th given name and, if
  found, will bind matching resource from `included` (if any) to target field
- **Serialization** - generate proper `relationships` member based on the value of the annotated field and add the value
  to `included` resources if not already there or within primary resources

## Resource standard members

Library handles conversion for the
following [standard resource object members](https://jsonapi.org/format/#document-resource-objects):
`type`, `id`, `lid`, `relationships`, `links`, and `meta`.

Member `attributes` (containing resource data) is converted using a delegate adapter down the chain. Depending on your
configuration of `Moshi` and definition of resource class these could be (in this order):

- your custom adapter for the given type (if any)
- codegen adapter (if kotlin codegen module is used)
- reflection adapter (either for Java or Kotlin)

Resource object members can be bound to a resource with annotations as shown in the example below also showing the full
api of this library for defining resources.

```kotlin
@Resource("articles")
class Article(
  // Standard resource object members
  @Type val type: String?,
  @Id val id: String?,
  @Lid val lid: String?,
  @RelationshipsObject val relationships: Relationships?,
  @LinksObject val links: Links?,
  @MetaObject val meta: Meta?,
  
  // Attributes
  var title: String?,
  // ... 
  
  // Relationships
  @ToOne("author") val author: Person?,
  @ToMany("comments") val comments: List<Comment>?
  // ...
)
```

## Annotations

| Annotation            | Target element    | Target type                                   | Description                                                                  |
|-----------------------|-------------------|-----------------------------------------------|------------------------------------------------------------------------------|
| `@Resource`           | class             | -                                             | Defines resource class            |
| `@Type`               | field or property | `String`                                      | Binds `type` member to field. For serialization value from this field will be used for `type` member. If this is not defined value from `@Resource` annotation is used for `type` member.            |
| `@Id`                 | field or property | `String`                                      | Bind `id` member to field. For serialization `id`  or `lid` is required.            |
| `@Lid`                | field or property | `String`                                      | Bind `lid` member to field. For serialization `id`  or `lid` is required. |
| `@RelationshipObject` | field or property | `Relationships`                               | Bind values from `relationships` member to field. For serialization relationships defined with this field will override ones generated from `ToOne` and `ToMany` field values.           |
| `@LinksObject`        | field or property | `Links`                                       | Bind `links` member to field.            |
| `@MetaObject`         | field or property | `Meta`                                        | Bind `meta` member to field.            |
| `@ToOne`              | field or property | `@Resource` class                             | Bind relationship from/to document `included` member. For serialization value for `relationships` member is generated for this field. | 
| `@ToMany`             | field or property | `Collection` or `List` of `@Resource` classes | Bind relationship from/to document `included` member. For serialization value for `relationships` member is generated for this field. |

## Custom adapters

You can register a custom adapter for a resource type. Make sure to register adapter after `JsonApiFactory` since Moshi
respects registration order.

```kotlin
Moshi.Builder()
  .add(factory)
  .add(MyCustomArticleAdapter())
  .build()
```

For deserialization library will delegate `attributes` object conversion to the adapter down the chain meaning that the
registered adapter will receive only `attributes` json - e.g. for the following resource

```json
{
  "type": "articles",
  "id": "1",
  "attributes": {
    "title": "Some Title",
    "slug": "slug",
    "likes": 10
  }
}
```

custom adapter receives the following `attributes` object for conversion

```json
{
  "title": "Some Title",
  "slug": "slug",
  "likes": 10
}
```

For serialization library will delegate resource value to the registered custom adapter. The custom adapter should
serialize only values relevant for `attributes` object since all other members are handled by the library.

## Download

Download the latest JAR or depend via Maven:
```xml
<dependency>
    <groupId>com.markomilos.jsonapi</groupId>
    <artifactId>jsonapi-adapters</artifactId>
    <version>1.0.0</version>
</dependency>
```

or Gradle:
```groovy
implementation("com.markomilos.jsonapi:jsonapi-adapters:1.0.0")
```

# Annotation processor

Document `included` members is defined as an array of resource objects that are related to the primary data and/or each
other (“included resources”). Library will try to deserialize each resource form that array to correct type based on
resource object `type` member. In order for library to know which class should be deserialized all resources are
required to be registered with `JsonApiFactory`:

```kotlin
JsonApiFactory.Builder()
  .addType(Article::class.java)
  .addType(Comment::class.java)
  .addType(Person::class.java)
  // ... many other resources ...
  .build()
```

Registering resources manually as shown in the example above is a boilerplate that can be avoided with the annotation
processor artifact. Annotation processor will scan source for all classes annotated with `@Resource` and will
generate `JsonApi` class with the following static methods:

```Java
public final class JsonApi {
  // List of all resource types
  public static List<Class<?>> resources();

  // Default factory built with all resources
  public static JsonAdapter.Factory factory();
}
```

You can then use `JsonApiFactory` without building it manually:

```kotlin
Moshi.Builder()
  .add(JsonApi.factory())
  .build()
```

## Download

Download the latest JAR or depend via Maven:
```xml
<dependency>
    <groupId>com.markomilos.jsonapi</groupId>
    <artifactId>jsonapi-compiler</artifactId>
    <version>1.0.0</version>
</dependency>
```

or Gradle:

```groovy
kapt("com.markomilos.jsonapi:jsonapi-compiler:1.0.0")
```

# Retrofit

When used with Retrofit definition of service could look something like the following:

```kotlin
interface Service {
  @GET("/")
  suspend fun article(): Document<Article>
}
```

Resource(s) needs to be unwrapped from `Document` when returned as a result.

```kotlin
val document = service.article()
if (!document.hasErrors()) {
  // Document does not have errors, handle result
  val article = document.data
  // ....
} else {
  // Document has errors throw or process errors
  throw Exception()
}
```

If you don't want/need to work with `Document` class and you want to send/receive resources directly you can use the
retrofit converter from `jsonapi.retrofit` artifact.

Adding `@Document` annotation to service definition like in the example below will unwrap the document and return
`Article` resource. In case of errors, an `ErrorsException` is thrown containing `errors` from the `Document`.

```kotlin
interface Service {
  @Document
  @GET("/")
  fun article(): Article
}
```

`@Document` annotation works for body parameters wrapping the target resource to `Document` before serialization.
Example:

```kotlin
interface Service {
  @POST("/")
  fun createArticle(@Document @Body article: Article)
}
```

## Download

Download the latest JAR or depend via Maven:
```xml
<dependency>
    <groupId>com.markomilos.jsonapi</groupId>
    <artifactId>jsonapi-retrofit</artifactId>
    <version>1.0.0</version>
</dependency>
```

or Gradle:
```groovy
implementation("com.markomilos.jsonapi:jsonapi-retrofit:1.0.0")
```

# R8 / ProGuard

Library keeps classes having json api annotations on fields and properties. Also, library keeps names of fields for kept
classes annotated with @Resource annotation since these are meant for serialization and thus should not be obfuscated.

If you are using R8 the shrinking and obfuscation rules are included automatically.

ProGuard users must manually add the options from 
[jsonapi-adapters.pro](https://github.com/MarkoMilos/jsonapi/blob/master/jsonapi-adapters/src/main/resources/META-INF/proguard/jsonapi-adapters.pro).

# License

Copyright 2021 Marko Milos.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
