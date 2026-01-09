package com.skinscan.sa.ui.screens.scan

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.skinscan.sa.ui.theme.Spacing
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Scan Screen with Camera Permission Handling (Story 2.1 + 2.2)
 *
 * Story 2.1: Camera permission flow
 * - Permission rationale dialog
 * - System permission request
 * - Settings deep-link for permanently denied
 *
 * Story 2.2: Face scan camera UI (placeholder)
 * - Full implementation requires CameraX + MediaPipe integration
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Check permission on screen entry
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            if (cameraPermissionState.status.shouldShowRationale) {
                showRationaleDialog = true
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    // Permission Rationale Dialog
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Camera Access Needed") },
            text = {
                Text("Glow Guide needs camera access to capture your face for skin analysis. Your image is processed on your device and never uploaded.")
            },
            confirmButton = {
                Button(onClick = {
                    showRationaleDialog = false
                    cameraPermissionState.launchPermissionRequest()
                }) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    onNavigateToHome()
                }) {
                    Text("Not Now")
                }
            }
        )
    }

    // Settings Dialog (for permanently denied)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Camera Permission Denied") },
            text = {
                Text("Please enable camera access in Settings > Apps > Glow Guide > Permissions")
            },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    showSettingsDialog = false
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    onNavigateToHome()
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Camera UI (Story 2.2 placeholder)
    if (cameraPermissionState.status.isGranted) {
        CameraPreviewPlaceholder(onBack = onNavigateToHome)
    } else {
        // Permission denied
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Camera access is required for face scanning")
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier.padding(top = Spacing.m)
            ) {
                Text("Return to Home")
            }
        }
    }
}

@Composable
fun CameraPreviewPlaceholder(onBack: () -> Unit) {
    // TODO Story 2.2: Implement full CameraX preview with face oval guide
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Preview",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Full CameraX implementation in Story 2.2",
            modifier = Modifier.padding(vertical = Spacing.m)
        )
        Text(
            text = "100% on-device processing",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = Spacing.s)
        )
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}
