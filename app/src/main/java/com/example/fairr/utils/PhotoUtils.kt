package com.example.fairr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * Utility object for image and photo processing in the Fairr app.
 *
 * Provides helpers for image file creation, compression, thumbnail generation,
 * orientation correction, and receipt OCR processing.
 */
object PhotoUtils {
    
    // Image compression constants
    private const val MAX_IMAGE_WIDTH = 1024
    private const val MAX_IMAGE_HEIGHT = 1024
    private const val THUMBNAIL_SIZE = 256
    private const val DEFAULT_JPEG_QUALITY = 85
    private const val THUMBNAIL_JPEG_QUALITY = 75
    private const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024 // 2MB
    
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
     * Enhanced image compression with memory optimization
     */
    suspend fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = MAX_IMAGE_WIDTH,
        maxHeight: Int = MAX_IMAGE_HEIGHT,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            // First, get image dimensions without loading the full bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
            
            // Calculate optimal sample size to reduce memory usage
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight)
            
            // Load bitmap with sample size to reduce memory footprint
            val decodingOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
                inDither = true
                inTempStorage = ByteArray(16 * 1024) // 16KB temp storage
            }
            
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodingOptions)
            } ?: throw IOException("Unable to decode image")
            
            // Fix orientation if needed
            val rotatedBitmap = fixImageOrientation(context, imageUri, bitmap)
            
            // Scale to exact dimensions if still too large
            val finalBitmap = if (rotatedBitmap.width > maxWidth || rotatedBitmap.height > maxHeight) {
                scaleBitmapSafely(rotatedBitmap, maxWidth, maxHeight)
            } else {
                rotatedBitmap
            }
            
            // Compress to ByteArray with quality control
            val result = compressBitmapToByteArray(finalBitmap, quality)
            
            // Clean up bitmaps to prevent memory leaks
            if (bitmap != rotatedBitmap) bitmap.recycle()
            if (rotatedBitmap != finalBitmap) rotatedBitmap.recycle()
            finalBitmap.recycle()
            
            result
        } catch (e: Exception) {
            throw IOException("Image compression failed: ${e.message}", e)
        }
    }
    
    /**
     * Create thumbnail with aggressive compression
     */
    suspend fun createThumbnail(
        context: Context,
        imageUri: Uri,
        size: Int = THUMBNAIL_SIZE
    ): ByteArray = withContext(Dispatchers.IO) {
        compressImage(context, imageUri, size, size, THUMBNAIL_JPEG_QUALITY)
    }
    
    /**
     * Process image for receipt scanning with optimization
     */
    suspend fun processReceiptImage(
        context: Context,
        imageUri: Uri
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        try {
            // Compress image for processing
            val compressedData = compressImage(context, imageUri, 1200, 1600, 90)
            
            // Create thumbnail
            val thumbnailData = createThumbnail(context, imageUri)
            
            // Convert compressed data to bitmap for OCR
            val bitmap = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.size)
            
            ProcessedImageResult(
                compressedImage = compressedData,
                thumbnail = thumbnailData,
                bitmap = bitmap,
                success = true
            )
        } catch (e: Exception) {
            ProcessedImageResult(
                error = e.message ?: "Processing failed",
                success = false
            )
        }
    }
    
    /**
     * Calculate optimal sample size for memory efficiency
     */
    private fun calculateSampleSize(
        imageWidth: Int,
        imageHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        
        if (imageHeight > reqHeight || imageWidth > reqWidth) {
            val halfHeight = imageHeight / 2
            val halfWidth = imageWidth / 2
            
            // Calculate largest inSampleSize that keeps dimensions larger than requested
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Fix image orientation based on EXIF data
     */
    private suspend fun fixImageOrientation(
        context: Context,
        imageUri: Uri,
        bitmap: Bitmap
    ): Bitmap = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = inputStream?.use { ExifInterface(it) }
            
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return@withContext bitmap
            }
            
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotatedBitmap != bitmap) {
                bitmap.recycle() // Clean up original if new bitmap was created
            }
            rotatedBitmap
        } catch (e: Exception) {
            // If orientation fix fails, return original bitmap
            bitmap
        }
    }
    
    /**
     * Scale bitmap safely to prevent OutOfMemoryError
     */
    private fun scaleBitmapSafely(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scaleFactor = min(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )
        
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()
        
        return try {
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } catch (e: OutOfMemoryError) {
            // If scaling fails due to memory, try with lower quality
            val matrix = Matrix()
            matrix.postScale(scaleFactor, scaleFactor)
            Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
        }
    }
    
    /**
     * Compress bitmap to byte array with size control
     */
    private fun compressBitmapToByteArray(bitmap: Bitmap, initialQuality: Int): ByteArray {
        var quality = initialQuality
        var result: ByteArray
        
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            result = outputStream.toByteArray()
            
            // If file is still too large, reduce quality
            if (result.size > MAX_FILE_SIZE_BYTES && quality > 50) {
                quality -= 10
            } else {
                break
            }
        } while (quality >= 50)
        
        return result
    }
    
    /**
     * Saves a bitmap to internal storage with compression
     */
    suspend fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "images/$filename")
            file.parentFile?.mkdirs()
            
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Loads a bitmap from file path with memory optimization
     */
    suspend fun loadBitmapFromPath(
        path: String,
        maxWidth: Int = MAX_IMAGE_WIDTH,
        maxHeight: Int = MAX_IMAGE_HEIGHT
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Get dimensions first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            
            // Calculate sample size
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight)
            
            // Load with sample size
            val decodingOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            
            BitmapFactory.decodeFile(path, decodingOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Extracts text from image using ML Kit OCR with memory optimization
     */
    suspend fun extractTextFromImage(
        bitmap: Bitmap,
        onSuccess: (ExtractedReceiptData) -> Unit,
        onFailure: (Exception) -> Unit
    ) = withContext(Dispatchers.Main) {
        try {
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
        } catch (e: Exception) {
            onFailure(e)
        }
    }
    
    /**
     * Parses receipt text to extract useful information
     */
    private fun parseReceiptText(text: String): ExtractedReceiptData {
        // Detect amounts in various formats
        val amountRegex = Regex("""(?:[$₱€£¥₹]|USD|EUR|GBP|JPY|INR|PHP)?\s*(\d{1,6}(?:[,.]?\d{3})*(?:[.,]\d{2})?)\s*(?:[$₱€£¥₹]|USD|EUR|GBP|JPY|INR|PHP)?""")
        val amounts = amountRegex.findAll(text)
            .mapNotNull { it.groupValues[1].replace(",", "").toDoubleOrNull() }
            .filter { it > 0.01 && it < 999999.99 }
            .toList()
        
        // Get the largest amount as the most likely total
        val suggestedAmount = amounts.maxOrNull()
        
        // Extract potential business name (usually on first few lines)
        val lines = text.lines().filter { it.trim().isNotEmpty() }
        val suggestedDescription = lines.take(3)
            .find { line -> 
                line.length in 3..50 && 
                !line.contains(Regex("""\d{4}""")) && // Avoid dates
                !amountRegex.containsMatchIn(line) // Avoid amount lines
            }?.trim()
        
        // Try to extract date
        val dateRegex = Regex("""(\d{1,2}[-/]\d{1,2}[-/]\d{2,4})""")
        val extractedDate = dateRegex.find(text)?.value
        
        return ExtractedReceiptData(
            suggestedAmount = suggestedAmount,
            suggestedDescription = suggestedDescription,
            extractedDate = extractedDate,
            allText = text,
            detectedAmounts = amounts
        )
    }
    
    /**
     * Clean up temporary files to free storage
     */
    suspend fun cleanupTempFiles(context: Context): Int = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.filesDir, "images")
            var deletedCount = 0
            
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("FAIRR_") && 
                    System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) { // 24 hours
                    if (file.delete()) deletedCount++
                }
            }
            
            deletedCount
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Compress image from URI and return Bitmap
     */
    fun compressImage(context: Context, imageUri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Scale down if too large
            if (bitmap.width > MAX_IMAGE_WIDTH || bitmap.height > MAX_IMAGE_HEIGHT) {
                scaleBitmapSafely(bitmap, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Compress image from file and return Bitmap
     */
    fun compressImageFromFile(file: File): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            
            // Scale down if too large
            if (bitmap.width > MAX_IMAGE_WIDTH || bitmap.height > MAX_IMAGE_HEIGHT) {
                scaleBitmapSafely(bitmap, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Save Bitmap to file
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, DEFAULT_JPEG_QUALITY, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun decodeSampledBitmapFromFile(
        path: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)

            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, this)
        }
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
 * Data class for receipt photos with optimization
 */
data class ReceiptPhoto(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String,
    val thumbnail: String? = null,
    val extractedData: ExtractedReceiptData? = null,
    val capturedAt: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L,
    val isCompressed: Boolean = false
)

/**
 * Result class for processed images
 */
data class ProcessedImageResult(
    val compressedImage: ByteArray? = null,
    val thumbnail: ByteArray? = null,
    val bitmap: Bitmap? = null,
    val error: String? = null,
    val success: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessedImageResult

        if (compressedImage != null) {
            if (other.compressedImage == null) return false
            if (!compressedImage.contentEquals(other.compressedImage)) return false
        } else if (other.compressedImage != null) return false
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false
        if (bitmap != other.bitmap) return false
        if (error != other.error) return false
        if (success != other.success) return false

        return true
    }

    override fun hashCode(): Int {
        var result = compressedImage?.contentHashCode() ?: 0
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + success.hashCode()
        return result
    }
} 