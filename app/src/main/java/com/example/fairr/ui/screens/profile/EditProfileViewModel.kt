package com.example.fairr.ui.screens.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.user.UserProfileService
import com.example.fairr.data.user.UserProfileUpdate
import com.example.fairr.data.user.UserProfileData
import com.example.fairr.data.user.ProfileUpdateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val hasChanges: Boolean = false,
    val isEmailVerified: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userProfileService: UserProfileService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    // Original values to track changes
    private var originalData: UserProfileData? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val profileData = userProfileService.getCurrentUserProfile()
                if (profileData != null) {
                    originalData = profileData
                    _uiState.value = _uiState.value.copy(
                        displayName = profileData.displayName ?: "",
                        email = profileData.email ?: "",
                        phoneNumber = profileData.phoneNumber ?: "",
                        photoUrl = profileData.photoUrl,
                        isEmailVerified = profileData.isEmailVerified,
                        isLoading = false,
                        hasChanges = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load profile data"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun updateDisplayName(name: String) {
        _uiState.value = _uiState.value.copy(
            displayName = name,
            hasChanges = hasAnyChanges(
                name,
                _uiState.value.email,
                _uiState.value.phoneNumber
            )
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            hasChanges = hasAnyChanges(
                _uiState.value.displayName,
                email,
                _uiState.value.phoneNumber
            )
        )
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phone,
            hasChanges = hasAnyChanges(
                _uiState.value.displayName,
                _uiState.value.email,
                phone
            )
        )
    }

    private fun hasAnyChanges(name: String, email: String, phone: String): Boolean {
        val original = originalData ?: return false
        return name != (original.displayName ?: "") ||
                email != (original.email ?: "") ||
                phone != (original.phoneNumber ?: "")
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, error = null)
            
            when (val result = userProfileService.uploadProfileImage(imageUri)) {
                is ProfileUpdateResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        photoUrl = imageUri.toString() // Temporary until we get the actual URL
                    )
                    snackbarMessage = "Profile image updated successfully"
                    // Refresh profile to get actual URL
                    loadUserProfile()
                }
                is ProfileUpdateResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        error = result.message
                    )
                    snackbarMessage = "Failed to upload image: ${result.message}"
                }
                is ProfileUpdateResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun saveProfile(): Boolean {
        val currentState = _uiState.value
        
        // Validate inputs
        val profileUpdate = UserProfileUpdate(
            displayName = currentState.displayName.trim().takeIf { it.isNotBlank() },
            phoneNumber = currentState.phoneNumber.trim().takeIf { it.isNotBlank() },
            // Email updates require special handling and re-authentication
        )
        
        val validationError = userProfileService.validateProfileUpdate(profileUpdate)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(error = validationError)
            snackbarMessage = validationError
            return false
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                when (val result = userProfileService.updateUserProfile(profileUpdate)) {
                    is ProfileUpdateResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            hasChanges = false,
                            successMessage = "Profile updated successfully"
                        )
                        snackbarMessage = "Profile updated successfully"
                        
                        // Update original data to reflect the changes
                        originalData = originalData?.copy(
                            displayName = currentState.displayName.trim(),
                            phoneNumber = currentState.phoneNumber.trim().takeIf { it.isNotBlank() }
                        )
                    }
                    is ProfileUpdateResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = result.message
                        )
                        snackbarMessage = "Failed to update profile: ${result.message}"
                    }
                    is ProfileUpdateResult.Loading -> {
                        // Already handled above
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save profile"
                )
                snackbarMessage = "Failed to save profile: ${e.message}"
            }
        }
        
        return true
    }

    fun updateEmailAddress(newEmail: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = userProfileService.updateEmail(newEmail)) {
                is ProfileUpdateResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        email = newEmail,
                        hasChanges = false
                    )
                    snackbarMessage = "Email updated successfully. Please verify your new email address."
                    loadUserProfile() // Refresh to get updated verification status
                }
                is ProfileUpdateResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    snackbarMessage = "Failed to update email: ${result.message}"
                }
                is ProfileUpdateResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearSnackbarMessage() {
        snackbarMessage = null
    }

    fun retry() {
        loadUserProfile()
    }

    fun resetChanges() {
        originalData?.let { original ->
            _uiState.value = _uiState.value.copy(
                displayName = original.displayName ?: "",
                email = original.email ?: "",
                phoneNumber = original.phoneNumber ?: "",
                hasChanges = false,
                error = null
            )
        }
    }
} 