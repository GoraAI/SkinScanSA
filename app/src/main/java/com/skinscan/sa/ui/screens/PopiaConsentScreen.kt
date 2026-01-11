package com.skinscan.sa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.GlassCard
import com.skinscan.sa.ui.theme.GlassSurface
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite
import com.skinscan.sa.ui.theme.glassSurface

/**
 * POPIA Biometric Consent Screen - Glow Guide Design
 *
 * Legal compliance for facial image processing (POPIA Section 26)
 * With glassmorphism styling
 */
@Composable
fun PopiaConsentScreen(
    onNavigateToProfileSetup: () -> Unit,
    onExitApp: () -> Unit,
    viewModel: PopiaConsentViewModel = hiltViewModel()
) {
    val biometricConsentChecked by viewModel.biometricConsentChecked.collectAsState()
    val analyticsConsentChecked by viewModel.analyticsConsentChecked.collectAsState()
    val isButtonEnabled by viewModel.isButtonEnabled.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Spacer(modifier = Modifier.height(Spacing.m))

            // Header with shield icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TealAccent.copy(alpha = 0.1f))
                        .border(1.dp, TealAccent.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = TealAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "Your Privacy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Matters",
                        style = MaterialTheme.typography.headlineMedium,
                        color = RoseGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            // Privacy explanation in glass card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    PrivacyPoint(text = "We process your facial image to analyze your skin")
                    PrivacyPoint(
                        text = "100% on-device processing - your image NEVER leaves your phone",
                        highlight = true
                    )
                    PrivacyPoint(text = "No cloud uploads, no third-party sharing")
                    PrivacyPoint(text = "You can delete all data anytime from Settings")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Required consent section
            Text(
                text = "Biometric Data Processing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoseGold
            )
            Text(
                text = "Required",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(cornerRadius = 12.dp)
                    .padding(Spacing.m)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = biometricConsentChecked,
                        onCheckedChange = { viewModel.setBiometricConsent(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TealAccent,
                            uncheckedColor = TextSecondary,
                            checkmarkColor = DarkBackground
                        )
                    )
                    Text(
                        text = "I consent to on-device facial image analysis for skin assessment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite,
                        modifier = Modifier.padding(start = Spacing.s)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            // Optional consent section
            Text(
                text = "Optional Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(cornerRadius = 12.dp)
                    .padding(Spacing.m)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = analyticsConsentChecked,
                        onCheckedChange = { viewModel.setAnalyticsConsent(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TealAccent,
                            uncheckedColor = TextSecondary,
                            checkmarkColor = DarkBackground
                        )
                    )
                    Text(
                        text = "I consent to anonymous usage analytics (helps improve the app)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite,
                        modifier = Modifier.padding(start = Spacing.s)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Accept & Continue button
            Button(
                onClick = {
                    viewModel.saveConsent()
                    onNavigateToProfileSetup()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoseGold,
                    contentColor = DarkBackground,
                    disabledContainerColor = GlassSurface.copy(alpha = 0.4f),
                    disabledContentColor = TextSecondary
                )
            ) {
                Text(
                    text = "Accept & Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Privacy policy link
            TextButton(
                onClick = { /* TODO: Open privacy policy */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "View Full Privacy Policy",
                    color = TealAccent
                )
            }

            Spacer(modifier = Modifier.height(Spacing.m))
        }
    }
}

@Composable
private fun PrivacyPoint(text: String, highlight: Boolean = false) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(if (highlight) TealAccent else TextSecondary)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) TextWhite else TextSecondary,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
