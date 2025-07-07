package com.example.fairr.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.Group
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.repository.ExpenseQueryParams
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.settlements.SettlementService
import com.example.fairr.data.settlements.SettlementSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

enum class ExportFormat {
    CSV,
    EXCEL,
    PDF
}

data class ExportOptions(
    val format: ExportFormat = ExportFormat.CSV,
    val dateRange: DateRange? = null,
    val includeSettlements: Boolean = true,
    val groupId: String? = null
)

data class DateRange(
    val startDate: Date,
    val endDate: Date
)

data class ExportResult(
    val success: Boolean,
    val fileUri: Uri? = null,
    val errorMessage: String? = null
)

/**
 * Export Service for the Fairr app.
 *
 * Handles exporting user and group data (expenses, settlements, groups) to CSV, Excel, or PDF formats.
 * Integrates with Firestore, Storage, and authentication to ensure secure, privacy-compliant exports.
 * Provides helpers for filtering, formatting, and packaging export data for user download or sharing.
 */
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
                else -> exportGroupData(options.groupId)
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
            val expenses = expenseRepository.getPaginatedExpenses(
                ExpenseQueryParams(
                    groupId = group.id,
                    pageSize = Int.MAX_VALUE
                )
            )
            allExpenses.addAll(expenses.expenses)
            
            if (options.includeSettlements) {
                val groupSettlements = settlementService.getSettlementSummary(group.id)
                settlements.addAll(groupSettlements)
            }
        }
        
        return ExportData(
            group = null,
            expenses = filterExpensesByDateRange(allExpenses, options.dateRange),
            settlements = settlements
        )
    }
    
    private suspend fun exportGroupData(groupId: String): ExportData {
        val group = groupService.getGroupById(groupId).first()
        val expenses = expenseRepository.getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = groupId,
                pageSize = Int.MAX_VALUE
            )
        )
        val settlements = settlementService.getSettlementSummary(groupId)
        
        return ExportData(
            group = group,
            expenses = expenses.expenses,
            settlements = settlements
        )
    }
    
    private fun filterExpensesByDateRange(expenses: List<Expense>, dateRange: DateRange?): List<Expense> {
        if (dateRange == null) return expenses
        return expenses.filter { expense ->
            expense.date.toDate().time in dateRange.startDate.time..dateRange.endDate.time
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
                val group = data.group
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
            writer.append("Generated on: ${formatDate(data.expenses.first().date.toDate())}\n\n")
            
            // Write expense data
            writer.append("Expenses\n")
            writer.append("Date,Group,Description,Amount,Currency,Paid By,Split Between,Category,Notes\n")
            
            data.expenses.forEach { expense ->
                val group = data.group
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
            writer.append("Generated on: ${formatDate(data.expenses.first().date.toDate())}\n")
            writer.append("Total Groups: ${data.group?.let { listOf(it) }?.size ?: 0}\n")
            writer.append("Total Expenses: ${data.expenses.size}\n")
            writer.append("Total Amount: ${data.expenses.sumOf { it.amount }}\n\n")
            
            // Write expense data
            writer.append("EXPENSES\n")
            writer.append("=".repeat(50) + "\n")
            
            data.expenses.forEach { expense ->
                val group = data.group
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

    private fun generateCSV(data: ExportData): String {
        val csvBuilder = StringBuilder()
        
        // Add headers
        csvBuilder.append("Type,Date,Amount,Category,Description,Paid By,Split With\n")
        
        // Add expense data
        data.expenses.forEach { expense ->
            csvBuilder.append("expense,")
            csvBuilder.append("${formatDate(expense.date.toDate())},")
            csvBuilder.append("${expense.amount},")
            csvBuilder.append("${expense.category.displayName},")
            csvBuilder.append("${expense.description},")
            csvBuilder.append("${expense.paidByName},")
            csvBuilder.append("${expense.splitBetween.joinToString("|") { it.userName }}\n")
        }
        
        return csvBuilder.toString()
    }

    private fun generateJSON(data: ExportData): String {
        return buildString {
            append("{\n")
            append("  \"expenses\": [\n")
            data.expenses.forEachIndexed { index, expense ->
                append("    {\n")
                append("      \"type\": \"expense\",\n")
                append("      \"date\": \"${formatDate(expense.date.toDate())}\",\n")
                append("      \"amount\": ${expense.amount},\n")
                append("      \"category\": \"${expense.category.displayName}\",\n")
                append("      \"description\": \"${expense.description}\",\n")
                append("      \"paidBy\": \"${expense.paidByName}\",\n")
                append("      \"splitWith\": [${expense.splitBetween.joinToString { "\"${it.userName}\"" }}]\n")
                append("    }${if (index < data.expenses.size - 1) "," else ""}\n")
            }
            append("  ]\n")
            append("}")
        }
    }

    /*
    suspend fun exportGroupExpenses(groupId: String, format: String = "csv"): String {
        val expenses = expenseRepository.getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = groupId,
                pageSize = Int.MAX_VALUE
            )
        )

        val group: Group = groupService.getGroupById(groupId).first()

        return when (format.lowercase()) {
            "csv" -> generateCSV(ExportData(group = group, expenses = expenses.expenses, settlements = emptyList()))
            "json" -> generateJSON(ExportData(group = group, expenses = expenses.expenses, settlements = emptyList()))
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
    }
    */

    /*
    suspend fun exportUserExpenses(userId: String, format: String = "csv"): String {
        val expenses = expenseRepository.getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = "", // We need a groupId, but for user expenses we'll need to handle this differently
                pageSize = Int.MAX_VALUE
            )
        )

        return when (format.lowercase()) {
            "csv" -> generateCSV(ExportData(expenses = expenses.expenses, settlements = emptyList()))
            "json" -> generateJSON(ExportData(expenses = expenses.expenses, settlements = emptyList()))
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
    }
    */

    /*
    suspend fun exportUserData(options: ExportOptions = ExportOptions()): File {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val expenses = expenseRepository.getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = "", // We need a groupId, but for user expenses we'll need to handle this differently
                startDate = options.dateRange?.startDate,
                endDate = options.dateRange?.endDate
            )
        )
        
        val settlements = if (options.includeSettlements) {
            emptyList() // getSettlementsByUserId doesn't exist, we'll need to implement it
        } else {
            emptyList()
        }
        
        val fileName = "fairr_user_export_${System.currentTimeMillis()}.${options.format.name.lowercase()}"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        val content = when (options.format) {
            ExportFormat.CSV -> generateCSV(ExportData(expenses = expenses.expenses, settlements = settlements))
            ExportFormat.EXCEL -> generateCSV(ExportData(expenses = expenses.expenses, settlements = settlements)) // For now, use CSV format
            ExportFormat.PDF -> generateCSV(ExportData(expenses = expenses.expenses, settlements = settlements)) // For now, use CSV format
        }
        
        FileWriter(file).use { writer ->
            writer.write(content)
        }
        
        return file
    }
    */
}

data class ExportData(
    val group: Group? = null,
    val expenses: List<Expense> = emptyList(),
    val settlements: List<SettlementSummary> = emptyList()
)

data class SettlementSummary(
    val userId: String,
    val userName: String,
    val totalOwed: Double,
    val totalOwedToThem: Double,
    val netBalance: Double
) 