package com.skinscan.sa.di

import android.content.Context
import androidx.room.Room
import com.skinscan.sa.data.db.AppDatabase
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.dao.UserProfileDao
import com.skinscan.sa.data.encryption.EncryptionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 *
 * Provides encrypted Room database using SQLCipher (AES-256-GCM)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        encryptionManager: EncryptionManager
    ): AppDatabase {
        try {
            // Get database passphrase from EncryptionManager
            val passphrase = encryptionManager.getDatabasePassphrase()

            // Initialize SQLCipher
            System.loadLibrary("sqlcipher")

            // Create Room database with SQLCipher factory
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .openHelperFactory(SupportFactory(passphrase))
                .fallbackToDestructiveMigration() // For MVP - replace with proper migrations later
                .build()
        } catch (e: Exception) {
            // Fail-secure: If encryption initialization fails, throw error
            // App will crash on startup showing "Security initialization failed"
            throw SecurityException("Database encryption initialization failed: ${e.message}", e)
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
}
