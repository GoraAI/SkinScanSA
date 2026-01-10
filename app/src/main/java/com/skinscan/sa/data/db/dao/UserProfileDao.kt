package com.skinscan.sa.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skinscan.sa.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for UserProfile operations
 */
@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    // Story 4.4: Delete all user data (POPIA right to deletion)
    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}
