package com.example.timeselfie.utils.export

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.scale
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Generates collages from multiple selfie images.
 */
@Singleton
class CollageGenerator @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val COLLAGE_SIZE = 2048 // Final collage size
        private const val GRID_PADDING = 8 // Padding between images
        private const val BORDER_SIZE = 4 // Border around each image
        private const val JPEG_QUALITY = 90
        private const val MAX_IMAGES_PER_BATCH = 4 // Process images in batches to reduce memory usage
    }
    
    /**
     * Generate a collage from a list of image paths.
     */
    suspend fun generateCollage(
        imagePaths: List<String>,
        outputFile: File,
        title: String? = null
    ): CollageResult = withContext(Dispatchers.IO) {
        try {
            if (imagePaths.isEmpty()) {
                return@withContext CollageResult.Error("No images to create collage")
            }
            
            // Calculate grid dimensions
            val imageCount = imagePaths.size
            val gridSize = ceil(sqrt(imageCount.toDouble())).toInt()
            val cellSize = (COLLAGE_SIZE - (gridSize + 1) * GRID_PADDING) / gridSize
            
            // Create collage bitmap
            val collageBitmap = Bitmap.createBitmap(
                COLLAGE_SIZE, 
                COLLAGE_SIZE + if (title != null) 100 else 0, 
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(collageBitmap)
            
            // Fill background with white
            canvas.drawColor(Color.WHITE)
            
            // Draw title if provided
            var yOffset = 0
            if (title != null) {
                yOffset = 100
                drawTitle(canvas, title, COLLAGE_SIZE, 100)
            }
            
            // Load and draw images with memory optimization
            val imageLoader = ImageLoader(context)
            val paint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
            }

            // Border paint
            val borderPaint = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.STROKE
                strokeWidth = BORDER_SIZE.toFloat()
            }

            // Process images in batches to reduce memory usage
            for (batchStart in imagePaths.indices step MAX_IMAGES_PER_BATCH) {
                val batchEnd = minOf(batchStart + MAX_IMAGES_PER_BATCH, imagePaths.size)

                for (i in batchStart until batchEnd) {
                    val row = i / gridSize
                    val col = i % gridSize

                    val x = GRID_PADDING + col * (cellSize + GRID_PADDING)
                    val y = yOffset + GRID_PADDING + row * (cellSize + GRID_PADDING)

                    try {
                        // Load image with size constraints to reduce memory usage
                        val request = ImageRequest.Builder(context)
                            .data(imagePaths[i])
                            .size(cellSize, cellSize) // Limit size during loading
                            .allowHardware(false) // Ensure we can access bitmap
                            .build()

                        val drawable = imageLoader.execute(request).drawable
                        val bitmap = (drawable as? BitmapDrawable)?.bitmap

                        if (bitmap != null) {
                            // Scale and crop image to fit cell
                            val scaledBitmap = scaleCenterCrop(bitmap, cellSize, cellSize)

                            // Draw image
                            canvas.drawBitmap(scaledBitmap, x.toFloat(), y.toFloat(), paint)

                            // Draw border
                            canvas.drawRect(
                                x.toFloat() - BORDER_SIZE/2,
                                y.toFloat() - BORDER_SIZE/2,
                                (x + cellSize).toFloat() + BORDER_SIZE/2,
                                (y + cellSize).toFloat() + BORDER_SIZE/2,
                                borderPaint
                            )

                            // Clean up immediately
                            if (scaledBitmap != bitmap) {
                                scaledBitmap.recycle()
                            }
                        } else {
                            // Draw placeholder for missing image
                            drawPlaceholder(canvas, x, y, cellSize)
                        }
                    } catch (e: Exception) {
                        // Draw placeholder for failed image
                        drawPlaceholder(canvas, x, y, cellSize)
                    }
                }

                // Force garbage collection between batches for large collages
                if (imagePaths.size > 16) {
                    System.gc()
                }
            }
            
            // Save collage
            val outputStream = FileOutputStream(outputFile)
            val success = collageBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            outputStream.close()
            
            collageBitmap.recycle()
            
            if (success) {
                CollageResult.Success(outputFile.absolutePath)
            } else {
                CollageResult.Error("Failed to save collage")
            }
            
        } catch (e: Exception) {
            CollageResult.Error("Error creating collage: ${e.message}")
        }
    }
    
    /**
     * Scale and center crop bitmap to fit target dimensions.
     */
    private fun scaleCenterCrop(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height
        
        // Calculate scale to fill target dimensions
        val scale = maxOf(
            targetWidth.toFloat() / sourceWidth,
            targetHeight.toFloat() / sourceHeight
        )
        
        val scaledWidth = (sourceWidth * scale).toInt()
        val scaledHeight = (sourceHeight * scale).toInt()
        
        // Scale bitmap
        val scaledBitmap = source.scale(scaledWidth, scaledHeight)
        
        // Calculate crop position (center)
        val cropX = (scaledWidth - targetWidth) / 2
        val cropY = (scaledHeight - targetHeight) / 2
        
        // Crop to target size
        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap, 
            cropX, 
            cropY, 
            targetWidth, 
            targetHeight
        )
        
        if (scaledBitmap != croppedBitmap) {
            scaledBitmap.recycle()
        }
        
        return croppedBitmap
    }
    
    /**
     * Draw title text at the top of the collage.
     */
    private fun drawTitle(canvas: Canvas, title: String, width: Int, height: Int) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        val x = width / 2f
        val y = height / 2f + paint.textSize / 3
        
        canvas.drawText(title, x, y, paint)
    }
    
    /**
     * Draw placeholder for missing images.
     */
    private fun drawPlaceholder(canvas: Canvas, x: Int, y: Int, size: Int) {
        val paint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }
        
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + size).toFloat(), (y + size).toFloat(), paint)
        
        // Draw X
        val linePaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        
        val margin = size * 0.3f
        canvas.drawLine(
            x + margin, y + margin,
            x + size - margin, y + size - margin,
            linePaint
        )
        canvas.drawLine(
            x + size - margin, y + margin,
            x + margin, y + size - margin,
            linePaint
        )
    }
}

/**
 * Result of collage generation.
 */
sealed class CollageResult {
    data class Success(val filePath: String) : CollageResult()
    data class Error(val message: String) : CollageResult()
}
