package com.skinscan.sa.ui.screens.results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.data.ml.SkinAnalysisInference.FaceZone
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.ui.theme.Coral400
import com.skinscan.sa.ui.theme.Green600
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.Teal600
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Results Screen (Story 2.5)
 *
 * Displays skin analysis results with:
 * - Skin type card
 * - Concern cards with confidence scores
 * - Interactive face zone visualization
 * - Navigation to recommendations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecommendations: (String) -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedZone by remember { mutableStateOf<FaceZone?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Skin Analysis") },
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
            is ResultsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal600)
                }
            }

            is ResultsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.l),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Coral400
                    )
                    Spacer(modifier = Modifier.height(Spacing.m))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.l))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }

            is ResultsUiState.Success -> {
                ResultsContent(
                    result = state.result,
                    onZoneSelected = { selectedZone = it },
                    onGetRecommendations = { onNavigateToRecommendations(state.result.scanId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Zone Detail Bottom Sheet (Story 2.6)
    if (selectedZone != null) {
        val result = (uiState as? ResultsUiState.Success)?.result
        ModalBottomSheet(
            onDismissRequest = { selectedZone = null },
            sheetState = bottomSheetState
        ) {
            result?.let {
                ZoneDetailContent(
                    zone = selectedZone!!,
                    concerns = it.zoneAnalysis[selectedZone] ?: emptyMap()
                )
            }
        }
    }
}

@Composable
private fun ResultsContent(
    result: ParsedScanResult,
    onZoneSelected: (FaceZone) -> Unit,
    onGetRecommendations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(Spacing.m)
    ) {
        // Scan date
        Text(
            text = "Scanned ${dateFormat.format(result.scannedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.m))

        // Section 1: Skin Type Card
        SkinTypeCard(
            fitzpatrickType = result.fitzpatrickType,
            confidence = result.fitzpatrickConfidence
        )

        Spacer(modifier = Modifier.height(Spacing.l))

        // Section 2: Detected Concerns or All Clear
        if (result.hasNoConcerns) {
            NoConcernsCard()
        } else {
            Text(
                text = "Detected Concerns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Spacing.s))

            result.primaryConcerns.forEach { concern ->
                ConcernCard(
                    concern = concern,
                    score = result.concernScores[concern] ?: 0f
                )
                Spacer(modifier = Modifier.height(Spacing.s))
            }
        }

        Spacer(modifier = Modifier.height(Spacing.l))

        // Section 3: Face Zone Visualization
        Text(
            text = "Zone Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tap a zone for details",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.s))

        FaceZoneVisualization(
            zoneAnalysis = result.zoneAnalysis,
            onZoneSelected = onZoneSelected
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Bottom CTA
        Button(
            onClick = onGetRecommendations,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Teal600)
        ) {
            Text("Get Product Recommendations")
        }

        Spacer(modifier = Modifier.height(Spacing.m))

        // Privacy note
        Text(
            text = "100% on-device analysis. Your image was not stored or uploaded.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Spacing.l))
    }
}

@Composable
private fun SkinTypeCard(
    fitzpatrickType: Int?,
    confidence: Float?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fitzpatrick type indicator
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = getFitzpatrickColor(fitzpatrickType),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fitzpatrickType?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if ((fitzpatrickType ?: 3) <= 3) Color.Black else Color.White
                )
            }

            Spacer(modifier = Modifier.width(Spacing.m))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Fitzpatrick Type ${fitzpatrickType ?: "Unknown"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = getFitzpatrickDescription(fitzpatrickType),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                confidence?.let {
                    Text(
                        text = "${(it * 100).toInt()}% confidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NoConcernsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Green600.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.m))
            Column {
                Text(
                    text = "Great news!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Green600
                )
                Text(
                    text = "No major skin concerns detected. Keep up your current skincare routine!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ConcernCard(
    concern: SkinConcern,
    score: Float
) {
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
                    text = concern.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(score * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = getConcernColor(score)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            LinearProgressIndicator(
                progress = { score },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = getConcernColor(score),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = concern.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FaceZoneVisualization(
    zoneAnalysis: Map<FaceZone, Map<SkinConcern, Float>>,
    onZoneSelected: (FaceZone) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .padding(Spacing.m)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val centerX = size.width / 2
                val faceWidth = size.width * 0.7f
                val faceHeight = size.height * 0.85f

                // Draw face outline
                drawOval(
                    color = Color.Gray.copy(alpha = 0.3f),
                    topLeft = Offset(centerX - faceWidth / 2, size.height * 0.05f),
                    size = Size(faceWidth, faceHeight),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Zone buttons overlaid on face
            ZoneButton(
                zone = FaceZone.FOREHEAD,
                score = zoneAnalysis[FaceZone.FOREHEAD]?.values?.maxOrNull() ?: 0f,
                onClick = { onZoneSelected(FaceZone.FOREHEAD) },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
            )
            ZoneButton(
                zone = FaceZone.LEFT_CHEEK,
                score = zoneAnalysis[FaceZone.LEFT_CHEEK]?.values?.maxOrNull() ?: 0f,
                onClick = { onZoneSelected(FaceZone.LEFT_CHEEK) },
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp)
            )
            ZoneButton(
                zone = FaceZone.RIGHT_CHEEK,
                score = zoneAnalysis[FaceZone.RIGHT_CHEEK]?.values?.maxOrNull() ?: 0f,
                onClick = { onZoneSelected(FaceZone.RIGHT_CHEEK) },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp)
            )
            ZoneButton(
                zone = FaceZone.NOSE,
                score = zoneAnalysis[FaceZone.NOSE]?.values?.maxOrNull() ?: 0f,
                onClick = { onZoneSelected(FaceZone.NOSE) },
                modifier = Modifier.align(Alignment.Center)
            )
            ZoneButton(
                zone = FaceZone.CHIN,
                score = zoneAnalysis[FaceZone.CHIN]?.values?.maxOrNull() ?: 0f,
                onClick = { onZoneSelected(FaceZone.CHIN) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp)
            )
        }
    }
}

@Composable
private fun ZoneButton(
    zone: FaceZone,
    score: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                color = getConcernColor(score).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = zone.displayName,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = "${(score * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = getConcernColor(score)
        )
    }
}

/**
 * Zone Detail Bottom Sheet Content (Story 2.6)
 */
@Composable
private fun ZoneDetailContent(
    zone: FaceZone,
    concerns: Map<SkinConcern, Float>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.l)
    ) {
        Text(
            text = zone.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Spacing.m))

        if (concerns.isEmpty()) {
            Text(
                text = "No detailed analysis available for this zone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            concerns.entries
                .sortedByDescending { it.value }
                .forEach { (concern, score) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = concern.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = concern.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${(score * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = getConcernColor(score)
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.s))
                }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

// Helper functions
private fun getFitzpatrickColor(type: Int?): Color {
    return when (type) {
        1 -> Color(0xFFFFE4C4) // Bisque
        2 -> Color(0xFFFFD9B3) // Light tan
        3 -> Color(0xFFD4A574) // Tan
        4 -> Color(0xFFA67B5B) // Medium brown
        5 -> Color(0xFF6B4423) // Brown
        6 -> Color(0xFF3D2314) // Dark brown
        else -> Color.Gray
    }
}

private fun getFitzpatrickDescription(type: Int?): String {
    return when (type) {
        1 -> "Very fair, always burns"
        2 -> "Fair, burns easily"
        3 -> "Medium, sometimes burns"
        4 -> "Olive, rarely burns"
        5 -> "Brown, very rarely burns"
        6 -> "Dark brown/black, never burns"
        else -> "Unable to determine"
    }
}

private fun getConcernColor(score: Float): Color {
    return when {
        score >= 0.7f -> Coral400
        score >= 0.4f -> Color(0xFFFFA726) // Orange
        else -> Green600
    }
}
