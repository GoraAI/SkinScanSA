package com.skinscan.sa.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Skin Analysis Inference using LiteRT (TensorFlow Lite)
 *
 * Story 2.4: Skin Analysis with Loading Feedback
 * - Runs EfficientNet-Lite skin analysis model
 * - Detects skin concerns: hyperpigmentation, acne, dryness, oiliness, wrinkles
 * - Estimates Fitzpatrick skin type (I-VI)
 * - Zone-based analysis for 5 face regions
 *
 * Model inputs: 224x224 RGB image (normalized 0-1)
 * Model outputs:
 *   - skin_concerns: [batch, 5] (confidence scores for each concern)
 *   - fitzpatrick_type: [batch, 6] (probability for types I-VI)
 *   - zone_analysis: [batch, 5, 5] (5 zones x 5 concerns)
 */
@Singleton
class SkinAnalysisInference @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SkinAnalysisInference"
        private const val MODEL_NAME = "skin_analysis_model.tflite"
        private const val INPUT_SIZE = 224
        private const val PIXEL_SIZE = 3 // RGB
        private const val NUM_CONCERNS = 5
        private const val NUM_FITZPATRICK_TYPES = 6
        private const val NUM_ZONES = 5
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    /**
     * Skin concern types detected by the model
     */
    enum class SkinConcern(val displayName: String, val description: String) {
        HYPERPIGMENTATION("Dark Spots", "Uneven skin tone or dark patches"),
        ACNE("Acne", "Pimples, blackheads, or breakouts"),
        DRYNESS("Dryness", "Flaky, rough, or tight skin"),
        OILINESS("Oiliness", "Excess sebum or shiny appearance"),
        WRINKLES("Fine Lines", "Wrinkles or signs of aging")
    }

    /**
     * Face zones for zone-specific analysis
     */
    enum class FaceZone(val displayName: String) {
        FOREHEAD("Forehead"),
        LEFT_CHEEK("Left Cheek"),
        RIGHT_CHEEK("Right Cheek"),
        NOSE("Nose"),
        CHIN("Chin")
    }

    /**
     * Complete skin analysis result
     */
    data class SkinAnalysisResult(
        val overallConcerns: Map<SkinConcern, Float>,
        val fitzpatrickType: Int,
        val fitzpatrickConfidence: Float,
        val zoneAnalysis: Map<FaceZone, Map<SkinConcern, Float>>,
        val primaryConcerns: List<SkinConcern>,
        val analysisTimestamp: Long = System.currentTimeMillis()
    )

    /**
     * Initialize the TFLite interpreter with GPU acceleration if available
     */
    fun initialize(): Boolean {
        return try {
            val compatList = CompatibilityList()

            val options = Interpreter.Options().apply {
                if (compatList.isDelegateSupportedOnThisDevice) {
                    gpuDelegate = GpuDelegate(compatList.bestOptionsForThisDevice)
                    addDelegate(gpuDelegate)
                    Log.d(TAG, "GPU acceleration enabled")
                } else {
                    setNumThreads(4)
                    Log.d(TAG, "Using CPU with 4 threads")
                }
            }

            // For MVP, use mock model since we don't have the actual .tflite yet
            // In production, load from assets
            interpreter = try {
                val modelBuffer = loadModelFile(MODEL_NAME)
                Interpreter(modelBuffer, options)
            } catch (e: Exception) {
                Log.w(TAG, "Model file not found, using mock inference", e)
                null
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize interpreter", e)
            false
        }
    }

    /**
     * Run skin analysis inference on face image
     *
     * @param bitmap Face image bitmap
     * @return SkinAnalysisResult with all analysis data
     */
    fun analyze(bitmap: Bitmap): SkinAnalysisResult {
        // If model not loaded, return mock results for MVP
        if (interpreter == null) {
            return generateMockResults()
        }

        val inputBuffer = preprocessImage(bitmap)
        val outputs = runInference(inputBuffer)
        return postprocessOutputs(outputs)
    }

    /**
     * Preprocess image for model input
     * Resize to 224x224, normalize to 0-1, convert to ByteBuffer
     */
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            // Extract RGB and normalize to 0-1
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }

        return byteBuffer
    }

    /**
     * Run TFLite inference
     */
    private fun runInference(inputBuffer: ByteBuffer): InferenceOutputs {
        val concernsOutput = Array(1) { FloatArray(NUM_CONCERNS) }
        val fitzpatrickOutput = Array(1) { FloatArray(NUM_FITZPATRICK_TYPES) }
        val zoneOutput = Array(1) { Array(NUM_ZONES) { FloatArray(NUM_CONCERNS) } }

        val outputs = mapOf(
            0 to concernsOutput,
            1 to fitzpatrickOutput,
            2 to zoneOutput
        )

        interpreter?.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

        return InferenceOutputs(
            concerns = concernsOutput[0],
            fitzpatrick = fitzpatrickOutput[0],
            zones = zoneOutput[0]
        )
    }

    private data class InferenceOutputs(
        val concerns: FloatArray,
        val fitzpatrick: FloatArray,
        val zones: Array<FloatArray>
    )

    /**
     * Convert raw outputs to structured result
     */
    private fun postprocessOutputs(outputs: InferenceOutputs): SkinAnalysisResult {
        // Map concern scores
        val overallConcerns = SkinConcern.entries.mapIndexed { index, concern ->
            concern to outputs.concerns.getOrElse(index) { 0f }
        }.toMap()

        // Find Fitzpatrick type (highest probability)
        val fitzpatrickIndex = outputs.fitzpatrick.indices.maxByOrNull {
            outputs.fitzpatrick[it]
        } ?: 0
        val fitzpatrickType = fitzpatrickIndex + 1 // Types are 1-6
        val fitzpatrickConfidence = outputs.fitzpatrick[fitzpatrickIndex]

        // Map zone analysis
        val zoneAnalysis = FaceZone.entries.mapIndexed { zoneIndex, zone ->
            val zoneConcerns = SkinConcern.entries.mapIndexed { concernIndex, concern ->
                concern to outputs.zones.getOrElse(zoneIndex) { FloatArray(NUM_CONCERNS) }
                    .getOrElse(concernIndex) { 0f }
            }.toMap()
            zone to zoneConcerns
        }.toMap()

        // Find primary concerns (score > 0.4)
        val primaryConcerns = overallConcerns
            .filter { it.value > 0.4f }
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }

        return SkinAnalysisResult(
            overallConcerns = overallConcerns,
            fitzpatrickType = fitzpatrickType,
            fitzpatrickConfidence = fitzpatrickConfidence,
            zoneAnalysis = zoneAnalysis,
            primaryConcerns = primaryConcerns
        )
    }

    /**
     * Generate mock results for MVP (before real model is available)
     */
    private fun generateMockResults(): SkinAnalysisResult {
        Log.d(TAG, "Generating mock analysis results")

        val overallConcerns = mapOf(
            SkinConcern.HYPERPIGMENTATION to 0.78f,
            SkinConcern.DRYNESS to 0.65f,
            SkinConcern.ACNE to 0.32f,
            SkinConcern.OILINESS to 0.45f,
            SkinConcern.WRINKLES to 0.18f
        )

        val zoneAnalysis = mapOf(
            FaceZone.FOREHEAD to mapOf(
                SkinConcern.HYPERPIGMENTATION to 0.82f,
                SkinConcern.DRYNESS to 0.55f,
                SkinConcern.ACNE to 0.28f,
                SkinConcern.OILINESS to 0.62f,
                SkinConcern.WRINKLES to 0.15f
            ),
            FaceZone.LEFT_CHEEK to mapOf(
                SkinConcern.HYPERPIGMENTATION to 0.75f,
                SkinConcern.DRYNESS to 0.68f,
                SkinConcern.ACNE to 0.35f,
                SkinConcern.OILINESS to 0.38f,
                SkinConcern.WRINKLES to 0.20f
            ),
            FaceZone.RIGHT_CHEEK to mapOf(
                SkinConcern.HYPERPIGMENTATION to 0.80f,
                SkinConcern.DRYNESS to 0.70f,
                SkinConcern.ACNE to 0.30f,
                SkinConcern.OILINESS to 0.35f,
                SkinConcern.WRINKLES to 0.18f
            ),
            FaceZone.NOSE to mapOf(
                SkinConcern.HYPERPIGMENTATION to 0.60f,
                SkinConcern.DRYNESS to 0.40f,
                SkinConcern.ACNE to 0.45f,
                SkinConcern.OILINESS to 0.72f,
                SkinConcern.WRINKLES to 0.12f
            ),
            FaceZone.CHIN to mapOf(
                SkinConcern.HYPERPIGMENTATION to 0.68f,
                SkinConcern.DRYNESS to 0.58f,
                SkinConcern.ACNE to 0.42f,
                SkinConcern.OILINESS to 0.48f,
                SkinConcern.WRINKLES to 0.22f
            )
        )

        return SkinAnalysisResult(
            overallConcerns = overallConcerns,
            fitzpatrickType = 5, // Fitzpatrick V (brown skin)
            fitzpatrickConfidence = 0.89f,
            zoneAnalysis = zoneAnalysis,
            primaryConcerns = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.DRYNESS)
        )
    }

    /**
     * Load TFLite model from assets
     */
    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Release resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
    }
}
