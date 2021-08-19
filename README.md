# About JSON:API

JSON:API is a specification for how a client should request that resources be fetched or modified, and how a server
should respond to those requests.

JSON:API is designed to minimize both the number of requests and the amount of data transmitted between clients and
servers. This efficiency is achieved without compromising readability, flexibility, or discoverability.

# Deviation from specification

- `hreflang` is specified as string or an array of strings indicating the language(s) of the link’s target. This library
  implements `hreflang` as array of strings only. Deserialization and serialization will work properly since string
  values will be deserialized as array with size 1 and arrays with size equal to 1 will be serialized as string.

# Unsupported features

- Extensions as specified in specification v1.1 [here](https://jsonapi.org/format/1.1/#extensions)
- Profiles as specified in specification v1.1 [here](https://jsonapi.org/format/1.1/#profiles)

# R8 / ProGuard
Library keeps classes having json api annotations on fields and properties. 
Also, library keeps names of fields for kept classes annotated with @Resource annotation since these are meant for
serialization and thus should not be obfuscated.

If you are using R8 the shrinking and obfuscation rules are included automatically.

ProGuard users must manually add the options from [jsonapi-adapters.pro](https://github.com/MarkoMilos/jsonapi/blob/master/jsonapi-adapters/src/main/resources/META-INF/proguard/jsonapi-adapters.pro).