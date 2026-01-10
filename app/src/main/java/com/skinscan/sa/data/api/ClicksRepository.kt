package com.skinscan.sa.data.api

import com.skinscan.sa.data.db.dao.ProductDao
import com.skinscan.sa.data.db.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Clicks product data (Story 3.6)
 *
 * Coordinates between local database and Clicks API:
 * - Uses local product database as source of truth
 * - Enriches with real-time availability from API
 * - Handles caching and offline support
 */
@Singleton
class ClicksRepository @Inject constructor(
    private val productDao: ProductDao,
    private val clicksApi: ClicksApiService
) {

    /**
     * Product with live availability data
     */
    data class EnrichedProduct(
        val product: ProductEntity,
        val availability: ClicksApiService.ProductAvailability?,
        val specialOffer: ClicksApiService.SpecialOffer?,
        val productUrl: String
    )

    /**
     * Get product with live availability data
     *
     * @param productId The product identifier
     * @return Flow emitting enriched product data
     */
    fun getProductWithAvailability(productId: String): Flow<EnrichedProduct?> = flow {
        // First emit local data
        val product = productDao.getById(productId)
        if (product == null) {
            emit(null)
            return@flow
        }

        // Emit with basic data first
        emit(
            EnrichedProduct(
                product = product,
                availability = null,
                specialOffer = null,
                productUrl = clicksApi.generateProductUrl(product)
            )
        )

        // Then fetch and emit with live availability
        val availability = clicksApi.checkAvailability(productId).getOrNull()
        val specialOffer = clicksApi.checkSpecialOffer(productId).getOrNull()

        emit(
            EnrichedProduct(
                product = product,
                availability = availability,
                specialOffer = specialOffer,
                productUrl = clicksApi.generateProductUrl(product)
            )
        )
    }

    /**
     * Get all products for a category with availability
     *
     * @param category Product category
     * @return List of enriched products
     */
    suspend fun getProductsByCategory(category: String): List<EnrichedProduct> {
        val products = productDao.getByCategory(category)
        return products.map { product ->
            EnrichedProduct(
                product = product,
                availability = clicksApi.checkAvailability(product.productId).getOrNull(),
                specialOffer = null, // Skip special offers for list views
                productUrl = clicksApi.generateProductUrl(product)
            )
        }
    }

    /**
     * Get products on special offer
     *
     * @return List of products currently on sale
     */
    suspend fun getProductsOnOffer(): List<EnrichedProduct> {
        val allProducts = productDao.getClicksProducts()
        return allProducts.mapNotNull { product ->
            val offer = clicksApi.checkSpecialOffer(product.productId).getOrNull()
            if (offer != null) {
                EnrichedProduct(
                    product = product,
                    availability = clicksApi.checkAvailability(product.productId).getOrNull(),
                    specialOffer = offer,
                    productUrl = clicksApi.generateProductUrl(product)
                )
            } else null
        }
    }

    /**
     * Find nearby stores with product in stock
     *
     * @param productId The product identifier
     * @param latitude User latitude
     * @param longitude User longitude
     * @return List of nearby stores
     */
    suspend fun findNearbyStores(
        productId: String,
        latitude: Double,
        longitude: Double
    ): List<ClicksApiService.StoreAvailability> {
        return clicksApi.getNearbyStores(
            productId = productId,
            latitude = latitude,
            longitude = longitude,
            radiusKm = 15.0
        ).getOrDefault(emptyList())
    }

    /**
     * Get product purchase URL
     *
     * @param product The product to purchase
     * @return URL to buy product on Clicks website
     */
    fun getProductUrl(product: ProductEntity): String {
        return clicksApi.generateProductUrl(product)
    }

    /**
     * Get add to cart URL
     *
     * @param productId The product identifier
     * @return URL to add product to cart
     */
    fun getAddToCartUrl(productId: String): String {
        return clicksApi.generateAddToCartUrl(productId)
    }

    /**
     * Search products by query
     *
     * @param query Search query
     * @return List of matching products
     */
    suspend fun searchProducts(query: String): List<ProductEntity> {
        return productDao.searchProducts(query)
    }

    /**
     * Get price history for analytics
     *
     * @param productId The product identifier
     * @return List of historical prices
     */
    suspend fun getPriceHistory(productId: String): List<ClicksApiService.PriceHistory> {
        return clicksApi.getPriceHistory(productId, daysBack = 30).getOrDefault(emptyList())
    }
}
