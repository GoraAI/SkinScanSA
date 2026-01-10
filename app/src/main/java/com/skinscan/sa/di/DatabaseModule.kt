package com.skinscan.sa.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skinscan.sa.data.db.AppDatabase
import com.skinscan.sa.data.db.dao.ConsentAuditLogDao
import com.skinscan.sa.data.db.dao.ProductDao
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.dao.UserProfileDao
import com.skinscan.sa.data.encryption.EncryptionManager
import com.skinscan.sa.data.local.ProductSeedData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 *
 * Provides encrypted Room database using SQLCipher (AES-256-GCM)
 *
 * Story 3.1: Added ProductDao and database seeding callback
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        encryptionManager: EncryptionManager
    ): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context, encryptionManager).also { INSTANCE = it }
        }
    }

    private fun buildDatabase(
        context: Context,
        encryptionManager: EncryptionManager
    ): AppDatabase {
        try {
            // Get database passphrase from EncryptionManager
            val passphrase = encryptionManager.getDatabasePassphrase()

            // Initialize SQLCipher
            System.loadLibrary("sqlcipher")

            // Create Room database with SQLCipher factory
            val factory = SupportOpenHelperFactory(passphrase)

            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration() // For MVP - replace with proper migrations later
                .addCallback(ProductSeedCallback())
                .build()
        } catch (e: Exception) {
            // Fail-secure: If encryption initialization fails, throw error
            // App will crash on startup showing "Security initialization failed"
            throw SecurityException("Database encryption initialization failed: ${e.message}", e)
        }
    }

    /**
     * Callback to seed product database on first creation
     * Story 3.1: Seed Product Database with MVP Catalog
     */
    private class ProductSeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed products on database creation
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedProducts(database.productDao())
                }
            }
        }

        private suspend fun seedProducts(productDao: ProductDao) {
            val existingCount = productDao.getProductCount()
            if (existingCount == 0) {
                val products = ProductSeedData.getAllProducts()
                productDao.insertAll(products)
            }
        }
    }

    @Provides
    @Singleton
    fun provideScanResultDao(database: AppDatabase): ScanResultDao {
        return database.scanResultDao()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideConsentAuditLogDao(database: AppDatabase): ConsentAuditLogDao {
        return database.consentAuditLogDao()
    }
}
