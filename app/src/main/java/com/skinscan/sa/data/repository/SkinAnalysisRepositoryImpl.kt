package com.skinscan.sa.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.ml.FaceDetectionService
import com.skinscan.sa.data.ml.SkinAnalysisInference
import com.skinscan.sa.domain.repository.SkinAnalysisRepository
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject

/**
 * Implementation of SkinAnalysisRepository
 *
 * Story 2.3: Face Detection Integration
 * Story 2.4: Skin Analysis with LiteRT
 *
 * PRIVACY CONTROLS (Story 6.3 - POPIA Compliance):
 * 1. NO disk persistence - Bitmap never written to file system
 * 2. NO network transmission - Bitmap never leaves device
 * 3. In-memory only - Bitmap processed in RAM, then cleared
 * 4. Only derived data saved - ScanResult entity (no image) to encrypted DB
 */
class SkinAnalysisRepositoryImpl @Inject constructor(
    private val scanResultDao: ScanResultDao,
    private val faceDetectionService: FaceDetectionService,
    private val skinAnalysisInference: SkinAnalysisInference
) : SkinAnalysisRepository {

    companion object {
        private const val TAG = "SkinAnalysisRepo"
    }

    @Volatile
    private var isInitialized = false

    /**
     * Ensure ML models are initialized before use
     */
    private fun ensureInitialized() {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    Log.d(TAG, "Initializing ML models...")
                    val faceDetectInit = faceDetectionService.initialize()
                    val analysisInit = skinAnalysisInference.initialize()
                    Log.d(TAG, "ML initialization complete: faceDetect=$faceDetectInit, analysis=$analysisInit")
                    isInitialized = true
                }
            }
        }
    }

    override suspend fun analyzeFace(image: Bitmap, userId: String): ScanResultEntity {
        Log.d(TAG, "Starting face analysis for user: $userId")

        // Ensure models are loaded
        ensureInitialized()

        try {
            // Step 1: Detect face (Story 2.3)
            val faceResult = faceDetectionService.detectFace(image)
            if (!faceResult.faceDetected) {
                Log.w(TAG, "No face detected: ${faceResult.validationMessage}")
                // Return result with no concerns detected
                return createEmptyResult(userId, "No face detected")
            }

            // Step 2: Run skin analysis (Story 2.4)
            val analysisResult = skinAnalysisInference.analyze(image)

            // Step 3: Convert to entity and save
            val scanResult = convertToEntity(userId, analysisResult)

            // Save ONLY derived data (NOT image) to encrypted database
            scanResultDao.insert(scanResult)
            Log.d(TAG, "Analysis saved with ID: ${scanResult.scanId}")

            return scanResult

        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed", e)
            throw e
        }
    }

    /**
     * Convert ML result to database entity
     */
    private fun convertToEntity(
        userId: String,
        result: SkinAnalysisInference.SkinAnalysisResult
    ): ScanResultEntity {
        // Convert primary concerns to JSON array
        val concernsArray = JSONArray()
        result.primaryConcerns.forEach { concern ->
            concernsArray.put(concern.name)
        }

        // Convert confidence scores to JSON object
        val scoresObject = JSONObject()
        result.overallConcerns.forEach { (concern, score) ->
            scoresObject.put(concern.name, score.toDouble())
        }

        // Convert zone analysis to JSON object
        val zoneObject = JSONObject()
        result.zoneAnalysis.forEach { (zone, concerns) ->
            val zoneConcerns = JSONObject()
            concerns.forEach { (concern, score) ->
                zoneConcerns.put(concern.name, score.toDouble())
            }
            zoneObject.put(zone.name, zoneConcerns)
        }

        return ScanResultEntity(
            userId = userId,
            scannedAt = Date(result.analysisTimestamp),
            faceImagePath = "", // CRITICAL: No image path - privacy compliance
            detectedConcerns = concernsArray.toString(),
            fitzpatrickType = result.fitzpatrickType,
            fitzpatrickConfidence = result.fitzpatrickConfidence,
            confidenceScores = scoresObject.toString(),
            zoneAnalysis = zoneObject.toString(),
            recommendedProductIds = null, // Will be populated by recommendation engine
            modelVersion = "mediapipe-landmarker-v1"
        )
    }

    /**
     * Create empty result when face not detected
     */
    private suspend fun createEmptyResult(userId: String, reason: String): ScanResultEntity {
        val result = ScanResultEntity(
            userId = userId,
            scannedAt = Date(),
            faceImagePath = "",
            detectedConcerns = "[]",
            fitzpatrickType = null,
            confidenceScores = null,
            zoneAnalysis = null,
            modelVersion = "none-$reason"
        )
        scanResultDao.insert(result)
        return result
    }
}
