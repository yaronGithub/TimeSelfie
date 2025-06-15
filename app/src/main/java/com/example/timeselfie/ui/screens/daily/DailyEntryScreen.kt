package com.example.timeselfie.ui.screens.daily

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.timeselfie.ui.components.camera.CameraPreview
import com.example.timeselfie.ui.theme.*
import com.example.timeselfie.utils.date.DateUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Daily entry screen for capturing selfies and entering mood.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DailyEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var showCamera by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageCaptured(it) }
    }
    
    // Handle save success
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            showSuccessDialog = true
        }
    }
    
    // Show camera if requested
    if (showCamera) {
        CameraPreview(
            onImageCaptured = { uri ->
                viewModel.onImageCaptured(uri)
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetSavedState()
                onNavigateBack()
            },
            title = { Text("Entry Saved! 🎉") },
            text = { Text("Your selfie and mood have been saved for today.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetSavedState()
                        onNavigateBack()
                    }
                ) {
                    Text("Great!")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.hasExistingEntry) "Update Today's Entry" else "Today's Entry",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = DateUtils.formatForDisplay(DateUtils.getTodayString()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnPrimaryLight.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimaryLight,
                    navigationIconContentColor = OnPrimaryLight
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Gray100)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.capturedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(uiState.capturedImageUri),
                            contentDescription = "Captured selfie",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Retake button overlay
                        FloatingActionButton(
                            onClick = { viewModel.onRetakePhoto() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            containerColor = AccentPink,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retake")
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Take a selfie to capture today's moment",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Gray600,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Camera/Gallery buttons
            if (uiState.capturedImageUri == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            when {
                                cameraPermissionState.status.isGranted -> {
                                    showCamera = true
                                }
                                cameraPermissionState.status.shouldShowRationale -> {
                                    // Show rationale and request permission
                                    cameraPermissionState.launchPermissionRequest()
                                }
                                else -> {
                                    // First time or permanently denied
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }
                    
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Gallery")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Mood input
            OutlinedTextField(
                value = uiState.mood,
                onValueChange = viewModel::onMoodChanged,
                label = { Text("How are you feeling?") },
                placeholder = { Text("happy, excited, calm...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )
            
            Text(
                text = "Enter one word that describes your mood today",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorLight.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = error,
                        color = ErrorLight,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Save button
            Button(
                onClick = viewModel::onSaveEntry,
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Gray300
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.hasExistingEntry) "Update Entry" else "Save Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
