package com.skinscan.sa.ui.screens.history

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.ui.theme.Coral400
import com.skinscan.sa.ui.theme.Green600
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.Teal600
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Progress Timeline Screen (Story 4.3)
 *
 * Line chart visualization of skin health trends over time
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Progress Over Time") },
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
            is TimelineUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal600)
                }
            }

            is TimelineUiState.Error -> {
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

            is TimelineUiState.NotEnoughData -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Not Enough Data Yet",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.s))
                        Text(
                            text = "Complete at least 3 scans to see your progress timeline",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is TimelineUiState.Success -> {
                TimelineContent(
                    state = state,
                    selectedRange = selectedRange,
                    onRangeSelected = { viewModel.setDateRange(it) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TimelineContent(
    state: TimelineUiState.Success,
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.s))

            // Date range selector
            DateRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected
            )
        }

        // Overall health trend chart
        item {
            HealthTrendCard(
                dataPoints = state.healthScoreHistory,
                currentScore = state.currentHealthScore,
                trend = state.overallTrend
            )
        }

        // Concern-specific trends
        item {
            Text(
                text = "Concern Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = Spacing.s)
            )
        }

        items(state.concernTrends) { concernTrend ->
            ConcernTrendCard(concernTrend = concernTrend)
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun DateRangeSelector(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedRange.displayName)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DateRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = { Text(range.displayName) },
                    onClick = {
                        onRangeSelected(range)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HealthTrendCard(
    dataPoints: List<TimelineDataPoint>,
    currentScore: Int,
    trend: TrendDirection
) {
    val trendColor = when (trend) {
        TrendDirection.IMPROVING -> Green600
        TrendDirection.DECLINING -> Coral400
        TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Skin Health",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currentScore",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                    Text(
                        text = "/100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Icon(
                        imageVector = when (trend) {
                            TrendDirection.IMPROVING -> Icons.AutoMirrored.Filled.TrendingUp
                            TrendDirection.DECLINING -> Icons.AutoMirrored.Filled.TrendingDown
                            TrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                        },
                        contentDescription = null,
                        tint = trendColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            // Line chart
            if (dataPoints.size >= 2) {
                LineChart(
                    dataPoints = dataPoints,
                    trendColor = trendColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    dataPoints: List<TimelineDataPoint>,
    trendColor: Color,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (dataPoints.size < 2) return@Canvas

            val maxValue = dataPoints.maxOf { it.value }.coerceAtLeast(100f)
            val minValue = dataPoints.minOf { it.value }.coerceAtMost(0f)
            val valueRange = maxValue - minValue

            val width = size.width
            val height = size.height
            val paddingTop = 20f
            val paddingBottom = 30f
            val chartHeight = height - paddingTop - paddingBottom

            val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

            // Draw grid lines
            val gridColor = Color.Gray.copy(alpha = 0.2f)
            for (i in 0..4) {
                val y = paddingTop + (chartHeight * i / 4)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Draw line
            val path = Path()
            dataPoints.forEachIndexed { index, point ->
                val x = index * stepX
                val normalizedValue = (point.value - minValue) / valueRange
                val y = paddingTop + chartHeight * (1 - normalizedValue)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = trendColor,
                style = Stroke(width = 3f)
            )

            // Draw data points
            dataPoints.forEachIndexed { index, point ->
                val x = index * stepX
                val normalizedValue = (point.value - minValue) / valueRange
                val y = paddingTop + chartHeight * (1 - normalizedValue)

                drawCircle(
                    color = trendColor,
                    radius = 6f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (dataPoints.isNotEmpty()) {
                Text(
                    text = dateFormat.format(dataPoints.first().date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(dataPoints.last().date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConcernTrendCard(concernTrend: ConcernTrendData) {
    val trendColor = when (concernTrend.trend) {
        TrendDirection.IMPROVING -> Green600
        TrendDirection.DECLINING -> Coral400
        TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = concernTrend.concern.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%.0f%%".format(concernTrend.startSeverity * 100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " → ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.0f%%".format(concernTrend.endSeverity * 100),
                        style = MaterialTheme.typography.bodySmall,
                        color = trendColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Mini trend indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(trendColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (concernTrend.trend) {
                        TrendDirection.IMPROVING -> Icons.AutoMirrored.Filled.TrendingDown
                        TrendDirection.DECLINING -> Icons.AutoMirrored.Filled.TrendingUp
                        TrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                    },
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Date range options
 */
enum class DateRange(val displayName: String, val days: Int) {
    SEVEN_DAYS("Last 7 Days", 7),
    THIRTY_DAYS("Last 30 Days", 30),
    NINETY_DAYS("Last 90 Days", 90),
    ALL_TIME("All Time", Int.MAX_VALUE)
}

/**
 * Trend direction
 */
enum class TrendDirection {
    IMPROVING,
    DECLINING,
    STABLE
}

/**
 * Data point for timeline chart
 */
data class TimelineDataPoint(
    val date: java.util.Date,
    val value: Float
)

/**
 * Trend data for a specific concern
 */
data class ConcernTrendData(
    val concern: SkinConcern,
    val startSeverity: Float,
    val endSeverity: Float,
    val trend: TrendDirection,
    val dataPoints: List<TimelineDataPoint>
)
