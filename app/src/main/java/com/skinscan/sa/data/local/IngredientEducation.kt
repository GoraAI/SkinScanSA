package com.skinscan.sa.data.local

import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern

/**
 * Ingredient Education Database (Story 5.4)
 *
 * Static database of skincare ingredient information for educational tooltips.
 */
object IngredientEducation {

    /**
     * Ingredient information data class
     */
    data class IngredientInfo(
        val name: String,
        val aliases: List<String> = emptyList(),
        val benefits: List<String>,
        val idealFor: List<SkinConcern>,
        val melaninSafe: Boolean,
        val melaninNote: String? = null,
        val learnMoreUrl: String? = null
    )

    /**
     * Comprehensive ingredient database (~30 common ingredients)
     */
    val INGREDIENT_INFO: Map<String, IngredientInfo> = mapOf(
        // Brightening Ingredients
        "Niacinamide" to IngredientInfo(
            name = "Niacinamide",
            aliases = listOf("Vitamin B3", "Nicotinamide"),
            benefits = listOf(
                "Brightens skin and reduces dark spots",
                "Strengthens skin barrier",
                "Reduces inflammation and redness",
                "Minimizes pore appearance"
            ),
            idealFor = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.ACNE, SkinConcern.OILINESS),
            melaninSafe = true,
            learnMoreUrl = "https://www.paulaschoice.com/ingredient-dictionary/skin-brightening/niacinamide.html"
        ),

