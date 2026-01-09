package com.skinscan.sa.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.skinscan.sa.data.db.entity.ScanResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for ScanResult operations
 */
@Dao
interface ScanResultDao {
    @Insert
    suspend fun insert(scanResult: ScanResultEntity)

    @Query("SELECT * FROM scan_results WHERE userId = :userId ORDER BY scannedAt DESC")
    fun getAllByUser(userId: String): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE scanId = :scanId")
    suspend fun getById(scanId: String): ScanResultEntity?

    @Query("DELETE FROM scan_results WHERE scanId = :scanId")
    suspend fun deleteById(scanId: String)
}
