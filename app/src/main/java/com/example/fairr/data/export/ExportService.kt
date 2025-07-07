package com.example.fairr.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.Group
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.settlements.SettlementService
import com.example.fairr.data.settlements.SettlementSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class ExportOptions(
    val format: ExportFormat = ExportFormat.CSV,
    val includeSettlements: Boolean = true,
    val dateRange: String = "All Time",
    val groupId: String? = null
)

enum class ExportFormat {
    CSV, EXCEL, PDF
}

data class ExportResult(
    val success: Boolean,
    val fileUri: Uri? = null,
    val errorMessage: String? = null
)

@Singleton
class ExportService @Inject constructor(
    private val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val groupService: GroupService,
    private val settlementService: SettlementService,
    private val auth: FirebaseAuth
) {
    
    suspend fun exportData(options: ExportOptions): ExportResult {
        return try {
            // Verify user is authenticated before proceeding with export
            if (auth.currentUser?.uid == null) {
                return ExportResult(false, errorMessage = "User not authenticated")
            }
            
            val data = when (options.groupId) {
                null -> exportAllData(options)
                else -> exportGroupData(options.groupId, options)
            }
            
            val file = when (options.format) {
                ExportFormat.CSV -> createCSVFile(data, options)
                ExportFormat.EXCEL -> createExcelFile(data, options)
                ExportFormat.PDF -> createPDFFile(data, options)
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            ExportResult(true, fileUri = uri)
        } catch (e: Exception) {
            ExportResult(false, errorMessage = e.message ?: "Export failed")
        }
    }
    
    private suspend fun exportAllData(options: ExportOptions): ExportData {
        val groups = groupService.getUserGroups().first()
        val allExpenses = mutableListOf<Expense>()
        val settlements = mutableListOf<SettlementSummary>()
        
        groups.forEach { group ->
            val expenses = expenseRepository.getExpensesByGroupId(group.id)
            allExpenses.addAll(expenses)
            
            if (options.includeSettlements) {
                val groupSettlements = settlementService.getSettlementSummary(group.id)
                settlements.addAll(groupSettlements)
            }
        }
        
        return ExportData(
            groups = groups,
            expenses = filterExpensesByDateRange(allExpenses, options.dateRange),
            settlements = settlements,
            exportDate = Date()
        )
    }
    
    private suspend fun exportGroupData(groupId: String, options: ExportOptions): ExportData {
        val group = groupService.getGroupById(groupId).first()
        val expenses = expenseRepository.getExpensesByGroupId(groupId)
        val settlements = if (options.includeSettlements) {
            settlementService.getSettlementSummary(groupId)
        } else {
            emptyList()
        }
        
        return ExportData(
            groups = listOf(group),
            expenses = filterExpensesByDateRange(expenses, options.dateRange),
            settlements = settlements,
            exportDate = Date()
        )
    }
    
    private fun filterExpensesByDateRange(expenses: List<Expense>, dateRange: String): List<Expense> {
        val calendar = Calendar.getInstance()
        
        val startDate = when (dateRange) {
            "Last 30 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.time
            }
            "Last 3 Months" -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.time
            }
            "Last 6 Months" -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.time
            }
            "This Year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.time
            }
            else -> Date(0) // All Time
        }
        
        return expenses.filter { expense ->
            val expenseDate = expense.date.toDate()
            expenseDate.after(startDate) || expenseDate.equals(startDate)
        }
    }
    
    private fun createCSVFile(data: ExportData, options: ExportOptions): File {
        val fileName = "fairr_export_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            // Write header
            writer.append("Date,Group,Description,Amount,Currency,Paid By,Split Between,Category,Notes\n")
            
            // Write expense data
            data.expenses.forEach { expense ->
                val group = data.groups.find { it.id == expense.groupId }
                val splitNames = expense.splitBetween.joinToString(";") { it.userName }
                
                writer.append("${formatDate(expense.date.toDate())},")
                writer.append("${group?.name ?: "Unknown"},")
                writer.append("\"${expense.description}\",")
                writer.append("${expense.amount},")
                writer.append("${expense.currency},")
                writer.append("${expense.paidByName},")
                writer.append("\"$splitNames\",")
                writer.append("${expense.category.name},")
                writer.append("\"${expense.notes}\"\n")
            }
            
            // Write settlement data if included
            if (options.includeSettlements && data.settlements.isNotEmpty()) {
                writer.append("\nSettlements\n")
                writer.append("User,Total Owed,Total Owed To Them,Net Balance\n")
                
                data.settlements.forEach { settlement ->
                    writer.append("${settlement.userName},")
                    writer.append("${settlement.totalOwed},")
                    writer.append("${settlement.totalOwedToThem},")
                    writer.append("${settlement.netBalance}\n")
                }
            }
        }
        
        return file
    }
    
    private fun createExcelFile(data: ExportData, options: ExportOptions): File {
        // For now, create a CSV file with .xlsx extension
        // In a real implementation, you would use a library like Apache POI
        val fileName = "fairr_export_${System.currentTimeMillis()}.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        // Create a simple CSV format for now
        FileWriter(file).use { writer ->
            writer.append("Fairr Export Report\n")
            writer.append("Generated on: ${formatDate(data.exportDate)}\n\n")
            
            // Write expense data
            writer.append("Expenses\n")
            writer.append("Date,Group,Description,Amount,Currency,Paid By,Split Between,Category,Notes\n")
            
            data.expenses.forEach { expense ->
                val group = data.groups.find { it.id == expense.groupId }
                val splitNames = expense.splitBetween.joinToString(";") { it.userName }
                
                writer.append("${formatDate(expense.date.toDate())},")
                writer.append("${group?.name ?: "Unknown"},")
                writer.append("\"${expense.description}\",")
                writer.append("${expense.amount},")
                writer.append("${expense.currency},")
                writer.append("${expense.paidByName},")
                writer.append("\"$splitNames\",")
                writer.append("${expense.category.name},")
                writer.append("\"${expense.notes}\"\n")
            }
        }
        
        return file
    }
    
    private fun createPDFFile(data: ExportData, options: ExportOptions): File {
        // For now, create a text file with .pdf extension
        // In a real implementation, you would use a library like iText or PDFBox
        val fileName = "fairr_export_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            writer.append("FAIRR EXPORT REPORT\n")
            writer.append("Generated on: ${formatDate(data.exportDate)}\n")
            writer.append("Total Groups: ${data.groups.size}\n")
            writer.append("Total Expenses: ${data.expenses.size}\n")
            writer.append("Total Amount: ${data.expenses.sumOf { it.amount }}\n\n")
            
            // Write expense data
            writer.append("EXPENSES\n")
            writer.append("=".repeat(50) + "\n")
            
            data.expenses.forEach { expense ->
                val group = data.groups.find { it.id == expense.groupId }
                writer.append("Date: ${formatDate(expense.date.toDate())}\n")
                writer.append("Group: ${group?.name ?: "Unknown"}\n")
                writer.append("Description: ${expense.description}\n")
                writer.append("Amount: ${expense.amount} ${expense.currency}\n")
                writer.append("Paid By: ${expense.paidByName}\n")
                writer.append("Category: ${expense.category.name}\n")
                if (expense.notes.isNotEmpty()) {
                    writer.append("Notes: ${expense.notes}\n")
                }
                writer.append("-".repeat(30) + "\n")
            }
        }
        
        return file
    }
    
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }
    
    fun shareFile(uri: Uri, format: ExportFormat) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when (format) {
                ExportFormat.CSV -> "text/csv"
                ExportFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ExportFormat.PDF -> "application/pdf"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Fairr Export - ${format.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Share Export File")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

data class ExportData(
    val groups: List<Group>,
    val expenses: List<Expense>,
    val settlements: List<SettlementSummary>,
    val exportDate: Date
)

data class SettlementSummary(
    val userId: String,
    val userName: String,
    val totalOwed: Double,
    val totalOwedToThem: Double,
    val netBalance: Double
) 