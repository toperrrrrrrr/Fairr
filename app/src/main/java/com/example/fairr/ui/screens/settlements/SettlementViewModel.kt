package com.example.fairr.ui.screens.settlements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.settlements.DebtInfo
import com.example.fairr.data.settlements.SettlementService
import com.example.fairr.data.settlements.SettlementSummary
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettlementEvent {
    data class ShowError(val message: String) : SettlementEvent()
    data object SettlementRecorded : SettlementEvent()
}

data class SettlementUiState(
    val isLoading: Boolean = false,
    val debts: List<DebtInfo> = emptyList(),
    val settlementSummary: List<SettlementSummary> = emptyList(),
    val currentUserDebts: List<UserDebt> = emptyList(),
    val error: String? = null
)

data class UserDebt(
    val personId: String,
    val personName: String,
    val amount: Double,
    val type: DebtType
)

enum class DebtType {
    YOU_OWE,    // Current user owes this person
    OWES_YOU    // This person owes current user
}

@HiltViewModel
class SettlementViewModel @Inject constructor(
    private val settlementService: SettlementService,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    var state by mutableStateOf(SettlementUiState())
        private set
        
    private val _events = MutableSharedFlow<SettlementEvent>()
    val events = _events.asSharedFlow()
    
    fun loadSettlements(groupId: String) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, error = null)
                
                // Get all debts for the group
                val allDebts = settlementService.calculateGroupSettlements(groupId)
                val settlementSummary = settlementService.getSettlementSummary(groupId)
                
                // Filter debts relevant to current user
                val currentUserId = getCurrentUserId()
                val currentUserDebts = allDebts.mapNotNull { debt ->
                    when {
                        debt.debtorId == currentUserId -> {
                            // Current user owes money to creditor
                            UserDebt(
                                personId = debt.creditorId,
                                personName = debt.creditorName,
                                amount = debt.amount,
                                type = DebtType.YOU_OWE
                            )
                        }
                        debt.creditorId == currentUserId -> {
                            // Debtor owes money to current user
                            UserDebt(
                                personId = debt.debtorId,
                                personName = debt.debtorName,
                                amount = debt.amount,
                                type = DebtType.OWES_YOU
                            )
                        }
                        else -> null
                    }
                }
                
                state = state.copy(
                    isLoading = false,
                    debts = allDebts,
                    settlementSummary = settlementSummary,
                    currentUserDebts = currentUserDebts
                )
                
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load settlements"
                )
                _events.emit(SettlementEvent.ShowError(e.message ?: "Failed to load settlements"))
            }
        }
    }
    
    fun recordSettlement(
        groupId: String,
        debtInfo: UserDebt,
        amount: Double,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                
                val currentUserId = getCurrentUserId()

                // Determine who pays whom based on debt type
                val (payerId, payeeId) = when (debtInfo.type) {
                    DebtType.YOU_OWE -> currentUserId to debtInfo.personId
                    DebtType.OWES_YOU -> debtInfo.personId to currentUserId
                }

                settlementService.recordSettlement(
                    groupId = groupId,
                    payerId = payerId,
                    payeeId = payeeId,
                    amount = amount,
                    paymentMethod = paymentMethod
                )

                // Refresh settlements after recording
                loadSettlements(groupId)
                
                _events.emit(SettlementEvent.SettlementRecorded)
                
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _events.emit(SettlementEvent.ShowError(e.message ?: "Failed to record settlement"))
            }
        }
    }
    
    fun getTotalAmountOwed(): Double {
        return state.currentUserDebts
            .filter { it.type == DebtType.YOU_OWE }
            .sumOf { it.amount }
    }
    
    fun getTotalAmountOwedToYou(): Double {
        return state.currentUserDebts
            .filter { it.type == DebtType.OWES_YOU }
            .sumOf { it.amount }
    }
    
    fun getNetBalance(): Double {
        return getTotalAmountOwedToYou() - getTotalAmountOwed()
    }
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
} 