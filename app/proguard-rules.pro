# DTOs do Retrofit (não podem ser ofuscados)
-keep class com.cresup.app.data.remote.dto.** { *; }

# Room entities e DAOs
-keep class com.cresup.app.data.local.entity.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Retrofit + OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn okio.**
-keep interface retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Remove logs em produção
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
