package com.skinscan.sa.domain.usecase

import com.skinscan.sa.data.db.dao.ProductDao
import com.skinscan.sa.data.db.entity.ProductEntity
import com.skinscan.sa.data.local.IngredientDatabase
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import org.json.JSONArray
import javax.inject.Inject

/**
 * Use Case: Get Product Recommendations
 *
 * Story 3.3: Product Ranking Algorithm
 *
 * Calculates compatibility scores for products based on:
 * 1. Ingredient Match Score (50 points max)
 * 2. Fitzpatrick Compatibility (20 points max)
 * 3. Concern Targeting Score (20 points max)
 * 4. Melanin Optimization Bonus (10 points max)
 */
class GetRecommendationsUseCase @Inject constructor(
    private val productDao: ProductDao
) {
    companion object {
        private const val MAX_INGREDIENT_SCORE = 50f
        private const val MAX_FITZPATRICK_SCORE = 20f
        private const val MAX_CONCERN_SCORE = 20f
        private const val MAX_MELANIN_BONUS = 10f
    }

    /**
     * Recommendation result with score breakdown
     */
    data class ProductRecommendation(
        val product: ProductEntity,
        val compatibilityScore: Int, // 0-100
        val ingredientScore: Float,
        val fitzpatrickScore: Float,
        val concernScore: Float,
        val melaninBonus: Float,
        val matchingIngredients: List<String>,
        val matchingConcerns: List<String>
    )

    /**
     * Grouped recommendations by category
     */
    data class RecommendationResult(
        val cleansers: List<ProductRecommendation>,
        val serums: List<ProductRecommendation>,
        val moisturizers: List<ProductRecommendation>,
        val sunscreens: List<ProductRecommendation>,
        val treatments: List<ProductRecommendation>,
        val allProducts: List<ProductRecommendation>
    )

    /**
     * Get personalized product recommendations
     *
     * @param concerns List of detected skin concerns
     * @param fitzpatrickType User's Fitzpatrick skin type (1-6)
     * @param maxPerCategory Maximum products per category
     * @return RecommendationResult with ranked products
     */
    suspend fun execute(
        concerns: List<SkinConcern>,
        fitzpatrickType: Int,
        maxPerCategory: Int = 4
    ): RecommendationResult {
        // Get all in-stock products
        val allProducts = productDao.getClicksProducts() // Focus on Clicks availability

        // Score and rank all products
        val scoredProducts = allProducts.map { product ->
            scoreProduct(product, concerns, fitzpatrickType)
        }.sortedByDescending { it.compatibilityScore }

        // Group by category
        val byCategory = scoredProducts.groupBy { it.product.category }

        return RecommendationResult(
            cleansers = byCategory["CLEANSER"]?.take(maxPerCategory) ?: emptyList(),
            serums = byCategory["SERUM"]?.take(maxPerCategory) ?: emptyList(),
            moisturizers = byCategory["MOISTURIZER"]?.take(maxPerCategory) ?: emptyList(),
            sunscreens = byCategory["SUNSCREEN"]?.take(maxPerCategory) ?: emptyList(),
            treatments = byCategory["TREATMENT"]?.take(maxPerCategory) ?: emptyList(),
            allProducts = scoredProducts
        )
    }

    /**
     * Score a single product
     */
    private fun scoreProduct(
        product: ProductEntity,
        concerns: List<SkinConcern>,
        fitzpatrickType: Int
    ): ProductRecommendation {
        // Parse product data
        val productIngredients = parseJsonArray(product.keyIngredients)
        val targetConcerns = parseJsonArray(product.targetConcerns)
        val suitableFitzpatrick = parseJsonArrayInt(product.suitableFitzpatrickTypes)

        // 1. Ingredient Match Score (50 points max)
        val beneficialIngredients = concerns.flatMap {
            IngredientDatabase.getIngredientsForConcern(it)
        }.map { it.name }.toSet()

        val matchingIngredients = productIngredients.filter { ingredient ->
            IngredientDatabase.findIngredient(ingredient) != null &&
                    beneficialIngredients.contains(IngredientDatabase.findIngredient(ingredient)?.name)
        }

        val ingredientScore = if (beneficialIngredients.isNotEmpty()) {
            (matchingIngredients.size.toFloat() / beneficialIngredients.size.coerceAtMost(5)) * MAX_INGREDIENT_SCORE
        } else 0f

        // 2. Fitzpatrick Compatibility (20 points max)
        val fitzpatrickScore = if (suitableFitzpatrick.isEmpty() || suitableFitzpatrick.contains(fitzpatrickType)) {
            MAX_FITZPATRICK_SCORE
        } else {
            // Partial score if within 1 type
            val closest = suitableFitzpatrick.minByOrNull { kotlin.math.abs(it - fitzpatrickType) } ?: fitzpatrickType
            if (kotlin.math.abs(closest - fitzpatrickType) == 1) {
                MAX_FITZPATRICK_SCORE * 0.5f
            } else {
                0f
            }
        }

        // 3. Concern Targeting Score (20 points max)
        val concernNames = concerns.map { it.name }
        val matchingConcerns = targetConcerns.filter { concernNames.contains(it) }
        val concernScore = if (concerns.isNotEmpty()) {
            (matchingConcerns.size.toFloat() / concerns.size) * MAX_CONCERN_SCORE
        } else 0f

        // 4. Melanin Optimization Bonus (10 points max)
        val melaninBonus = if (product.isMelaninOptimized && fitzpatrickType >= 4) {
            MAX_MELANIN_BONUS
        } else if (product.isMelaninOptimized) {
            MAX_MELANIN_BONUS * 0.5f
        } else {
            0f
        }

        // Calculate total score
        val totalScore = (ingredientScore + fitzpatrickScore + concernScore + melaninBonus).toInt()
            .coerceIn(0, 100)

        return ProductRecommendation(
            product = product,
            compatibilityScore = totalScore,
            ingredientScore = ingredientScore,
            fitzpatrickScore = fitzpatrickScore,
            concernScore = concernScore,
            melaninBonus = melaninBonus,
            matchingIngredients = matchingIngredients,
            matchingConcerns = matchingConcerns
        )
    }

    /**
     * Parse JSON array of strings
     */
    private fun parseJsonArray(json: String): List<String> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Parse JSON array of integers
     */
    private fun parseJsonArrayInt(json: String): List<Int> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getInt(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
