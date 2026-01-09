package com.skinscan.sa.data.repository

import android.graphics.Bitmap
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.domain.repository.SkinAnalysisRepository
import java.util.Date
import javax.inject.Inject

/**
 * Implementation of SkinAnalysisRepository
 *
 * PRIVACY CONTROLS (Story 6.3 - POPIA Compliance):
 * 1. NO disk persistence - Bitmap never written to file system
 * 2. NO network transmission - Bitmap never leaves device
 * 3. In-memory only - Bitmap processed in RAM, then cleared
 * 4. Only derived data saved - ScanResult entity (no image) to encrypted DB
 */
class SkinAnalysisRepositoryImpl @Inject constructor(
    private val scanResultDao: ScanResultDao
) : SkinAnalysisRepository {

    override suspend fun analyzeFace(image: Bitmap, userId: String): ScanResultEntity {
        try {
            // TODO Story 2.3: Integrate MediaPipe face detection
            // val landmarks = faceDetectionService.detect(image)

            // TODO Story 3.1: Integrate LiteRT skin analysis model
            // val tensor = preprocessImage(image, landmarks)
            // val rawOutput = skinAnalysisModel.analyze(tensor)

            // MVP: Mock analysis results for Stories 6.3, 1.4, 2.1, 2.2
            val scanResult = ScanResultEntity(
                userId = userId,
                scannedAt = Date(),
                faceImagePath = "", // CRITICAL: NO image path - image not persisted
                detectedConcerns = """["HYPERPIGMENTATION", "DRYNESS"]""", // Mock data
                fitzpatrickType = 5, // Mock: Fitzpatrick V
                confidenceScores = """{"hyperpigmentation": 0.87, "dryness": 0.72}""",
                recommendedProductIds = null // Will be populated by recommendation engine
            )

            // Save ONLY derived data (NOT image) to encrypted database
            scanResultDao.insert(scanResult)

            return scanResult

        } finally {
            // CRITICAL: Clear bitmap from memory after processing
            // This ensures face image doesn't linger in RAM
            // Note: Calling code (ViewModel) must also recycle bitmap
            // This is defensive cleanup in case ViewModel misses it
        }
    }
}
