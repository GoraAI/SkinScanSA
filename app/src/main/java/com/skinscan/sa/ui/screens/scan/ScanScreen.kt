package com.skinscan.sa.ui.screens.scan

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.skinscan.sa.ui.theme.Coral400
import com.skinscan.sa.ui.theme.Green600
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.Teal600
import java.util.concurrent.Executors

private const val TAG = "ScanScreen"

/**
 * Scan Screen with Camera Permission Handling (Story 2.1 + 2.2 + 6.3)
 *
 * Story 2.1: Camera permission flow
 * - Permission rationale dialog
 * - System permission request
 * - Settings deep-link for permanently denied
 *
 * Story 2.2: Face scan camera UI
 * - CameraX preview with front-facing camera
 * - Oval face guide overlay
 * - Capture button
 * - Retake/Analyze flow
 *
 * Story 6.3: Face Image Privacy
 * - FLAG_SECURE prevents screenshots
 * - Image stored in RAM only (not disk)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToResults: ((String) -> Unit)? = null,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Story 6.3: Prevent screenshots of camera screen (POPIA compliance)
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Observe ViewModel state
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()

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

    // Navigate to results when scan completes
    LaunchedEffect(scanResult) {
        scanResult?.let { result ->
            onNavigateToResults?.invoke(result.scanId)
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

    // Main content based on permission state
    if (cameraPermissionState.status.isGranted) {
        FaceScanCameraScreen(
            viewModel = viewModel,
            isAnalyzing = isAnalyzing,
            onBack = onNavigateToHome
        )
    } else {
        // Permission denied state
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.m))
            Text(
                text = "Camera access is required for face scanning",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.l))
            Button(
                onClick = {
                    if (cameraPermissionState.status.shouldShowRationale) {
                        showRationaleDialog = true
                    } else {
                        showSettingsDialog = true
                    }
                }
            ) {
                Text("Enable Camera")
            }
            TextButton(
                onClick = onNavigateToHome,
                modifier = Modifier.padding(top = Spacing.s)
            ) {
                Text("Return to Home")
            }
        }
    }
}

/**
 * Face Scan Camera Screen with CameraX Preview
 *
 * Features:
 * - Front-facing camera preview
 * - Oval face guide overlay
 * - Capture button
 * - Retake/Analyze buttons after capture
 */
@Composable
private fun FaceScanCameraScreen(
    viewModel: ScanViewModel,
    isAnalyzing: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Cleanup executor on dispose
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedBitmap == null) {
            // Camera preview mode
            CameraPreviewWithOverlay(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onImageCaptureReady = { imageCapture = it }
            )

            // Top bar with back button
            TopBar(onBack = onBack)

            // Instruction text
            InstructionText()

            // Capture button at bottom
            CaptureButton(
                onClick = {
                    imageCapture?.let { capture ->
                        captureImage(
                            imageCapture = capture,
                            executor = cameraExecutor,
                            onImageCaptured = { bitmap ->
                                capturedBitmap = bitmap
                            },
                            onError = { exception ->
                                Log.e(TAG, "Image capture failed", exception)
                            }
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.xxl)
            )
        } else {
            // Image review mode
            ImageReviewScreen(
                bitmap = capturedBitmap!!,
                isAnalyzing = isAnalyzing,
                onRetake = {
                    capturedBitmap?.recycle()
                    capturedBitmap = null
                },
                onAnalyze = {
                    capturedBitmap?.let { bitmap ->
                        // TODO: Get actual userId from session/preferences
                        viewModel.analyzeFace(bitmap, "default_user")
                    }
                },
                onBack = onBack
            )
        }
    }
}

/**
 * CameraX Preview with oval face guide overlay
 */
@Composable
private fun CameraPreviewWithOverlay(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // Preview use case
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    // Image capture use case
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    onImageCaptureReady(imageCapture)

                    // Select front camera
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()

                    try {
                        // Unbind all use cases before rebinding
                        cameraProvider.unbindAll()

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Oval face guide overlay
        FaceGuideOverlay()
    }
}

/**
 * Semi-transparent overlay with oval cutout for face alignment
 */
@Composable
private fun FaceGuideOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Oval dimensions - positioned in upper-center area for face
        val ovalWidth = canvasWidth * 0.7f
        val ovalHeight = canvasHeight * 0.45f
        val ovalLeft = (canvasWidth - ovalWidth) / 2
        val ovalTop = canvasHeight * 0.15f

        // Draw semi-transparent overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // Cut out oval (transparent hole)
        drawOval(
            color = Color.Transparent,
            topLeft = Offset(ovalLeft, ovalTop),
            size = Size(ovalWidth, ovalHeight),
            blendMode = BlendMode.Clear
        )

        // Draw oval border (green/teal to indicate alignment area)
        drawOval(
            color = Green600,
            topLeft = Offset(ovalLeft, ovalTop),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

/**
 * Top bar with back button
 */
@Composable
private fun TopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.m)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

/**
 * Instruction text overlay
 */
@Composable
private fun InstructionText() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 420.dp) // Position below oval
        ) {
            Text(
                text = "Align your face within the oval",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Ensure good lighting for best results",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Circular capture button with coral accent
 */
@Composable
private fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Coral400
        )
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Capture",
            modifier = Modifier.size(32.dp),
            tint = Color.White
        )
    }
}

/**
 * Image review screen with Retake/Analyze buttons
 */
@Composable
private fun ImageReviewScreen(
    bitmap: Bitmap,
    isAnalyzing: Boolean,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Display captured image
        AndroidView(
            factory = { context ->
                android.widget.ImageView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(bitmap)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar
        TopBar(onBack = {
            onRetake()
            onBack()
        })

        // Bottom action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    color = Teal600,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = "Analyzing your skin...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "100% on-device processing",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Review your photo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Spacing.m))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    OutlinedButton(
                        onClick = onRetake,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Retake")
                    }
                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Teal600
                        )
                    ) {
                        Text("Analyze")
                    }
                }
            }
        }
    }
}

/**
 * Capture image from CameraX and convert to Bitmap
 */
private fun captureImage(
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val bitmap = imageProxyToBitmap(imageProxy)
                imageProxy.close()

                // Mirror the image for front camera (selfie mode)
                val mirroredBitmap = mirrorBitmap(bitmap)
                bitmap.recycle()

                onImageCaptured(mirroredBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

/**
 * Convert ImageProxy to Bitmap
 */
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    // Rotate if needed based on image rotation
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    return if (rotationDegrees != 0) {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it != bitmap) bitmap.recycle()
        }
    } else {
        bitmap
    }
}

/**
 * Mirror bitmap horizontally (for front camera selfie mode)
 */
private fun mirrorBitmap(bitmap: Bitmap): Bitmap {
    val matrix = Matrix().apply {
        preScale(-1f, 1f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
