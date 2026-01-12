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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.GlassSurface
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite
import com.skinscan.sa.ui.theme.glassSurface
import java.util.concurrent.Executors

private const val TAG = "ScanScreen"

/**
 * Scan Screen - Glow Guide Design
 *
 * Features glassmorphism overlays and teal accent styling
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
            containerColor = GlassSurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = TealAccent
                )
            },
            title = { Text("Camera Access Needed", color = TextWhite) },
            text = {
                Text(
                    "Glow Guide needs camera access to capture your face for skin analysis. Your image is processed on your device and never uploaded.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        cameraPermissionState.launchPermissionRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealAccent,
                        contentColor = DarkBackground
                    )
                ) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    onNavigateToHome()
                }) {
                    Text("Not Now", color = TextSecondary)
                }
            }
        )
    }

    // Settings Dialog (for permanently denied)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = GlassSurface,
            title = { Text("Camera Permission Denied", color = TextWhite) },
            text = {
                Text(
                    "Please enable camera access in Settings > Apps > Glow Guide > Permissions",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealAccent,
                        contentColor = DarkBackground
                    )
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    onNavigateToHome()
                }) {
                    Text("Cancel", color = TextSecondary)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GlassSurface.copy(alpha = 0.6f))
                        .border(1.dp, TealAccent.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = TealAccent
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.l))
                Text(
                    text = "Camera access is required for face scanning",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite,
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealAccent,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable Camera")
                }
                TextButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.padding(top = Spacing.s)
                ) {
                    Text("Return to Home", color = TextSecondary)
                }
            }
        }
    }
}

/**
 * Face Scan Camera Screen with CameraX Preview
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
                    .padding(bottom = Spacing.xxl + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
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
                        viewModel.analyzeFace(bitmap)
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
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Oval dimensions - positioned in upper-center area for face
        val ovalWidth = canvasWidth * 0.7f
        val ovalHeight = canvasHeight * 0.45f
        val ovalLeft = (canvasWidth - ovalWidth) / 2
        val ovalTop = canvasHeight * 0.15f

        // Draw semi-transparent overlay with dark background
        drawRect(
            color = DarkBackground.copy(alpha = 0.7f),
            size = size
        )

        // Cut out oval (transparent hole)
        drawOval(
            color = Color.Transparent,
            topLeft = Offset(ovalLeft, ovalTop),
            size = Size(ovalWidth, ovalHeight),
            blendMode = BlendMode.Clear
        )

        // Draw oval border (teal to match theme)
        drawOval(
            color = TealAccent,
            topLeft = Offset(ovalLeft, ovalTop),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 3.dp.toPx())
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
                .glassSurface(cornerRadius = 24.dp, alpha = 0.4f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextWhite
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
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Ensure good lighting for best results",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Circular capture button with teal accent
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
            containerColor = TealAccent
        )
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Capture",
            modifier = Modifier.size(32.dp),
            tint = DarkBackground
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
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .glassSurface(cornerRadius = 24.dp)
                .padding(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    color = TealAccent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = "Analyzing your skin...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "100% on-device processing",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = "Review your photo",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite
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
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retake")
                    }
                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealAccent,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
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

                // Crop to match the oval face guide region
                // This ensures only the face (not shoulders/chest) is used for analysis
                val croppedFaceBitmap = cropToFaceOval(mirroredBitmap)
                mirroredBitmap.recycle()

                onImageCaptured(croppedFaceBitmap)
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

/**
 * Crop bitmap to match the oval face guide region
 *
 * The oval guide uses these proportions (from FaceGuideOverlay):
 * - Width: 70% of screen width
 * - Height: 45% of screen height
 * - Position: centered horizontally, 15% from top
 *
 * We apply the same proportions to crop just the face area
 */
private fun cropToFaceOval(bitmap: Bitmap): Bitmap {
    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height

    // Calculate crop region using same proportions as FaceGuideOverlay
    val cropWidth = (bitmapWidth * 0.7f).toInt()
    val cropHeight = (bitmapHeight * 0.45f).toInt()
    val cropLeft = ((bitmapWidth - cropWidth) / 2f).toInt()
    val cropTop = (bitmapHeight * 0.15f).toInt()

    // Ensure we don't exceed bitmap bounds
    val safeLeft = cropLeft.coerceIn(0, bitmapWidth - 1)
    val safeTop = cropTop.coerceIn(0, bitmapHeight - 1)
    val safeWidth = cropWidth.coerceAtMost(bitmapWidth - safeLeft)
    val safeHeight = cropHeight.coerceAtMost(bitmapHeight - safeTop)

    return Bitmap.createBitmap(bitmap, safeLeft, safeTop, safeWidth, safeHeight)
}
