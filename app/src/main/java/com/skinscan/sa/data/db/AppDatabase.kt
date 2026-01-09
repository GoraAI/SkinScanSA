package com.skinscan.sa.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skinscan.sa.data.db.converters.DateConverters
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.dao.UserProfileDao
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.db.entity.UserProfileEntity

/**
 * Room Database for Glow Guide
 *
 * Encrypted with SQLCipher (AES-256-GCM) for POPIA compliance
 * Contains biometric data (facial scan results) requiring encryption
 */
@Database(
    entities = [
        ScanResultEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false // TODO: Enable schema export for production
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        const val DATABASE_NAME = "glowguide.db"
    }
}
