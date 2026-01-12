package com.skinscan.sa.ui.screens.results

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.ml.SkinAnalysisInference.FaceZone
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.data.session.ScanImageHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * ViewModel for Results Screen (Story 2.5)
 *
 * Loads and parses scan results from database
 * Also provides temporary access to captured face image for zone visualization
 */
@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanResultDao: ScanResultDao,
    private val scanImageHolder: ScanImageHolder
) : ViewModel() {

    private val scanId: String = savedStateHandle.get<String>("scanId") ?: ""

    /**
     * Get the captured face image if available (RAM only, not persisted)
     * Returns null if viewing from history (image was cleared)
     */
    fun getCapturedImage(): Bitmap? = scanImageHolder.getImage(scanId)

    /**
     * Check if a captured image is available for this scan
     */
    fun hasLiveImage(): Boolean = scanImageHolder.hasImage(scanId)

    /**
     * Clear the captured image - call when leaving the results screen
     * IMPORTANT: This ensures POPIA compliance by not keeping images in memory
     */
    fun clearCapturedImage() {
        scanImageHolder.clear()
    }

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    private fun loadResults() {
        viewModelScope.launch {
            try {
                val entity = scanResultDao.getById(scanId)
                if (entity != null) {
                    _uiState.value = ResultsUiState.Success(parseEntity(entity))
                } else {
                    _uiState.value = ResultsUiState.Error("Scan result not found")
                }
            } catch (e: Exception) {
                _uiState.value = ResultsUiState.Error(e.message ?: "Failed to load results")
            }
        }
    }

    fun refresh() {
        _uiState.value = ResultsUiState.Loading
        loadResults()
    }

    private fun parseEntity(entity: ScanResultEntity): ParsedScanResult {
        // Parse detected concerns
        val concerns = mutableListOf<SkinConcern>()
        try {
            val concernsArray = JSONArray(entity.detectedConcerns)
            for (i in 0 until concernsArray.length()) {
                val concernName = concernsArray.getString(i)
                SkinConcern.entries.find { it.name == concernName }?.let {
                    concerns.add(it)
                }
            }
        } catch (e: Exception) {
            // Empty concerns
        }

        // Parse confidence scores
        val scores = mutableMapOf<SkinConcern, Float>()
        try {
            entity.confidenceScores?.let { json ->
                val scoresObj = JSONObject(json)
                SkinConcern.entries.forEach { concern ->
                    if (scoresObj.has(concern.name)) {
                        scores[concern] = scoresObj.getDouble(concern.name).toFloat()
                    }
                }
            }
        } catch (e: Exception) {
            // Empty scores
        }

        // Parse zone analysis
        val zoneAnalysis = mutableMapOf<FaceZone, Map<SkinConcern, Float>>()
        try {
            entity.zoneAnalysis?.let { json ->
                val zonesObj = JSONObject(json)
                FaceZone.entries.forEach { zone ->
                    if (zonesObj.has(zone.name)) {
                        val zoneScores = mutableMapOf<SkinConcern, Float>()
                        val zoneConcernsObj = zonesObj.getJSONObject(zone.name)
                        SkinConcern.entries.forEach { concern ->
                            if (zoneConcernsObj.has(concern.name)) {
                                zoneScores[concern] = zoneConcernsObj.getDouble(concern.name).toFloat()
                            }
                        }
                        zoneAnalysis[zone] = zoneScores
                    }
                }
            }
        } catch (e: Exception) {
            // Empty zone analysis
        }

        return ParsedScanResult(
            scanId = entity.scanId,
            scannedAt = entity.scannedAt,
            fitzpatrickType = entity.fitzpatrickType,
            fitzpatrickConfidence = entity.fitzpatrickConfidence,
            primaryConcerns = concerns,
            concernScores = scores,
            zoneAnalysis = zoneAnalysis,
            hasNoConcerns = concerns.isEmpty() || scores.values.all { it < 0.4f }
        )
    }
}

/**
 * UI State for Results Screen
 */
sealed class ResultsUiState {
    data object Loading : ResultsUiState()
    data class Success(val result: ParsedScanResult) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}

/**
 * Parsed scan result for UI display
 */
data class ParsedScanResult(
    val scanId: String,
    val scannedAt: java.util.Date,
    val fitzpatrickType: Int?,
    val fitzpatrickConfidence: Float?,
    val primaryConcerns: List<SkinConcern>,
    val concernScores: Map<SkinConcern, Float>,
    val zoneAnalysis: Map<FaceZone, Map<SkinConcern, Float>>,
    val hasNoConcerns: Boolean
)
