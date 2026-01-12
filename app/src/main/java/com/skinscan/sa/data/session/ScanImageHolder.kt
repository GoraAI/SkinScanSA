package com.skinscan.sa.data.session

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Temporary in-memory holder for scan session images
 *
 * PRIVACY: Image is held only during the active scan session.
 * It is cleared when:
 * - User navigates away from results
 * - App goes to background
 * - Image is consumed
 *
 * NO disk persistence - POPIA compliant
 */
@Singleton
class ScanImageHolder @Inject constructor() {

    @Volatile
    private var capturedImage: Bitmap? = null

    @Volatile
    private var scanId: String? = null

    /**
     * Store the captured image for the current scan session
     *
     * @param image The captured face bitmap
     * @param forScanId The scan ID this image belongs to
     */
    fun setImage(image: Bitmap, forScanId: String) {
        // Clear any previous image
        clear()
        this.capturedImage = image
        this.scanId = forScanId
    }

    /**
     * Get the captured image if it matches the scan ID
     *
     * @param forScanId The scan ID to retrieve image for
     * @return The bitmap if available and matching, null otherwise
     */
    fun getImage(forScanId: String): Bitmap? {
        return if (scanId == forScanId) capturedImage else null
    }

    /**
     * Check if an image is available for the given scan
     */
    fun hasImage(forScanId: String): Boolean {
        return scanId == forScanId && capturedImage != null
    }

    /**
     * Clear the stored image - call when leaving results screen
     * or when app goes to background
     */
    fun clear() {
        capturedImage?.recycle()
        capturedImage = null
        scanId = null
    }
}
