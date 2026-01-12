package com.skinscan.sa.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skinscan.sa.ui.theme.ChartLine1
import com.skinscan.sa.ui.theme.ChartLine2
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.GlassCard
import com.skinscan.sa.ui.theme.GlassSurface
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.RoseGoldDark
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.SurfaceBlack
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite
import com.skinscan.sa.ui.theme.glassCircle
import com.skinscan.sa.ui.theme.glassSurface
import java.util.Calendar

/**
 * Home Screen - Glow Guide Design
 *
 * Features glassmorphism cards, greeting, scan button, and bottom navigation
 */
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    userName: String = "Sarah"
) {
    var selectedNavItem by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Column {
                GlowGuideBottomNav(
                    selectedIndex = selectedNavItem,
                    onItemSelected = { index ->
                        selectedNavItem = index
                        when (index) {
                            0 -> { /* Already on home */ }
                            1 -> onNavigateToHistory()
                            2 -> onNavigateToProfile()
                        }
                    }
                )
                // Add navigation bar padding
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                        .background(SurfaceBlack)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.l))

            // Greeting Header
            GreetingHeader(userName = userName)

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Start Skin Scan Button
            StartScanButton(onClick = onNavigateToScan)

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Glass Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                // My Routine Card
                MyRoutineCard(
                    modifier = Modifier.weight(1f)
                )

                // Skin Progress Card
                SkinProgressCard(
                    modifier = Modifier.weight(1f),
                    onViewHistory = onNavigateToHistory
                )
            }

            Spacer(modifier = Modifier.height(Spacing.l))
        }
    }
}

@Composable
private fun GreetingHeader(userName: String) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        }

        // Profile Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(GlassSurface.copy(alpha = 0.6f))
                .border(1.dp, RoseGold.copy(alpha = 0.5f), CircleShape),
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
private fun StartScanButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .glassCircle()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Face,
                contentDescription = null,
                tint = TealAccent,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            Text(
                text = "Start Skin",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite
            )
            Text(
                text = "Scan",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite
            )
        }
    }
}

@Composable
private fun MyRoutineCard(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.height(180.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Routine",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "...",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            // Product icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RoutineProduct(name = "Morning\nCleanser", color = RoseGold)
                RoutineProduct(name = "Vitamin C\nSerum", color = Champagne)
                RoutineProduct(name = "Hydrating\nMoisturizer", color = TealAccent)
            }
        }
    }
}

@Composable
private fun RoutineProduct(name: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            // Simple bottle icon placeholder
            Box(
                modifier = Modifier
                    .size(20.dp, 28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.6f))
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            lineHeight = MaterialTheme.typography.labelSmall.lineHeight
        )
    }
}

// Using Color directly for Champagne since we already imported it
private val Champagne = com.skinscan.sa.ui.theme.Champagne

@Composable
private fun SkinProgressCard(
    modifier: Modifier = Modifier,
    onViewHistory: () -> Unit = {}
) {
    GlassCard(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onViewHistory)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skin Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "0.1%",
                    style = MaterialTheme.typography.labelMedium,
                    color = TealAccent
                )
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Mini line chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                MiniLineChart()
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartLegendItem(color = TealAccent, label = "Hydration")
                ChartLegendItem(color = RoseGold, label = "Clarity")
            }
        }
    }
}

@Composable
private fun MiniLineChart() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw grid lines
        val gridColor = Color.White.copy(alpha = 0.1f)
        for (i in 0..3) {
            val y = height * i / 3
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Draw hydration line (teal)
        val hydrationPath = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.25f, height * 0.5f,
                width * 0.5f, height * 0.6f,
                width * 0.75f, height * 0.3f
            )
            lineTo(width, height * 0.4f)
        }
        drawPath(
            path = hydrationPath,
            color = TealAccent,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw clarity line (rose gold)
        val clarityPath = Path().apply {
            moveTo(0f, height * 0.8f)
            cubicTo(
                width * 0.25f, height * 0.6f,
                width * 0.5f, height * 0.7f,
                width * 0.75f, height * 0.5f
            )
            lineTo(width, height * 0.45f)
        }
        drawPath(
            path = clarityPath,
            color = RoseGold,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun GlowGuideBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceBlack,
        contentColor = TextWhite,
        modifier = Modifier.height(80.dp)
    ) {
        BottomNavItem.entries.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealAccent,
                    selectedTextColor = TealAccent,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = TealAccent.copy(alpha = 0.1f)
                )
            )
        }
    }
}

private enum class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("Home", Icons.Filled.Home, Icons.Outlined.Home),
    History("History", Icons.Filled.Face, Icons.Outlined.Face),
    Profile("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}
