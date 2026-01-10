package com.skinscan.sa.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skinscan.sa.data.db.converters.DateConverters
import com.skinscan.sa.data.db.dao.ConsentAuditLogDao
import com.skinscan.sa.data.db.dao.ProductDao
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.dao.UserProfileDao
import com.skinscan.sa.data.db.entity.ConsentAuditLogEntity
import com.skinscan.sa.data.db.entity.ProductEntity
import com.skinscan.sa.data.db.entity.ScanResultEntity
import com.skinscan.sa.data.db.entity.UserProfileEntity

/**
 * Room Database for Glow Guide
 *
 * Encrypted with SQLCipher (AES-256-GCM) for POPIA compliance
 * Contains biometric data (facial scan results) requiring encryption
 *
 * Story 3.1: Added ProductEntity for product catalog
 * Story 4.1: Added isStarred and healthScore fields to ScanResultEntity
 * Story 4.4: Added profile fields to UserProfileEntity
 * Story 6.4: Added ConsentAuditLogEntity for POPIA consent audit trail
 */
@Database(
    entities = [
        ScanResultEntity::class,
        UserProfileEntity::class,
        ProductEntity::class,
        ConsentAuditLogEntity::class
    ],
    version = 5,
    exportSchema = false // TODO: Enable schema export for production
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun productDao(): ProductDao
    abstract fun consentAuditLogDao(): ConsentAuditLogDao

    companion object {
        const val DATABASE_NAME = "glowguide.db"
    }
}
