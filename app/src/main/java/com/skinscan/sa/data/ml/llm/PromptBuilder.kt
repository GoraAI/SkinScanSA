package com.skinscan.sa.data.ml.llm

import com.skinscan.sa.data.db.entity.ProductEntity
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.db.entity.UserProfileEntity
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import org.json.JSONArray
import javax.inject.Inject

/**
 * Prompt Builder (Story 5.2)
 *
 * Constructs structured prompts for Gemma 3n to generate
 * personalized product explanations.
 */
class PromptBuilder @Inject constructor() {

    companion object {
        /**
         * Main prompt template for product explanations
         */
        private const val RECOMMENDATION_PROMPT_TEMPLATE = """
You are a skincare expert assistant. Explain why this product is recommended for this user in 2-3 sentences.

User's Detected Concerns: {{detected_concerns}}
User's Skin Type: Fitzpatrick Type {{fitzpatrick_type}}
User's Location: {{location}}

Product: {{product_name}} by {{brand}}
Category: {{category}}
Key Ingredients: {{key_ingredients}}
Price: R{{price}}

Guidelines:
1. Focus on how the KEY INGREDIENTS address the DETECTED CONCERNS
2. Explain why this product is suitable for melanin-rich skin (Fitzpatrick IV-VI)
3. Mention how it fits into their skincare routine ({{category}} step)
4. Use cosmetic/wellness language ONLY - never medical claims (avoid "treats", "cures", "heals")
5. Use South African context if relevant
6. Keep it conversational and empowering
7. Maximum 3 sentences

Generate the explanation:
"""

        /**
         * Climate descriptions by South African province
         */
        private val CLIMATE_DESCRIPTIONS = mapOf(
            "Gauteng" to "dry Highveld climate with intense sun",
            "Western Cape" to "Mediterranean climate with dry summers",
            "KwaZulu-Natal" to "humid subtropical climate",
            "Eastern Cape" to "varied coastal and inland climate",
            "Free State" to "cold winters and warm summers",
            "Mpumalanga" to "subtropical Lowveld climate",
            "Limpopo" to "hot subtropical climate",
            "North West" to "semi-arid climate",
            "Northern Cape" to "arid desert climate"
        )

        /**
         * Fitzpatrick type descriptions
         */
        private val FITZPATRICK_DESCRIPTIONS = mapOf(
            1 to "Very fair, always burns",
            2 to "Fair, usually burns",
            3 to "Medium, sometimes burns",
            4 to "Olive, rarely burns",
            5 to "Brown, very rarely burns",
            6 to "Dark brown/black, never burns"
        )
    }

    /**
     * Build prompt for product recommendation explanation
     *
     * @param product The recommended product
     * @param scanResult The user's scan result
     * @param userProfile The user's profile (optional)
     * @return Formatted prompt string
     */
    fun buildRecommendationPrompt(
        product: ProductEntity,
        scanResult: ScanResultEntity,
        userProfile: UserProfileEntity? = null
    ): String {
        val concerns = parseConcerns(scanResult.detectedConcerns)
        val concernNames = concerns.map { formatConcernName(it) }

        val location = userProfile?.location ?: "South Africa"
        val climate = CLIMATE_DESCRIPTIONS[location] ?: "South African climate"

        val keyIngredients = parseJsonArray(product.keyIngredients)

        return RECOMMENDATION_PROMPT_TEMPLATE
            .replace("{{detected_concerns}}", concernNames.joinToString(", "))
            .replace("{{fitzpatrick_type}}", "${scanResult.fitzpatrickType ?: 5}")
            .replace("{{location}}", "$location ($climate)")
            .replace("{{product_name}}", product.name)
            .replace("{{brand}}", product.brand)
            .replace("{{category}}", product.category)
            .replace("{{key_ingredients}}", keyIngredients.joinToString(", "))
            .replace("{{price}}", "%.0f".format(product.priceZar))
            .trim()
    }

    /**
     * Build prompt for ingredient education
     *
     * @param ingredientName Name of the ingredient
     * @return Formatted prompt for ingredient explanation
     */
    fun buildIngredientPrompt(ingredientName: String): String {
        return """
Explain the skincare ingredient "$ingredientName" in 2 sentences.

Guidelines:
1. Describe its main benefits for skin
2. Mention if it's safe for melanin-rich skin
3. Use simple, conversational language
4. Avoid medical claims

Generate the explanation:
""".trim()
    }

    /**
     * Build prompt for progress insight
     *
     * @param improvements List of improving concerns
     * @param worsening List of worsening concerns
     * @param daysBetween Days between scans
     * @return Formatted prompt for progress insight
     */
    fun buildProgressInsightPrompt(
        improvements: List<SkinConcern>,
        worsening: List<SkinConcern>,
        daysBetween: Int
    ): String {
        val improvingNames = improvements.map { formatConcernName(it) }
        val worseningNames = worsening.map { formatConcernName(it) }

        return """
Generate a brief, encouraging insight about this user's skin progress.

Time period: $daysBetween days
Improving concerns: ${improvingNames.joinToString(", ").ifEmpty { "None" }}
Areas needing attention: ${worseningNames.joinToString(", ").ifEmpty { "None" }}

Guidelines:
1. Be encouraging and supportive
2. Acknowledge improvements specifically
3. Give gentle suggestions for areas needing attention
4. Keep it to 2 sentences maximum
5. Use wellness language, not medical

Generate the insight:
""".trim()
    }

    /**
     * Format concern name for display in prompt
     */
    private fun formatConcernName(concern: SkinConcern): String {
        return concern.displayName
    }

    /**
     * Parse concerns from JSON string
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
     * Get template fallback explanation (when LLM unavailable)
     */
    fun getTemplateExplanation(
        product: ProductEntity,
        concerns: List<SkinConcern>
    ): String {
        val concernText = concerns.take(2).joinToString(" and ") {
            it.displayName.lowercase()
        }.ifEmpty { "your skin concerns" }

        val ingredients = parseJsonArray(product.keyIngredients).take(3)
        val ingredientText = when {
            ingredients.isEmpty() -> "active ingredients"
            ingredients.size == 1 -> ingredients[0]
            else -> ingredients.dropLast(1).joinToString(", ") + " and " + ingredients.last()
        }

        val categoryStep = when (product.category.uppercase()) {
            "CLEANSER" -> "cleansing"
            "SERUM" -> "treatment"
            "MOISTURIZER" -> "moisturizing"
            "SUNSCREEN" -> "sun protection"
            "TREATMENT" -> "targeted treatment"
            else -> "skincare"
        }

        return "This ${product.category.lowercase()} contains $ingredientText which can help with $concernText. " +
                "It's formulated to be gentle on melanin-rich skin and fits perfectly into your $categoryStep routine step."
    }

    /**
     * Validate prompt length (for token limits)
     */
    fun validatePromptLength(prompt: String, maxTokens: Int = 512): Boolean {
        // Rough estimate: 4 characters per token
        val estimatedTokens = prompt.length / 4
        return estimatedTokens <= maxTokens
    }
}
