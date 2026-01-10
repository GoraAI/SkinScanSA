package com.skinscan.sa.domain.usecase

import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.abs

/**
 * Use Case: Compare Progress Between Two Scans (Story 4.2)
 *
 * Calculates improvement metrics between a baseline and current scan:
 * - Overall health score change
 * - Concern-by-concern deltas
 * - Improvement percentage
 */
class CompareScanProgressUseCase @Inject constructor() {

    /**
     * Comparison result with detailed metrics
     */
    data class ComparisonResult(
        val baselineScanId: String,
        val currentScanId: String,
        val baselineDate: java.util.Date,
        val currentDate: java.util.Date,
        val daysBetween: Int,
        val overallImprovement: Float, // -100 to +100
        val baselineHealthScore: Int,
        val currentHealthScore: Int,
        val concernComparisons: List<ConcernComparison>,
        val improvingSkinConcerns: List<SkinConcern>,
        val worseningSkinConcerns: List<SkinConcern>,
        val unchangedSkinConcerns: List<SkinConcern>
    )

    /**
     * Individual concern comparison
     */
    data class ConcernComparison(
        val concern: SkinConcern,
        val baselineSeverity: Float, // 0.0 - 1.0
        val currentSeverity: Float,
        val delta: Float, // Negative = improvement
        val changePercent: Float,
        val trend: Trend
    )

    enum class Trend {
        IMPROVING,
        WORSENING,
        STABLE
    }

    /**
     * Compare two scans to calculate progress
     *
     * @param baseline The earlier scan (reference point)
     * @param current The later scan (comparison point)
     * @return ComparisonResult with all metrics
     */
    fun execute(baseline: ScanResultEntity, current: ScanResultEntity): ComparisonResult {
        val daysBetween = ((current.scannedAt.time - baseline.scannedAt.time) / (24 * 60 * 60 * 1000)).toInt()

        val baselineHealth = baseline.healthScore ?: calculateHealthScore(baseline)
        val currentHealth = current.healthScore ?: calculateHealthScore(current)
        val overallImprovement = (currentHealth - baselineHealth).toFloat()

        val concernComparisons = compareConcerns(baseline, current)

        val improving = concernComparisons.filter { it.trend == Trend.IMPROVING }.map { it.concern }
        val worsening = concernComparisons.filter { it.trend == Trend.WORSENING }.map { it.concern }
        val stable = concernComparisons.filter { it.trend == Trend.STABLE }.map { it.concern }

        return ComparisonResult(
            baselineScanId = baseline.scanId,
            currentScanId = current.scanId,
            baselineDate = baseline.scannedAt,
            currentDate = current.scannedAt,
            daysBetween = daysBetween,
            overallImprovement = overallImprovement,
            baselineHealthScore = baselineHealth,
            currentHealthScore = currentHealth,
            concernComparisons = concernComparisons,
            improvingSkinConcerns = improving,
            worseningSkinConcerns = worsening,
            unchangedSkinConcerns = stable
        )
    }

    private fun compareConcerns(
        baseline: ScanResultEntity,
        current: ScanResultEntity
    ): List<ConcernComparison> {
        val baselineScores = parseConfidenceScores(baseline.confidenceScores)
        val currentScores = parseConfidenceScores(current.confidenceScores)

        return SkinConcern.entries.mapNotNull { concern ->
            val baselineSeverity = baselineScores[concern.name] ?: return@mapNotNull null
            val currentSeverity = currentScores[concern.name] ?: baselineSeverity

            val delta = currentSeverity - baselineSeverity
            val changePercent = if (baselineSeverity > 0.01f) {
                (delta / baselineSeverity) * 100f
            } else {
                0f
            }

            val trend = when {
                delta < -0.05f -> Trend.IMPROVING // Severity decreased = improvement
                delta > 0.05f -> Trend.WORSENING
                else -> Trend.STABLE
            }

            ConcernComparison(
                concern = concern,
                baselineSeverity = baselineSeverity,
                currentSeverity = currentSeverity,
                delta = delta,
                changePercent = changePercent,
                trend = trend
            )
        }
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

    private fun calculateHealthScore(scan: ScanResultEntity): Int {
        val scores = parseConfidenceScores(scan.confidenceScores)
        if (scores.isEmpty()) return 70

        val avgSeverity = scores.values.average().toFloat()
        return ((1f - avgSeverity) * 100).toInt().coerceIn(0, 100)
    }
}
