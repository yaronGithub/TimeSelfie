package com.example.timeselfie.utils.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for processing and manipulating images.
 */
class ImageProcessor(private val context: Context) {
    
    companion object {
        private const val MAX_IMAGE_SIZE = 1024
        private const val THUMBNAIL_SIZE = 200
        private const val JPEG_QUALITY = 85
        private const val MEMORY_THRESHOLD_MB = 50 // Force GC if memory usage exceeds this

        /**
         * Calculate the largest inSampleSize value that is a power of 2 and keeps both
         * height and width larger than the requested height and width.
         */
        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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

        /**
         * Check memory usage and force garbage collection if needed.
         */
        private fun checkMemoryUsage() {
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            if (usedMemory > MEMORY_THRESHOLD_MB) {
                System.gc()
            }
        }
    }
    
    /**
     * Compress and resize an image to reduce file size while maintaining quality.
     */
    suspend fun compressImage(sourceUri: Uri, targetFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // Use BitmapFactory.Options to decode efficiently
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            val inputStream = context.contentResolver.openInputStream(sourceUri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate sample size to reduce memory usage
            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory

            val inputStream2 = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2?.close()

            if (originalBitmap == null) return@withContext false

            // Rotate image if needed based on EXIF data
            val rotatedBitmap = rotateImageIfRequired(originalBitmap, sourceUri)

            // Resize image if it's too large
            val resizedBitmap = resizeImage(rotatedBitmap, MAX_IMAGE_SIZE)
            
            // Save compressed image
            val outputStream = FileOutputStream(targetFile)
            val success = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            outputStream.close()
            
            // Clean up bitmaps
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            resizedBitmap.recycle()

            // Check memory usage after processing
            checkMemoryUsage()

            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Generate a thumbnail for the given image.
     */
    suspend fun generateThumbnail(sourceFile: File, thumbnailFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            if (originalBitmap == null) return@withContext false
            
            val thumbnailBitmap = resizeImage(originalBitmap, THUMBNAIL_SIZE)
            
            // Ensure thumbnail directory exists
            thumbnailFile.parentFile?.mkdirs()
            
            val outputStream = FileOutputStream(thumbnailFile)
            val success = thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            outputStream.close()
            
            originalBitmap.recycle()
            thumbnailBitmap.recycle()
            
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Resize image while maintaining aspect ratio.
     */
    private fun resizeImage(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val ratio = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return bitmap.scale(newWidth, newHeight)
    }
    
    /**
     * Rotate image based on EXIF orientation data.
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, imageUri: Uri): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()
            
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: IOException) {
            return bitmap
        }
    }
    
    /**
     * Rotate bitmap by specified degrees.
     */
    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Get the file size in a human-readable format.
     */
    fun getFileSizeString(file: File): String {
        val bytes = file.length()
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
