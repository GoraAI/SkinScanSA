package com.skinscan.sa.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.skinscan.sa.domain.usecase.CompareScanProgressUseCase
import com.skinscan.sa.ui.theme.Coral400
import com.skinscan.sa.ui.theme.Green600
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.Teal600
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Progress Comparison Screen (Story 4.2)
 *
 * Side-by-side comparison of two scans showing improvement metrics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    onNavigateBack: () -> Unit,
    viewModel: ComparisonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Comparison") },
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
            is ComparisonUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal600)
                }
            }

            is ComparisonUiState.Error -> {
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

            is ComparisonUiState.Success -> {
                ComparisonContent(
                    result = state.comparison,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ComparisonContent(
    result: CompareScanProgressUseCase.ComparisonResult,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.s))

            // Date range header
            Text(
                text = "${dateFormat.format(result.baselineDate)} → ${dateFormat.format(result.currentDate)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${result.daysBetween} days between scans",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Overall improvement score
        item {
            OverallImprovementCard(result = result)
        }

        // Concern comparisons
        item {
            Text(
                text = "Concern-by-Concern Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = Spacing.s)
            )
        }

        items(result.concernComparisons) { comparison ->
            ConcernComparisonCard(comparison = comparison)
        }

        // Summary
        item {
            SummaryCard(result = result)
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun OverallImprovementCard(result: CompareScanProgressUseCase.ComparisonResult) {
    val improvementColor = when {
        result.overallImprovement > 0 -> Green600
        result.overallImprovement < 0 -> Coral400
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = improvementColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (result.overallImprovement > 0) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Improving",
                        tint = Green600,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (result.overallImprovement < 0) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = "Declining",
                        tint = Coral400,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.s))
                Text(
                    text = if (result.overallImprovement >= 0) "+${result.overallImprovement.toInt()}%" else "${result.overallImprovement.toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = improvementColor
                )
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            Text(
                text = when {
                    result.overallImprovement > 0 -> "Improvement"
                    result.overallImprovement < 0 -> "Decline"
                    else -> "No Change"
                },
                style = MaterialTheme.typography.titleMedium,
                color = improvementColor
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            // Health score comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Before",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${result.baselineHealthScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "After",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${result.currentHealthScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = improvementColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ConcernComparisonCard(comparison: CompareScanProgressUseCase.ConcernComparison) {
    val trendColor = when (comparison.trend) {
        CompareScanProgressUseCase.Trend.IMPROVING -> Green600
        CompareScanProgressUseCase.Trend.WORSENING -> Coral400
        CompareScanProgressUseCase.Trend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Concern name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comparison.concern.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Before severity
                    SeverityIndicator(
                        severity = comparison.baselineSeverity,
                        label = "Before"
                    )

                    Spacer(modifier = Modifier.width(Spacing.m))

                    // Arrow
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(Spacing.m))

                    // After severity
                    SeverityIndicator(
                        severity = comparison.currentSeverity,
                        label = "After"
                    )
                }
            }

            // Trend indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(trendColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (comparison.trend) {
                            CompareScanProgressUseCase.Trend.IMPROVING -> Icons.AutoMirrored.Filled.TrendingDown
                            CompareScanProgressUseCase.Trend.WORSENING -> Icons.AutoMirrored.Filled.TrendingUp
                            CompareScanProgressUseCase.Trend.STABLE -> Icons.Default.Remove
                        },
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = getSeverityLabel(comparison.currentSeverity),
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor
                )
            }
        }
    }
}

@Composable
private fun SeverityIndicator(severity: Float, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "%.0f%%".format(severity * 100),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SummaryCard(result: CompareScanProgressUseCase.ComparisonResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Teal600.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.s))

            if (result.improvingSkinConcerns.isNotEmpty()) {
                Row {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Green600)
                    )
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Text(
                        text = "Improving: ${result.improvingSkinConcerns.joinToString { it.displayName }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green600
                    )
                }
            }

            if (result.worseningSkinConcerns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Coral400)
                    )
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Text(
                        text = "Needs attention: ${result.worseningSkinConcerns.joinToString { it.displayName }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Coral400
                    )
                }
            }

            if (result.unchangedSkinConcerns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Text(
                        text = "Stable: ${result.unchangedSkinConcerns.joinToString { it.displayName }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getSeverityLabel(severity: Float): String {
    return when {
        severity >= 0.7f -> "Severe"
        severity >= 0.5f -> "Moderate"
        severity >= 0.3f -> "Mild"
        else -> "Minimal"
    }
}
