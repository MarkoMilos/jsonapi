# Keep all classes having fields or methods annotated with json api annotations
-keepclasseswithmembers class * { @jsonapi.* <fields>; }
-keepclasseswithmembers class * { @jsonapi.* <methods>; }

# Keep names of the fields used for reflective serialization
-keepclassmembers @jsonapi.Resource class * { <fields>; }
