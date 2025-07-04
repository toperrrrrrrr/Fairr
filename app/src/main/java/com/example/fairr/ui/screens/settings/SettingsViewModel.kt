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

    var isDarkModeEnabled by mutableStateOf(false)
        private set

    var isNotificationsEnabled by mutableStateOf(true)
        private set

    var isSoundEnabled by mutableStateOf(true)
        private set

    init {
        viewModelScope.launch {
            // Collect currency changes
            settingsDataStore.defaultCurrency.collectLatest { currency ->
                selectedCurrency = currency
            }
        }

        viewModelScope.launch {
            // Collect dark mode changes
            settingsDataStore.darkModeEnabled.collectLatest { enabled ->
                isDarkModeEnabled = enabled
            }
        }

        viewModelScope.launch {
            // Collect notifications changes
            settingsDataStore.notificationsEnabled.collectLatest { enabled ->
                isNotificationsEnabled = enabled
            }
        }

        viewModelScope.launch {
            // Collect sound changes
            settingsDataStore.soundEnabled.collectLatest { enabled ->
                isSoundEnabled = enabled
            }
        }
    }

    fun updateDefaultCurrency(currency: String) {
        viewModelScope.launch {
            settingsDataStore.setDefaultCurrency(currency)
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkModeEnabled(enabled)
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotificationsEnabled(enabled)
        }
    }

    fun updateSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSoundEnabled(enabled)
        }
    }
} 