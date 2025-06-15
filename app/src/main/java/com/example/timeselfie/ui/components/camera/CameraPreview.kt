package com.example.timeselfie.ui.components.camera

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.timeselfie.ui.theme.*
import com.example.timeselfie.utils.camera.CameraManager
import kotlinx.coroutines.launch

/**
 * Camera preview component with capture controls.
 */
@Composable
fun CameraPreview(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    var isCapturing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val cameraManager = remember { CameraManager(context) }
    
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    
    // Initialize camera when preview view is ready
    LaunchedEffect(previewView) {
        previewView?.let { preview ->
            try {
                cameraManager.initializeCamera(lifecycleOwner, preview)
            } catch (e: Exception) {
                error = "Failed to initialize camera: ${e.message}"
            }
        }
    }
    
    // Cleanup camera when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.cleanup()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    previewView = this
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Top bar with close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Take your selfie",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.width(48.dp)) // Balance the close button
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error message
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorLight.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Capture button
            FloatingActionButton(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        error = null
                        
                        coroutineScope.launch {
                            try {
                                val uri = cameraManager.capturePhoto()
                                onImageCaptured(uri)
                            } catch (e: Exception) {
                                error = "Failed to capture photo: ${e.message}"
                            } finally {
                                isCapturing = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, Color.White, CircleShape),
                containerColor = if (isCapturing) Gray500 else AccentPink,
                contentColor = Color.White
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isCapturing) "Capturing..." else "Tap to capture",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
