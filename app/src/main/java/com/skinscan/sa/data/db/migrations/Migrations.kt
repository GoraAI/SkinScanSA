package com.skinscan.sa.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database Migrations
 *
 * Proper migration strategy for production:
 * - Never use fallbackToDestructiveMigration() as it wipes user data
 * - Each schema change requires a new migration
 * - Test migrations thoroughly before release
 *
 * Current version: 5
 * Future migrations should be added here as MIGRATION_X_Y objects
 */
object Migrations {

    /**
     * List of all migrations to apply
     * Add new migrations here as they are created
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        // Future migrations will be added here
        // Example: MIGRATION_5_6, MIGRATION_6_7, etc.
    )

    // Example migration template for future use:
    // val MIGRATION_5_6 = object : Migration(5, 6) {
    //     override fun migrate(db: SupportSQLiteDatabase) {
    //         // Add new column example:
    //         // db.execSQL("ALTER TABLE scan_results ADD COLUMN new_field TEXT")
    //
    //         // Create new table example:
    //         // db.execSQL("""
    //         //     CREATE TABLE IF NOT EXISTS new_table (
    //         //         id TEXT PRIMARY KEY NOT NULL,
    //         //         ...
    //         //     )
    //         // """)
    //     }
    // }
}
