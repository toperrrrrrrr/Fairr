package com.example.fairr.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.viewmodels.GDPRViewModel

@Composable
fun GDPRAccountDeletionDialog(
    onDismiss: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: GDPRViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var currentStep by remember { mutableStateOf(AccountDeletionStep.Warning) }
    var exportDataFirst by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = !isProcessing
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentStep) {
                    AccountDeletionStep.Warning -> {
                        WarningStep(
                            onNext = { currentStep = AccountDeletionStep.DataExport },
                            onCancel = onDismiss
                        )
                    }
                    AccountDeletionStep.DataExport -> {
                        DataExportStep(
                            exportDataFirst = exportDataFirst,
                            onExportToggle = { exportDataFirst = it },
                            onNext = { currentStep = AccountDeletionStep.FinalConfirmation },
                            onBack = { currentStep = AccountDeletionStep.Warning },
                            onExportData = {
                                coroutineScope.launch {
                                    isProcessing = true
                                    progressMessage = "Exporting your data..."
                                    
                                    val result = viewModel.exportData()
                                    if (result.isSuccess) {
                                        progressMessage = "Data exported successfully"
                                        currentStep = AccountDeletionStep.FinalConfirmation
                                    } else {
                                        showError = true
                                        errorMessage = "Failed to export data: ${result.exceptionOrNull()?.message}"
                                    }
                                    isProcessing = false
                                }
                            }
                        )
                    }
                    AccountDeletionStep.FinalConfirmation -> {
                        FinalConfirmationStep(
                            onConfirm = {
                                coroutineScope.launch {
                                    isProcessing = true
                                    
                                    val result = viewModel.deleteAccount { message ->
                                        progressMessage = message
                                    }
                                    
                                    if (result.isSuccess) {
                                        onAccountDeleted()
                                    } else {
                                        showError = true
                                        errorMessage = "Account deletion failed: ${result.exceptionOrNull()?.message}"
                                        isProcessing = false
                                    }
                                }
                            },
                            onBack = { currentStep = AccountDeletionStep.DataExport }
                        )
                    }
                    AccountDeletionStep.Processing -> {
                        ProcessingStep(
                            progressMessage = progressMessage
                        )
                    }
                }

                if (isProcessing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ProcessingIndicator(progressMessage)
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorDisplay(
                        message = errorMessage,
                        onDismiss = { 
                            showError = false
                            errorMessage = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningStep(
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Column {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Warning",
                tint = ErrorRed,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Delete Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Warning content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚠️ This action is permanent and cannot be undone",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "The following data will be permanently deleted:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                val itemsToDelete = listOf(
                    "Your user profile and account information",
                    "All expenses you created or participated in",
                    "All groups where you are the only member",
                    "Your settlement history and payment records",
                    "All notifications and activity logs",
                    "Friend connections and group invitations",
                    "Uploaded receipts and profile pictures",
                    "App preferences and settings"
                )
                
                itemsToDelete.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text("• ", color = TextSecondary, fontSize = 14.sp)
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GDPR Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlue.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Privacy",
                        tint = DarkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Privacy Rights",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkBlue
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Under GDPR, you have the right to request deletion of your personal data (\"Right to be Forgotten\"). We will permanently delete all your data within 30 days.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    contentColor = NeutralWhite
                )
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun DataExportStep(
    exportDataFirst: Boolean,
    onExportToggle: (Boolean) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onExportData: () -> Unit
) {
    Column {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = "Export",
                tint = DarkGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Export Your Data",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Before deleting your account, would you like to export your data?",
            fontSize = 16.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Data Export includes:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                val exportItems = listOf(
                    "All your expenses and group information",
                    "Settlement history and payment records",
                    "Group membership details",
                    "Activity logs and notifications",
                    "Account information and preferences"
                )
                
                exportItems.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text("• ", color = DarkGreen, fontSize = 14.sp)
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Export options
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = exportDataFirst,
                onCheckedChange = onExportToggle,
                colors = CheckboxDefaults.colors(checkedColor = DarkGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Export my data before deletion",
                fontSize = 16.sp,
                color = TextPrimary
            )
        }

        if (exportDataFirst) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onExportData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    contentColor = NeutralWhite
                )
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Data Now")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = NeutralWhite
                )
            ) {
                Text("Proceed to Delete")
            }
        }
    }
}

@Composable
private fun FinalConfirmationStep(
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.DeleteForever,
                contentDescription = "Delete",
                tint = ErrorRed,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Final Confirmation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Are you absolutely sure?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "This will permanently delete your Fairr account and all associated data. This action cannot be undone.",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "You will be immediately logged out and your account will be permanently deleted within 30 days as required by GDPR.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = NeutralWhite
                )
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account")
            }
        }
    }
}

@Composable
private fun ProcessingStep(
    progressMessage: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.CloudSync,
            contentDescription = "Processing",
            tint = DarkGreen,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Deleting Account",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = progressMessage,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = DarkGreen
        )
    }
}

@Composable
private fun ProcessingIndicator(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = DarkGreen,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ErrorDisplay(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Error",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ErrorRed
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Text(
                text = message,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private enum class AccountDeletionStep {
    Warning,
    DataExport,
    FinalConfirmation,
    Processing
}