package com.skinscan.sa.domain.usecase

import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.dao.UserProfileDao
import com.skinscan.sa.data.session.UserSessionManager
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
    private val scanResultDao: ScanResultDao,
    private val userSessionManager: UserSessionManager
) {
    /**
     * Delete all user data from the app
     *
     * @return Result indicating success or failure
     */
    suspend fun execute(): Result<Unit> {
        return try {
            val userId = userSessionManager.userId

            // Delete all scan results
            scanResultDao.deleteAllByUser(userId)

            // Delete user profile
            userProfileDao.deleteAll()

            // Clear session to generate new userId on next access
            userSessionManager.clearSession()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
