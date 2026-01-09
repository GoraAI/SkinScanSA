package com.skinscan.sa.domain.repository

import android.graphics.Bitmap
import com.skinscan.sa.data.db.entity.ScanResultEntity

/**
 * Repository for skin analysis operations
 *
 * CRITICAL PRIVACY RULE (Story 6.3):
 * - Face image Bitmap is NEVER persisted to disk
 * - Face image Bitmap is NEVER transmitted over network
 * - Only derived analysis data (ScanResult) is saved to Room DB
 * - Bitmap is processed in-memory only, then cleared
 */
interface SkinAnalysisRepository {
    /**
     * Analyze face image and return scan results
     *
     * Privacy guarantee:
     * - Bitmap stays in RAM only during processing
     * - After processing, Bitmap is recycled and cleared
     * - Only ScanResult (no image data) is persisted
     */
    suspend fun analyzeFace(image: Bitmap, userId: String): ScanResultEntity
}
