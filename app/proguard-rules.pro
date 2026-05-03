# Retrofit + Gson — manter DTOs
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.iptv.tv.data.remote.dto.** { *; }

# Room — manter entidades
-keep class com.iptv.tv.data.local.entity.** { *; }

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
