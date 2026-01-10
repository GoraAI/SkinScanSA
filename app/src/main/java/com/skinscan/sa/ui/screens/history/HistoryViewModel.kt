package com.skinscan.sa.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for Scan History Screen (Story 4.1)
 *
 * Manages scan history list with filtering and starring functionality
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val scanResultDao: ScanResultDao
) : ViewModel() {

    companion object {
        private const val DEFAULT_USER_ID = "default_user" // MVP single-user
    }

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                _uiState.value = HistoryUiState.Loading

                scanResultDao.getAllByUser(DEFAULT_USER_ID).collect { scans ->
                    val filteredScans = applyFilter(scans, _selectedFilter.value)
                    val scanItems = filteredScans.map { scan ->
                        ScanHistoryItem(
                            scanId = scan.scanId,
                            date = scan.scannedAt,
                            fitzpatrickType = scan.fitzpatrickType ?: 5,
                            concerns = parseConcerns(scan.detectedConcerns),
                            healthScore = scan.healthScore ?: calculateHealthScore(scan),
                            isStarred = scan.isStarred
                        )
                    }

                    _uiState.value = HistoryUiState.Success(
                        scans = scanItems,
                        totalCount = scans.size,
                        filteredCount = filteredScans.size
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Failed to load history")
            }
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
        loadHistory()
    }

    fun toggleStar(scanId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HistoryUiState.Success) {
                val scan = currentState.scans.find { it.scanId == scanId } ?: return@launch
                scanResultDao.setStarred(scanId, !scan.isStarred)
            }
        }
    }

    fun deleteScan(scanId: String) {
        viewModelScope.launch {
            scanResultDao.deleteById(scanId)
        }
    }

    private fun applyFilter(scans: List<ScanResultEntity>, filter: HistoryFilter): List<ScanResultEntity> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (filter) {
            HistoryFilter.ALL -> scans
            HistoryFilter.THIS_WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis
                scans.filter { it.scannedAt.time >= weekAgo }
            }
            HistoryFilter.THIS_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.timeInMillis
                scans.filter { it.scannedAt.time >= monthAgo }
            }
            HistoryFilter.STARRED -> scans.filter { it.isStarred }
        }
    }

    private fun parseConcerns(json: String): List<SkinConcern> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val name = array.getString(i)
                SkinConcern.entries.find { it.name == name }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun calculateHealthScore(scan: ScanResultEntity): Int {
        // Calculate health score from confidence scores
        // Lower concern scores = higher health
        return try {
            val confidenceJson = scan.confidenceScores
            if (confidenceJson.isNullOrEmpty()) return 70

            val obj = org.json.JSONObject(confidenceJson)
            var totalScore = 0f
            var count = 0

            SkinConcern.entries.forEach { concern ->
                if (obj.has(concern.name)) {
                    val severity = obj.getDouble(concern.name).toFloat()
                    totalScore += (1f - severity) // Invert: low severity = high health
                    count++
                }
            }

            if (count > 0) {
                ((totalScore / count) * 100).toInt().coerceIn(0, 100)
            } else {
                70 // Default
            }
        } catch (e: Exception) {
            70
        }
    }
}

/**
 * Filter options for scan history
 */
enum class HistoryFilter(val displayName: String) {
    ALL("All Scans"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    STARRED("Starred Only")
}

/**
 * Scan history item for display
 */
data class ScanHistoryItem(
    val scanId: String,
    val date: java.util.Date,
    val fitzpatrickType: Int,
    val concerns: List<SkinConcern>,
    val healthScore: Int,
    val isStarred: Boolean
)

/**
 * UI State for History Screen
 */
sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data class Success(
        val scans: List<ScanHistoryItem>,
        val totalCount: Int,
        val filteredCount: Int
    ) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}
