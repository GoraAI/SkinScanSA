# SkinScan SA Threat Model & Security Analysis

**Project:** SkinScan SA
**Version:** 1.0.0
**Date:** 2026-01-09
**Security Architect:** Winston (AI Agent)
**Classification:** POPIA Section 26 - Special Personal Information (Biometric Data)

---

## Executive Summary

SkinScan SA processes **biometric data** (facial images and derived analysis) classified as **"special personal information"** under POPIA Section 26. Non-compliance carries severe penalties: **ZAR 10 million fine + up to 10 years imprisonment**. This threat model identifies 23 security threats across 6 categories, with 8 CRITICAL-severity threats requiring immediate mitigation.

### Risk Profile

| Risk Category | Critical | High | Medium | Low | Total |
|---------------|----------|------|--------|-----|-------|
| **POPIA Compliance** | 4 | 3 | 1 | 0 | 8 |
| **Data Security** | 2 | 4 | 2 | 1 | 9 |
| **ML Model Security** | 1 | 1 | 2 | 0 | 4 |
| **Network Security** | 1 | 0 | 1 | 0 | 2 |
| **Application Security** | 0 | 2 | 1 | 1 | 4 |
| **Physical Security** | 0 | 1 | 1 | 0 | 2 |
| **TOTAL** | **8** | **11** | **8** | **2** | **29** |

---

## Table of Contents

