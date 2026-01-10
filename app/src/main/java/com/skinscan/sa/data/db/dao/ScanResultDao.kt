package com.skinscan.sa.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.skinscan.sa.data.db.entity.ScanResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for ScanResult operations
 *
 * Story 4.1: Extended for scan history features
 */
@Dao
interface ScanResultDao {
    @Insert
    suspend fun insert(scanResult: ScanResultEntity)

    @Update
    suspend fun update(scanResult: ScanResultEntity)

    @Query("SELECT * FROM scan_results WHERE userId = :userId ORDER BY scannedAt DESC")
    fun getAllByUser(userId: String): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE userId = :userId ORDER BY scannedAt DESC")
    suspend fun getAllByUserSync(userId: String): List<ScanResultEntity>

    @Query("SELECT * FROM scan_results WHERE scanId = :scanId")
    suspend fun getById(scanId: String): ScanResultEntity?

    @Query("DELETE FROM scan_results WHERE scanId = :scanId")
    suspend fun deleteById(scanId: String)

    // Story 4.1: Filtered queries
    @Query("SELECT * FROM scan_results WHERE userId = :userId AND isStarred = 1 ORDER BY scannedAt DESC")
    fun getStarredByUser(userId: String): Flow<List<ScanResultEntity>>

    @Query("""
        SELECT * FROM scan_results
        WHERE userId = :userId
        AND scannedAt >= :startDate
        ORDER BY scannedAt DESC
    """)
    fun getByUserSinceDate(userId: String, startDate: Long): Flow<List<ScanResultEntity>>

    @Query("SELECT COUNT(*) FROM scan_results WHERE userId = :userId")
    suspend fun getCountByUser(userId: String): Int

    @Query("UPDATE scan_results SET isStarred = :starred WHERE scanId = :scanId")
    suspend fun setStarred(scanId: String, starred: Boolean)

    // Story 4.2/4.3: Progress tracking queries
    @Query("""
        SELECT * FROM scan_results
        WHERE userId = :userId
        AND scannedAt >= :startDate
        ORDER BY scannedAt ASC
    """)
    suspend fun getByUserInDateRange(userId: String, startDate: Long): List<ScanResultEntity>

    @Query("DELETE FROM scan_results WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
