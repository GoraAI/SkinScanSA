package com.skinscan.sa.data.repository

import android.util.Log
import com.skinscan.sa.data.db.entity.ProductEntity
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.db.entity.UserProfileEntity
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.data.ml.llm.LLMInference
import com.skinscan.sa.data.ml.llm.PromptBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Explainability Repository (Story 5.3)
 *
 * Coordinates explanation generation with caching and fallback logic.
 */
@Singleton
class ExplainabilityRepository @Inject constructor(
    private val llmInference: LLMInference,
    private val promptBuilder: PromptBuilder
) {
    companion object {
        private const val TAG = "ExplainabilityRepo"
        private const val CACHE_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    // In-memory cache for explanations (would be Room entity in production)
    private val explanationCache = ConcurrentHashMap<String, CachedExplanation>()
    private val generationMutex = Mutex()

    /**
     * Cached explanation data
     */
    data class CachedExplanation(
        val explanation: String,
        val generatedAt: Long,
        val isLLMGenerated: Boolean
    )

    /**
     * Get or generate explanation for a product recommendation
     *
     * @param product The recommended product
     * @param scanResult User's scan result
     * @param userProfile User's profile (optional)
     * @return Explanation text
     */
    suspend fun getExplanation(
        product: ProductEntity,
        scanResult: ScanResultEntity,
        userProfile: UserProfileEntity? = null
    ): String {
        val cacheKey = "${scanResult.scanId}_${product.productId}"

        // Check cache first
        val cached = explanationCache[cacheKey]
        if (cached != null && !isCacheExpired(cached)) {
            Log.d(TAG, "Using cached explanation for ${product.name}")
            return cached.explanation
        }

        // Generate new explanation
        return generationMutex.withLock {
            // Double-check cache after acquiring lock
            val rechecked = explanationCache[cacheKey]
            if (rechecked != null && !isCacheExpired(rechecked)) {
                return@withLock rechecked.explanation
            }

            try {
                val explanation = generateExplanation(product, scanResult, userProfile)

                // Cache the result
                explanationCache[cacheKey] = CachedExplanation(
                    explanation = explanation,
                    generatedAt = System.currentTimeMillis(),
                    isLLMGenerated = llmInference.isAvailable()
                )

                explanation
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate explanation", e)
                // Return template fallback on error
                getTemplateFallback(product, scanResult)
            }
        }
    }

    /**
     * Generate explanation using LLM or template fallback
     */
    private suspend fun generateExplanation(
        product: ProductEntity,
        scanResult: ScanResultEntity,
        userProfile: UserProfileEntity?
    ): String {
        return if (llmInference.isAvailable()) {
            try {
                val prompt = promptBuilder.buildRecommendationPrompt(
                    product = product,
                    scanResult = scanResult,
                    userProfile = userProfile
                )

                val result = llmInference.generateExplanation(prompt)
                result.getOrElse {
                    Log.w(TAG, "LLM generation failed, using fallback")
                    getTemplateFallback(product, scanResult)
                }
            } catch (e: Exception) {
                Log.e(TAG, "LLM inference error", e)
                getTemplateFallback(product, scanResult)
            }
        } else {
            Log.d(TAG, "LLM not available, using template")
            getTemplateFallback(product, scanResult)
        }
    }

    /**
     * Get template-based fallback explanation
     */
    private fun getTemplateFallback(
        product: ProductEntity,
        scanResult: ScanResultEntity
    ): String {
        val concerns = parseConcerns(scanResult.detectedConcerns)
        return promptBuilder.getTemplateExplanation(product, concerns)
    }

    /**
     * Generate batch explanations for multiple products
     */
    suspend fun getExplanations(
        products: List<ProductEntity>,
        scanResult: ScanResultEntity,
        userProfile: UserProfileEntity? = null
    ): Map<String, String> {
        return products.associate { product ->
            product.productId to getExplanation(product, scanResult, userProfile)
        }
    }

    /**
     * Clear explanation cache
     */
    fun clearCache() {
        explanationCache.clear()
    }

    /**
     * Clear expired cache entries
     */
    fun clearExpiredCache() {
        val now = System.currentTimeMillis()
        explanationCache.entries.removeIf { (_, value) ->
            now - value.generatedAt > CACHE_EXPIRY_MS
        }
    }

    /**
     * Check if cached entry is expired
     */
    private fun isCacheExpired(cached: CachedExplanation): Boolean {
        return System.currentTimeMillis() - cached.generatedAt > CACHE_EXPIRY_MS
    }

    /**
     * Parse concerns from JSON
     */
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
}
