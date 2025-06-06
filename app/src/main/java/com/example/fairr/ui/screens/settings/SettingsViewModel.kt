package com.example.fairr.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.settings.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    
    var selectedCurrency by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            settingsDataStore.defaultCurrency.collectLatest { currency ->
                selectedCurrency = currency
            }
        }
    }

    fun updateDefaultCurrency(currency: String) {
        viewModelScope.launch {
            settingsDataStore.setDefaultCurrency(currency)
        }
    }
} 