package com.skinscan.sa.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.db.dao.UserProfileDao
import com.skinscan.sa.data.db.entity.UserProfileEntity
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.domain.usecase.DeleteAllUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for Profile Management Screen (Story 4.4)
 *
 * Manages user profile data, scan statistics, and POPIA data deletion
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val scanResultDao: ScanResultDao,
    private val deleteAllUserDataUseCase: DeleteAllUserDataUseCase
) : ViewModel() {

    companion object {
        private const val DEFAULT_USER_ID = "default_user"
    }

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _deleteConfirmation = MutableStateFlow(false)
    val deleteConfirmation: StateFlow<Boolean> = _deleteConfirmation.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading

                val profile = userProfileDao.getProfileOnce() ?: createDefaultProfile()
                val scanCount = scanResultDao.getCountByUser(DEFAULT_USER_ID)
                val scans = scanResultDao.getAllByUserSync(DEFAULT_USER_ID)

                // Calculate most common concern
                val concernCounts = mutableMapOf<SkinConcern, Int>()
                scans.forEach { scan ->
                    parseConcerns(scan.detectedConcerns).forEach { concern ->
                        concernCounts[concern] = (concernCounts[concern] ?: 0) + 1
                    }
                }
                val mostCommonConcern = concernCounts.maxByOrNull { it.value }?.key

                // Calculate scan frequency
                val scanFrequency = if (scans.size >= 2) {
                    val firstScan = scans.minByOrNull { it.scannedAt.time }?.scannedAt
                    val lastScan = scans.maxByOrNull { it.scannedAt.time }?.scannedAt
                    if (firstScan != null && lastScan != null) {
                        val daysBetween = ((lastScan.time - firstScan.time) / (24 * 60 * 60 * 1000)).toInt()
                        if (daysBetween > 0) {
                            "~${(daysBetween / scans.size)} days"
                        } else "N/A"
                    } else "N/A"
                } else "N/A"

                // Days since last scan
                val daysSinceLastScan = scans.maxByOrNull { it.scannedAt.time }?.let { lastScan ->
                    ((System.currentTimeMillis() - lastScan.scannedAt.time) / (24 * 60 * 60 * 1000)).toInt()
                }

                _uiState.value = ProfileUiState.Success(
                    profile = profile,
                    currentConcerns = parseConcerns(profile.knownConcerns ?: "[]"),
                    budgetRange = BudgetRange.fromString(profile.budgetRange),
                    location = profile.location,
                    allergies = profile.knownAllergies,
                    scanStats = ScanStatistics(
                        totalScans = scanCount,
                        mostCommonConcern = mostCommonConcern,
                        concernOccurrences = concernCounts[mostCommonConcern] ?: 0,
                        scanFrequency = scanFrequency,
                        daysSinceLastScan = daysSinceLastScan
                    ),
                    popiaConsentDate = profile.popiaConsentDate
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateConcerns(concerns: List<SkinConcern>) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                val jsonArray = JSONArray(concerns.map { it.name })
                val updatedProfile = currentState.profile.copy(
                    knownConcerns = jsonArray.toString(),
                    updatedAt = Date()
                )
                userProfileDao.update(updatedProfile)
                loadProfile()
            }
        }
    }

    fun updateBudget(budget: BudgetRange) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                val updatedProfile = currentState.profile.copy(
                    budgetRange = budget.name,
                    updatedAt = Date()
                )
                userProfileDao.update(updatedProfile)
                loadProfile()
            }
        }
    }

    fun updateLocation(location: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                val updatedProfile = currentState.profile.copy(
                    location = location.ifBlank { null },
                    updatedAt = Date()
                )
                userProfileDao.update(updatedProfile)
                loadProfile()
            }
        }
    }

    fun updateAllergies(allergies: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                val updatedProfile = currentState.profile.copy(
                    knownAllergies = allergies.ifBlank { null },
                    updatedAt = Date()
                )
                userProfileDao.update(updatedProfile)
                loadProfile()
            }
        }
    }

    fun showDeleteConfirmation() {
        _deleteConfirmation.value = true
    }

    fun hideDeleteConfirmation() {
        _deleteConfirmation.value = false
    }

    fun deleteAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            deleteAllUserDataUseCase.execute()
            _deleteConfirmation.value = false
            onComplete()
        }
    }

    private suspend fun createDefaultProfile(): UserProfileEntity {
        val profile = UserProfileEntity(
            userId = DEFAULT_USER_ID,
            popiaConsentGiven = true,
            popiaConsentDate = Date()
        )
        userProfileDao.insert(profile)
        return profile
    }

    private fun parseConcerns(json: String?): List<SkinConcern> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val name = array.getString(i)
                SkinConcern.entries.find { it.name == name }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Budget range options
 */
enum class BudgetRange(val displayName: String, val priceRange: String) {
    LOW("Budget", "Under R200"),
    MEDIUM("Mid-range", "R200-500"),
    HIGH("Premium", "R500+");

    companion object {
        fun fromString(value: String): BudgetRange {
            return entries.find { it.name == value } ?: MEDIUM
        }
    }
}

/**
 * Scan statistics
 */
data class ScanStatistics(
    val totalScans: Int,
    val mostCommonConcern: SkinConcern?,
    val concernOccurrences: Int,
    val scanFrequency: String,
    val daysSinceLastScan: Int?
)

/**
 * UI State for Profile Screen
 */
sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(
        val profile: UserProfileEntity,
        val currentConcerns: List<SkinConcern>,
        val budgetRange: BudgetRange,
        val location: String?,
        val allergies: String?,
        val scanStats: ScanStatistics,
        val popiaConsentDate: Date?
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
