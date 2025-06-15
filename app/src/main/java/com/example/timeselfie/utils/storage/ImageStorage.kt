package com.example.timeselfie.utils.storage

import android.content.Context
import android.net.Uri
import com.example.timeselfie.utils.image.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles storage operations for images in the Time Selfie app.
 */
@Singleton
class ImageStorage @Inject constructor(
    private val context: Context,
    private val imageProcessor: ImageProcessor
) {
    
    companion object {
        private const val SELFIES_DIR = "selfies"
        private const val THUMBNAILS_DIR = "thumbnails"
        private const val EXPORTS_DIR = "exports"
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Save a selfie image for a specific date.
     */
    suspend fun saveSelfie(sourceUri: Uri, date: String): SaveResult = withContext(Dispatchers.IO) {
        try {
            val selfieFile = getSelfieFile(date)
            val thumbnailFile = getThumbnailFile(date)
            
            // Ensure directories exist
            selfieFile.parentFile?.mkdirs()
            thumbnailFile.parentFile?.mkdirs()
            
            // Compress and save the main image
            val imageSuccess = imageProcessor.compressImage(sourceUri, selfieFile)
            if (!imageSuccess) {
                return@withContext SaveResult.Error("Failed to save image")
            }
            
            // Generate thumbnail
            val thumbnailSuccess = imageProcessor.generateThumbnail(selfieFile, thumbnailFile)
            if (!thumbnailSuccess) {
                // Main image saved but thumbnail failed - still consider success
                return@withContext SaveResult.Success(
                    imagePath = selfieFile.absolutePath,
                    thumbnailPath = null,
                    fileName = selfieFile.name
                )
            }
            
            SaveResult.Success(
                imagePath = selfieFile.absolutePath,
                thumbnailPath = thumbnailFile.absolutePath,
                fileName = selfieFile.name
            )
        } catch (e: Exception) {
            SaveResult.Error("Failed to save selfie: ${e.message}")
        }
    }
    
    /**
     * Delete a selfie and its thumbnail for a specific date.
     */
    suspend fun deleteSelfie(date: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val selfieFile = getSelfieFile(date)
            val thumbnailFile = getThumbnailFile(date)
            
            var success = true
            if (selfieFile.exists()) {
                success = selfieFile.delete()
            }
            if (thumbnailFile.exists()) {
                success = thumbnailFile.delete() && success
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the file for a selfie on a specific date.
     */
    fun getSelfieFile(date: String): File {
        return File(context.filesDir, "$SELFIES_DIR/$date.jpg")
    }
    
    /**
     * Get the thumbnail file for a selfie on a specific date.
     */
    fun getThumbnailFile(date: String): File {
        return File(context.filesDir, "$THUMBNAILS_DIR/thumb_$date.jpg")
    }
    
    /**
     * Get the export directory for collages.
     */
    fun getExportDir(): File {
        return File(context.filesDir, EXPORTS_DIR).also { it.mkdirs() }
    }
    
    /**
     * Check if a selfie exists for a specific date.
     */
    fun hasSelfie(date: String): Boolean {
        return getSelfieFile(date).exists()
    }
    
    /**
     * Get the URI for a selfie file.
     */
    fun getSelfieUri(date: String): Uri? {
        val file = getSelfieFile(date)
        return if (file.exists()) Uri.fromFile(file) else null
    }
    
    /**
     * Get the URI for a thumbnail file.
     */
    fun getThumbnailUri(date: String): Uri? {
        val file = getThumbnailFile(date)
        return if (file.exists()) Uri.fromFile(file) else null
    }
    
    /**
     * Get storage usage information.
     */
    suspend fun getStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
        val selfiesDir = File(context.filesDir, SELFIES_DIR)
        val thumbnailsDir = File(context.filesDir, THUMBNAILS_DIR)
        val exportsDir = File(context.filesDir, EXPORTS_DIR)
        
        val selfiesSize = calculateDirectorySize(selfiesDir)
        val thumbnailsSize = calculateDirectorySize(thumbnailsDir)
        val exportsSize = calculateDirectorySize(exportsDir)
        
        val selfieCount = selfiesDir.listFiles()?.size ?: 0
        
        StorageInfo(
            totalSize = selfiesSize + thumbnailsSize + exportsSize,
            selfiesSize = selfiesSize,
            thumbnailsSize = thumbnailsSize,
            exportsSize = exportsSize,
            selfieCount = selfieCount
        )
    }
    
    private fun calculateDirectorySize(directory: File): Long {
        if (!directory.exists()) return 0L
        
        return directory.listFiles()?.sumOf { file ->
            if (file.isDirectory) calculateDirectorySize(file) else file.length()
        } ?: 0L
    }
    
    /**
     * Clean up old files if storage is getting full.
     */
    suspend fun cleanupOldFiles(keepDays: Int = 60): Int = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - (keepDays * 24 * 60 * 60 * 1000L)
        var deletedCount = 0
        
        val selfiesDir = File(context.filesDir, SELFIES_DIR)
        val thumbnailsDir = File(context.filesDir, THUMBNAILS_DIR)
        
        selfiesDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                if (file.delete()) deletedCount++
            }
        }
        
        thumbnailsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                if (file.delete()) deletedCount++
            }
        }
        
        deletedCount
    }
}

/**
 * Result of saving an image.
 */
sealed class SaveResult {
    data class Success(
        val imagePath: String,
        val thumbnailPath: String?,
        val fileName: String
    ) : SaveResult()
    
    data class Error(val message: String) : SaveResult()
}

/**
 * Storage usage information.
 */
data class StorageInfo(
    val totalSize: Long,
    val selfiesSize: Long,
    val thumbnailsSize: Long,
    val exportsSize: Long,
    val selfieCount: Int
)
