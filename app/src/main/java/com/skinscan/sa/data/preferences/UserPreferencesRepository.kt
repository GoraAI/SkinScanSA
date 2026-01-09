package com.skinscan.sa.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository for managing user preferences using Proto DataStore
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val POPIA_CONSENT_GIVEN = booleanPreferencesKey("popia_consent_given")
        val ANALYTICS_CONSENT_GIVEN = booleanPreferencesKey("analytics_consent_given")
    }

    /**
     * Flow that emits true if onboarding has been completed
     */
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    /**
     * Mark onboarding as completed
     */
    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    /**
     * Save POPIA consent status
     */
    suspend fun setPOPIAConsent(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[POPIA_CONSENT_GIVEN] = granted
        }
    }

    /**
     * Save analytics consent status
     */
    suspend fun setAnalyticsConsent(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANALYTICS_CONSENT_GIVEN] = granted
        }
    }
}
