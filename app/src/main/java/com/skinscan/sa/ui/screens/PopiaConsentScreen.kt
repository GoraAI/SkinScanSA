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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.GlassBorder
import com.skinscan.sa.ui.theme.GlassSurface
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite

/**
 * POPIA Biometric Consent Screen (Story 1.4)
 * Glow Guide premium dark mode design
 *
 * Legal compliance for facial image processing (POPIA Section 26)
 * - Explicit consent required before any biometric data processing
 * - Checkboxes NOT pre-checked
 * - Full disclosure of on-device processing
 * - Audit trail of consent stored in encrypted Room DB
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
        // Ambient glow effects
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(180.dp)
                .blur(radius = 70.dp)
                .background(RoseGold.copy(alpha = 0.15f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(200.dp)
                .blur(radius = 80.dp)
                .background(TealAccent.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            // Header with shield icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Privacy Shield",
                    tint = TealAccent,
                    modifier = Modifier.size(48.dp)
                )
                Column {
                    Text(
                        text = "Your Privacy",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Matters",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoseGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Privacy explanation in glass card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                PrivacyPoint(
                    icon = Icons.Default.CheckCircle,
                    text = "We process your facial image to analyze your skin",
                    isBold = false
                )
                PrivacyPoint(
                    icon = Icons.Default.CheckCircle,
                    text = "100% on-device processing - your image NEVER leaves your phone",
                    isBold = true
                )
                PrivacyPoint(
                    icon = Icons.Default.CheckCircle,
                    text = "No cloud uploads, no third-party sharing",
                    isBold = false
                )
                PrivacyPoint(
                    icon = Icons.Default.CheckCircle,
                    text = "You can delete all data anytime from Settings",
                    isBold = false
                )
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            // Required consent section
            Text(
                text = "Biometric Data Processing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoseGold
            )
            Text(
                text = "Required",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(Spacing.s),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = biometricConsentChecked,
                    onCheckedChange = { viewModel.setBiometricConsent(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = RoseGold,
                        uncheckedColor = TextSecondary,
                        checkmarkColor = DarkBackground
                    )
                )
                Text(
                    text = "I consent to on-device facial image analysis for skin assessment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite,
                    modifier = Modifier.padding(start = Spacing.xs, top = Spacing.s)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            // Optional consent section
            Text(
                text = "Optional Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(Spacing.s),
                verticalAlignment = Alignment.Top
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
                    color = TextSecondary,
                    modifier = Modifier.padding(start = Spacing.xs, top = Spacing.s)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            // Accept & Continue button
            Button(
                onClick = {
                    viewModel.saveConsent()
                    onNavigateToProfileSetup()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoseGold,
                    contentColor = DarkBackground,
                    disabledContainerColor = GlassSurface,
                    disabledContentColor = TextSecondary
                )
            ) {
                Text(
                    text = "Accept & Continue",
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
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PrivacyPoint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isBold: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TealAccent,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) TextWhite else TextSecondary
        )
    }
}
