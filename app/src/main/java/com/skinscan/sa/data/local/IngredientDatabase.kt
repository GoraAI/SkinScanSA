package com.skinscan.sa.data.local

import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern

/**
 * Ingredient-to-Concern Mapping Engine
 *
 * Story 3.2: Maps skin concerns to scientifically-proven beneficial ingredients
 *
 * Based on dermatological research for ingredients effective on melanin-rich skin
 */
object IngredientDatabase {

    /**
     * Key active ingredients with their properties
     */
    enum class Ingredient(
        val displayName: String,
        val aliases: List<String> = emptyList(),
        val description: String,
        val concerns: List<SkinConcern>,
        val effectivenessScore: Float = 0.8f, // 0.0 - 1.0
        val safeForMelaninRichSkin: Boolean = true
    ) {
        // Hyperpigmentation fighters
        NIACINAMIDE(
            displayName = "Niacinamide (Vitamin B3)",
            aliases = listOf("Nicotinamide", "Vitamin B3"),
            description = "Reduces melanin transfer, brightens skin, strengthens barrier",
            concerns = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.OILINESS, SkinConcern.ACNE),
            effectivenessScore = 0.95f,
            safeForMelaninRichSkin = true
        ),
        VITAMIN_C(
            displayName = "Vitamin C",
            aliases = listOf("Ascorbic Acid", "L-Ascorbic Acid", "Sodium Ascorbyl Phosphate", "Ascorbyl Glucoside"),
            description = "Antioxidant, brightens, protects against sun damage",
            concerns = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.WRINKLES),
            effectivenessScore = 0.90f,
            safeForMelaninRichSkin = true
        ),
        ALPHA_ARBUTIN(
            displayName = "Alpha Arbutin",
            aliases = listOf("Arbutin"),
            description = "Inhibits tyrosinase to reduce dark spots safely",
            concerns = listOf(SkinConcern.HYPERPIGMENTATION),
            effectivenessScore = 0.88f,
            safeForMelaninRichSkin = true
        ),
        KOJIC_ACID(
            displayName = "Kojic Acid",
            description = "Natural skin lightener from fungi",
            concerns = listOf(SkinConcern.HYPERPIGMENTATION),
            effectivenessScore = 0.82f,
            safeForMelaninRichSkin = true
        ),
        TRANEXAMIC_ACID(
            displayName = "Tranexamic Acid",
            description = "Reduces melanin production, effective for melasma",
            concerns = listOf(SkinConcern.HYPERPIGMENTATION),
            effectivenessScore = 0.85f,
            safeForMelaninRichSkin = true
        ),
        AZELAIC_ACID(
            displayName = "Azelaic Acid",
            description = "Reduces hyperpigmentation, anti-inflammatory, antibacterial",
            concerns = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.ACNE),
            effectivenessScore = 0.87f,
            safeForMelaninRichSkin = true
        ),

        // Hydration boosters
        HYALURONIC_ACID(
            displayName = "Hyaluronic Acid",
            aliases = listOf("Sodium Hyaluronate", "HA"),
            description = "Holds 1000x its weight in water, deep hydration",
            concerns = listOf(SkinConcern.DRYNESS, SkinConcern.WRINKLES),
            effectivenessScore = 0.95f,
            safeForMelaninRichSkin = true
        ),
        GLYCERIN(
            displayName = "Glycerin",
            aliases = listOf("Glycerol"),
            description = "Humectant that draws moisture to skin",
            concerns = listOf(SkinConcern.DRYNESS),
            effectivenessScore = 0.90f,
            safeForMelaninRichSkin = true
        ),
        CERAMIDES(
            displayName = "Ceramides",
            description = "Restores skin barrier, locks in moisture",
            concerns = listOf(SkinConcern.DRYNESS),
            effectivenessScore = 0.92f,
            safeForMelaninRichSkin = true
        ),
        SQUALANE(
            displayName = "Squalane",
            description = "Lightweight oil, mimics natural sebum",
            concerns = listOf(SkinConcern.DRYNESS),
            effectivenessScore = 0.85f,
            safeForMelaninRichSkin = true
        ),
        SHEA_BUTTER(
            displayName = "Shea Butter",
            aliases = listOf("Butyrospermum Parkii"),
            description = "Rich moisturizer, popular in African skincare",
            concerns = listOf(SkinConcern.DRYNESS),
            effectivenessScore = 0.88f,
            safeForMelaninRichSkin = true
        ),

        // Acne fighters
        SALICYLIC_ACID(
            displayName = "Salicylic Acid",
            aliases = listOf("BHA", "Beta Hydroxy Acid"),
            description = "Penetrates pores, clears congestion",
            concerns = listOf(SkinConcern.ACNE, SkinConcern.OILINESS),
            effectivenessScore = 0.90f,
            safeForMelaninRichSkin = true
        ),
        BENZOYL_PEROXIDE(
            displayName = "Benzoyl Peroxide",
            description = "Kills acne bacteria, reduces inflammation",
            concerns = listOf(SkinConcern.ACNE),
            effectivenessScore = 0.88f,
            safeForMelaninRichSkin = true // Use 2.5-5% for melanin-rich skin
        ),
        TEA_TREE_OIL(
            displayName = "Tea Tree Oil",
            aliases = listOf("Melaleuca Alternifolia"),
            description = "Natural antibacterial and anti-inflammatory",
            concerns = listOf(SkinConcern.ACNE),
            effectivenessScore = 0.75f,
            safeForMelaninRichSkin = true
        ),
        ZINC(
            displayName = "Zinc",
            aliases = listOf("Zinc Oxide", "Zinc PCA"),
            description = "Reduces sebum, anti-inflammatory",
            concerns = listOf(SkinConcern.ACNE, SkinConcern.OILINESS),
            effectivenessScore = 0.80f,
            safeForMelaninRichSkin = true
        ),

        // Oil control
        KAOLIN(
            displayName = "Kaolin Clay",
            description = "Absorbs excess oil without drying",
            concerns = listOf(SkinConcern.OILINESS),
            effectivenessScore = 0.82f,
            safeForMelaninRichSkin = true
        ),

        // Anti-aging
        RETINOL(
            displayName = "Retinol",
            aliases = listOf("Vitamin A", "Retinyl Palmitate", "Retinal"),
            description = "Gold standard for anti-aging, increases cell turnover",
            concerns = listOf(SkinConcern.WRINKLES, SkinConcern.HYPERPIGMENTATION),
            effectivenessScore = 0.95f,
            safeForMelaninRichSkin = true // Start low, increase gradually
        ),
        PEPTIDES(
            displayName = "Peptides",
            aliases = listOf("Palmitoyl Tripeptide", "Matrixyl", "Copper Peptides"),
            description = "Signal skin to produce collagen",
            concerns = listOf(SkinConcern.WRINKLES),
            effectivenessScore = 0.85f,
            safeForMelaninRichSkin = true
        ),
        BAKUCHIOL(
            displayName = "Bakuchiol",
            description = "Plant-based retinol alternative, gentler",
            concerns = listOf(SkinConcern.WRINKLES),
            effectivenessScore = 0.80f,
            safeForMelaninRichSkin = true
        )
    }

    /**
     * Get ingredients effective for a specific skin concern
     */
    fun getIngredientsForConcern(concern: SkinConcern): List<Ingredient> {
        return Ingredient.entries
            .filter { concern in it.concerns }
            .sortedByDescending { it.effectivenessScore }
    }

    /**
     * Get ingredients safe for melanin-rich skin
     */
    fun getMelaninSafeIngredients(): List<Ingredient> {
        return Ingredient.entries.filter { it.safeForMelaninRichSkin }
    }

    /**
     * Check if an ingredient matches by name or alias (case-insensitive)
     */
    fun findIngredient(name: String): Ingredient? {
        val searchTerm = name.uppercase().trim()
        return Ingredient.entries.find { ingredient ->
            ingredient.name == searchTerm ||
                    ingredient.displayName.uppercase() == searchTerm ||
                    ingredient.aliases.any { it.uppercase() == searchTerm }
        }
    }

    /**
     * Calculate ingredient match score for a product
     */
    fun calculateIngredientScore(
        productIngredients: List<String>,
        targetConcerns: List<SkinConcern>
    ): Float {
        if (targetConcerns.isEmpty()) return 0f

        val relevantIngredients = targetConcerns.flatMap { getIngredientsForConcern(it) }.toSet()
        var totalScore = 0f
        var matchCount = 0

        productIngredients.forEach { ingredientName ->
            val ingredient = findIngredient(ingredientName)
            if (ingredient != null && ingredient in relevantIngredients) {
                totalScore += ingredient.effectivenessScore
                matchCount++
            }
        }

        return if (matchCount > 0) totalScore / matchCount else 0f
    }
}
