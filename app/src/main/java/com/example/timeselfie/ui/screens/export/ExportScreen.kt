package com.example.timeselfie.ui.screens.export

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.timeselfie.ui.theme.*

/** Export screen for creating and sharing collages. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(onNavigateBack: () -> Unit, viewModel: ExportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Share launcher
    val shareLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                // Handle share result - could show success message or handle errors
                if (result.resultCode == Activity.RESULT_OK) {
                    // Share was successful, could show a toast or update UI
                }
            }

    // Handle messages and errors
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            // Show snackbar or toast
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Show error snackbar
            viewModel.clearError()
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Export Time Capsule") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Primary,
                                        titleContentColor = OnPrimaryLight,
                                        navigationIconContentColor = OnPrimaryLight
                                )
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .background(BackgroundLight)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val errorMessage = uiState.error
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                errorMessage != null -> {
                    ErrorContent(error = errorMessage, onRetry = {
                        viewModel.clearError()
                        viewModel.loadCapsuleData()
                    })
                }
                else -> {
                    ExportContent(
                            uiState = uiState,
                            onExport = viewModel::exportCollage,
                            onShare = {
                                viewModel.shareCollage()?.let { intent ->
                                    val shareIntent =
                                            Intent.createChooser(intent, "Share your Time Capsule")
                                    shareLauncher.launch(shareIntent)
                                }
                            },
                            onSaveToGallery = viewModel::saveToGallery
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading your time capsule...")
        }
    }
}

@Composable
private fun ErrorContent(error: String?, onRetry: () -> Unit) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Error", style = MaterialTheme.typography.headlineSmall, color = ErrorLight)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = error ?: "An unknown error occurred",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Gray700
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text("Retry")
        }
    }
}

@Composable
private fun ExportContent(
        uiState: ExportUiState,
        onExport: () -> Unit,
        onShare: () -> Unit,
        onSaveToGallery: () -> Unit
) {
    // Capsule summary
    CapsuleSummaryCard(
            capsuleName = uiState.capsuleName,
            completedDays = uiState.completedDays,
            totalDays = uiState.totalDays,
            completionPercentage = uiState.completionPercentage
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (!uiState.exportSuccess) {
        // Export section
        ExportSection(
                canExport = uiState.canExport,
                isExporting = uiState.isExporting,
                exportProgress = uiState.exportProgress,
                onExport = onExport
        )
    } else {
        // Success section with preview and actions
        ExportSuccessSection(
                filePath = uiState.exportedFilePath,
                fileName = uiState.exportedFileName,
                onShare = onShare,
                onSaveToGallery = onSaveToGallery,
                savedToGallery = uiState.savedToGallery
        )
    }
}

@Composable
private fun CapsuleSummaryCard(
        capsuleName: String,
        completedDays: Int,
        totalDays: Int,
        completionPercentage: Int
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = capsuleName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                        progress = { completionPercentage / 100f },
                        modifier = Modifier.size(120.dp),
                        color = Primary,
                        strokeWidth = 8.dp,
                        trackColor = Gray200
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                            text = "$completionPercentage%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                    )
                    Text(
                            text = "Complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                    text = "$completedDays of $totalDays days captured",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceLight
            )
        }
    }
}

@Composable
private fun ExportSection(
        canExport: Boolean,
        isExporting: Boolean,
        exportProgress: Float,
        onExport: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = "Create Your Collage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                    text = "Generate a beautiful collage from all your selfies",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Gray700
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isExporting) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                            progress = { exportProgress },
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                            color = Primary,
                            trackColor = Gray200
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                            text = "Creating collage... ${(exportProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                    )
                }
            } else {
                Button(
                        onClick = onExport,
                        enabled = canExport,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = Primary,
                                        disabledContainerColor = Gray300
                                )
                ) {
                    Text(
                            text = if (canExport) "Create Collage" else "No Images to Export",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportSuccessSection(
        filePath: String?,
        fileName: String?,
        onShare: () -> Unit,
        onSaveToGallery: () -> Unit,
        savedToGallery: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Success message
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.1f))
        ) {
            Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                        text = "🎉 Collage Created!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = fileName ?: "Your collage is ready",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preview (if available)
        filePath?.let { path ->
            Card(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                        painter = rememberAsyncImagePainter(path),
                        contentDescription = "Collage preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Action buttons
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }

            Button(
                    onClick = onSaveToGallery,
                    modifier = Modifier.weight(1f),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = if (savedToGallery) AccentGreen else Primary
                            )
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (savedToGallery) "Saved!" else "Save")
            }
        }
    }
}
