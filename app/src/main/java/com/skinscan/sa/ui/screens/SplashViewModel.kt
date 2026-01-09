package com.skinscan.sa.ui.screens

import androidx.lifecycle.ViewModel
import com.skinscan.sa.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel for Splash Screen
 * Checks if onboarding has been completed to determine navigation route
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val isOnboardingCompleted: Flow<Boolean> = userPreferencesRepository.isOnboardingCompleted
}
