# Proguard rules for Photo Tool
-keepattributes *Annotation*
-dontwarn java.lang.invoke.**

# Keep app code & engines
-keep class com.rvbhati.phototool.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# ZXing QR Code
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# AndroidX & Compose
-dontwarn androidx.compose.**
-dontwarn androidx.**

# Coil & OkHttp
-dontwarn coil.**
-dontwarn okhttp3.**
-dontwarn okio.**
