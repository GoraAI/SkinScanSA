package com.skinscan.sa.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Room entity for storing facial skin scan results
 *
 * CRITICAL: Contains biometric data - MUST be encrypted (POPIA Section 26)
 * Stored in SQLCipher encrypted database
 *
 * Story 2.4/2.5: Extended to include zone analysis data
 */
@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey
    val scanId: String = UUID.randomUUID().toString(),

    val userId: String,

    val scannedAt: Date = Date(),

    // Face image path - ALWAYS EMPTY for privacy (Story 6.3)
    // Image never persisted to disk, only processed in RAM
    val faceImagePath: String = "",

    // Detected skin concerns (JSON array)
    // e.g., ["HYPERPIGMENTATION", "DRYNESS"]
    val detectedConcerns: String,

    // Skin tone analysis (Fitzpatrick scale 1-6)
    val fitzpatrickType: Int? = null,

    // Fitzpatrick classification confidence (0.0 - 1.0)
    val fitzpatrickConfidence: Float? = null,

    // Overall concern confidence scores (JSON object)
    // e.g., {"HYPERPIGMENTATION": 0.87, "DRYNESS": 0.72, ...}
    val confidenceScores: String? = null,

    // Zone-specific analysis (JSON object)
    // e.g., {"FOREHEAD": {"HYPERPIGMENTATION": 0.82, ...}, ...}
    val zoneAnalysis: String? = null,

    // Recommendation IDs (JSON array)
    // e.g., ["uuid1", "uuid2", ...]
    val recommendedProductIds: String? = null,

    // Analysis metadata
    val analysisVersion: String = "1.0.0",
    val modelVersion: String = "mediapipe-landmarker-v1",

    // Story 4.1: User bookmarking
    val isStarred: Boolean = false,

    // Story 4.3: Overall skin health score (0-100)
    val healthScore: Int? = null
)
