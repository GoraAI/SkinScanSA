package com.skinscan.sa.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinscan.sa.data.db.dao.ScanResultDao
import com.skinscan.sa.domain.usecase.CompareScanProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Progress Comparison Screen (Story 4.2)
 *
 * Compares two scans to calculate progress metrics
 */
@HiltViewModel
class ComparisonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanResultDao: ScanResultDao,
    private val compareScanProgressUseCase: CompareScanProgressUseCase
) : ViewModel() {

    private val baselineScanId: String = savedStateHandle.get<String>("baselineScanId") ?: ""
    private val currentScanId: String = savedStateHandle.get<String>("currentScanId") ?: ""

    private val _uiState = MutableStateFlow<ComparisonUiState>(ComparisonUiState.Loading)
    val uiState: StateFlow<ComparisonUiState> = _uiState.asStateFlow()

    init {
        loadComparison()
    }

    private fun loadComparison() {
        viewModelScope.launch {
            try {
                val baseline = scanResultDao.getById(baselineScanId)
                val current = scanResultDao.getById(currentScanId)

                if (baseline == null || current == null) {
                    _uiState.value = ComparisonUiState.Error("One or both scans not found")
                    return@launch
                }

                val comparison = compareScanProgressUseCase.execute(baseline, current)
                _uiState.value = ComparisonUiState.Success(comparison)
            } catch (e: Exception) {
                _uiState.value = ComparisonUiState.Error(e.message ?: "Failed to load comparison")
            }
        }
    }
}

/**
 * UI State for Comparison Screen
 */
sealed class ComparisonUiState {
    data object Loading : ComparisonUiState()
    data class Success(
        val comparison: CompareScanProgressUseCase.ComparisonResult
    ) : ComparisonUiState()
    data class Error(val message: String) : ComparisonUiState()
}
