package com.skinscan.sa.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user session with device-based unique identifier
 *
 * Uses EncryptedSharedPreferences for secure storage of user ID.
 * Generates a unique UUID on first launch that persists across app updates.
 *
 * POPIA Compliance: User can clear session via clearSession() for data deletion.
 */
@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }

    companion object {
        private const val PREFS_NAME = "skinscan_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    /**
     * Returns the device-unique user ID.
     * Generates a new UUID on first access.
     */
    val userId: String
        get() {
            val existingId = sharedPreferences.getString(KEY_USER_ID, null)
            return existingId ?: generateAndStoreUserId()
        }

    /**
     * Returns true if this is the first app launch.
     * Once userId is accessed, this returns false.
     */
    val isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)

    /**
     * Clears the user session.
     * Called when user requests POPIA data deletion.
     * Next access to userId will generate a new UUID.
     */
    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_USER_ID)
            .putBoolean(KEY_FIRST_LAUNCH, true)
            .apply()
    }

    private fun generateAndStoreUserId(): String {
        val newUserId = UUID.randomUUID().toString()
        sharedPreferences.edit()
            .putString(KEY_USER_ID, newUserId)
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .apply()
        return newUserId
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
