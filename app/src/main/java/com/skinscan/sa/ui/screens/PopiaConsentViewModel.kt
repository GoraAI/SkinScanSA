package com.skinscan.sa.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for POPIA Consent Screen (Story 1.4)
 *
 * Manages consent state and persists to encrypted DataStore
 */
@HiltViewModel
class PopiaConsentViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _biometricConsentChecked = MutableStateFlow(false)
    val biometricConsentChecked: StateFlow<Boolean> = _biometricConsentChecked.asStateFlow()

    private val _analyticsConsentChecked = MutableStateFlow(false)
    val analyticsConsentChecked: StateFlow<Boolean> = _analyticsConsentChecked.asStateFlow()

    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> = _isButtonEnabled.asStateFlow()

    fun setBiometricConsent(checked: Boolean) {
        _biometricConsentChecked.value = checked
        updateButtonState()
    }

    fun setAnalyticsConsent(checked: Boolean) {
        _analyticsConsentChecked.value = checked
    }

    private fun updateButtonState() {
        // Button enabled ONLY when required biometric consent is given
        _isButtonEnabled.value = _biometricConsentChecked.value
    }

    fun saveConsent() {
        viewModelScope.launch {
            // Save POPIA consent
            userPreferencesRepository.setPOPIAConsent(_biometricConsentChecked.value)

            // Save analytics consent (optional)
            userPreferencesRepository.setAnalyticsConsent(_analyticsConsentChecked.value)

            // Mark onboarding as completed
            userPreferencesRepository.setOnboardingCompleted()
        }
    }
}
