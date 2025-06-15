package com.example.timeselfie.utils.export

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.utils.performance.PerformanceMonitor
import com.example.timeselfie.utils.storage.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages export operations for time capsules.
 */
@Singleton
class ExportManager @Inject constructor(
    private val context: Context,
    private val imageStorage: ImageStorage,
    private val collageGenerator: CollageGenerator
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * Export a time capsule as a collage.
     */
    suspend fun exportCapsule(
        capsuleName: String,
        entries: List<CapsuleEntry>
    ): ExportResult = PerformanceMonitor.monitorImageOperation("Export Capsule") {
        withContext(Dispatchers.IO) {
            try {
                if (entries.isEmpty()) {
                    return@withContext ExportResult.Error("No entries to export")
                }

                // Get image paths from entries
                val imagePaths = PerformanceMonitor.measure("Prepare image paths") {
                    entries
                        .sortedBy { it.dayNumber }
                        .mapNotNull { entry ->
                            val file = File(entry.imagePath)
                            if (file.exists()) entry.imagePath else null
                        }
                }

                if (imagePaths.isEmpty()) {
                    return@withContext ExportResult.Error("No valid images found")
                }

                // Create export file
                val exportDir = imageStorage.getExportDir()
                val fileName = "TimeCapsule_${capsuleName.replace(" ", "_")}_${fileNameFormat.format(Date())}.jpg"
                val exportFile = File(exportDir, fileName)

                // Generate collage with performance monitoring
                val collageResult = PerformanceMonitor.measureSuspend("Generate collage") {
                    collageGenerator.generateCollage(
                        imagePaths = imagePaths,
                        outputFile = exportFile,
                        title = capsuleName
                    )
                }

                when (collageResult) {
                    is CollageResult.Success -> {
                        ExportResult.Success(
                            filePath = collageResult.filePath,
                            fileName = fileName,
                            imageCount = imagePaths.size
                        )
                    }
                    is CollageResult.Error -> {
                        ExportResult.Error(collageResult.message)
                    }
                }

            } catch (e: Exception) {
                ExportResult.Error("Export failed: ${e.message}")
            }
        }
    }
    
    /**
     * Share an exported collage.
     */
    fun shareCollage(filePath: String): Intent? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "My Time Capsule Selfies")
                putExtra(Intent.EXTRA_TEXT, "Check out my 30-day selfie journey!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Save collage to device gallery.
     */
    suspend fun saveToGallery(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext false
            
            // For Android 10+ (API 29+), we would use MediaStore
            // For now, we'll just ensure the file is in a shareable location
            
            // Trigger media scan so the file appears in gallery
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/jpeg"),
                null
            )
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get export statistics.
     */
    suspend fun getExportStats(): ExportStats = withContext(Dispatchers.IO) {
        val exportDir = imageStorage.getExportDir()
        val exportFiles = exportDir.listFiles()?.filter { it.name.endsWith(".jpg") } ?: emptyList()
        
        val totalSize = exportFiles.sumOf { it.length() }
        val lastExportTime = exportFiles.maxOfOrNull { it.lastModified() }
        
        ExportStats(
            totalExports = exportFiles.size,
            totalSizeBytes = totalSize,
            lastExportTime = lastExportTime
        )
    }
    
    /**
     * Clean up old export files.
     */
    suspend fun cleanupOldExports(keepDays: Int = 30): Int = withContext(Dispatchers.IO) {
        val exportDir = imageStorage.getExportDir()
        val cutoffTime = System.currentTimeMillis() - (keepDays * 24 * 60 * 60 * 1000L)
        var deletedCount = 0
        
        exportDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime && file.delete()) {
                deletedCount++
            }
        }
        
        deletedCount
    }
    
    /**
     * Get file size in human-readable format.
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

/**
 * Result of export operation.
 */
sealed class ExportResult {
    data class Success(
        val filePath: String,
        val fileName: String,
        val imageCount: Int
    ) : ExportResult()
    
    data class Error(val message: String) : ExportResult()
}

/**
 * Export statistics.
 */
data class ExportStats(
    val totalExports: Int,
    val totalSizeBytes: Long,
    val lastExportTime: Long?
)
