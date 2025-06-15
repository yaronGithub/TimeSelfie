package com.example.timeselfie.utils.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manager class for handling camera operations using CameraX.
 */
class CameraManager(private val context: Context) {
    
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    
    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: androidx.camera.view.PreviewView
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                // ImageCapture use case
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                // Select front camera for selfies
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                try {
                    // Unbind use cases before rebinding
                    cameraProvider?.unbindAll()
                    
                    // Bind use cases to camera
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    
                    continuation.resume(true)
                } catch (exc: Exception) {
                    continuation.resumeWithException(exc)
                }
                
            } catch (exc: Exception) {
                continuation.resumeWithException(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    suspend fun capturePhoto(): Uri = suspendCancellableCoroutine { continuation ->
        val imageCapture = imageCapture ?: run {
            continuation.resumeWithException(IllegalStateException("Camera not initialized"))
            return@suspendCancellableCoroutine
        }
        
        // Create time stamped name and MediaStore entry
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        
        // Create output file
        val photoFile = File(
            context.filesDir,
            "selfies/$name.jpg"
        )
        
        // Ensure directory exists
        photoFile.parentFile?.mkdirs()
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Set up image capture listener
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    continuation.resume(Uri.fromFile(photoFile))
                }
            }
        )
    }
    
    fun switchCamera() {
        // TODO: Implement camera switching between front and back
    }
    
    fun cleanup() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
    }
}
