# Add project specific ProGuard rules here.

# Keep LiteRT classes
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.ai.edge.litert.** { *; }

# Keep MediaPipe classes
-keep class com.google.mediapipe.** { *; }

# Keep SQLCipher classes
-keep class net.sqlcipher.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Proto DataStore classes
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
