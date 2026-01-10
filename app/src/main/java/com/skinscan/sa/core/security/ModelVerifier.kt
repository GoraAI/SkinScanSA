package com.skinscan.sa.core.security

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model Verifier (Story 6.5)
 *
 * Verifies ML model integrity using SHA-256 checksums.
 * Protects against model tampering/poisoning attacks.
 */
@Singleton
class ModelVerifier @Inject constructor() {

    companion object {
        private const val TAG = "ModelVerifier"
        private const val BUFFER_SIZE = 8192
    }

    /**
     * Model metadata with expected hash
     */
    data class ModelMetadata(
        val modelName: String,
        val version: String,
        val downloadUrl: String,
        val sha256Hash: String,
        val fileSizeBytes: Long
    )

    /**
     * Registry of known models with their expected hashes
     */
    object ModelRegistry {
        // Gemma 3n LLM model (downloaded)
        val GEMMA_3N = ModelMetadata(
            modelName = "gemma-3n-e4b",
            version = "1.0",
            downloadUrl = "https://huggingface.co/google/gemma-3n-e4b/resolve/main/gemma-3n-4bit.litertlm",
            sha256Hash = "placeholder_sha256_checksum", // To be updated with actual hash
            fileSizeBytes = 529_000_000L // 529MB
        )

        // Skin analysis model (bundled in APK)
        val SKIN_ANALYSIS = ModelMetadata(
            modelName = "efficientnet-lite4-skin",
            version = "1.0",
            downloadUrl = "", // Bundled, no download URL
            sha256Hash = "placeholder_sha256_checksum", // To be updated with actual hash
            fileSizeBytes = 15_000_000L // ~15MB
        )
    }

    /**
     * Verification result
     */
    sealed class VerificationResult {
        data object Success : VerificationResult()
        data class HashMismatch(val expected: String, val actual: String) : VerificationResult()
        data class FileError(val message: String) : VerificationResult()
        data object SkippedPlaceholder : VerificationResult()
    }

    /**
     * Verify model file integrity
     *
     * @param file The model file to verify
     * @param expectedHash Expected SHA-256 hash
     * @return VerificationResult indicating success or failure
     */
    suspend fun verifyModelIntegrity(
        file: File,
        expectedHash: String
    ): VerificationResult = withContext(Dispatchers.IO) {
        try {
            // Skip verification for placeholder hashes (MVP mode)
            if (expectedHash == "placeholder_sha256_checksum") {
                Log.d(TAG, "Skipping verification for placeholder hash")
                return@withContext VerificationResult.SkippedPlaceholder
            }

            if (!file.exists()) {
                return@withContext VerificationResult.FileError("File does not exist: ${file.path}")
            }

            if (!file.canRead()) {
                return@withContext VerificationResult.FileError("Cannot read file: ${file.path}")
            }

            val actualHash = calculateSha256(file)

            if (actualHash.equals(expectedHash, ignoreCase = true)) {
                Log.d(TAG, "Model verification PASSED: ${file.name}")
                VerificationResult.Success
            } else {
                Log.e(TAG, "Model verification FAILED: ${file.name}")
                Log.e(TAG, "Expected: $expectedHash")
                Log.e(TAG, "Actual: $actualHash")
                VerificationResult.HashMismatch(expectedHash, actualHash)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Model verification error", e)
            VerificationResult.FileError(e.message ?: "Unknown error")
        }
    }

    /**
     * Verify model using metadata
     *
     * @param file The model file to verify
     * @param metadata Model metadata with expected hash
     * @return VerificationResult
     */
    suspend fun verifyModel(
        file: File,
        metadata: ModelMetadata
    ): VerificationResult {
        // First check file size
        if (file.exists() && file.length() != metadata.fileSizeBytes) {
            Log.w(TAG, "File size mismatch for ${metadata.modelName}")
            Log.w(TAG, "Expected: ${metadata.fileSizeBytes}, Actual: ${file.length()}")
            // Continue to hash verification anyway
        }

        return verifyModelIntegrity(file, metadata.sha256Hash)
    }

    /**
     * Calculate SHA-256 hash of a file
     *
     * @param file File to hash
     * @return Lowercase hex string of SHA-256 hash
     */
    suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")

        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Handle verification failure
     *
     * @param file The corrupted file to delete
     */
    fun handleVerificationFailure(file: File) {
        if (file.exists()) {
            Log.w(TAG, "Deleting corrupted model file: ${file.name}")
            file.delete()
        }
    }

    /**
     * Check if model verification is enabled (not using placeholder)
     */
    fun isVerificationEnabled(metadata: ModelMetadata): Boolean {
        return metadata.sha256Hash != "placeholder_sha256_checksum"
    }
}
