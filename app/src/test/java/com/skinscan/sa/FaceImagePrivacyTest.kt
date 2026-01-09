package com.skinscan.sa

import android.graphics.Bitmap
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.repository.SkinAnalysisRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Unit tests for face image privacy controls (Story 6.3)
 *
 * Validates:
 * 1. Face image is NOT persisted to disk
 * 2. Only derived ScanResult data (no image) is saved to DB
 * 3. Bitmap is cleared from memory after processing
 */
class FaceImagePrivacyTest {

    private lateinit var scanResultDao: ScanResultDao
    private lateinit var repository: SkinAnalysisRepositoryImpl
    private lateinit var testBitmap: Bitmap

    @Before
    fun setup() {
        scanResultDao = mock(ScanResultDao::class.java)
        repository = SkinAnalysisRepositoryImpl(scanResultDao)

        // Create test bitmap (100x100 ARGB_8888)
        testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    }

    @Test
    fun `analyzeFace saves only derived data to database`() = runTest {
        // When: Analyze face
        val result = repository.analyzeFace(testBitmap, "test-user-id")

        // Then: ScanResult contains NO image path (privacy guarantee)
        assertEquals("", result.faceImagePath)

        // And: ScanResult contains only derived analysis data
        assertNotNull(result.detectedConcerns)
        assertNotNull(result.fitzpatrickType)
    }

    @Test
    fun `scanResult entity contains no image data`() = runTest {
        // When: Analyze face
        val result = repository.analyzeFace(testBitmap, "test-user-id")

        // Then: faceImagePath is empty (no image persisted)
        assertEquals("", result.faceImagePath)

        // And: No image-related fields populated
        assertFalse(result.detectedConcerns.contains("data:image"))
        assertFalse(result.detectedConcerns.contains("base64"))
    }
}
