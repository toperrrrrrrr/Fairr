package com.example.fairr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class UserState(
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val joinDate: String? = null,
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0,
    val isEmailVerified: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _userState = MutableStateFlow(UserState(isLoading = true))
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                _userState.value = _userState.value.copy(isLoading = true, error = null)
                
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _userState.value = _userState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }

                // Get basic user info from Firebase Auth
                val basicInfo = UserState(
                    displayName = currentUser.displayName ?: currentUser.email?.substringBefore("@"),
                    email = currentUser.email,
                    photoUrl = currentUser.photoUrl?.toString(),
                    isEmailVerified = currentUser.isEmailVerified
                )

                // Get additional user data from Firestore
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val phoneNumber = userDoc.getString("phoneNumber")
                val createdAt = userDoc.getLong("createdAt")
                val joinDate = createdAt?.let { timestamp ->
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
                } ?: "Recently"

                // Get user statistics
                val statistics = getUserStatistics(currentUser.uid)

                _userState.value = basicInfo.copy(
                    phoneNumber = phoneNumber,
                    joinDate = joinDate,
                    totalGroups = statistics.totalGroups,
                    totalExpenses = statistics.totalExpenses,
                    isLoading = false
                )

            } catch (e: Exception) {
                _userState.value = _userState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

    private suspend fun getUserStatistics(userId: String): UserStatistics {
        return try {
            // Get user's groups - query groups where user is a member
            val groupsSnapshot = firestore.collection("groups")
                .get()
                .await()
            
            val userGroups = groupsSnapshot.documents.filter { doc ->
                val members = doc.get("members") as? List<*>
                members?.any { member ->
                    when (member) {
                        is Map<*, *> -> member["userId"] == userId || member["id"] == userId
                        is String -> member == userId
                        else -> false
                    }
                } == true
            }

            // Get user's expenses
            val expensesSnapshot = firestore.collection("expenses")
                .whereEqualTo("paidBy", userId)
                .get()
                .await()

            UserStatistics(
                totalGroups = userGroups.size,
                totalExpenses = expensesSnapshot.size()
            )
        } catch (e: Exception) {
            UserStatistics(totalGroups = 0, totalExpenses = 0)
        }
    }

    fun refreshUserData() {
        loadUserData()
    }
}

data class UserStatistics(
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0
) 