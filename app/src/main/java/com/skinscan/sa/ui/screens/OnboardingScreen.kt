package com.skinscan.sa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite

/**
 * Glow Guide Onboarding Screen
 * Premium dark mode design with glassmorphism effects
 */
@Composable
fun OnboardingScreen(
    onNavigateToPOPIAConsent: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Ambient glow effects
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .blur(radius = 70.dp)
                .background(RoseGold.copy(alpha = 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(220.dp)
                .blur(radius = 90.dp)
                .background(TealAccent.copy(alpha = 0.15f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Skin Analysis",
                tint = RoseGold,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.l))

            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary
            )
            Text(
                text = "Glow Guide",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = RoseGold
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            Text(
                text = "Personalized skincare recommendations powered by AI, designed specifically for melanin-rich skin.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.m)
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Button(
                onClick = onNavigateToPOPIAConsent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoseGold,
                    contentColor = DarkBackground
                ),
                modifier = Modifier.padding(horizontal = Spacing.l)
            ) {
                Text(
                    text = "Get Started",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
