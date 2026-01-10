package com.skinscan.sa.data.api

import com.skinscan.sa.data.db.entity.ProductEntity
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock Clicks API Service (Story 3.6)
 *
 * Simulates the Clicks retail API for product availability and pricing.
 * In production, this would connect to the real Clicks API.
 *
 * Features:
 * - Product availability checking
 * - Real-time price updates
 * - Stock level queries
 * - Store locator integration
 */
@Singleton
class ClicksApiService @Inject constructor() {

    companion object {
        private const val MOCK_DELAY_MS = 500L
        private const val BASE_URL = "https://api.clicks.co.za/v1" // Mock URL
    }

    /**
     * Product availability response
     */
    data class ProductAvailability(
        val productId: String,
        val isAvailable: Boolean,
        val stockLevel: StockLevel,
        val currentPrice: Double,
        val originalPrice: Double?,
        val isOnSale: Boolean,
        val storesWithStock: Int
    )

    /**
     * Stock level enum
     */
    enum class StockLevel {
        IN_STOCK,       // > 10 units
        LOW_STOCK,      // 1-10 units
        OUT_OF_STOCK,   // 0 units
        COMING_SOON     // Pre-order available
    }

    /**
     * Store availability info
     */
    data class StoreAvailability(
        val storeId: String,
        val storeName: String,
        val address: String,
        val city: String,
        val province: String,
        val distanceKm: Double?,
        val stockLevel: StockLevel,
        val phoneNumber: String?
    )

    /**
     * Price history entry
     */
    data class PriceHistory(
        val date: Long,
        val price: Double
    )

    /**
     * Check product availability at Clicks
     *
     * @param productId The product identifier
     * @return ProductAvailability with current status
     */
    suspend fun checkAvailability(productId: String): Result<ProductAvailability> {
        // Simulate network delay
        delay(MOCK_DELAY_MS)

        // Mock response - in production this would be a real API call
        return Result.success(
            ProductAvailability(
                productId = productId,
                isAvailable = true,
                stockLevel = StockLevel.IN_STOCK,
                currentPrice = generateMockPrice(productId),
                originalPrice = null,
                isOnSale = false,
                storesWithStock = (5..25).random()
            )
        )
    }

    /**
     * Get nearby stores with product in stock
     *
     * @param productId The product identifier
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radiusKm Search radius in kilometers
     * @return List of nearby stores with availability
     */
    suspend fun getNearbyStores(
        productId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<StoreAvailability>> {
        delay(MOCK_DELAY_MS)

        // Mock South African stores
        val mockStores = listOf(
            StoreAvailability(
                storeId = "clicks_001",
                storeName = "Clicks Sandton City",
                address = "Sandton City Shopping Centre",
                city = "Sandton",
                province = "Gauteng",
                distanceKm = 2.3,
                stockLevel = StockLevel.IN_STOCK,
                phoneNumber = "+27 11 883 4000"
            ),
            StoreAvailability(
                storeId = "clicks_002",
                storeName = "Clicks Mall of Africa",
                address = "Mall of Africa, Midrand",
                city = "Midrand",
                province = "Gauteng",
                distanceKm = 5.7,
                stockLevel = StockLevel.IN_STOCK,
                phoneNumber = "+27 10 596 1000"
            ),
            StoreAvailability(
                storeId = "clicks_003",
                storeName = "Clicks Rosebank Mall",
                address = "The Zone @ Rosebank",
                city = "Rosebank",
                province = "Gauteng",
                distanceKm = 3.1,
                stockLevel = StockLevel.LOW_STOCK,
                phoneNumber = "+27 11 447 4000"
            ),
            StoreAvailability(
                storeId = "clicks_004",
                storeName = "Clicks V&A Waterfront",
                address = "V&A Waterfront Shopping Centre",
                city = "Cape Town",
                province = "Western Cape",
                distanceKm = 1.2,
                stockLevel = StockLevel.IN_STOCK,
                phoneNumber = "+27 21 418 5000"
            ),
            StoreAvailability(
                storeId = "clicks_005",
                storeName = "Clicks Gateway Theatre",
                address = "Gateway Theatre of Shopping",
                city = "Umhlanga",
                province = "KwaZulu-Natal",
                distanceKm = 0.8,
                stockLevel = StockLevel.IN_STOCK,
                phoneNumber = "+27 31 566 4000"
            )
        )

        return Result.success(
            mockStores
                .filter { it.distanceKm != null && it.distanceKm <= radiusKm }
                .sortedBy { it.distanceKm }
        )
    }

    /**
     * Get price history for a product
     *
     * @param productId The product identifier
     * @param daysBack Number of days of history
     * @return List of price history entries
     */
    suspend fun getPriceHistory(
        productId: String,
        daysBack: Int = 30
    ): Result<List<PriceHistory>> {
        delay(MOCK_DELAY_MS)

        val basePrice = generateMockPrice(productId)
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        // Generate mock price history
        val history = (0..daysBack).map { daysAgo ->
            val variation = (Math.random() * 20 - 10) // +/- 10 Rand variation
            PriceHistory(
                date = now - (daysAgo * dayMs),
                price = (basePrice + variation).coerceAtLeast(basePrice * 0.8)
            )
        }.reversed()

        return Result.success(history)
    }

    /**
     * Check if product is on special offer
     *
     * @param productId The product identifier
     * @return Current offer details if any
     */
    suspend fun checkSpecialOffer(productId: String): Result<SpecialOffer?> {
        delay(MOCK_DELAY_MS / 2)

        // 30% chance of having a special offer
        return if (Math.random() < 0.3) {
            val basePrice = generateMockPrice(productId)
            val discount = listOf(10, 15, 20, 25).random()
            Result.success(
                SpecialOffer(
                    productId = productId,
                    discountPercent = discount,
                    originalPrice = basePrice,
                    salePrice = basePrice * (1 - discount / 100.0),
                    validUntil = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L),
                    offerType = OfferType.PERCENTAGE_OFF
                )
            )
        } else {
            Result.success(null)
        }
    }

    /**
     * Generate product deep link URL
     *
     * @param product The product entity
     * @return Deep link URL to product on Clicks website
     */
    fun generateProductUrl(product: ProductEntity): String {
        val slug = product.name
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        return "https://clicks.co.za/p/${product.productId}/$slug"
    }

    /**
     * Generate add to cart URL (for future cart integration)
     *
     * @param productId The product identifier
     * @param quantity Quantity to add
     * @return URL that adds product to Clicks online cart
     */
    fun generateAddToCartUrl(productId: String, quantity: Int = 1): String {
        return "https://clicks.co.za/cart/add?sku=$productId&qty=$quantity"
    }

    /**
     * Special offer details
     */
    data class SpecialOffer(
        val productId: String,
        val discountPercent: Int,
        val originalPrice: Double,
        val salePrice: Double,
        val validUntil: Long,
        val offerType: OfferType
    )

    /**
     * Offer type enum
     */
    enum class OfferType {
        PERCENTAGE_OFF,
        BUY_ONE_GET_ONE,
        CLUBCARD_ONLY,
        BUNDLE_DEAL
    }

    /**
     * Generate a mock price based on product ID hash
     * This ensures consistent pricing for the same product
     */
    private fun generateMockPrice(productId: String): Double {
        val hash = productId.hashCode().toLong().and(0xFFFFFFFFL)
        val basePrice = 89.0 + (hash % 400) // R89 to R489
        return (basePrice * 100).toLong() / 100.0 // Round to 2 decimal places
    }
}
