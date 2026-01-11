# Spec Delta: production-readiness

## ADDED

### UserSessionManager.kt (new file in data/session/)
```kotlin
@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Uses EncryptedSharedPreferences
    val userId: String  // Returns device-unique UUID
    val isFirstLaunch: Boolean
    fun clearSession()  // For POPIA deletion
}
```

### Room Migrations (new files in data/db/migrations/)
```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Preserve data during schema changes
    }
}
```

## MODIFIED

### SkinAnalysisInference.kt
```diff
- // For MVP, uses mock results. Real TFLite integration pending model file
- fun analyze(bitmap: Bitmap): SkinAnalysisResult {
-     return generateMockResults()  // Always returns same fake data
- }

+ // Real skin analysis using MediaPipe/TFLite
+ fun analyze(bitmap: Bitmap): SkinAnalysisResult {
+     val faceRegions = detectFaceRegions(bitmap)
+     val skinTone = analyzeSkinTone(faceRegions)
+     val concerns = detectConcerns(bitmap, faceRegions)
+     return SkinAnalysisResult(concerns, skinTone, ...)
+ }
```

### ProfileViewModel.kt
```diff
- companion object {
-     private const val DEFAULT_USER_ID = "default_user"
- }

+ private val userId: String
+     get() = userSessionManager.userId
```

### HistoryViewModel.kt
```diff
- companion object {
-     private const val DEFAULT_USER_ID = "default_user"
- }

+ private val userId: String
+     get() = userSessionManager.userId
```

### TimelineViewModel.kt
```diff
- companion object {
-     private const val DEFAULT_USER_ID = "default_user"
- }

+ private val userId: String
+     get() = userSessionManager.userId
```

### ScanViewModel.kt
```diff
- fun analyzeFace(image: Bitmap, userId: String) {

+ fun analyzeFace(image: Bitmap) {
+     val userId = userSessionManager.userId
```

### ScanScreen.kt
```diff
- viewModel.analyzeFace(bitmap, "default_user")

+ viewModel.analyzeFace(bitmap)
```

### DeleteAllUserDataUseCase.kt
```diff
- companion object {
-     private const val DEFAULT_USER_ID = "default_user"
- }
- scanResultDao.deleteAllByUser(DEFAULT_USER_ID)

+ val userId = userSessionManager.userId
+ scanResultDao.deleteAllByUser(userId)
+ userSessionManager.clearSession()
```

### DatabaseModule.kt
```diff
- .fallbackToDestructiveMigration()

+ .addMigrations(MIGRATION_5_6)
```

### ScanScreen.kt (FaceGuideOverlay)
```diff
- Canvas(modifier = Modifier.fillMaxSize()) {

+ Canvas(
+     modifier = Modifier
+         .fillMaxSize()
+         .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
+ ) {
```

### ModelDownloadManager.kt
```diff
- private const val MVP_MOCK_MODEL = true
- if (MVP_MOCK_MODEL) {
-     simulateDownload()
- }

+ private const val MVP_MOCK_MODEL = false
+ // Real download implementation OR
+ // Clear fallback with user-facing message
```

### LLMInference.kt
```diff
- private const val MVP_USE_TEMPLATES = true

+ // Either real LLM inference
+ // OR honest template usage without "Gemma" branding
```

## REMOVED

- All hardcoded "default_user" strings
- Mock skin analysis results (generateMockResults())
- Fake download simulation in ModelDownloadManager
- fallbackToDestructiveMigration() usage
