package com.skinscan.sa.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.skinscan.sa.ui.theme.Spacing

/**
 * Home Screen placeholder (Story 2.1)
 *
 * Full implementation in future stories
 * For Sprint 1: Just "Start Your First Scan" button to trigger camera permission flow
 */
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Glow Guide!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Your personalized skincare companion",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = Spacing.m)
        )

        Button(
            onClick = onNavigateToScan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Your First Scan")
        }
    }
}
