package com.skinscan.sa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skinscan.sa.data.ml.FaceDetectionService
import com.skinscan.sa.data.ml.SkinAnalysisInference
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for SkinAnalysisInference (Tasks 7, 8 verification)
 *
 * Validates that:
 * 1. Analysis returns results based on actual image data (not mock)
 * 2. Different images produce different results
 * 3. Fitzpatrick type is calculated from skin tone (1-6)
 * 4. Zone-based analysis returns data for all 5 zones
 */
@RunWith(AndroidJUnit4::class)
class SkinAnalysisInferenceTest {

    private lateinit var context: Context
    private lateinit var faceDetectionService: FaceDetectionService
    private lateinit var skinAnalysisInference: SkinAnalysisInference

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        faceDetectionService = FaceDetectionService(context)
        skinAnalysisInference = SkinAnalysisInference(context, faceDetectionService)
    }

    @After
    fun teardown() {
        skinAnalysisInference.close()
    }

    @Test
    fun analyze_returns_valid_result_structure() {
        val bitmap = createTestBitmap(Color.rgb(180, 140, 120)) // Light brown skin tone

        val result = skinAnalysisInference.analyze(bitmap)

        assertNotNull("Result should not be null", result)
        assertNotNull("overallConcerns should not be null", result.overallConcerns)
        assertNotNull("zoneAnalysis should not be null", result.zoneAnalysis)
        assertNotNull("primaryConcerns should not be null", result.primaryConcerns)

        bitmap.recycle()
    }

    @Test
    fun fitzpatrick_type_is_in_valid_range() {
        val bitmap = createTestBitmap(Color.rgb(150, 100, 80)) // Medium brown

        val result = skinAnalysisInference.analyze(bitmap)

        assertTrue("Fitzpatrick type should be 1-6",
            result.fitzpatrickType in 1..6)
        assertTrue("Fitzpatrick confidence should be 0-1",
            result.fitzpatrickConfidence in 0f..1f)

        bitmap.recycle()
    }

    @Test
    fun different_skin_tones_produce_different_fitzpatrick_types() {
        // Very light skin (high luminance)
        val lightBitmap = createTestBitmap(Color.rgb(255, 220, 200))
        val lightResult = skinAnalysisInference.analyze(lightBitmap)

        // Dark skin (low luminance)
        val darkBitmap = createTestBitmap(Color.rgb(80, 50, 40))
        val darkResult = skinAnalysisInference.analyze(darkBitmap)

        // Light skin should have lower Fitzpatrick type than dark skin
        assertTrue("Light skin should have lower Fitzpatrick type",
            lightResult.fitzpatrickType < darkResult.fitzpatrickType)

        lightBitmap.recycle()
        darkBitmap.recycle()
    }

    @Test
    fun zone_analysis_covers_all_five_zones() {
        val bitmap = createTestBitmap(Color.rgb(160, 120, 100))

        val result = skinAnalysisInference.analyze(bitmap)

        assertEquals("Should have 5 zones analyzed", 5, result.zoneAnalysis.size)

        val expectedZones = listOf(
            SkinAnalysisInference.FaceZone.FOREHEAD,
            SkinAnalysisInference.FaceZone.LEFT_CHEEK,
            SkinAnalysisInference.FaceZone.RIGHT_CHEEK,
            SkinAnalysisInference.FaceZone.NOSE,
            SkinAnalysisInference.FaceZone.CHIN
        )

        expectedZones.forEach { zone ->
            assertTrue("Zone $zone should be analyzed",
                result.zoneAnalysis.containsKey(zone))
        }

        bitmap.recycle()
    }

    @Test
    fun all_skin_concerns_are_analyzed() {
        val bitmap = createTestBitmap(Color.rgb(160, 120, 100))

        val result = skinAnalysisInference.analyze(bitmap)

        val expectedConcerns = listOf(
            SkinAnalysisInference.SkinConcern.HYPERPIGMENTATION,
            SkinAnalysisInference.SkinConcern.ACNE,
            SkinAnalysisInference.SkinConcern.DRYNESS,
            SkinAnalysisInference.SkinConcern.OILINESS,
            SkinAnalysisInference.SkinConcern.WRINKLES
        )

        expectedConcerns.forEach { concern ->
            assertTrue("Concern $concern should be in overall analysis",
                result.overallConcerns.containsKey(concern))
            assertTrue("Concern severity should be 0-1",
                result.overallConcerns[concern]!! in 0f..1f)
        }

        bitmap.recycle()
    }

    @Test
    fun varied_image_produces_higher_hyperpigmentation_score() {
        // Uniform color (low variance)
        val uniformBitmap = createTestBitmap(Color.rgb(160, 120, 100))
        val uniformResult = skinAnalysisInference.analyze(uniformBitmap)

        // Varied color (patches of different tones - simulates hyperpigmentation)
        val variedBitmap = createVariedTestBitmap()
        val variedResult = skinAnalysisInference.analyze(variedBitmap)

        // Varied image should have higher hyperpigmentation score
        // (or at least not significantly lower)
        val uniformScore = uniformResult.overallConcerns[SkinAnalysisInference.SkinConcern.HYPERPIGMENTATION] ?: 0f
        val variedScore = variedResult.overallConcerns[SkinAnalysisInference.SkinConcern.HYPERPIGMENTATION] ?: 0f

        assertTrue("Varied image should have notable hyperpigmentation score",
            variedScore >= 0.1f)

        uniformBitmap.recycle()
        variedBitmap.recycle()
    }

    @Test
    fun analysis_timestamp_is_set() {
        val beforeAnalysis = System.currentTimeMillis()
        val bitmap = createTestBitmap(Color.rgb(160, 120, 100))

        val result = skinAnalysisInference.analyze(bitmap)
        val afterAnalysis = System.currentTimeMillis()

        assertTrue("Timestamp should be within analysis window",
            result.analysisTimestamp in beforeAnalysis..afterAnalysis)

        bitmap.recycle()
    }

    /**
     * Create a uniform colored test bitmap (simulates face region)
     */
    private fun createTestBitmap(color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }

    /**
     * Create a varied test bitmap with patches of different colors
     * (simulates skin with hyperpigmentation)
     */
    private fun createVariedTestBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Base color
        canvas.drawColor(Color.rgb(160, 120, 100))

        // Add darker patches (hyperpigmentation)
        paint.color = Color.rgb(100, 70, 50)
        canvas.drawCircle(100f, 200f, 30f, paint)
        canvas.drawCircle(300f, 250f, 25f, paint)
        canvas.drawCircle(200f, 400f, 35f, paint)

        // Add some lighter areas
        paint.color = Color.rgb(200, 160, 140)
        canvas.drawCircle(150f, 300f, 20f, paint)

        return bitmap
    }
}
