package com.example.fairr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.gdpr.GDPRComplianceService
import com.example.fairr.data.gdpr.GDPRResult
import com.example.fairr.data.gdpr.DataProcessingSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GDPRViewModel @Inject constructor(
    private val gdprComplianceService: GDPRComplianceService
) : ViewModel() {

    private val _dataProcessingSummary = MutableStateFlow<DataProcessingSummary?>(null)
    val dataProcessingSummary: StateFlow<DataProcessingSummary?> = _dataProcessingSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDataProcessingSummary()
    }

    /**
     * Load user's data processing summary for transparency
     */
    private fun loadDataProcessingSummary() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val summary = gdprComplianceService.getDataProcessingSummary()
                _dataProcessingSummary.value = summary
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data summary: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Export user data for GDPR data portability
     */
    suspend fun exportData(): Result<String> {
        return try {
            when (val result = gdprComplianceService.requestDataPortability()) {
                is GDPRResult.Success -> {
                    Result.success(result.message)
                }
                is GDPRResult.Error -> {
                    Result.failure(Exception(result.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete user account with progress feedback
     */
    suspend fun deleteAccount(provideFeedback: (String) -> Unit): Result<String> {
        return try {
            when (val result = gdprComplianceService.deleteUserAccount(provideFeedback)) {
                is GDPRResult.Success -> {
                    Result.success(result.message)
                }
                is GDPRResult.Error -> {
                    Result.failure(Exception(result.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Refresh data processing summary
     */
    fun refreshSummary() {
        loadDataProcessingSummary()
    }
} 