        "Vitamin C" to IngredientInfo(
            name = "Vitamin C",
            aliases = listOf("Ascorbic Acid", "L-Ascorbic Acid", "Sodium Ascorbyl Phosphate"),
            benefits = listOf(
                "Powerful antioxidant protection",
                "Brightens and evens skin tone",
                "Stimulates collagen production",
                "Reduces appearance of dark spots"
            ),
            idealFor = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.WRINKLES),
            melaninSafe = true,
            melaninNote = "Start with lower concentrations (10-15%) to avoid irritation"
        ),

        "Alpha Arbutin" to IngredientInfo(
            name = "Alpha Arbutin",
            aliases = listOf("Arbutin"),
            benefits = listOf(
                "Gently inhibits melanin production",
                "Fades dark spots and hyperpigmentation",
                "Safe for sensitive skin",
                "Works well with other brighteners"
            ),
            idealFor = listOf(SkinConcern.HYPERPIGMENTATION),
            melaninSafe = true,
            melaninNote = "Excellent choice for melanin-rich skin - gentle yet effective"
        ),

        "Tranexamic Acid" to IngredientInfo(
            name = "Tranexamic Acid",
            aliases = listOf("TXA"),
            benefits = listOf(
                "Reduces stubborn dark spots",
                "Helps with melasma",
                "Works on all skin tones",
                "Gentle and non-irritating"
            ),
            idealFor = listOf(SkinConcern.HYPERPIGMENTATION),
            melaninSafe = true,
            melaninNote = "One of the safest options for treating dark spots on melanin-rich skin"
        ),

        // Hydrating Ingredients
        "Hyaluronic Acid" to IngredientInfo(
            name = "Hyaluronic Acid",
            aliases = listOf("HA", "Sodium Hyaluronate"),
            benefits = listOf(
                "Holds 1000x its weight in water",
                "Deeply hydrates all skin layers",
                "Plumps skin and reduces fine lines",
                "Suitable for all skin types"
            ),
            idealFor = listOf(SkinConcern.DRYNESS, SkinConcern.WRINKLES),
            melaninSafe = true
        ),

        "Glycerin" to IngredientInfo(
            name = "Glycerin",
            aliases = listOf("Glycerol"),
            benefits = listOf(
                "Excellent moisturizer",
                "Draws water to skin",
                "Strengthens skin barrier",
                "Non-comedogenic"
            ),
            idealFor = listOf(SkinConcern.DRYNESS),
            melaninSafe = true
        ),

        "Ceramides" to IngredientInfo(
            name = "Ceramides",
            aliases = listOf("Ceramide NP", "Ceramide AP", "Ceramide EOP"),
            benefits = listOf(
                "Restores skin barrier",
                "Locks in moisture",
                "Protects against environmental damage",
                "Reduces sensitivity"
            ),
            idealFor = listOf(SkinConcern.DRYNESS),
            melaninSafe = true
        ),

        "Squalane" to IngredientInfo(
            name = "Squalane",
            aliases = listOf("Squalene"),
            benefits = listOf(
                "Lightweight moisturizer",
                "Mimics natural skin oils",
                "Non-greasy hydration",
                "Antioxidant properties"
            ),
            idealFor = listOf(SkinConcern.DRYNESS, SkinConcern.OILINESS),
            melaninSafe = true
        ),

        // Acne-Fighting Ingredients
        "Salicylic Acid" to IngredientInfo(
            name = "Salicylic Acid",
            aliases = listOf("BHA", "Beta Hydroxy Acid"),
            benefits = listOf(
                "Penetrates and unclogs pores",
                "Exfoliates inside the pore",
                "Reduces breakouts",
                "Calms inflammation"
            ),
            idealFor = listOf(SkinConcern.ACNE, SkinConcern.OILINESS),
            melaninSafe = true,
            melaninNote = "Use 2% concentration max to avoid post-inflammatory hyperpigmentation"
        ),

        "Benzoyl Peroxide" to IngredientInfo(
            name = "Benzoyl Peroxide",
            aliases = listOf("BPO"),
            benefits = listOf(
                "Kills acne-causing bacteria",
                "Reduces inflammation",
                "Prevents new breakouts",
                "Works quickly"
            ),
            idealFor = listOf(SkinConcern.ACNE),
            melaninSafe = true,
            melaninNote = "Start with 2.5% - higher concentrations can cause dryness and dark spots"
        ),

        "Tea Tree Oil" to IngredientInfo(
            name = "Tea Tree Oil",
            aliases = listOf("Melaleuca Alternifolia Leaf Oil"),
            benefits = listOf(
                "Natural antibacterial",
                "Reduces inflammation",
                "Helps with minor breakouts",
                "Soothes irritation"
            ),
            idealFor = listOf(SkinConcern.ACNE),
            melaninSafe = true,
            melaninNote = "Always use diluted - pure tea tree oil can irritate"
        ),

        // Anti-Aging Ingredients
        "Retinol" to IngredientInfo(
            name = "Retinol",
            aliases = listOf("Vitamin A", "Retinoid", "Retinyl Palmitate"),
            benefits = listOf(
                "Speeds cell turnover",
                "Reduces fine lines and wrinkles",
                "Improves skin texture",
                "Fades dark spots"
            ),
            idealFor = listOf(SkinConcern.WRINKLES, SkinConcern.HYPERPIGMENTATION, SkinConcern.ACNE),
            melaninSafe = true,
            melaninNote = "Start low (0.25-0.5%) and build up slowly - can cause irritation and dark spots if overused"
        ),

        "Peptides" to IngredientInfo(
            name = "Peptides",
            aliases = listOf("Matrixyl", "Argireline", "Copper Peptides"),
            benefits = listOf(
                "Stimulates collagen production",
                "Reduces fine lines",
                "Improves skin firmness",
                "Gentle and non-irritating"
            ),
            idealFor = listOf(SkinConcern.WRINKLES),
            melaninSafe = true
        ),

        "Bakuchiol" to IngredientInfo(
            name = "Bakuchiol",
            aliases = listOf(),
            benefits = listOf(
                "Natural retinol alternative",
                "Gentle anti-aging",
                "Safe during pregnancy",
                "Less irritating than retinol"
            ),
            idealFor = listOf(SkinConcern.WRINKLES, SkinConcern.HYPERPIGMENTATION),
            melaninSafe = true,
            melaninNote = "Excellent retinol alternative for sensitive melanin-rich skin"
        ),

        // Soothing Ingredients
        "Aloe Vera" to IngredientInfo(
            name = "Aloe Vera",
            aliases = listOf("Aloe Barbadensis Leaf Juice"),
            benefits = listOf(
                "Soothes and calms skin",
                "Hydrates without greasiness",
                "Helps with sunburn",
                "Anti-inflammatory"
            ),
            idealFor = listOf(SkinConcern.DRYNESS),
            melaninSafe = true
        ),

        "Centella Asiatica" to IngredientInfo(
            name = "Centella Asiatica",
            aliases = listOf("Cica", "Tiger Grass", "Gotu Kola"),
            benefits = listOf(
                "Calms irritation and redness",
                "Supports wound healing",
                "Strengthens skin barrier",
                "Anti-inflammatory"
            ),
            idealFor = listOf(SkinConcern.ACNE, SkinConcern.DRYNESS),
            melaninSafe = true
        ),

        "Allantoin" to IngredientInfo(
            name = "Allantoin",
            aliases = listOf(),
            benefits = listOf(
                "Soothes irritated skin",
                "Promotes healing",
                "Moisturizes",
                "Non-irritating"
            ),
            idealFor = listOf(SkinConcern.DRYNESS),
            melaninSafe = true
        ),

        // Exfoliating Ingredients
        "Glycolic Acid" to IngredientInfo(
            name = "Glycolic Acid",
            aliases = listOf("AHA", "Alpha Hydroxy Acid"),
            benefits = listOf(
                "Exfoliates dead skin cells",
                "Brightens dull skin",
                "Reduces fine lines",
                "Improves texture"
            ),
            idealFor = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.WRINKLES),
            melaninSafe = true,
            melaninNote = "Use with caution - start with low concentrations (5-10%) and always use sunscreen"
        ),

        "Lactic Acid" to IngredientInfo(
            name = "Lactic Acid",
            aliases = listOf("AHA"),
            benefits = listOf(
                "Gentle exfoliation",
                "Hydrates while exfoliating",
                "Brightens skin",
                "Suitable for sensitive skin"
            ),
            idealFor = listOf(SkinConcern.HYPERPIGMENTATION, SkinConcern.DRYNESS),
            melaninSafe = true,
            melaninNote = "Gentler than glycolic acid - great for melanin-rich skin"
        ),

        "PHA" to IngredientInfo(
            name = "Polyhydroxy Acids",
            aliases = listOf("Gluconolactone", "Lactobionic Acid"),
            benefits = listOf(
                "Gentlest chemical exfoliant",
                "Hydrates while exfoliating",
                "Won't increase sun sensitivity",
                "Great for sensitive skin"
            ),
            idealFor = listOf(SkinConcern.DRYNESS, SkinConcern.WRINKLES),
            melaninSafe = true,
            melaninNote = "Best choice for sensitive melanin-rich skin"
        ),

        // Oil Control
        "Zinc" to IngredientInfo(
            name = "Zinc",
            aliases = listOf("Zinc Oxide", "Zinc PCA"),
            benefits = listOf(
                "Controls oil production",
                "Reduces shine",
                "Calms inflammation",
                "Protects from sun (zinc oxide)"
            ),
            idealFor = listOf(SkinConcern.OILINESS, SkinConcern.ACNE),
            melaninSafe = true,
            melaninNote = "Zinc oxide sunscreens may leave white cast - look for tinted versions"
        ),

        "Kaolin" to IngredientInfo(
            name = "Kaolin Clay",
            aliases = listOf("China Clay", "White Clay"),
            benefits = listOf(
                "Absorbs excess oil",
                "Gentle deep cleansing",
                "Minimizes pores",
                "Suitable for sensitive skin"
            ),
            idealFor = listOf(SkinConcern.OILINESS),
            melaninSafe = true
        ),

        // Sun Protection
        "Titanium Dioxide" to IngredientInfo(
            name = "Titanium Dioxide",
            aliases = listOf(),
            benefits = listOf(
                "Physical UV protection",
                "Reflects UV rays",
                "Gentle on sensitive skin",
                "Stable in sunlight"
            ),
            idealFor = listOf(),
            melaninSafe = true,
            melaninNote = "May leave white cast - look for micronized or tinted formulas"
        ),

        "Avobenzone" to IngredientInfo(
            name = "Avobenzone",
            aliases = listOf("Butyl Methoxydibenzoylmethane"),
            benefits = listOf(
                "UVA protection",
                "No white cast",
                "Absorbs UV rays",
                "Works well with other filters"
            ),
            idealFor = listOf(),
            melaninSafe = true
        )
    )

    /**
     * Find ingredient info by name (case-insensitive, includes aliases)
     */
    fun findIngredient(name: String): IngredientInfo? {
        val normalizedName = name.trim().lowercase()

        // Direct match
        INGREDIENT_INFO.entries.find {
            it.key.lowercase() == normalizedName
        }?.let { return it.value }

        // Alias match
        INGREDIENT_INFO.values.find { info ->
            info.aliases.any { alias -> alias.lowercase() == normalizedName }
        }?.let { return it }

        // Partial match
        INGREDIENT_INFO.entries.find {
            it.key.lowercase().contains(normalizedName) ||
                    normalizedName.contains(it.key.lowercase())
        }?.let { return it.value }

        return null
    }

    /**
     * Get all ingredients for a specific concern
     */
    fun getIngredientsForConcern(concern: SkinConcern): List<IngredientInfo> {
        return INGREDIENT_INFO.values.filter { it.idealFor.contains(concern) }
    }

    /**
     * Get melanin-safe note for an ingredient
     */
    fun getMelaninSafetyInfo(name: String): String {
        val info = findIngredient(name) ?: return "Information not available"

        return if (info.melaninSafe) {
            info.melaninNote ?: "Safe for all skin tones"
        } else {
            info.melaninNote ?: "Use with caution on melanin-rich skin"
        }
    }
}
