package com.skinscan.sa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinscan.sa.ui.theme.*
import java.util.Calendar

/**
 * Glow Guide Home Screen
 *
 * Premium dark mode design with glassmorphism effects
 * and rose gold accents for a luxury skincare experience.
 */
@Composable
fun HomeScreen(
    userName: String = "Beautiful",
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background Ambient Glows
        AmbientGlowEffects()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with greeting and avatar
            HeaderSection(
                userName = userName,
                onProfileClick = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Main Scan Button (Glowing)
            GlowingScanButton(onClick = onNavigateToScan)

            Spacer(modifier = Modifier.height(48.dp))

            // Dashboard Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // My Routine Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp),
                    title = "My Routine",
                    onClick = onNavigateToHistory
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RoutineStep(step = "AM", isCompleted = true)
                        RoutineStep(step = "PM", isCompleted = false)
                        RoutineStep(step = "SPF", isCompleted = true)
                    }
                }

                // Skin Progress Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp),
                    title = "Skin Progress",
                    onClick = onNavigateToHistory
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Progress",
                                tint = TealAccent,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "+10% Clarity",
                                color = TealAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "This week",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.History,
                    label = "History",
                    onClick = onNavigateToHistory
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Person,
                    label = "Profile",
                    onClick = onNavigateToProfile
                )
            }
        }
    }
}

@Composable
private fun AmbientGlowEffects() {
    // Top-left rose gold glow
    Box(
        modifier = Modifier
            .offset(x = (-50).dp, y = (-50).dp)
            .size(200.dp)
            .blur(radius = 80.dp)
            .background(RoseGold.copy(alpha = 0.2f), CircleShape)
    )
    // Bottom-right teal glow
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .size(250.dp)
                .blur(radius = 100.dp)
                .background(TealAccent.copy(alpha = 0.15f), CircleShape)
        )
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = getGreeting(),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
        // User Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(GlassSurface)
                .border(1.dp, RoseGold, CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = RoseGold,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun GlowingScanButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(RoseGold.copy(alpha = 0.3f), Color.Transparent)
                )
            )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = RoseGold,
                contentColor = DarkBackground
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 4.dp
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Scan",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start Skin\nScan",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextWhite,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun RoutineStep(
    step: String,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) RoseGold.copy(alpha = 0.3f)
                    else GlassSurface
                )
                .border(
                    width = 1.dp,
                    color = if (isCompleted) RoseGold else GlassBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "✓",
                    color = RoseGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = step,
            color = if (isCompleted) TextWhite else TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = RoseGold,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = TextWhite,
            fontSize = 14.sp
        )
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning,"
        hour < 17 -> "Good Afternoon,"
        else -> "Good Evening,"
    }
}
