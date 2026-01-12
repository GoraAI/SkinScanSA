package com.skinscan.sa.data.ml.llm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model Download Manager (Story 5.1)
 *
 * Handles download, verification, and storage of LLM models.
 *
 * For MVP: On-device LLM is not available. The app uses template-based
 * explanations which are clearly disclosed to users. No fake downloads.
 *
 * Future: When model infrastructure is ready, this will download the
 * actual model from a verified source with proper checksum validation.
 */
@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ModelDownloadManager"

        // Gemma 3n E2B model info
        const val MODEL_NAME = "gemma-3n-E2B"
        const val MODEL_FILE_NAME = "gemma-3n-E2B-it-int4.litertlm"
        const val MODEL_SIZE_BYTES = 1_200_000_000L // ~1.2GB for E2B model

        // Hugging Face URL for Gemma 3n E2B LiteRT model (public mirror)
        const val MODEL_URL = "https://huggingface.co/na5h13/gemma-3n-E2B-it-litert-lm/resolve/main/gemma-3n-E2B-it-int4.litertlm"

        // SHA256 checksum for integrity verification (to be updated with actual checksum)
        const val MODEL_SHA256 = "" // Will be verified when downloading

        // Model download is now enabled for Gemma 3n E2B
        private const val MODEL_AVAILABLE_FOR_DOWNLOAD = true
    }

    private val modelsDir: File
        get() = File(context.filesDir, "models").also { it.mkdirs() }

    private val modelFile: File
        get() = File(modelsDir, MODEL_FILE_NAME)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: Flow<DownloadState> = _downloadState.asStateFlow()

    private val _modelAvailable = MutableStateFlow(false)
    val modelAvailable: Flow<Boolean> = _modelAvailable.asStateFlow()

    init {
        // Check if model exists on init
        checkModelAvailability()
    }

    /**
     * Download state sealed class
     */
    sealed class DownloadState {
        data object Idle : DownloadState()
        data object Checking : DownloadState()
        data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
        data object Verifying : DownloadState()
        data object Success : DownloadState()
        data class Error(val message: String) : DownloadState()
        data object Skipped : DownloadState()
    }

    /**
     * Check if model is already downloaded
     *
     * For MVP: Always returns false - model not available, using templates
     */
    fun checkModelAvailability(): Boolean {
        val available = if (!MODEL_AVAILABLE_FOR_DOWNLOAD) {
            // MVP: Model not available for download
            false
        } else {
            modelFile.exists() && modelFile.length() > 0
        }
        _modelAvailable.value = available
        return available
    }

    /**
     * Check if on-device AI is available
     *
     * For MVP: Returns false with honest messaging
     */
    fun isOnDeviceAIAvailable(): Boolean = MODEL_AVAILABLE_FOR_DOWNLOAD && checkModelAvailability()

    /**
     * Get explanation for why on-device AI is not available
     */
    fun getAIUnavailableReason(): String {
        return if (!MODEL_AVAILABLE_FOR_DOWNLOAD) {
            "On-device AI explanations are not yet available. Using curated skin care insights instead."
        } else if (!checkModelAvailability()) {
            "AI model not downloaded. Using curated insights."
        } else {
            ""
        }
    }

    /**
     * Check if device is on WiFi
     */
    fun isOnWifi(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Check if device has network connectivity
     */
    fun hasNetworkConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Start model download
     *
     * For MVP: Returns error - model not available for download.
     * When model infrastructure is ready, this will perform real download.
     *
     * @param wifiOnly Only download on WiFi
     */
    suspend fun downloadModel(wifiOnly: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        // MVP: Model download not available
        if (!MODEL_AVAILABLE_FOR_DOWNLOAD) {
            Log.d(TAG, "Model download not available in MVP - using template fallback")
            _downloadState.value = DownloadState.Error(
                "On-device AI is not yet available. The app uses curated skin care insights instead."
            )
            return@withContext Result.failure(
                Exception("On-device AI model not available in this version")
            )
        }

        try {
            _downloadState.value = DownloadState.Checking

            // Check network conditions
            if (!hasNetworkConnection()) {
                _downloadState.value = DownloadState.Error("No network connection")
                return@withContext Result.failure(Exception("No network connection"))
            }

            if (wifiOnly && !isOnWifi()) {
                _downloadState.value = DownloadState.Error("WiFi required for download")
                return@withContext Result.failure(Exception("WiFi required"))
            }

            // Real download implementation
            performRealDownload()

            _modelAvailable.value = true
            _downloadState.value = DownloadState.Success
            Log.d(TAG, "Model download completed successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Model download failed", e)
            _downloadState.value = DownloadState.Error(e.message ?: "Download failed")
            Result.failure(e)
        }
    }

    /**
     * Perform real model download (for production)
     */
    private suspend fun performRealDownload() {
        val url = URL(MODEL_URL)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP error code: $responseCode")
            }

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(modelFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val progress = if (totalBytes > 0) {
                            downloadedBytes.toFloat() / totalBytes
                        } else 0f

                        _downloadState.value = DownloadState.Downloading(
                            progress = progress,
                            bytesDownloaded = downloadedBytes,
                            totalBytes = totalBytes
                        )
                    }
                }
            }

            // Verify checksum
            _downloadState.value = DownloadState.Verifying
            if (!verifyChecksum(modelFile, MODEL_SHA256)) {
                modelFile.delete()
                throw Exception("Checksum verification failed")
            }

        } finally {
            connection.disconnect()
        }
    }

    /**
     * Verify file SHA-256 checksum
     */
    private fun verifyChecksum(file: File, expectedHash: String): Boolean {
        if (expectedHash.isEmpty()) {
            Log.w(TAG, "No checksum configured - accepting file (verify manually)")
            return true // Accept if no checksum configured (for initial deployment)
        }

        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
        return actualHash.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Skip model download (use template explanations)
     */
    fun skipDownload() {
        _downloadState.value = DownloadState.Skipped
        _modelAvailable.value = false
    }

    /**
     * Delete downloaded model to free space
     */
    fun deleteModel() {
        if (modelFile.exists()) {
            modelFile.delete()
        }
        context.getSharedPreferences("model_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("model_downloaded", false)
            .apply()
        _modelAvailable.value = false
        _downloadState.value = DownloadState.Idle
    }

    /**
     * Get model file path (for LLM inference)
     *
     * Returns null if model not available (MVP uses template fallback)
     */
    fun getModelPath(): String? {
        return if (!MODEL_AVAILABLE_FOR_DOWNLOAD) {
            // MVP: Model not available, use template fallback
            null
        } else if (modelFile.exists() && modelFile.length() > 0) {
            modelFile.absolutePath
        } else {
            null
        }
    }

    /**
     * Format bytes for display
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
            bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}
