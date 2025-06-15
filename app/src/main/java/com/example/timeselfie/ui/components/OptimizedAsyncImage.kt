package com.example.timeselfie.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.timeselfie.R
import com.example.timeselfie.ui.theme.Gray200
import com.example.timeselfie.ui.theme.Primary

/**
 * Optimized async image component for timeline grid items.
 * Uses memory-efficient loading with proper caching and placeholder handling.
 */
@Composable
fun OptimizedAsyncImage(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    cornerRadius: Dp = 8.dp,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
) {
    if (imagePath.isNullOrEmpty()) {
        // Show placeholder for empty images
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Gray200),
            contentAlignment = Alignment.Center
        ) {
            placeholder?.invoke() ?: Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = 0.5f))
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .size(size.value.toInt()) // Optimize loading size
                .crossfade(true)
                .memoryCacheKey("timeline_$imagePath")
                .diskCacheKey("timeline_$imagePath")
                .build(),
            contentDescription = contentDescription,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray200),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Primary,
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray200),
                    contentAlignment = Alignment.Center
                ) {
                    error?.invoke() ?: Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Error loading image",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Red.copy(alpha = 0.5f))
                    )
                }
            }
        )
    }
}

/**
 * Optimized thumbnail image component specifically for timeline grid.
 * Prioritizes thumbnail path for faster loading.
 */
@Composable
fun TimelineThumbnailImage(
    imagePath: String?,
    thumbnailPath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    // Prefer thumbnail for faster loading, fallback to full image
    val imageSource = thumbnailPath?.takeIf { it.isNotEmpty() } ?: imagePath
    
    OptimizedAsyncImage(
        imagePath = imageSource,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size,
        cornerRadius = 8.dp
    )
}

/**
 * Memory-efficient image component for export preview.
 * Uses larger cache keys and optimized loading for export scenarios.
 */
@Composable
fun ExportPreviewImage(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    if (imagePath.isNullOrEmpty()) {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(Gray200),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = contentDescription,
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = 0.5f))
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .size(size.value.toInt())
                .crossfade(true)
                .memoryCacheKey("export_preview_$imagePath")
                .diskCacheKey("export_preview_$imagePath")
                .build(),
            contentDescription = contentDescription,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray200),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Primary,
                        strokeWidth = 3.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray200),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Error loading image",
                        modifier = Modifier.size(48.dp),
                        colorFilter = ColorFilter.tint(Color.Red.copy(alpha = 0.5f))
                    )
                }
            }
        )
    }
}
