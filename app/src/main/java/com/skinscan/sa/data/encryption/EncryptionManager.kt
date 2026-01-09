package com.skinscan.sa.data.encryption

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import java.security.SecureRandom

/**
 * Manages database encryption passphrase using Android KeyStore
 *
 * Security architecture:
 * 1. Generate 256-bit random passphrase
 * 2. Encrypt passphrase with AndroidKeyStore key (AES-256-GCM)
 * 3. Store encrypted passphrase in SharedPreferences
 * 4. Decrypt passphrase on-demand for database access
 *
 * POPIA Compliance: Biometric data encrypted with hardware-backed key (API 28+)
 */
@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val DB_KEY_ALIAS = "skinscan_db_passphrase_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_LENGTH = 32 // 256 bits

        private const val PREFS_NAME = "skinscan_encryption"
        private const val PREF_ENCRYPTED_PASSPHRASE = "encrypted_passphrase"
        private const val PREF_PASSPHRASE_IV = "passphrase_iv"
    }

    /**
     * Get database passphrase for SQLCipher
     * Generates new passphrase if not exists, otherwise retrieves existing one
     */
    fun getDatabasePassphrase(): ByteArray {
        return if (keyStore.containsAlias(DB_KEY_ALIAS)) {
            retrievePassphrase()
        } else {
            generateAndStorePassphrase()
        }
    }

    /**
     * Generate new 256-bit random passphrase and encrypt it with KeyStore key
     */
    private fun generateAndStorePassphrase(): ByteArray {
        // Generate 256-bit random passphrase using SecureRandom
        val passphrase = ByteArray(PASSPHRASE_LENGTH)
        SecureRandom().nextBytes(passphrase)

        // Generate KeyStore key if not exists
        if (!keyStore.containsAlias(DB_KEY_ALIAS)) {
            generateKeystoreKey()
        }

        // Encrypt passphrase with KeyStore key
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(DB_KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedPassphrase = cipher.doFinal(passphrase)
        val iv = cipher.iv

        // Store encrypted passphrase and IV in SharedPreferences
        sharedPreferences.edit()
            .putString(PREF_ENCRYPTED_PASSPHRASE, Base64.encodeToString(encryptedPassphrase, Base64.NO_WRAP))
            .putString(PREF_PASSPHRASE_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()

        return passphrase
    }

    /**
     * Retrieve and decrypt passphrase from secure storage
     */
    private fun retrievePassphrase(): ByteArray {
        // Get encrypted passphrase and IV from SharedPreferences
        val encryptedPassphraseB64 = sharedPreferences.getString(PREF_ENCRYPTED_PASSPHRASE, null)
            ?: throw IllegalStateException("Encrypted passphrase not found")
        val ivB64 = sharedPreferences.getString(PREF_PASSPHRASE_IV, null)
            ?: throw IllegalStateException("Passphrase IV not found")

        val encryptedPassphrase = Base64.decode(encryptedPassphraseB64, Base64.NO_WRAP)
        val iv = Base64.decode(ivB64, Base64.NO_WRAP)

        // Decrypt passphrase using KeyStore key
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(DB_KEY_ALIAS, null) as SecretKey
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        return cipher.doFinal(encryptedPassphrase)
    }

    /**
     * Generate AES-256-GCM key in AndroidKeyStore
     * Hardware-backed on devices with TEE/Secure Element (API 28+)
     */
    private fun generateKeystoreKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val keyGenSpec = KeyGenParameterSpec.Builder(
            DB_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(false) // No biometric lock for background DB access
            .build()

        keyGenerator.init(keyGenSpec)
        keyGenerator.generateKey()
    }
}
