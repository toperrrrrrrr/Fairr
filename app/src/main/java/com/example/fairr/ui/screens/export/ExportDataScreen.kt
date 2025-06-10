package com.example.fairr.ui.screens.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataScreen(
    navController: NavController,
    groupId: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedFormat by remember { mutableStateOf("CSV") }
    var selectedDateRange by remember { mutableStateOf("All Time") }
    var includeSettlements by remember { mutableStateOf(true) }
    var includeMembers by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val formats = listOf("CSV", "Excel", "PDF")
    val dateRanges = listOf("Last 30 Days", "Last 3 Months", "Last 6 Months", "This Year", "All Time")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Export Data",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Export",
                            tint = DarkGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (groupId != null) "Export Group Data" else "Export All Data",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (groupId != null) {
                                "Download expenses and settlement data for this group"
                            } else {
                                "Download all your expenses, groups, and settlement data"
                            },
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Format Selection
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "File Format",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        formats.forEach { format ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = DarkGreen
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = format,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = when (format) {
                                            "CSV" -> "Comma-separated values, compatible with spreadsheet apps"
                                            "Excel" -> "Microsoft Excel spreadsheet format"
                                            "PDF" -> "Formatted report document"
                                            else -> ""
                                        },
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Date Range Selection
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Date Range",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        dateRanges.forEach { range ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDateRange == range,
                                    onClick = { selectedDateRange = range },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = DarkGreen
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = range,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Include Options
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Include in Export",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeSettlements,
                                onCheckedChange = { includeSettlements = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Settlement Records",
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Include payment and settlement history",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeMembers,
                                onCheckedChange = { includeMembers = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Member Information",
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Include group member details and contact info",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Export Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isExporting = true
                            // Simulate export process
                            delay(2000)
                            
                            // Create sample export data
                            val exportData = generateSampleExportData(selectedFormat, emptyList())
                            
                            // Share the data (in real app, this would save to file)
                            shareExportData(context, exportData, selectedFormat)
                            
                            showSuccessDialog = true
                            isExporting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = NeutralWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeutralWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exporting...")
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Export Data",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBlue.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = DarkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Privacy Notice",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkBlue
                            )
                            Text(
                                text = "Your data will be downloaded to your device. Fairr does not store or share your exported data.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Complete")
                }
            },
            text = {
                Text("Your data has been successfully exported and saved to your device's Downloads folder.")
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

// Helper function to generate sample export data
private fun generateSampleExportData(format: String, selectedGroups: List<String>): String {
    return when (format) {
        "CSV" -> {
            """
Date,Group,Description,Amount,Paid By,Split Between
2025-01-15,Roommates,Groceries,$85.50,John,"John, Alice, Bob"
2025-01-14,Weekend Trip,Gas,$45.00,Alice,"Alice, Bob"
2025-01-13,Dinner Group,Restaurant,$120.75,Bob,"John, Alice, Bob, Charlie"
2025-01-12,Roommates,Utilities,$150.00,John,"John, Alice, Bob"
2025-01-11,Weekend Trip,Hotel,$300.00,Alice,"Alice, Bob"
            """.trimIndent()
        }
        "JSON" -> {
            """
{
  "export_date": "2025-01-15",
  "groups": [
    {
      "name": "Roommates",
      "expenses": [
        {
          "date": "2025-01-15",
          "description": "Groceries",
          "amount": 85.50,
          "paid_by": "John",
          "split_between": ["John", "Alice", "Bob"]
        },
        {
          "date": "2025-01-12",
          "description": "Utilities",
          "amount": 150.00,
          "paid_by": "John",
          "split_between": ["John", "Alice", "Bob"]
        }
      ]
    },
    {
      "name": "Weekend Trip",
      "expenses": [
        {
          "date": "2025-01-14",
          "description": "Gas",
          "amount": 45.00,
          "paid_by": "Alice",
          "split_between": ["Alice", "Bob"]
        },
        {
          "date": "2025-01-11",
          "description": "Hotel",
          "amount": 300.00,
          "paid_by": "Alice",
          "split_between": ["Alice", "Bob"]
        }
      ]
    }
  ]
}
            """.trimIndent()
        }
        else -> "Export format not supported"
    }
}

// Helper function to share export data
private fun shareExportData(context: Context, data: String, format: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, data)
        putExtra(Intent.EXTRA_SUBJECT, "Fairr Export Data - $format")
    }
    context.startActivity(Intent.createChooser(intent, "Share Export Data"))
}

@Preview(showBackground = true)
@Composable
fun ExportDataScreenPreview() {
    FairrTheme {
        ExportDataScreen(
            navController = rememberNavController()
        )
    }
} 
