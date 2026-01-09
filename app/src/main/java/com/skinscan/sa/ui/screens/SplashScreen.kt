package com.skinscan.sa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.ui.theme.Spacing
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState(initial = false)

    // Navigate after 2 seconds
    LaunchedEffect(isOnboardingCompleted) {
        delay(2000)
        if (isOnboardingCompleted) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00897B), // Teal 600
                        Color(0xFF004D40)  // Teal 900 (darker)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            modifier = Modifier.padding(Spacing.l)
        ) {
            // App logo placeholder (using Face icon for MVP)
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "SkinScan SA Logo",
                modifier = Modifier.size(120.dp),
                tint = Color.White
            )

            // App name
            Text(
                text = "SkinScan SA",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Tagline
            Text(
                text = "Personalized Skincare for Your Skin Tone",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}
