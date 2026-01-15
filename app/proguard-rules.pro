# Keep Kotlin serialization
-keepclassmembers class **$Companion {
    **;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.Serializer;
}
