package com.skinscan.sa.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for skincare products
 *
 * Story 3.1: Seed Product Database with MVP Catalog
 * Contains 50 curated South African skincare products
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val productId: String = UUID.randomUUID().toString(),

    // Basic info
    val name: String,
    val brand: String,
    val category: String, // CLEANSER, MOISTURIZER, SERUM, SUNSCREEN, TREATMENT

    // Description
    val description: String,
    val howToUse: String? = null,

    // Key ingredients (JSON array)
    // e.g., ["NIACINAMIDE", "VITAMIN_C", "HYALURONIC_ACID"]
    val keyIngredients: String,

    // Full ingredients list (JSON array)
    val fullIngredients: String? = null,

    // Target concerns (JSON array)
    // e.g., ["HYPERPIGMENTATION", "DRYNESS"]
    val targetConcerns: String,

    // Suitable Fitzpatrick types (JSON array)
    // e.g., [4, 5, 6] for melanin-rich skin focused products
    val suitableFitzpatrickTypes: String,

    // Pricing
    val priceZar: Double,
    val sizeMl: Int? = null,

    // Availability
    val retailers: String, // JSON array: ["CLICKS", "DISCHEM", "WOOLWORTHS"]
    val inStock: Boolean = true,

    // Ratings
    val rating: Float? = null, // 0.0 - 5.0
    val reviewCount: Int = 0,

    // Image URL (for future use)
    val imageUrl: String? = null,

    // Metadata
    val isLocalBrand: Boolean = false,
    val isMelaninOptimized: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Product category enum
 */
enum class ProductCategory(val displayName: String) {
    CLEANSER("Cleanser"),
    MOISTURIZER("Moisturizer"),
    SERUM("Serum"),
    SUNSCREEN("Sunscreen"),
    TREATMENT("Treatment"),
    TONER("Toner"),
    MASK("Face Mask"),
    EXFOLIATOR("Exfoliator"),
    EYE_CREAM("Eye Cream"),
    OIL("Face Oil")
}
