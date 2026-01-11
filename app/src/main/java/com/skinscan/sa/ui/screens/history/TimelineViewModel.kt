package com.skinscan.sa.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.data.session.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for Progress Timeline Screen (Story 4.3)
 *
 * Calculates multi-scan trends over time for visualization
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val scanResultDao: ScanResultDao,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    companion object {
        private const val MIN_SCANS_FOR_TIMELINE = 3
    }

    private val userId: String
        get() = userSessionManager.userId

    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _selectedRange = MutableStateFlow(DateRange.THIRTY_DAYS)
    val selectedRange: StateFlow<DateRange> = _selectedRange.asStateFlow()

    init {
        loadTimeline()
    }

    fun setDateRange(range: DateRange) {
        _selectedRange.value = range
        loadTimeline()
    }

    private fun loadTimeline() {
        viewModelScope.launch {
            try {
                _uiState.value = TimelineUiState.Loading

                val startDate = calculateStartDate(_selectedRange.value)
                val scans = scanResultDao.getByUserInDateRange(userId, startDate)

                if (scans.size < MIN_SCANS_FOR_TIMELINE) {
                    _uiState.value = TimelineUiState.NotEnoughData
                    return@launch
                }

                // Calculate overall health trend
                val healthHistory = scans.map { scan ->
                    TimelineDataPoint(
                        date = scan.scannedAt,
                        value = (scan.healthScore ?: calculateHealthScore(scan)).toFloat()
                    )
                }

                val currentScore = healthHistory.lastOrNull()?.value?.toInt() ?: 70
                val overallTrend = calculateTrend(healthHistory)

                // Calculate concern-specific trends
                val concernTrends = SkinConcern.entries.mapNotNull { concern ->
                    calculateConcernTrend(scans, concern)
                }

                _uiState.value = TimelineUiState.Success(
                    healthScoreHistory = healthHistory,
                    currentHealthScore = currentScore,
                    overallTrend = overallTrend,
                    concernTrends = concernTrends
                )
            } catch (e: Exception) {
                _uiState.value = TimelineUiState.Error(e.message ?: "Failed to load timeline")
            }
        }
    }

    private fun calculateStartDate(range: DateRange): Long {
        if (range == DateRange.ALL_TIME) return 0L

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -range.days)
        return calendar.timeInMillis
    }

    private fun calculateHealthScore(scan: ScanResultEntity): Int {
        val scores = parseConfidenceScores(scan.confidenceScores)
        if (scores.isEmpty()) return 70

        val avgSeverity = scores.values.average().toFloat()
        return ((1f - avgSeverity) * 100).toInt().coerceIn(0, 100)
    }

    private fun calculateTrend(dataPoints: List<TimelineDataPoint>): TrendDirection {
        if (dataPoints.size < 2) return TrendDirection.STABLE

        val first = dataPoints.take(dataPoints.size / 3).map { it.value }.average()
        val last = dataPoints.takeLast(dataPoints.size / 3).map { it.value }.average()

        val delta = last - first

        return when {
            delta > 5 -> TrendDirection.IMPROVING
            delta < -5 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }

    private fun calculateConcernTrend(
        scans: List<ScanResultEntity>,
        concern: SkinConcern
    ): ConcernTrendData? {
        val dataPoints = scans.mapNotNull { scan ->
            val scores = parseConfidenceScores(scan.confidenceScores)
            val severity = scores[concern.name] ?: return@mapNotNull null
            TimelineDataPoint(
                date = scan.scannedAt,
                value = severity
            )
        }

        if (dataPoints.size < 2) return null

        val startSeverity = dataPoints.first().value
        val endSeverity = dataPoints.last().value
        val delta = endSeverity - startSeverity

        val trend = when {
            delta < -0.05f -> TrendDirection.IMPROVING // Severity decreased = improving
            delta > 0.05f -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }

        return ConcernTrendData(
            concern = concern,
            startSeverity = startSeverity,
            endSeverity = endSeverity,
            trend = trend,
            dataPoints = dataPoints
        )
    }

    private fun parseConfidenceScores(json: String?): Map<String, Float> {
        if (json.isNullOrEmpty()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            SkinConcern.entries.associate { concern ->
                concern.name to obj.optDouble(concern.name, 0.0).toFloat()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

/**
 * UI State for Timeline Screen
 */
sealed class TimelineUiState {
    data object Loading : TimelineUiState()
    data object NotEnoughData : TimelineUiState()
    data class Success(
        val healthScoreHistory: List<TimelineDataPoint>,
        val currentHealthScore: Int,
        val overallTrend: TrendDirection,
        val concernTrends: List<ConcernTrendData>
    ) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
}
