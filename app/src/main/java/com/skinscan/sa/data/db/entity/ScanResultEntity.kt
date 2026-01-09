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
 */
@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey
    val scanId: String = UUID.randomUUID().toString(),

    val userId: String,

    val scannedAt: Date = Date(),

    // Face image path (encrypted file on disk)
    val faceImagePath: String,

    // Detected skin concerns (JSON array)
    val detectedConcerns: String, // ["HYPERPIGMENTATION", "ACNE", ...]

    // Skin tone analysis
    val fitzpatrickType: Int? = null, // 1-6 scale

    // Confidence scores (JSON object)
    val confidenceScores: String? = null, // {"hyperpigmentation": 0.87, ...}

    // Recommendation IDs (JSON array)
    val recommendedProductIds: String? = null // ["uuid1", "uuid2", ...]
)
