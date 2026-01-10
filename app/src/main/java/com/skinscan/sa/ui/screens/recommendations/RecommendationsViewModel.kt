package com.skinscan.sa.ui.screens.recommendations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.domain.usecase.GetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

/**
 * ViewModel for Recommendations Screen (Story 3.4)
 *
 * Loads scan results and generates personalized product recommendations
 */
@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanResultDao: ScanResultDao,
    private val getRecommendationsUseCase: GetRecommendationsUseCase
) : ViewModel() {

    private val scanId: String = savedStateHandle.get<String>("scanId") ?: ""

    private val _uiState = MutableStateFlow<RecommendationsUiState>(RecommendationsUiState.Loading)
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _expandedProductId = MutableStateFlow<String?>(null)
    val expandedProductId: StateFlow<String?> = _expandedProductId.asStateFlow()

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            try {
                // Load scan result
                val scanResult = scanResultDao.getById(scanId)
                if (scanResult == null) {
                    _uiState.value = RecommendationsUiState.Error("Scan result not found")
                    return@launch
                }

                // Parse concerns from scan result
                val concerns = parseConcerns(scanResult.detectedConcerns)
                val fitzpatrickType = scanResult.fitzpatrickType ?: 5

                // Get recommendations
                val recommendations = getRecommendationsUseCase.execute(
                    concerns = concerns,
                    fitzpatrickType = fitzpatrickType,
                    maxPerCategory = 4
                )

                _uiState.value = RecommendationsUiState.Success(
                    recommendations = recommendations,
                    scanDate = scanResult.scannedAt,
                    concerns = concerns,
                    fitzpatrickType = fitzpatrickType
                )
            } catch (e: Exception) {
                _uiState.value = RecommendationsUiState.Error(e.message ?: "Failed to load recommendations")
            }
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun toggleProductExpansion(productId: String) {
        _expandedProductId.value = if (_expandedProductId.value == productId) null else productId
    }

    fun refresh() {
        _uiState.value = RecommendationsUiState.Loading
        loadRecommendations()
    }

    private fun parseConcerns(json: String): List<SkinConcern> {
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
 * UI State for Recommendations Screen
 */
sealed class RecommendationsUiState {
    data object Loading : RecommendationsUiState()
    data class Success(
        val recommendations: GetRecommendationsUseCase.RecommendationResult,
        val scanDate: java.util.Date,
        val concerns: List<SkinConcern>,
        val fitzpatrickType: Int
    ) : RecommendationsUiState()
    data class Error(val message: String) : RecommendationsUiState()
}