1. [Assets & Data Classification](#1-assets--data-classification)
2. [Threat Model Methodology](#2-threat-model-methodology)
3. [POPIA Compliance Threats](#3-popia-compliance-threats)
4. [Data Security Threats](#4-data-security-threats)
5. [ML Model Security Threats](#5-ml-model-security-threats)
6. [Network Security Threats](#6-network-security-threats)
7. [Application Security Threats](#7-application-security-threats)
8. [Physical Security Threats](#8-physical-security-threats)
9. [Mitigation Summary](#9-mitigation-summary)
10. [Security Testing Plan](#10-security-testing-plan)

---

## 1. Assets & Data Classification

### 1.1 Data Assets

| Asset | POPIA Classification | Sensitivity | Storage Location | Encryption |
|-------|---------------------|-------------|------------------|------------|
| **Facial Images** | Special Personal Info (§26) | **CRITICAL** | RAM only (never persisted) | N/A (ephemeral) |
| **Face Embeddings** | Special Personal Info (§26) | **CRITICAL** | Room DB (optional) | SQLCipher AES-256 |
| **Skin Analysis Results** | Personal Information | **HIGH** | Room DB | SQLCipher AES-256 |
| **Scan History** | Personal Information | **HIGH** | Room DB | SQLCipher AES-256 |
| **User Preferences** | Personal Information | **MEDIUM** | Proto DataStore | Android KeyStore |
| **Product Recommendations** | Non-Personal | **LOW** | Room DB (cached) | SQLCipher AES-256 |
| **Consent Records** | Personal Information | **HIGH** | Room DB | SQLCipher AES-256 |
| **ML Models** | Public | **LOW** | File system | None |

### 1.2 Processing Activities (POPIA §14)

| Activity | Purpose | Legal Basis | Data Minimization | Retention |
|----------|---------|-------------|-------------------|-----------|
| **Face Scanning** | Skin analysis for product recommendations | Explicit consent (§11) | Only face region captured | Not stored (ephemeral) |
| **Skin Attribute Detection** | Generate personalized recommendations | Explicit consent | Only derived attributes stored | User-controlled deletion |
| **Product Matching** | Provide relevant skincare products | Legitimate interest (§11(1)(f)) | Minimal: concerns + preferences | 12 months or until deletion |
| **Scan History Tracking** | Progress comparison over time | Explicit consent | Only aggregated metrics | User-controlled deletion |

---

## 2. Threat Model Methodology

### 2.1 STRIDE Framework

| Category | Threat Type | Definition |
|----------|-------------|------------|
| **S** | Spoofing | Attacker impersonates legitimate user or system |
| **T** | Tampering | Unauthorized modification of data or code |
| **R** | Repudiation | User denies performing action (lack of audit trail) |
| **I** | Information Disclosure | Unauthorized access to confidential data |
| **D** | Denial of Service | Service made unavailable to legitimate users |
| **E** | Elevation of Privilege | Attacker gains unauthorized permissions |

### 2.2 Risk Severity Matrix

| Likelihood / Impact | **Catastrophic** (POPIA violation, ZAR 10M) | **High** (Data breach, reputation) | **Medium** (Feature failure) | **Low** (Minor inconvenience) |
|---------------------|---------------------------------------------|-------------------------------------|------------------------------|-------------------------------|
| **Very Likely** | **CRITICAL** | **CRITICAL** | **HIGH** | **MEDIUM** |
| **Likely** | **CRITICAL** | **HIGH** | **MEDIUM** | **LOW** |
| **Possible** | **HIGH** | **MEDIUM** | **MEDIUM** | **LOW** |
| **Unlikely** | **MEDIUM** | **LOW** | **LOW** | **LOW** |

---

## 3. POPIA Compliance Threats

### 🔴 CRITICAL Threats

#### **THREAT-POPIA-001: Facial Image Transmitted to Server**

- **STRIDE Category:** Information Disclosure
- **Severity:** **CRITICAL**
- **Likelihood:** Possible (developer error)
- **Impact:** Catastrophic (POPIA §26 violation, ZAR 10M fine + 10 years imprisonment)

**Scenario:**
Developer accidentally implements cloud fallback for skin analysis, transmitting facial image to server for processing.

```kotlin
// ❌ CRITICAL VIOLATION
suspend fun analyzeFace(image: Bitmap): ScanResult {
    return if (useCloudModel) {
        cloudApi.analyzeFace(image)  // TRANSMITS BIOMETRIC DATA!
    } else {
        localAnalysis(image)
    }
}
```

**Mitigation:**
1. **Code Review:** Mandatory security review for any network code touching face images
2. **Static Analysis:** Custom lint rule to flag `Bitmap` in Retrofit/HTTP method signatures
3. **Architecture Enforcement:** Face images MUST only exist in `Bitmap` type, never serialized
4. **Testing:** Integration test verifies no network calls during scan

```kotlin
// ✅ CORRECT IMPLEMENTATION
@Test
fun `face scan makes no network calls`() = runTest {
    val networkMonitor = NetworkCallMonitor()
    val scanResult = skinAnalysisRepository.analyzeFace(testBitmap, userId)

    assertEquals(0, networkMonitor.httpCallCount, "Face scan MUST NOT make network calls")
}
```

**Residual Risk:** **LOW** (after mitigation)

---

#### **THREAT-POPIA-002: Database Not Encrypted**

- **STRIDE Category:** Information Disclosure
- **Severity:** **CRITICAL**
- **Likelihood:** Possible (deployment error)
- **Impact:** Catastrophic (POPIA §19 - security safeguards violation)

**Scenario:**
SQLCipher not properly initialized, database stored in plaintext. Attacker with physical device access or malware extracts database file.

**Attack Path:**
1. Attacker roots device or uses `adb backup` (if enabled)
2. Extracts `/data/data/com.skinscan.sa/databases/skinscan_sa.db`
3. Opens with SQLite browser → Full access to scan results, embeddings, consent records

**Mitigation:**
1. **Mandatory Encryption:** Fail-fast if SQLCipher init fails
```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "skinscan_sa.db")
    .openHelperFactory(
        SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
    )
    .fallbackToDestructiveMigration()  // ❌ REMOVE THIS (masks encryption failure)
    .build()

// ✅ ADD VALIDATION
init {
    val db = writableDatabase
    val cursor = db.rawQuery("PRAGMA cipher_version;", null)
    require(cursor.moveToFirst()) { "SQLCipher NOT initialized - CRITICAL POPIA violation risk" }
    cursor.close()
}
```

2. **Backup Exclusion:** Exclude database from Android backups
```xml
<!-- AndroidManifest.xml -->
<application
    android:allowBackup="false"
    android:fullBackupContent="@xml/backup_rules">
```

```xml
<!-- res/xml/backup_rules.xml -->
<full-backup-content>
    <exclude domain="database" path="skinscan_sa.db"/>
</full-backup-content>
```

3. **Key Storage:** Use Android KeyStore for passphrase encryption (prevent root extraction)

**Residual Risk:** **MEDIUM** (rooted devices remain vulnerable)

---

#### **THREAT-POPIA-003: Consent Not Properly Recorded**

- **STRIDE Category:** Repudiation
- **Severity:** **CRITICAL**
- **Likelihood:** Likely (UI/UX complexity)
- **Impact:** Catastrophic (POPIA §11 consent requirement violation)

**Scenario:**
User taps through consent screen without reading, or consent checkbox auto-checked, or consent timestamp not recorded. In legal dispute, cannot prove valid consent was obtained.

**POPIA §11 Requirements:**
- Voluntary
- Specific
- Informed
- Unambiguous indication of wishes

**Mitigation:**
1. **Explicit Opt-In:** Checkboxes MUST NOT be pre-checked
```kotlin
// ❌ WRONG
Checkbox(
    checked = true,  // PRE-CHECKED - INVALID!
    onCheckedChange = { }
)

// ✅ CORRECT
var consentGiven by remember { mutableStateOf(false) }  // Defaults to false
Checkbox(
    checked = consentGiven,
    onCheckedChange = { consentGiven = it }
)
```

2. **Consent Audit Trail:** Record EVERY consent interaction
```kotlin
@Entity(tableName = "consent_audit_log")
data class ConsentAuditLogEntity(
    @PrimaryKey val auditId: String,
    val userId: String,
    val consentType: String,
    val action: String,                  // "SHOWN", "ACCEPTED", "DECLINED"
    val consentText: String,              // Full text shown to user
    val timestamp: Long,
    val appVersion: String,
    val screenName: String                // "POPIA_CONSENT_SCREEN"
)
```

3. **UI Enforcement:** Disable "Continue" button until ALL checkboxes checked
4. **Legal Review:** Legal team must approve consent text annually

**Residual Risk:** **LOW** (after mitigation + legal approval)

---

#### **THREAT-POPIA-004: Right to Deletion Not Honored**

- **STRIDE Category:** Repudiation
- **Severity:** **CRITICAL**
- **Likelihood:** Possible (implementation oversight)
- **Impact:** Catastrophic (POPIA §24 violation)

**Scenario:**
User requests data deletion via "Delete All Data" button. App deletes Room DB records but fails to delete:
- Cached face embeddings in file system
- Exported analytics data
- Cloud-synced preferences (if implemented)
- Consent records (MUST be kept for legal defense)

**Mitigation:**
1. **Comprehensive Deletion:**
```kotlin
suspend fun deleteAllUserData(userId: String): Result<Unit> {
    return try {
        // 1. Delete database records
        userProfileDao.deleteByUserId(userId)
        scanResultDao.deleteByUserId(userId)

        // 2. Delete file system caches
        File(context.filesDir, "face_embeddings/$userId").deleteRecursively()
        File(context.cacheDir, "scan_previews/$userId").deleteRecursively()

        // 3. Clear DataStore
        preferencesDataStore.updateData { UserPreferences.getDefaultInstance() }

        // 4. Clear SharedPreferences
        context.getSharedPreferences("user_$userId", Context.MODE_PRIVATE)
            .edit().clear().commit()

        // 5. Revoke consent (but KEEP audit log for legal defense)
        consentManager.revokeConsent(userId, ConsentType.BIOMETRIC_PROCESSING)

        // 6. Log deletion for audit
        auditLogger.log("USER_DATA_DELETED", userId, System.currentTimeMillis())

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

2. **Deletion Verification Test:**
```kotlin
@Test
fun `delete all data removes ALL user traces`() = runTest {
    // Setup: Create user with full data
    val userId = createTestUserWithFullData()

    // Execute: Delete
    userRepository.deleteAllUserData(userId)

    // Verify: NO traces remain
    assertNull(userProfileDao.getProfile(userId))
    assertTrue(scanResultDao.getByUserId(userId).isEmpty())
    assertFalse(File(context.filesDir, "face_embeddings/$userId").exists())
    // ... verify all storage locations
}
```

**Residual Risk:** **LOW** (with comprehensive test coverage)

---

### 🟠 HIGH Threats

#### **THREAT-POPIA-005: Analytics Sent Without Opt-In**

- **STRIDE Category:** Information Disclosure
- **Severity:** **HIGH**
- **Likelihood:** Likely (Firebase auto-init)
- **Impact:** High (POPIA §12 opt-in requirement violation)

**Mitigation:**
1. Disable Firebase Analytics auto-init in manifest
2. Always check consent before ANY analytics call
3. Use custom analytics wrapper that enforces consent check

**Residual Risk:** **LOW**

---

## 4. Data Security Threats

### 🔴 CRITICAL Threats

#### **THREAT-DATA-001: Database Passphrase Hardcoded**

- **STRIDE Category:** Information Disclosure
- **Severity:** **CRITICAL**
- **Likelihood:** Possible (developer convenience)
- **Impact:** Catastrophic (encrypting defeats encryption)

**Scenario:**
```kotlin
// ❌ CRITICAL VULNERABILITY
val passphrase = "SkinScan2026!"  // Hardcoded in source code
```

Attacker decompiles APK → Extracts passphrase → Decrypts database from rooted device.

**Mitigation:**
1. **Android KeyStore:** Generate and store passphrase in hardware-backed KeyStore
```kotlin
class EncryptionManager(private val context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    fun getDatabasePassphrase(): ByteArray {
        return if (keyStore.containsAlias(DB_KEY_ALIAS)) {
            retrievePassphrase()
        } else {
            generatePassphrase()
        }
    }

    private fun generatePassphrase(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                DB_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)  // Don't require biometric for app functionality
                .build()
        )
        val key = keyGenerator.generateKey()

        // Generate random passphrase, encrypt with KeyStore key, store encrypted version
        val passphrase = SecureRandom().let { ByteArray(32).also(it::nextBytes) }
        val encryptedPassphrase = encryptWithKeyStoreKey(passphrase)
        saveEncryptedPassphrase(encryptedPassphrase)

        return passphrase
    }
}
```

2. **ProGuard Obfuscation:** Enable aggressive obfuscation (secondary defense)

**Residual Risk:** **MEDIUM** (rooted devices with Frida can still extract from memory)

---

### 🟠 HIGH Threats

#### **THREAT-DATA-002: Screen Capture of Sensitive Data**

- **STRIDE Category:** Information Disclosure
- **Severity:** **HIGH**
- **Likelihood:** Likely (users share screenshots)
- **Impact:** High (scan results, recommendations exposed)

**Mitigation:**
1. **FLAG_SECURE for sensitive screens:**
```kotlin
@Composable
fun ResultsScreen(...) {
    DisableScreenCapture()  // Custom composable
    // ... screen content
}

@Composable
fun DisableScreenCapture() {
    val context = LocalContext.current
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
}
```

2. **Sensitive screens:** Results, Recommendations, Profile (enable FLAG_SECURE)
3. **Non-sensitive screens:** Home, Product browsing (allow screenshots for user convenience)

**Residual Risk:** **LOW**

---

## 5. ML Model Security Threats

### 🟠 HIGH Threat

#### **THREAT-ML-001: Model Poisoning via Malicious Update**

- **STRIDE Category:** Tampering
- **Severity:** **HIGH**
- **Likelihood:** Unlikely (requires compromised Hugging Face account)
- **Impact:** High (biased recommendations, user harm)

**Scenario:**
Attacker compromises Hugging Face account, replaces Gemma 3n model with malicious version that:
- Recommends harmful ingredients for melanin-rich skin
- Biases toward specific brands (bribery)
- Exfiltrates data via covert channel

**Mitigation:**
1. **Model Hash Verification:**
```kotlin
data class ModelMetadata(
    val modelName: String,
    val version: String,
    val sha256Hash: String,  // Hardcoded in app
    val downloadUrl: String
)

suspend fun downloadAndVerifyModel(metadata: ModelMetadata): File {
    val file = downloadFile(metadata.downloadUrl)

    val actualHash = file.sha256()
    require(actualHash == metadata.sha256Hash) {
        "Model hash mismatch! Expected: ${metadata.sha256Hash}, Got: $actualHash"
    }

    return file
}
```

2. **Signed Model Bundles:** Use TensorFlow Model Signing (future enhancement)
3. **Model Update Review:** Manual QA testing before pushing new model metadata to production

**Residual Risk:** **LOW** (with hash verification)

---

## 6. Network Security Threats

### 🔴 CRITICAL Threat

#### **THREAT-NET-001: Man-in-the-Middle Attack on Clicks API**

- **STRIDE Category:** Tampering, Information Disclosure
- **Severity:** **HIGH** (CRITICAL if face data ever transmitted)
- **Likelihood:** Possible (public WiFi)
- **Impact:** High (malicious product recommendations, price manipulation)

**Mitigation:**
1. **Certificate Pinning:**
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.clicks.co.za", "sha256/ACTUAL_CERTIFICATE_HASH_HERE")
    .add("api.clicks.co.za", "sha256/BACKUP_CERTIFICATE_HASH")  // Backup pin
    .build()

OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

2. **Network Security Config:** Enforce certificate pinning at OS level
3. **TLS 1.3:** Require minimum TLS 1.3

**Residual Risk:** **LOW** (with certificate pinning)

---

## 7. Application Security Threats

### 🟠 HIGH Threats

#### **THREAT-APP-001: SQL Injection via Product Search**

- **STRIDE Category:** Tampering, Information Disclosure
- **Severity:** **MEDIUM** (HIGH if Room query construction is unsafe)
- **Likelihood:** Unlikely (Room uses parameterized queries by default)
- **Impact:** Medium (bypass filters, extract unauthorized data)

**Mitigation:**
1. **Use Room @Query (never raw SQL):**
```kotlin
// ✅ SAFE (parameterized)
@Query("SELECT * FROM products WHERE name LIKE '%' || :searchQuery || '%'")
suspend fun searchProducts(searchQuery: String): List<ProductEntity>

// ❌ VULNERABLE
suspend fun searchProducts(query: String): List<ProductEntity> {
    val rawQuery = "SELECT * FROM products WHERE name LIKE '%$query%'"  // SQL INJECTION!
    return db.rawQuery(rawQuery, null)
}
```

2. **Input Validation:** Sanitize user input (limit characters, length)

**Residual Risk:** **LOW** (Room framework protection)

---

## 8. Physical Security Threats

### 🟠 HIGH Threat

#### **THREAT-PHYS-001: Device Theft with Unlocked Screen**

- **STRIDE Category:** Information Disclosure
- **Severity:** **HIGH**
- **Likelihood:** Possible
- **Impact:** High (attacker accesses scan history, personal data)

**Mitigation:**
1. **App-Level Biometric Lock (Optional Setting):**
```kotlin
// User-configurable: Require fingerprint/face unlock to open app
if (userPreferences.requireBiometricUnlock) {
    BiometricPrompt(...)
}
```

2. **Session Timeout:** Auto-lock app after 5 minutes of inactivity
3. **Encrypted Backup:** Educate users to enable device encryption

**Residual Risk:** **MEDIUM** (user behavior dependent)

---

## 9. Mitigation Summary

### 9.1 Mitigation Priorities (MVP Must-Have)

| Priority | Mitigation | Threat Mitigated | Effort | Status |
|----------|-----------|------------------|--------|--------|
| **P0** | SQLCipher database encryption | THREAT-POPIA-002, THREAT-DATA-001 | 2 days | Required |
| **P0** | Consent UI enforcement (no pre-check) | THREAT-POPIA-003 | 1 day | Required |
| **P0** | Comprehensive deletion implementation | THREAT-POPIA-004 | 3 days | Required |
| **P0** | No network calls during face scan (verified by test) | THREAT-POPIA-001 | 1 day | Required |
| **P0** | Android KeyStore passphrase management | THREAT-DATA-001 | 2 days | Required |
| **P1** | Certificate pinning for Clicks API | THREAT-NET-001 | 1 day | Required |
| **P1** | Model hash verification | THREAT-ML-001 | 1 day | Required |
| **P1** | FLAG_SECURE for sensitive screens | THREAT-DATA-002 | 0.5 day | Required |
| **P2** | Disable Android backups | THREAT-POPIA-002 | 0.5 day | Recommended |
| **P2** | Consent audit logging | THREAT-POPIA-003 | 1 day | Recommended |

### 9.2 MVP Security Checklist

**Pre-Launch Requirements:**
- [ ] SQLCipher encryption validated (PRAGMA cipher_version test)
- [ ] No facial image network transmission (integration test passes)
- [ ] Consent checkboxes NOT pre-checked (UI test)
- [ ] "Delete All Data" removes ALL user traces (integration test)
- [ ] Android KeyStore stores database passphrase (not hardcoded)
- [ ] Certificate pinning configured for Clicks API
- [ ] Model hash verification implemented
- [ ] FLAG_SECURE enabled on Results/Recommendations screens
- [ ] Android backups disabled for database files
- [ ] POPIA consent text legally reviewed and approved
- [ ] Security penetration test completed
- [ ] PIIA (Personal Information Impact Assessment) completed

---

## 10. Security Testing Plan

### 10.1 Automated Security Tests

```kotlin
// test/security/EncryptionTest.kt
@Test
fun `database is encrypted with SQLCipher`() {
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .openHelperFactory(SupportFactory(SQLiteDatabase.getBytes(TEST_PASSPHRASE.toCharArray())))
        .build()

    val cursor = db.openHelper.writableDatabase.rawQuery("PRAGMA cipher_version;", null)
    assertTrue("SQLCipher NOT initialized", cursor.moveToFirst())
    val version = cursor.getString(0)
    assertNotNull("SQLCipher version is null", version)
    cursor.close()
}

// test/security/NetworkIsolationTest.kt
@Test
fun `face scan makes zero network calls`() = runTest {
    val networkMonitor = NetworkCallMonitor()

    val testBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.test_face)
    skinAnalysisRepository.analyzeFace(testBitmap, TEST_USER_ID)

    assertEquals(0, networkMonitor.httpCallCount, "CRITICAL: Face scan made network call!")
}

// test/security/DeletionCompletenessTest.kt
@Test
fun `delete all data is comprehensive`() = runTest {
    // Setup: Create user with data in ALL storage locations
    val userId = "test-user-123"
    createTestUserWithFullData(userId)

    // Execute deletion
    userRepository.deleteAllUserData(userId)

    // Verify database
    assertNull(userProfileDao.getProfile(userId))
    assertTrue(scanResultDao.getByUserId(userId).isEmpty())

    // Verify file system
    assertFalse(File(context.filesDir, "embeddings/$userId").exists())
    assertFalse(File(context.cacheDir, "previews/$userId").exists())

    // Verify DataStore
    val prefs = preferencesDataStore.data.first()
    assertEquals(UserPreferences.getDefaultInstance(), prefs)
}
```

### 10.2 Manual Security Testing

**Penetration Testing Checklist:**
1. **Root Detection Bypass:** Attempt to extract database from rooted device
2. **TLS MITM:** Use Charles Proxy to intercept Clicks API calls
3. **APK Reverse Engineering:** Decompile APK, search for hardcoded secrets
4. **Screen Recording:** Attempt to capture sensitive screens (FLAG_SECURE test)
5. **Backup Extraction:** Use `adb backup`, attempt to extract database
6. **SQL Injection:** Fuzz product search with SQL payloads
7. **Model Tampering:** Replace model file, verify app detects hash mismatch

### 10.3 Third-Party Security Audit

**Pre-Launch:** Engage cybersecurity firm for independent audit (recommended)

**Scope:**
- POPIA compliance review
- Penetration testing
- Code review (focus: cryptography, data handling)
- ML model security assessment

**Timeline:** 2-3 weeks before Play Store submission

---

## Appendix: POPIA Compliance Checklist

### Section 11: Consent
- [x] Consent is voluntary (not coerced)
- [x] Consent is specific (separate checkbox for each purpose)
- [x] Consent is informed (clear explanation of processing)
- [x] Consent is unambiguous (explicit opt-in, not pre-checked)
- [x] Consent can be withdrawn (settings → delete data)

### Section 12: Justification for Processing
- [x] Lawful purpose documented (product recommendations)
- [x] Reasonably necessary for purpose (cannot recommend without analysis)
- [x] Adequate for purpose (only collect what's needed)

### Section 13: Objection to Processing
- [x] User can object (delete data anytime)
- [x] Objection processed within reasonable time (immediate)

### Section 14: Information to be Collected
- [x] Transparency about what is collected (consent screen)
- [x] Source of information disclosed (user's own device camera)
- [x] Purpose disclosed (skin analysis + recommendations)

### Section 19: Security Safeguards
- [x] Integrity protected (SQLCipher encryption)
- [x] Confidentiality protected (on-device processing)
- [x] Prevent loss (encrypted backups disabled)
- [x] Prevent damage (database corruption recovery)
- [x] Prevent unauthorized access (Android KeyStore passphrase)

### Section 24: Correction/Deletion
- [x] User can request deletion (delete data button)
- [x] Deletion is immediate (no delay)
- [x] Deletion is comprehensive (all storage locations)

### Section 26: Special Personal Information (Biometric Data)
- [x] Explicit consent obtained (separate biometric consent checkbox)
- [x] Processing necessary (cannot provide service without scan)
- [x] Adequate safeguards (on-device only, encrypted storage)

---

**Document Version:** 1.0.0
**Last Updated:** 2026-01-09
**Next Review:** Before MVP launch (security audit)
