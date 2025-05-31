package com.example.fairr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PhotoUtils {
    
    /**
     * Creates a temporary file for camera capture
     */
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "FAIRR_${timeStamp}_"
        val storageDir = File(context.filesDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    /**
     * Gets a file URI for camera capture
     */
    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * Saves a bitmap to internal storage
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, filename: String): String? {
        return try {
            val file = File(context.filesDir, "images/$filename")
            file.parentFile?.mkdirs()
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Loads a bitmap from file path
     */
    fun loadBitmapFromPath(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Extracts text from image using ML Kit OCR
     */
    fun extractTextFromImage(
        bitmap: Bitmap,
        onSuccess: (ExtractedReceiptData) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedData = parseReceiptText(visionText.text)
                onSuccess(extractedData)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    /**
     * Parses receipt text to extract useful information
     */
    private fun parseReceiptText(text: String): ExtractedReceiptData {
        val lines = text.split("\n").map { it.trim() }
        
        // Extract potential amounts (patterns like $XX.XX, XX.XX)
        val amountPattern = Regex("""[\$]?(\d+\.?\d{0,2})""")
        val amounts = mutableListOf<Double>()
        
        // Extract potential merchant names (usually first few non-amount lines)
        val merchantCandidates = mutableListOf<String>()
        
        // Extract date patterns
        val datePattern = Regex("""\d{1,2}[/-]\d{1,2}[/-]\d{2,4}""")
        var extractedDate: String? = null
        
        lines.forEach { line ->
            // Look for amounts
            amountPattern.findAll(line).forEach { match ->
                val amount = match.groupValues[1].toDoubleOrNull()
                if (amount != null && amount > 0) {
                    amounts.add(amount)
                }
            }
            
            // Look for dates
            if (extractedDate == null) {
                val dateMatch = datePattern.find(line)
                if (dateMatch != null) {
                    extractedDate = dateMatch.value
                }
            }
            
            // Potential merchant names (non-empty, not just numbers/amounts)
            if (line.isNotEmpty() && 
                !line.matches(Regex("""^[\d\s\$\.\-,]+$""")) &&
                line.length > 3) {
                merchantCandidates.add(line)
            }
        }
        
        // Get the largest amount as likely total
        val suggestedAmount = amounts.maxOrNull()
        
        // Get first meaningful line as potential description
        val suggestedDescription = merchantCandidates.firstOrNull()
        
        return ExtractedReceiptData(
            suggestedAmount = suggestedAmount,
            suggestedDescription = suggestedDescription,
            extractedDate = extractedDate,
            allText = text,
            detectedAmounts = amounts
        )
    }
    
    /**
     * Compresses image if it's too large
     */
    fun compressImage(bitmap: Bitmap, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val scaleFactor = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )
        
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

/**
 * Data class for extracted receipt information
 */
data class ExtractedReceiptData(
    val suggestedAmount: Double?,
    val suggestedDescription: String?,
    val extractedDate: String?,
    val allText: String,
    val detectedAmounts: List<Double>
)

/**
 * Data class for receipt photos
 */
data class ReceiptPhoto(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String,
    val thumbnail: String? = null,
    val extractedData: ExtractedReceiptData? = null,
    val capturedAt: Long = System.currentTimeMillis()
) 