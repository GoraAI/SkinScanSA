# Spec Delta: Real ML Implementation

## Files to Modify

### 1. ModelDownloadManager.kt

**Current:**
```kotlin
const val MODEL_URL = "" // No URL for MVP
private const val MODEL_AVAILABLE_FOR_DOWNLOAD = false
```

**Change to:**
```kotlin
const val MODEL_NAME = "gemma-3n-e2b-it"
const val MODEL_FILE_NAME = "gemma-3n-e2b-it-int4.litertlm"
const val MODEL_SIZE_BYTES = 350_000_000L // ~350MB
const val MODEL_URL = "https://huggingface.co/google/gemma-3n-e2b-it/resolve/main/gemma-3n-e2b-it-int4.litertlm"
const val MODEL_SHA256 = "<actual_sha256_hash>" // Get from HuggingFace
private const val MODEL_AVAILABLE_FOR_DOWNLOAD = true
```

### 2. LLMInference.kt

**Add LiteRT LM loading:**
```kotlin
import com.google.ai.edge.litert.LiteRtModel
import com.google.ai.edge.litert.LiteRtSession

private var liteRtSession: LiteRtSession? = null

private suspend fun loadRealModel(modelPath: String) {
    val model = LiteRtModel.createFromFile(modelPath)
    liteRtSession = LiteRtSession.create(model)
}

private suspend fun runLLMInference(prompt: String): String {
    val session = liteRtSession ?: throw Exception("Model not loaded")
    return session.generateText(prompt, maxTokens = 256)
}
```

### 3. SkinAnalysisInference.kt

**Replace FaceDetectionService with FaceLandmarker:**
```kotlin
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

private lateinit var faceLandmarker: FaceLandmarker

fun initialize(): Boolean {
    val options = FaceLandmarker.FaceLandmarkerOptions.builder()
        .setBaseOptions(BaseOptions.builder().setModelAssetPath("face_landmarker.task").build())
        .setNumFaces(1)
        .setMinFaceDetectionConfidence(0.5f)
        .setMinTrackingConfidence(0.5f)
        .build()
    faceLandmarker = FaceLandmarker.createFromOptions(context, options)
    return true
}

private fun extractZoneFromLandmarks(
    landmarks: List<NormalizedLandmark>,
    indices: List<Int>,
    imageWidth: Int,
    imageHeight: Int
): RectF {
    val points = indices.map { landmarks[it] }
    val minX = points.minOf { it.x() } * imageWidth
    val maxX = points.maxOf { it.x() } * imageWidth
    val minY = points.minOf { it.y() } * imageHeight
    val maxY = points.maxOf { it.y() } * imageHeight
    return RectF(minX, minY, maxX, maxY)
}
```

### 4. All Screen Files - Bottom Padding

**Add to imports:**
```kotlin
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
```

**Modify Scaffold content:**
```kotlin
Scaffold(
    // ... existing params
    contentWindowInsets = WindowInsets(0) // Disable default insets
) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        // content
    }
}
```

**Or for LazyColumn:**
```kotlin
LazyColumn(
    contentPadding = PaddingValues(
        top = paddingValues.calculateTopPadding(),
        bottom = paddingValues.calculateBottomPadding() +
                 WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    )
)
```

## New Files

### assets/face_landmarker.task
- Download from MediaPipe: https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task
- Place in `app/src/main/assets/`

## Dependencies

**libs.versions.toml:**
```toml
# Already present, verify versions
litert = "1.0.1"
mediapipe = "0.10.18"
```

**build.gradle.kts:**
```kotlin
// Add if not present
implementation("com.google.ai.edge.litert:litert-genai:1.0.0")
```

## Landmark Index Reference

```
FOREHEAD: [10, 67, 69, 104, 105, 108, 109, 151, 337, 338, 297, 299, 333, 334]
LEFT_CHEEK: [50, 101, 117, 118, 119, 100, 126, 142, 36, 205, 206, 207]
RIGHT_CHEEK: [280, 330, 346, 347, 348, 329, 355, 371, 266, 425, 426, 427]
NOSE: [1, 2, 4, 5, 6, 168, 197, 195, 5, 4, 45, 275]
CHIN: [152, 148, 149, 150, 175, 176, 177, 178, 379, 378, 377, 400]
```

## Error Handling

```kotlin
// Fallback when model unavailable
fun generateExplanation(prompt: String): Result<String> {
    return if (modelDownloadManager.isOnDeviceAIAvailable()) {
        runLLMInference(prompt)
    } else {
        // Fall back to templates with clear messaging
        Result.success(generateTemplateResponse(prompt))
    }
}
```
