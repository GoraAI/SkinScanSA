package com.skinscan.sa.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skinscan.sa.data.db.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Product operations
 *
 * Story 3.1: Seed Product Database
 */
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE productId = :productId")
    suspend fun getById(productId: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY rating DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY rating DESC")
    fun getByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE inStock = 1 ORDER BY rating DESC")
    fun getInStockProducts(): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products
        WHERE targetConcerns LIKE '%' || :concern || '%'
        AND inStock = 1
        ORDER BY rating DESC
    """)
    suspend fun getProductsForConcern(concern: String): List<ProductEntity>

    @Query("""
        SELECT * FROM products
        WHERE suitableFitzpatrickTypes LIKE '%' || :fitzpatrickType || '%'
        AND inStock = 1
        ORDER BY rating DESC
    """)
    suspend fun getProductsForFitzpatrickType(fitzpatrickType: Int): List<ProductEntity>

    @Query("""
        SELECT * FROM products
        WHERE isMelaninOptimized = 1
        AND inStock = 1
        ORDER BY rating DESC
    """)
    suspend fun getMelaninOptimizedProducts(): List<ProductEntity>

    @Query("""
        SELECT * FROM products
        WHERE retailers LIKE '%CLICKS%'
        AND inStock = 1
        ORDER BY rating DESC
    """)
    suspend fun getClicksProducts(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
