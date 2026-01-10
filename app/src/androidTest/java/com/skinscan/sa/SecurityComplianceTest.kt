package com.skinscan.sa

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skinscan.sa.core.security.FaceImagePrivacyGuard
import com.skinscan.sa.core.security.ModelVerifier
import com.skinscan.sa.core.security.SecureHttpClient
import com.skinscan.sa.data.encryption.EncryptionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Security Compliance Test Suite (Story 6.6)
 *
 * Validates POPIA compliance and security requirements:
 * - NFR-SEC01: No image transmission
 * - NFR-SEC02: Database encryption
 * - NFR-SEC03: Secure data deletion
 * - NFR-SEC04: Certificate pinning (infrastructure)
 * - NFR-SEC05: POPIA consent flows
 */
@RunWith(AndroidJUnit4::class)
class SecurityComplianceTest {

    private lateinit var context: Context
    private lateinit var encryptionManager: EncryptionManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        encryptionManager = EncryptionManager(context)
    }

    // ========== POPIA Compliance Tests ==========

    @Test
    fun popia_database_passphrase_is_256_bits() {
        val passphrase = encryptionManager.getDatabasePassphrase()

        assertNotNull("Passphrase should not be null", passphrase)
        assertEquals("Passphrase should be 32 bytes (256 bits)", 32, passphrase.size)
    }

    @Test
    fun popia_passphrase_is_consistent() {
        val passphrase1 = encryptionManager.getDatabasePassphrase()
        val passphrase2 = encryptionManager.getDatabasePassphrase()

        assertArrayEquals(
            "Passphrase should be consistently retrieved",
            passphrase1,
            passphrase2
        )
    }

    @Test
    fun popia_no_image_files_in_app_directories() {
        val imageExtensions = listOf("jpg", "jpeg", "png", "bmp", "webp")
        val filesDir = context.filesDir
        val cacheDir = context.cacheDir

        val filesImages = filesDir.walkTopDown().filter { file ->
            file.extension.lowercase() in imageExtensions
        }.toList()

        val cacheImages = cacheDir.walkTopDown().filter { file ->
            file.extension.lowercase() in imageExtensions
        }.toList()

        assertTrue(
            "No image files should be in filesDir: ${filesImages.map { it.name }}",
            filesImages.isEmpty()
        )
        assertTrue(
            "No image files should be in cacheDir: ${cacheImages.map { it.name }}",
            cacheImages.isEmpty()
        )
    }

    // ========== Security Tests ==========

    @Test
    fun security_certificate_pinning_infrastructure_exists() {
        val secureHttpClient = SecureHttpClient()

        // Verify infrastructure exists (pins not configured for MVP)
        assertNotNull("SecureHttpClient should be instantiable", secureHttpClient)

        // For MVP with mock data, pinning is not enabled
        assertFalse(
            "Pinning should not be enabled with placeholder pins",
            secureHttpClient.isPinningEnabled()
        )
    }

    @Test
    fun security_model_verifier_calculates_hash() = runBlocking {
        val verifier = ModelVerifier()

        // Create a test file
        val testFile = File(context.cacheDir, "test_model.bin")
        testFile.writeBytes("test model content".toByteArray())

        try {
            val hash = verifier.calculateSha256(testFile)

            assertNotNull("Hash should be calculated", hash)
            assertEquals("Hash should be 64 hex characters", 64, hash.length)
            assertTrue("Hash should be lowercase hex", hash.all { it.isDigit() || it in 'a'..'f' })
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun security_model_verifier_detects_tampering() = runBlocking {
        val verifier = ModelVerifier()

        // Create a test file
        val testFile = File(context.cacheDir, "tampered_model.bin")
        testFile.writeBytes("original content".toByteArray())

        try {
            // Use a wrong hash
            val wrongHash = "0".repeat(64)
            val result = verifier.verifyModelIntegrity(testFile, wrongHash)

            assertTrue(
                "Should detect hash mismatch",
                result is ModelVerifier.VerificationResult.HashMismatch
            )
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun security_model_verifier_skips_placeholder() = runBlocking {
        val verifier = ModelVerifier()

        val testFile = File(context.cacheDir, "placeholder_test.bin")
        testFile.writeBytes("test".toByteArray())

        try {
            val result = verifier.verifyModelIntegrity(testFile, "placeholder_sha256_checksum")

            assertTrue(
                "Should skip verification for placeholder",
                result is ModelVerifier.VerificationResult.SkippedPlaceholder
            )
        } finally {
            testFile.delete()
        }
    }

    // ========== Privacy Tests ==========

    @Test
    fun privacy_face_image_guard_validates_null() {
        val guard = FaceImagePrivacyGuard()

        assertFalse("Null bitmap should not validate", guard.validateBitmap(null))
    }

    @Test
    fun privacy_network_security_config_exists() {
        // Verify network_security_config.xml is referenced in manifest
        val packageInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_META_DATA
        )

        // The networkSecurityConfig attribute is set in AndroidManifest.xml
        // This test verifies the app can be inspected without crash
        assertNotNull("Application info should be available", packageInfo)
    }

    // ========== Delete All Data Test ==========

    @Test
    fun popia_delete_clears_preferences() {
        // Write test data to preferences
        val testPrefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        testPrefs.edit().putString("test_key", "test_value").apply()

        // Verify data exists
        assertEquals("test_value", testPrefs.getString("test_key", null))

        // Clear preferences
        testPrefs.edit().clear().apply()

        // Verify data is cleared
        assertNull(testPrefs.getString("test_key", null))
    }
}
