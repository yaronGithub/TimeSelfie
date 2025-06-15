package com.example.timeselfie.ui.screens.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.timeselfie.data.models.TimelineItem
import com.example.timeselfie.ui.theme.*
import com.example.timeselfie.utils.date.DateUtils

/** Main timeline screen showing the 30-day grid of selfies and moods. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
        onNavigateToDaily: () -> Unit,
        onNavigateToExport: () -> Unit,
        viewModel: TimelineViewModel = hiltViewModel()
) {
        val uiState by viewModel.uiState.collectAsState()

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text(
                                                text = uiState.currentCapsule ?: "Time Capsule",
                                                style = MaterialTheme.typography.headlineSmall
                                        )
                                },
                                actions = {
                                        IconButton(onClick = onNavigateToExport) {
                                                Icon(
                                                        Icons.Default.FileDownload,
                                                        contentDescription = "Export Collage",
                                                        tint = OnPrimaryLight
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = Primary,
                                                titleContentColor = OnPrimaryLight
                                        )
                        )
                },
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = onNavigateToDaily,
                                containerColor = AccentPink,
                                contentColor = Color.White
                        ) { Icon(Icons.Default.Add, contentDescription = "Add Today's Entry") }
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .background(BackgroundLight)
                ) {
                        when {
                                uiState.isLoading -> {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) { CircularProgressIndicator(color = Primary) }
                                }
                                uiState.error != null -> {
                                        val errorMessage = uiState.error!!
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(
                                                        text = "Error: $errorMessage",
                                                        color = ErrorLight,
                                                        textAlign = TextAlign.Center
                                                )
                                        }
                                }
                                else -> {
                                        TimelineGrid(
                                                items = uiState.entries,
                                                onItemClick = { item ->
                                                        if (item.isEmpty &&
                                                                        DateUtils.isToday(item.date)
                                                        ) {
                                                                onNavigateToDaily()
                                                        }
                                                },
                                                modifier = Modifier.fillMaxSize().padding(16.dp)
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun TimelineGrid(
        items: List<TimelineItem>,
        onItemClick: (TimelineItem) -> Unit,
        modifier: Modifier = Modifier
) {
        LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier
        ) {
                items(items) { item ->
                        TimelineGridItem(item = item, onClick = { onItemClick(item) })
                }
        }
}

@Composable
private fun TimelineGridItem(item: TimelineItem, onClick: () -> Unit) {
        val isToday = DateUtils.isToday(item.date)
        val backgroundColor =
                when {
                        item.isEmpty && isToday -> AccentPink.copy(alpha = 0.3f)
                        item.isEmpty -> Gray200
                        else -> PastelBlue.copy(alpha = 0.7f)
                }

        Box(
                modifier =
                        Modifier.aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(backgroundColor)
                                .border(
                                        width = if (isToday) 2.dp else 1.dp,
                                        color = if (isToday) AccentPink else Gray300,
                                        shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onClick() }
        ) {
                if (!item.isEmpty && item.thumbnailPath != null) {
                        // Show thumbnail image
                        Image(
                                painter = rememberAsyncImagePainter(item.thumbnailPath),
                                contentDescription = "Selfie for ${item.date}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                        )

                        // Mood overlay
                        Box(
                                modifier =
                                        Modifier.align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(
                                                        Color.Black.copy(alpha = 0.6f),
                                                        RoundedCornerShape(
                                                                bottomStart = 12.dp,
                                                                bottomEnd = 12.dp
                                                        )
                                                )
                                                .padding(4.dp),
                                contentAlignment = Alignment.Center
                        ) {
                                Text(
                                        text = item.mood ?: "",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                )
                        }

                        // Day number overlay
                        Box(
                                modifier =
                                        Modifier.align(Alignment.TopStart)
                                                .background(
                                                        Color.Black.copy(alpha = 0.6f),
                                                        RoundedCornerShape(
                                                                topStart = 12.dp,
                                                                bottomEnd = 8.dp
                                                        )
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                                Text(
                                        text = item.dayNumber.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                )
                        }
                } else {
                        // Empty state
                        Column(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                                Text(
                                        text = item.dayNumber.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isEmpty) Gray600 else OnSurfaceLight
                                )

                                if (item.isEmpty && isToday) {
                                        Text(
                                                text = "Today",
                                                fontSize = 8.sp,
                                                color = AccentPink,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }
        }
}
