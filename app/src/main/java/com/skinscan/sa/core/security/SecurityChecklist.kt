package com.skinscan.sa.core.security

/**
 * Security Checklist (Story 6.6)
 *
 * Documents all security controls implemented for POPIA compliance
 * and penetration testing readiness.
 */
object SecurityChecklist {

    /**
     * POPIA Compliance Controls
     */
    object POPIACompliance {
        /**
         * Section 19: Security Safeguards
         * - SQLCipher AES-256-GCM database encryption ✓
         * - Android KeyStore hardware-backed key storage ✓
         */
        const val DATABASE_ENCRYPTION = true

        /**
         * Section 26: Local Processing Only
         * - Face images stored in RAM only ✓
         * - No disk persistence of biometric data ✓
         * - No network transmission of images ✓
         * - FLAG_SECURE prevents screenshots ✓
         */
        const val LOCAL_PROCESSING_ONLY = true

        /**
         * Section 24: Right to Deletion
         * - DeleteAllUserDataUseCase implemented ✓
         * - Comprehensive data deletion (DB, files, preferences) ✓
         * - Audit log retention (legal requirement) ✓
         */
        const val RIGHT_TO_DELETION = true

        /**
         * POPIA Consent Management
         * - Consent checkboxes not pre-checked ✓
         * - Full consent text stored with acceptance ✓
         * - Immutable audit log of all consent events ✓
         * - Device info captured for legal proof ✓
         */
        const val CONSENT_MANAGEMENT = true
    }

    /**
     * Security Controls (NFR-SEC requirements)
     */
    object SecurityControls {
        /**
         * NFR-SEC01: No Image Transmission
         * - Bitmap never passed to network code ✓
         * - Static analysis validation ready ✓
         * - Integration test for zero network calls ✓
         */
        const val NO_IMAGE_TRANSMISSION = true

        /**
         * NFR-SEC02: Database Encryption
         * - SQLCipher 4.6.x integration ✓
         * - AES-256-GCM encryption ✓
         * - 256-bit random passphrase ✓
         * - KeyStore-protected passphrase ✓
         */
        const val DATABASE_ENCRYPTION = true

        /**
         * NFR-SEC03: Secure Data Deletion
         * - Room entity deletion ✓
         * - File system cleanup ✓
         * - SharedPreferences cleanup ✓
         * - DataStore cleanup ✓
         */
        const val SECURE_DELETION = true

        /**
         * NFR-SEC04: Certificate Pinning
         * - Network security config ✓
         * - Cleartext traffic disabled ✓
         * - OkHttp CertificatePinner infrastructure ✓
         * - Pins added when real API integrated
         */
        const val CERTIFICATE_PINNING_INFRASTRUCTURE = true

        /**
         * NFR-SEC05: Model Integrity
         * - SHA-256 checksum verification ✓
         * - Tamper detection ✓
         * - Corrupted file deletion ✓
         */
        const val MODEL_INTEGRITY = true
    }

    /**
     * Penetration Testing Checklist
     */
    object PenTestChecklist {
        val checklist = listOf(
            // POPIA Tests
            "[ ] Face scan makes zero network calls",
            "[ ] Database is encrypted with SQLCipher",
            "[ ] Consent checkboxes not pre-checked",
            "[ ] Delete all data is comprehensive",
            "[ ] Audit logs are immutable",

            // Security Tests
            "[ ] Certificate pinning prevents MITM (when enabled)",
            "[ ] Model hash mismatch detected",
            "[ ] SQL injection blocked (parameterized queries)",
            "[ ] FLAG_SECURE prevents screenshots",
            "[ ] No face image in logcat output",

            // Manual Tests
            "[ ] Root detection bypass attempts",
            "[ ] APK reverse engineering (ProGuard obfuscation)",
            "[ ] Database extraction from rooted device",
            "[ ] Network interception (certificate pinning)",
            "[ ] Model file tampering (hash verification)"
        )
    }

    /**
     * Attack Surface Documentation
     */
    object AttackSurface {
        val exposedComponents = listOf(
            "MainActivity (exported=true) - Entry point only",
            "Network: HTTPS only, certificate pinning ready",
            "Storage: SQLCipher encrypted database",
            "Memory: Face images in RAM only, cleared after use"
        )

        val mitigations = mapOf(
            "Data at rest" to "SQLCipher AES-256-GCM",
            "Data in transit" to "HTTPS only, cert pinning ready",
            "Data in memory" to "Cleared after use, FLAG_SECURE",
            "Model tampering" to "SHA-256 verification",
            "Consent bypass" to "Server-side check (future), audit log"
        )
    }
}
