package com.example.fairr.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    fun onGetStartedClick(onCompleted: () -> Unit) {
        viewModelScope.launch {
            try {
                userPreferencesManager.setOnboardingCompleted(true)
                onCompleted()
            } catch (e: Exception) {
                // Handle error - still call onCompleted to prevent stuck state
                onCompleted()
            }
        }
    }
}
