package com.skinscan.sa.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.ui.theme.Spacing

/**
 * POPIA Biometric Consent Screen (Story 1.4)
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
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Privacy Shield",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Your Privacy Matters",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(Spacing.s))

        // Privacy explanation
        Text(
            text = "We process your facial image to analyze your skin",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "100% on-device processing - your image NEVER leaves your phone",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No cloud uploads, no third-party sharing",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "You can delete all data anytime from Settings",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(Spacing.m))

        // Required consent section
        Text(
            text = "Biometric Data Processing (Required)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = biometricConsentChecked,
                onCheckedChange = { viewModel.setBiometricConsent(it) }
            )
            Text(
                text = "I consent to on-device facial image analysis for skin assessment",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Spacing.s, top = Spacing.s)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.m))

        // Optional consent section
        Text(
            text = "Optional Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = analyticsConsentChecked,
                onCheckedChange = { viewModel.setAnalyticsConsent(it) }
            )
            Text(
                text = "I consent to anonymous usage analytics (helps improve the app)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Spacing.s, top = Spacing.s)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.l))

        // Accept & Continue button (disabled until required consent given)
        Button(
            onClick = {
                viewModel.saveConsent()
                onNavigateToProfileSetup()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isButtonEnabled
        ) {
            Text("Accept & Continue")
        }

        // Privacy policy link
        TextButton(
            onClick = { /* TODO: Open privacy policy */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("View Full Privacy Policy")
        }
    }
}
