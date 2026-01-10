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
 * Handles download, verification, and storage of Gemma 3n LLM model.
 * For MVP, we use a mock implementation since the actual model requires
 * significant storage and download infrastructure.
 */
@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ModelDownloadManager"

        // Model info (actual Gemma 3n would be ~529MB)
        // For MVP, we simulate the download and use template-based explanations
        const val MODEL_NAME = "gemma-3n-4bit"
        const val MODEL_FILE_NAME = "gemma-3n.litertlm"
        const val MODEL_SIZE_BYTES = 529_000_000L // 529MB
        const val MODEL_URL = "https://huggingface.co/google/gemma-3n-e4b/resolve/main/gemma-3n-4bit.litertlm"
        const val MODEL_SHA256 = "placeholder_sha256_checksum" // Would be actual checksum

        // For MVP, we'll mark model as available after simulated download
        private const val MVP_MOCK_MODEL = true
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
     */
    fun checkModelAvailability(): Boolean {
        val available = if (MVP_MOCK_MODEL) {
            // For MVP, check if we've previously "downloaded" (simulated)
            context.getSharedPreferences("model_prefs", Context.MODE_PRIVATE)
                .getBoolean("model_downloaded", false)
        } else {
            modelFile.exists() && modelFile.length() > 0
        }
        _modelAvailable.value = available
        return available
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
     * For MVP, this simulates the download process.
     * In production, this would download the actual Gemma 3n model.
     *
     * @param wifiOnly Only download on WiFi
     */
    suspend fun downloadModel(wifiOnly: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
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

            if (MVP_MOCK_MODEL) {
                // Simulate download for MVP
                simulateDownload()
            } else {
                // Real download implementation
                performRealDownload()
            }

            // Mark as downloaded
            context.getSharedPreferences("model_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("model_downloaded", true)
                .apply()

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
     * Simulate download for MVP (no actual model file)
     */
    private suspend fun simulateDownload() {
        val totalBytes = MODEL_SIZE_BYTES
        var downloaded = 0L
        val chunkSize = totalBytes / 20 // 20 progress updates

        while (downloaded < totalBytes) {
            // Simulate download progress
            kotlinx.coroutines.delay(100) // 100ms per chunk = 2 seconds total
            downloaded = (downloaded + chunkSize).coerceAtMost(totalBytes)
            val progress = downloaded.toFloat() / totalBytes
            _downloadState.value = DownloadState.Downloading(
                progress = progress,
                bytesDownloaded = downloaded,
                totalBytes = totalBytes
            )
        }

        _downloadState.value = DownloadState.Verifying
        kotlinx.coroutines.delay(500) // Simulate verification
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
        if (expectedHash == "placeholder_sha256_checksum") {
            // Skip verification for MVP placeholder
            return true
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
     */
    fun getModelPath(): String? {
        return if (MVP_MOCK_MODEL) {
            // For MVP, return null to indicate template fallback
            null
        } else if (modelFile.exists()) {
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
