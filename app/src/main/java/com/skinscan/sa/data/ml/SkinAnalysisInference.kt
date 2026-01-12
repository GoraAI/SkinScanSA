package com.skinscan.sa.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Skin Analysis Inference using real image analysis
 *
 * Story 2.4: Skin Analysis with Loading Feedback
 * - Detects skin concerns: hyperpigmentation, acne, dryness, oiliness, wrinkles
 * - Estimates Fitzpatrick skin type (I-VI) from actual skin tone
 * - Zone-based analysis for 5 face regions
 *
 * Uses MediaPipe Face Landmarker for precise 478-point facial landmark detection
 * and pixel-based analysis for skin characteristics.
 */
@Singleton
class SkinAnalysisInference @Inject constructor(
    @ApplicationContext private val context: Context,
    private val faceDetectionService: FaceDetectionService
) {
    companion object {
        private const val TAG = "SkinAnalysisInference"
        private const val SAMPLE_SIZE = 50 // Number of pixels to sample per region
        private const val FACE_LANDMARKER_MODEL = "face_landmarker.task"

        // MediaPipe Face Mesh landmark indices for each zone
        // Reference: https://github.com/google/mediapipe/blob/master/mediapipe/modules/face_geometry/data/canonical_face_model_uv_visualization.png
        val FOREHEAD_LANDMARKS = listOf(10, 67, 69, 104, 105, 108, 109, 151, 337, 338, 297, 299, 333, 334)
        val LEFT_CHEEK_LANDMARKS = listOf(50, 101, 117, 118, 119, 100, 126, 142, 36, 205, 206, 207)
        val RIGHT_CHEEK_LANDMARKS = listOf(280, 330, 346, 347, 348, 329, 355, 371, 266, 425, 426, 427)
        val NOSE_LANDMARKS = listOf(1, 2, 4, 5, 6, 168, 197, 195, 45, 275)
        val CHIN_LANDMARKS = listOf(152, 148, 149, 150, 175, 176, 177, 178, 379, 378, 377, 400)
    }

    private var faceLandmarker: FaceLandmarker? = null

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
        // Initialize face detection service as fallback
        val faceDetectInit = faceDetectionService.initialize()

        // Initialize Face Landmarker for precise zone detection
        val landmarkerInit = initializeFaceLandmarker()

        isInitialized = landmarkerInit || faceDetectInit
        Log.d(TAG, "Skin analysis inference initialized: $isInitialized (landmarker=$landmarkerInit, detector=$faceDetectInit)")
        return isInitialized
    }

    /**
     * Initialize MediaPipe Face Landmarker
     */
    private fun initializeFaceLandmarker(): Boolean {
        return try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(FACE_LANDMARKER_MODEL)
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
            Log.d(TAG, "Face Landmarker initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Face Landmarker", e)
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
        Log.d(TAG, "Analyzing face image (${bitmap.width}x${bitmap.height})")

        // Try Face Landmarker first for precise zone detection
        val landmarkerResult = runFaceLandmarker(bitmap)
        if (landmarkerResult != null && landmarkerResult.faceLandmarks().isNotEmpty()) {
            Log.d(TAG, "Using Face Landmarker for precise zone analysis")
            return analyzeWithLandmarks(bitmap, landmarkerResult)
        }

        // Fallback to face detection
        Log.d(TAG, "Face Landmarker unavailable, using face detection fallback")
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
     * Run Face Landmarker detection
     */
    private fun runFaceLandmarker(bitmap: Bitmap): FaceLandmarkerResult? {
        val landmarker = faceLandmarker ?: return null
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            landmarker.detect(mpImage)
        } catch (e: Exception) {
            Log.e(TAG, "Face Landmarker detection failed", e)
            null
        }
    }

    /**
     * Analyze face using precise 478-point landmarks
     */
    private fun analyzeWithLandmarks(bitmap: Bitmap, result: FaceLandmarkerResult): SkinAnalysisResult {
        val landmarks = result.faceLandmarks()[0]
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height

        // Extract precise zones from landmarks
        val zones = mapOf(
            FaceZone.FOREHEAD to extractZoneFromLandmarks(landmarks, FOREHEAD_LANDMARKS, imageWidth, imageHeight),
            FaceZone.LEFT_CHEEK to extractZoneFromLandmarks(landmarks, LEFT_CHEEK_LANDMARKS, imageWidth, imageHeight),
            FaceZone.RIGHT_CHEEK to extractZoneFromLandmarks(landmarks, RIGHT_CHEEK_LANDMARKS, imageWidth, imageHeight),
            FaceZone.NOSE to extractZoneFromLandmarks(landmarks, NOSE_LANDMARKS, imageWidth, imageHeight),
            FaceZone.CHIN to extractZoneFromLandmarks(landmarks, CHIN_LANDMARKS, imageWidth, imageHeight)
        )

        // Analyze Fitzpatrick type from cheek regions
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

        // Calculate overall concerns
        val overallConcerns = calculateOverallConcerns(zoneAnalysis)

        // Determine primary concerns
        val primaryConcerns = overallConcerns.entries
            .filter { it.value > 0.5f }
            .sortedByDescending { it.value }
            .map { it.key }
            .take(3)

        Log.d(TAG, "Landmark analysis complete - Fitzpatrick: $fitzpatrickType, Primary concerns: $primaryConcerns")

        return SkinAnalysisResult(
            overallConcerns = overallConcerns,
            fitzpatrickType = fitzpatrickType,
            fitzpatrickConfidence = fitzConfidence,
            zoneAnalysis = zoneAnalysis,
            primaryConcerns = primaryConcerns
        )
    }

    /**
     * Extract a zone bounding box from landmark indices
     */
    private fun extractZoneFromLandmarks(
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        indices: List<Int>,
        imageWidth: Int,
        imageHeight: Int
    ): RectF {
        val validIndices = indices.filter { it < landmarks.size }
        if (validIndices.isEmpty()) {
            return RectF(0f, 0f, imageWidth.toFloat(), imageHeight.toFloat())
        }

        val points = validIndices.map { landmarks[it] }
        val minX = points.minOf { it.x() } * imageWidth
        val maxX = points.maxOf { it.x() } * imageWidth
        val minY = points.minOf { it.y() } * imageHeight
        val maxY = points.maxOf { it.y() } * imageHeight

        // Add padding to zone
        val paddingX = (maxX - minX) * 0.1f
        val paddingY = (maxY - minY) * 0.1f

        return RectF(
            (minX - paddingX).coerceAtLeast(0f),
            (minY - paddingY).coerceAtLeast(0f),
            (maxX + paddingX).coerceAtMost(imageWidth.toFloat()),
            (maxY + paddingY).coerceAtMost(imageHeight.toFloat())
        )
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

        rr = if (rr > 0.04045) ((rr + 0.055) / 1.055).pow(2.4) else rr / 12.92
        gg = if (gg > 0.04045) ((gg + 0.055) / 1.055).pow(2.4) else gg / 12.92
        bb = if (bb > 0.04045) ((bb + 0.055) / 1.055).pow(2.4) else bb / 12.92

        val x = (rr * 0.4124 + gg * 0.3576 + bb * 0.1805) / 0.95047
        val y = (rr * 0.2126 + gg * 0.7152 + bb * 0.0722) / 1.00000
        val z = (rr * 0.0193 + gg * 0.1192 + bb * 0.9505) / 1.08883

        // XYZ to Lab
        val fx = if (x > 0.008856) x.pow(1.0/3.0) else (7.787 * x) + 16.0/116.0
        val fy = if (y > 0.008856) y.pow(1.0/3.0) else (7.787 * y) + 16.0/116.0
        val fz = if (z > 0.008856) z.pow(1.0/3.0) else (7.787 * z) + 16.0/116.0

        val labL = (116.0 * fy) - 16.0
        val labA = 500.0 * (fx - fy)
        val labB = 200.0 * (fy - fz)

        return doubleArrayOf(labL, labA, labB)
    }

    /**
     * Analyze zone for skin concerns using enhanced algorithms
     *
     * Implements:
     * - Color uniformity analysis for hyperpigmentation
     * - Specular highlight detection for oiliness
     * - Texture variance (simulated Laplacian) for dryness
     * - Red channel analysis for acne/inflammation
     * - Gradient variance for wrinkle detection
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
        val avgBlue = blues.average()

        // Enhanced concern detection algorithms with calibrated ranges
        // Values now range from 0.05 to 0.85 for realistic skin analysis

        // 1. HYPERPIGMENTATION: Color uniformity analysis
        // Detect uneven skin tone by measuring color deviation from mean
        val colorDeviations = samples.map { pixel ->
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            sqrt(
                (r - avgRed).pow(2.0) +
                (g - avgGreen).pow(2.0) +
                (b - avgBlue).pow(2.0)
            )
        }
        val colorUniformity = colorDeviations.average()
        // Calibrated: divide by 80 instead of 40 for more gradual scaling
        val hyperpigmentation = (colorUniformity / 80.0).coerceIn(0.05, 0.75).toFloat()

        // 2. OILINESS: Specular highlight detection
        // Detect shiny/reflective areas by counting bright spots
        val brightnessThreshold = 210.0 // Raised threshold for more specificity
        val specularCount = luminances.count { it > brightnessThreshold }
        val specularRatio = specularCount.toDouble() / samples.size
        val highlightIntensity = luminances.filter { it > brightnessThreshold }
            .takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val oiliness = when {
            specularRatio > 0.20 -> ((specularRatio * 2 + (highlightIntensity - 210) / 80.0) / 2).coerceIn(0.4, 0.80).toFloat()
            specularRatio > 0.10 -> (specularRatio * 3).coerceIn(0.25, 0.55).toFloat()
            specularRatio > 0.03 -> (specularRatio * 4).coerceIn(0.12, 0.35).toFloat()
            avgLuminance > 190 && stdLuminance < 12 -> 0.30f
            else -> 0.08f
        }

        // 3. DRYNESS: Texture variance analysis (simulated Laplacian variance)
        // Higher local variance indicates rough/dry texture
        val textureVariance = calculateLocalVariance(luminances)
        val dryness = when {
            textureVariance > 500 && avgLuminance < 130 -> (textureVariance / 900.0).coerceIn(0.35, 0.75).toFloat()
            textureVariance > 300 -> (textureVariance / 1000.0).coerceIn(0.20, 0.50).toFloat()
            textureVariance > 150 -> (textureVariance / 1200.0).coerceIn(0.10, 0.30).toFloat()
            avgLuminance < 90 -> 0.25f
            else -> 0.05f
        }

        // 4. ACNE: Red channel analysis for inflammation
        // Detect red spots by analyzing red-to-green ratio and local red peaks
        val redGreenRatios = samples.mapIndexed { i, _ ->
            if (greens[i] > 0) reds[i].toDouble() / greens[i] else 0.0
        }
        val highRedCount = redGreenRatios.count { it > 1.20 } // Raised threshold
        val highRedRatio = highRedCount.toDouble() / samples.size
        val avgRedGreenDiff = (avgRed - avgGreen) / 255.0
        val acne = when {
            highRedRatio > 0.25 && avgRedGreenDiff > 0.10 -> ((highRedRatio * 1.5 + avgRedGreenDiff * 3) / 2).coerceIn(0.45, 0.80).toFloat()
            highRedRatio > 0.15 -> (highRedRatio * 2.5 + avgRedGreenDiff * 2).coerceIn(0.25, 0.60).toFloat()
            highRedRatio > 0.08 -> (highRedRatio * 3).coerceIn(0.15, 0.40).toFloat()
            avgRedGreenDiff > 0.06 -> (avgRedGreenDiff * 3).coerceIn(0.10, 0.30).toFloat()
            else -> 0.05f
        }

        // 5. WRINKLES: Enhanced texture line detection
        // Use gradient variance to detect fine lines
        val gradientVariance = calculateGradientVariance(luminances)
        val wrinkles = when {
            gradientVariance > 400 -> (gradientVariance / 700.0).coerceIn(0.35, 0.70).toFloat()
            gradientVariance > 250 -> (gradientVariance / 800.0).coerceIn(0.20, 0.45).toFloat()
            gradientVariance > 120 -> (gradientVariance / 900.0).coerceIn(0.10, 0.30).toFloat()
            stdLuminance > 30 -> (stdLuminance / 80.0).coerceIn(0.08, 0.25).toFloat()
            else -> 0.03f
        }

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
     * Calculate local variance (simulated Laplacian variance)
     * Measures texture roughness by comparing each value to its neighbors
     */
    private fun calculateLocalVariance(values: List<Double>): Double {
        if (values.size < 3) return 0.0

        // Simulate local Laplacian by computing differences between adjacent samples
        val laplacianValues = mutableListOf<Double>()
        val gridSize = sqrt(values.size.toDouble()).toInt()

        for (i in values.indices) {
            val neighbors = mutableListOf<Double>()

            // Get neighboring values (up, down, left, right)
            if (i >= gridSize) neighbors.add(values[i - gridSize])
            if (i < values.size - gridSize) neighbors.add(values[i + gridSize])
            if (i % gridSize > 0) neighbors.add(values[i - 1])
            if (i % gridSize < gridSize - 1 && i + 1 < values.size) neighbors.add(values[i + 1])

            if (neighbors.isNotEmpty()) {
                val laplacian = abs(values[i] * neighbors.size - neighbors.sum())
                laplacianValues.add(laplacian)
            }
        }

        if (laplacianValues.isEmpty()) return 0.0

        val mean = laplacianValues.average()
        return laplacianValues.map { (it - mean).pow(2.0) }.average()
    }

    /**
     * Calculate gradient variance for wrinkle detection
     * Measures directional changes in luminance which indicate fine lines
     */
    private fun calculateGradientVariance(values: List<Double>): Double {
        if (values.size < 2) return 0.0

        // Calculate gradients (differences between consecutive samples)
        val gradients = values.zipWithNext { a, b -> abs(b - a) }

        if (gradients.isEmpty()) return 0.0

        val mean = gradients.average()
        return gradients.map { (it - mean).pow(2.0) }.average()
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
        try {
            faceLandmarker?.close()
            faceLandmarker = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing Face Landmarker: ${e.message}")
        }
        faceDetectionService.close()
        isInitialized = false
        Log.d(TAG, "Skin analysis inference closed")
    }
}
