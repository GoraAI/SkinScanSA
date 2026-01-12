package com.skinscan.sa.ui.screens.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.session.ScanImageHolder
import com.skinscan.sa.data.session.UserSessionManager
import com.skinscan.sa.domain.repository.SkinAnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Scan Screen
 *
 * PRIVACY ARCHITECTURE (Story 6.3):
 * - Captured face image stored ONLY in RAM
 * - Image held temporarily in ScanImageHolder for results display
 * - Image NEVER persisted to disk or cache
 * - Image cleared when leaving results screen
 * - Only ScanResult (derived data, no image) is saved to encrypted DB
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val skinAnalysisRepository: SkinAnalysisRepository,
    private val userSessionManager: UserSessionManager,
    private val scanImageHolder: ScanImageHolder
) : ViewModel() {

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> = _capturedImage.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResultEntity?>(null)
    val scanResult: StateFlow<ScanResultEntity?> = _scanResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    /**
     * Analyze captured face image
     *
     * Privacy guarantee:
     * - Bitmap stored in RAM only during processing
     * - Image held in ScanImageHolder for results display (temporary)
     * - Image cleared when user leaves results screen
     * - Only ScanResult (no image data) persisted to encrypted DB
     */
    fun analyzeFace(image: Bitmap) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _capturedImage.value = image // Store in RAM only

            try {
                // Analyze face (in-memory processing only)
                val userId = userSessionManager.userId
                val result = skinAnalysisRepository.analyzeFace(image, userId)
                _scanResult.value = result

                // Store image temporarily for results display (RAM only)
                // Will be cleared when user leaves results screen
                scanImageHolder.setImage(image, result.scanId)

            } catch (e: Exception) {
                // On error, clear the image immediately
                _capturedImage.value?.recycle()
                _capturedImage.value = null
                throw e
            } finally {
                // Clear local reference (image now held by ScanImageHolder)
                _capturedImage.value = null
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Clear scan result (for new scan)
     */
    fun clearScanResult() {
        _scanResult.value = null
        // Ensure any lingering bitmap is cleared
        _capturedImage.value?.recycle()
        _capturedImage.value = null
    }
}
