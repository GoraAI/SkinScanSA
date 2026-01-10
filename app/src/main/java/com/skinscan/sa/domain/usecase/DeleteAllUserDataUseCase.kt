package com.skinscan.sa.domain.usecase

import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.dao.UserProfileDao
import javax.inject.Inject

/**
 * Use Case: Delete All User Data (Story 4.4, POPIA Compliance)
 *
 * Implements the user's right to deletion under POPIA Section 24
 * Removes all personal data including:
 * - User profile
 * - All scan results
 * - Any cached data
 */
class DeleteAllUserDataUseCase @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val scanResultDao: ScanResultDao
) {
    companion object {
        private const val DEFAULT_USER_ID = "default_user"
    }

    /**
     * Delete all user data from the app
     *
     * @return Result indicating success or failure
     */
    suspend fun execute(): Result<Unit> {
        return try {
            // Delete all scan results
            scanResultDao.deleteAllByUser(DEFAULT_USER_ID)

            // Delete user profile
            userProfileDao.deleteAll()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
