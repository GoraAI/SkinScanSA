package com.skinscan.sa.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.ui.theme.Coral400
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.ErrorRed
import com.skinscan.sa.ui.theme.GlassSurface
import com.skinscan.sa.ui.theme.Green600
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.SuccessGreen
import com.skinscan.sa.ui.theme.SurfaceBlack
import com.skinscan.sa.ui.theme.Teal600
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite
import com.skinscan.sa.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Scan History Screen (Story 4.1)
 *
 * Displays chronological list of past skin scans with filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal600)
                }
            }

            is HistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is HistoryUiState.Success -> {
                HistoryContent(
                    state = state,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) },
                    onScanClicked = onNavigateToResults,
                    onStarToggled = { viewModel.toggleStar(it) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState.Success,
    selectedFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    onScanClicked: (String) -> Unit,
    onStarToggled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header with scan count
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.m)
        ) {
            Text(
                text = "${state.totalCount} scans total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.s))

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                HistoryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal600,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.m))

        if (state.scans.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedFilter == HistoryFilter.STARRED)
                            "No starred scans yet"
                        else
                            "No scans found",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Text(
                        text = if (selectedFilter == HistoryFilter.STARRED)
                            "Star your favorite scans to find them easily"
                        else
                            "Complete a skin scan to see it here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                items(state.scans, key = { it.scanId }) { scan ->
                    ScanHistoryCard(
                        scan = scan,
                        onClick = { onScanClicked(scan.scanId) },
                        onStarToggle = { onStarToggled(scan.scanId) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScanHistoryCard(
    scan: ScanHistoryItem,
    onClick: () -> Unit,
    onStarToggle: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            verticalAlignment = Alignment.Top
        ) {
            // Face diagram placeholder (color-coded zones)
            FaceDiagramThumbnail(
                concerns = scan.concerns,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(Spacing.m))

            Column(modifier = Modifier.weight(1f)) {
                // Date and time
                Text(
                    text = "${dateFormat.format(scan.date)} • ${timeFormat.format(scan.date)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Fitzpatrick type
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(getFitzpatrickColor(scan.fitzpatrickType))
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Type ${scan.fitzpatrickType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.s))

                // Top concerns
                if (scan.concerns.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        scan.concerns.take(2).forEach { concern ->
                            ConcernPill(concern = concern)
                        }
                    }
                }
            }

            // Health score and star
            Column(horizontalAlignment = Alignment.End) {
                // Star button
                IconButton(
                    onClick = onStarToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (scan.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (scan.isStarred) "Unstar" else "Star",
                        tint = if (scan.isStarred) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.s))

                // Health score badge
                HealthScoreBadge(score = scan.healthScore)
            }
        }
    }
}

@Composable
private fun FaceDiagramThumbnail(
    concerns: List<SkinConcern>,
    modifier: Modifier = Modifier
) {
    // Simplified face diagram with color-coded zones
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Simple face outline with concern indicators
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Forehead
            Box(
                modifier = Modifier
                    .size(40.dp, 16.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(getConcernColor(concerns, 0))
            )
            // Cheeks
            Row {
                Box(
                    modifier = Modifier
                        .size(18.dp, 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(getConcernColor(concerns, 1))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp, 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(getConcernColor(concerns, 2))
                )
            }
            // Chin
            Box(
                modifier = Modifier
                    .size(24.dp, 12.dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(getConcernColor(concerns, 3))
            )
        }
    }
}

@Composable
private fun ConcernPill(concern: SkinConcern) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(getConcernPillColor(concern).copy(alpha = 0.1f))
            .padding(horizontal = Spacing.s, vertical = 4.dp)
    ) {
        Text(
            text = concern.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = getConcernPillColor(concern)
        )
    }
}

@Composable
private fun HealthScoreBadge(score: Int) {
    val color = when {
        score >= 70 -> Green600
        score >= 50 -> Color(0xFFFFA726)
        else -> Coral400
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = "Health",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getFitzpatrickColor(type: Int): Color {
    return when (type) {
        1 -> Color(0xFFFFE0BD)
        2 -> Color(0xFFEAC086)
        3 -> Color(0xFFD4A574)
        4 -> Color(0xFFA67C52)
        5 -> Color(0xFF8B5A2B)
        6 -> Color(0xFF5D3A1A)
        else -> Color(0xFF8B5A2B)
    }
}

private fun getConcernColor(concerns: List<SkinConcern>, index: Int): Color {
    if (concerns.isEmpty()) return Color(0xFFE0E0E0)
    val concern = concerns.getOrNull(index % concerns.size) ?: return Color(0xFFE0E0E0)
    return getConcernPillColor(concern).copy(alpha = 0.3f)
}

private fun getConcernPillColor(concern: SkinConcern): Color {
    return when (concern) {
        SkinConcern.HYPERPIGMENTATION -> Color(0xFF8B4513)
        SkinConcern.ACNE -> Color(0xFFE57373)
        SkinConcern.DRYNESS -> Color(0xFF64B5F6)
        SkinConcern.OILINESS -> Color(0xFFFFD54F)
        SkinConcern.WRINKLES -> Color(0xFFBA68C8)
    }
}
