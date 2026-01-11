package com.skinscan.sa.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Skin Analysis Inference using real image analysis
 *
 * Story 2.4: Skin Analysis with Loading Feedback
 * - Detects skin concerns: hyperpigmentation, acne, dryness, oiliness, wrinkles
 * - Estimates Fitzpatrick skin type (I-VI) from actual skin tone
 * - Zone-based analysis for 5 face regions
 *
 * Uses MediaPipe Face Detection for face localization and
 * pixel-based analysis for skin characteristics.
 */
@Singleton
class SkinAnalysisInference @Inject constructor(
    @ApplicationContext private val context: Context,
    private val faceDetectionService: FaceDetectionService
) {
    companion object {
        private const val TAG = "SkinAnalysisInference"
        private const val SAMPLE_SIZE = 50 // Number of pixels to sample per region
    }

    private var isInitialized = false

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
     * Initialize the inference engine
     */
    fun initialize(): Boolean {
        isInitialized = faceDetectionService.initialize()
        Log.d(TAG, "Skin analysis inference initialized: $isInitialized")
        return isInitialized
    }

    /**
     * Run skin analysis inference on face image
     *
     * @param bitmap Face image bitmap
     * @return SkinAnalysisResult with all analysis data
     */
    fun analyze(bitmap: Bitmap): SkinAnalysisResult {
        Log.d(TAG, "Analyzing face image (${bitmap.width}x${bitmap.height})")

        // Detect face to get bounding box
        val faceResult = faceDetectionService.detectFace(bitmap)

        return if (faceResult.faceDetected && faceResult.boundingBox != null) {
            analyzeWithFaceRegion(bitmap, faceResult.boundingBox)
        } else {
            // Fallback: analyze center region if no face detected
            Log.w(TAG, "No face detected, analyzing center region")
            val centerBox = RectF(
                bitmap.width * 0.2f,
                bitmap.height * 0.15f,
                bitmap.width * 0.8f,
                bitmap.height * 0.65f
            )
            analyzeWithFaceRegion(bitmap, centerBox)
        }
    }

    /**
     * Analyze face region for skin characteristics
     */
    private fun analyzeWithFaceRegion(bitmap: Bitmap, faceBox: RectF): SkinAnalysisResult {
        // Define face zones relative to bounding box
        val zones = defineFaceZones(faceBox, bitmap.width, bitmap.height)

        // Analyze Fitzpatrick type from cheek regions (most reliable for skin tone)
        val leftCheekZone = zones[FaceZone.LEFT_CHEEK]!!
        val rightCheekZone = zones[FaceZone.RIGHT_CHEEK]!!
        val (fitzpatrickType, fitzConfidence) = analyzeFitzpatrickType(
            bitmap, leftCheekZone, rightCheekZone
        )

        // Analyze each zone for concerns
        val zoneAnalysis = mutableMapOf<FaceZone, Map<SkinConcern, Float>>()
        zones.forEach { (zone, bounds) ->
            zoneAnalysis[zone] = analyzeZoneConcerns(bitmap, bounds)
        }

        // Calculate overall concerns (weighted average across zones)
        val overallConcerns = calculateOverallConcerns(zoneAnalysis)

        // Determine primary concerns (severity > 0.5)
        val primaryConcerns = overallConcerns.entries
            .filter { it.value > 0.5f }
            .sortedByDescending { it.value }
            .map { it.key }
            .take(3)

        Log.d(TAG, "Analysis complete - Fitzpatrick: $fitzpatrickType, Primary concerns: $primaryConcerns")

        return SkinAnalysisResult(
            overallConcerns = overallConcerns,
            fitzpatrickType = fitzpatrickType,
            fitzpatrickConfidence = fitzConfidence,
            zoneAnalysis = zoneAnalysis,
            primaryConcerns = primaryConcerns
        )
    }

    /**
     * Define face zones within the detected bounding box
     */
    private fun defineFaceZones(
        faceBox: RectF,
        imageWidth: Int,
        imageHeight: Int
    ): Map<FaceZone, RectF> {
        val faceWidth = faceBox.width()
        val faceHeight = faceBox.height()

        return mapOf(
            FaceZone.FOREHEAD to RectF(
                faceBox.left + faceWidth * 0.2f,
                faceBox.top + faceHeight * 0.05f,
                faceBox.right - faceWidth * 0.2f,
                faceBox.top + faceHeight * 0.25f
            ).clamp(imageWidth, imageHeight),

            FaceZone.LEFT_CHEEK to RectF(
                faceBox.left + faceWidth * 0.05f,
                faceBox.top + faceHeight * 0.35f,
                faceBox.left + faceWidth * 0.35f,
                faceBox.top + faceHeight * 0.65f
            ).clamp(imageWidth, imageHeight),

            FaceZone.RIGHT_CHEEK to RectF(
                faceBox.right - faceWidth * 0.35f,
                faceBox.top + faceHeight * 0.35f,
                faceBox.right - faceWidth * 0.05f,
                faceBox.top + faceHeight * 0.65f
            ).clamp(imageWidth, imageHeight),

            FaceZone.NOSE to RectF(
                faceBox.left + faceWidth * 0.35f,
                faceBox.top + faceHeight * 0.30f,
                faceBox.right - faceWidth * 0.35f,
                faceBox.top + faceHeight * 0.60f
            ).clamp(imageWidth, imageHeight),

            FaceZone.CHIN to RectF(
                faceBox.left + faceWidth * 0.25f,
                faceBox.top + faceHeight * 0.70f,
                faceBox.right - faceWidth * 0.25f,
                faceBox.top + faceHeight * 0.95f
            ).clamp(imageWidth, imageHeight)
        )
    }

    /**
     * Clamp RectF to image bounds
     */
    private fun RectF.clamp(width: Int, height: Int): RectF {
        return RectF(
            left.coerceIn(0f, width.toFloat()),
            top.coerceIn(0f, height.toFloat()),
            right.coerceIn(0f, width.toFloat()),
            bottom.coerceIn(0f, height.toFloat())
        )
    }

    /**
     * Analyze Fitzpatrick skin type from cheek regions
     * Uses ITA (Individual Typology Angle) method for skin tone classification
     */
    private fun analyzeFitzpatrickType(
        bitmap: Bitmap,
        leftCheek: RectF,
        rightCheek: RectF
    ): Pair<Int, Float> {
        val leftSamples = samplePixels(bitmap, leftCheek)
        val rightSamples = samplePixels(bitmap, rightCheek)
        val allSamples = leftSamples + rightSamples

        if (allSamples.isEmpty()) {
            return Pair(4, 0.5f) // Default to Type IV with low confidence
        }

        // Calculate average L*a*b* values
        var totalL = 0.0
        var totalA = 0.0
        var totalB = 0.0

        allSamples.forEach { pixel ->
            val lab = rgbToLab(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
            totalL += lab[0]
            totalA += lab[1]
            totalB += lab[2]
        }

        val avgL = totalL / allSamples.size
        val avgB = totalB / allSamples.size

        // Calculate ITA (Individual Typology Angle)
        // ITA = arctan((L* - 50) / b*) × 180 / π
        val ita = if (avgB != 0.0) {
            kotlin.math.atan((avgL - 50) / avgB) * 180 / kotlin.math.PI
        } else {
            55.0 // Default for undefined
        }

        // Map ITA to Fitzpatrick type
        // ITA > 55° → Type I (very light)
        // ITA 41-55° → Type II (light)
        // ITA 28-41° → Type III (intermediate)
        // ITA 10-28° → Type IV (tan)
        // ITA -30-10° → Type V (brown)
        // ITA < -30° → Type VI (dark brown/black)
        val fitzpatrickType = when {
            ita > 55 -> 1
            ita > 41 -> 2
            ita > 28 -> 3
            ita > 10 -> 4
            ita > -30 -> 5
            else -> 6
        }

        // Confidence based on sample consistency
        val stdDevL = calculateStdDev(allSamples.map {
            rgbToLab(Color.red(it), Color.green(it), Color.blue(it))[0]
        })
        val confidence = (1.0f - (stdDevL / 50.0).coerceIn(0.0, 0.5)).toFloat()

        Log.d(TAG, "Fitzpatrick analysis: ITA=$ita, Type=$fitzpatrickType, L*=$avgL")

        return Pair(fitzpatrickType, confidence)
    }

    /**
     * Convert RGB to CIE L*a*b* color space
     */
    private fun rgbToLab(r: Int, g: Int, b: Int): DoubleArray {
        // RGB to XYZ
        var rr = r / 255.0
        var gg = g / 255.0
        var bb = b / 255.0

        rr = if (rr > 0.04045) kotlin.math.pow((rr + 0.055) / 1.055, 2.4) else rr / 12.92
        gg = if (gg > 0.04045) kotlin.math.pow((gg + 0.055) / 1.055, 2.4) else gg / 12.92
        bb = if (bb > 0.04045) kotlin.math.pow((bb + 0.055) / 1.055, 2.4) else bb / 12.92

        val x = (rr * 0.4124 + gg * 0.3576 + bb * 0.1805) / 0.95047
        val y = (rr * 0.2126 + gg * 0.7152 + bb * 0.0722) / 1.00000
        val z = (rr * 0.0193 + gg * 0.1192 + bb * 0.9505) / 1.08883

        // XYZ to Lab
        val fx = if (x > 0.008856) kotlin.math.pow(x, 1.0/3.0) else (7.787 * x) + 16.0/116.0
        val fy = if (y > 0.008856) kotlin.math.pow(y, 1.0/3.0) else (7.787 * y) + 16.0/116.0
        val fz = if (z > 0.008856) kotlin.math.pow(z, 1.0/3.0) else (7.787 * z) + 16.0/116.0

        val labL = (116.0 * fy) - 16.0
        val labA = 500.0 * (fx - fy)
        val labB = 200.0 * (fy - fz)

        return doubleArrayOf(labL, labA, labB)
    }

    /**
     * Analyze zone for skin concerns
     */
    private fun analyzeZoneConcerns(bitmap: Bitmap, zone: RectF): Map<SkinConcern, Float> {
        val samples = samplePixels(bitmap, zone)
        if (samples.isEmpty()) {
            return SkinConcern.entries.associateWith { 0.3f } // Default low values
        }

        // Calculate color statistics
        val reds = samples.map { Color.red(it) }
        val greens = samples.map { Color.green(it) }
        val blues = samples.map { Color.blue(it) }
        val luminances = samples.map { (Color.red(it) * 0.299 + Color.green(it) * 0.587 + Color.blue(it) * 0.114) }

        val avgLuminance = luminances.average()
        val stdLuminance = calculateStdDev(luminances)
        val avgRed = reds.average()
        val avgGreen = greens.average()

        // Hyperpigmentation: High variance in luminance indicates uneven tone
        val hyperpigmentation = (stdLuminance / 30.0).coerceIn(0.0, 1.0).toFloat()

        // Oiliness: High luminance with low variance (shiny/reflective)
        val oiliness = if (avgLuminance > 150 && stdLuminance < 20) {
            ((avgLuminance - 150) / 100.0).coerceIn(0.0, 1.0).toFloat()
        } else {
            0.2f
        }

        // Dryness: Low luminance with high texture variance
        val dryness = if (avgLuminance < 130 && stdLuminance > 15) {
            (stdLuminance / 40.0).coerceIn(0.0, 1.0).toFloat()
        } else {
            0.2f
        }

        // Acne: Red spots (high red channel relative to green)
        val redness = (avgRed - avgGreen) / 255.0
        val acne = if (redness > 0.05) {
            (redness * 3).coerceIn(0.0, 1.0).toFloat()
        } else {
            0.15f
        }

        // Wrinkles: High local variance (texture lines) - simplified detection
        val wrinkles = (stdLuminance / 50.0).coerceIn(0.0, 0.6).toFloat()

        return mapOf(
            SkinConcern.HYPERPIGMENTATION to hyperpigmentation,
            SkinConcern.OILINESS to oiliness,
            SkinConcern.DRYNESS to dryness,
            SkinConcern.ACNE to acne,
            SkinConcern.WRINKLES to wrinkles
        )
    }

    /**
     * Sample pixels from a region
     */
    private fun samplePixels(bitmap: Bitmap, region: RectF): List<Int> {
        val pixels = mutableListOf<Int>()
        val left = region.left.toInt().coerceIn(0, bitmap.width - 1)
        val top = region.top.toInt().coerceIn(0, bitmap.height - 1)
        val right = region.right.toInt().coerceIn(0, bitmap.width - 1)
        val bottom = region.bottom.toInt().coerceIn(0, bitmap.height - 1)

        if (right <= left || bottom <= top) return pixels

        val stepX = ((right - left) / sqrt(SAMPLE_SIZE.toDouble())).toInt().coerceAtLeast(1)
        val stepY = ((bottom - top) / sqrt(SAMPLE_SIZE.toDouble())).toInt().coerceAtLeast(1)

        var x = left
        while (x < right) {
            var y = top
            while (y < bottom) {
                pixels.add(bitmap.getPixel(x, y))
                y += stepY
            }
            x += stepX
        }

        return pixels
    }

    /**
     * Calculate standard deviation
     */
    private fun calculateStdDev(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    /**
     * Calculate overall concerns from zone analysis
     */
    private fun calculateOverallConcerns(
        zoneAnalysis: Map<FaceZone, Map<SkinConcern, Float>>
    ): Map<SkinConcern, Float> {
        val result = mutableMapOf<SkinConcern, Float>()

        SkinConcern.entries.forEach { concern ->
            val zoneScores = zoneAnalysis.values.mapNotNull { it[concern] }
            result[concern] = if (zoneScores.isNotEmpty()) {
                zoneScores.average().toFloat()
            } else {
                0.3f
            }
        }

        return result
    }

    /**
     * Release resources
     */
    fun close() {
        faceDetectionService.close()
        isInitialized = false
        Log.d(TAG, "Skin analysis inference closed")
    }
}
