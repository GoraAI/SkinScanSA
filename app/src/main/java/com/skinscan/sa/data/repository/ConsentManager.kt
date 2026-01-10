package com.skinscan.sa.data.repository

import android.os.Build
import android.util.Log
import com.skinscan.sa.BuildConfig
import com.skinscan.sa.data.db.dao.ConsentAuditLogDao
import com.skinscan.sa.data.db.entity.ConsentAuditLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Consent Manager (Story 6.4)
 *
 * Manages consent recording and audit logging for POPIA compliance.
 * All consent events are logged to an immutable audit trail.
 */
@Singleton
class ConsentManager @Inject constructor(
    private val auditDao: ConsentAuditLogDao
) {
    companion object {
        private const val TAG = "ConsentManager"
    }

    /**
     * Consent type enum
     */
    enum class ConsentType {
        BIOMETRIC_PROCESSING,
        ANALYTICS
    }

    /**
     * Log consent screen shown event
     */
    suspend fun logConsentShown(
        userId: String,
        consentType: ConsentType,
        consentText: String,
        screenName: String = ConsentAuditLogEntity.SCREEN_POPIA_CONSENT
    ) = withContext(Dispatchers.IO) {
        val log = createAuditLog(
            userId = userId,
            eventType = ConsentAuditLogEntity.EVENT_SHOWN,
            consentType = consentType.name,
            screenName = screenName,
            consentText = consentText
        )
        auditDao.insert(log)
        Log.d(TAG, "Logged consent shown: $consentType for user $userId")
    }

    /**
     * Record consent acceptance with full audit trail
     */
    suspend fun recordConsentAccepted(
        userId: String,
        consentType: ConsentType,
        consentText: String,
        screenName: String = ConsentAuditLogEntity.SCREEN_POPIA_CONSENT
    ) = withContext(Dispatchers.IO) {
        val log = createAuditLog(
            userId = userId,
            eventType = ConsentAuditLogEntity.EVENT_ACCEPTED,
            consentType = consentType.name,
            screenName = screenName,
            consentText = consentText
        )
        auditDao.insert(log)
        Log.d(TAG, "Logged consent accepted: $consentType for user $userId")
    }

    /**
     * Log consent declined event
     */
    suspend fun logConsentDeclined(
        userId: String,
        consentType: ConsentType,
        screenName: String = ConsentAuditLogEntity.SCREEN_POPIA_CONSENT
    ) = withContext(Dispatchers.IO) {
        val log = createAuditLog(
            userId = userId,
            eventType = ConsentAuditLogEntity.EVENT_DECLINED,
            consentType = consentType.name,
            screenName = screenName,
            consentText = "User declined consent"
        )
        auditDao.insert(log)
        Log.d(TAG, "Logged consent declined: $consentType for user $userId")
    }

    /**
     * Log consent revocation event
     */
    suspend fun logConsentRevoked(
        userId: String,
        consentType: ConsentType,
        screenName: String = ConsentAuditLogEntity.SCREEN_SETTINGS
    ) = withContext(Dispatchers.IO) {
        val log = createAuditLog(
            userId = userId,
            eventType = ConsentAuditLogEntity.EVENT_REVOKED,
            consentType = consentType.name,
            screenName = screenName,
            consentText = "User revoked consent"
        )
        auditDao.insert(log)
        Log.d(TAG, "Logged consent revoked: $consentType for user $userId")
    }

    /**
     * Log data deletion event (POPIA Right to Deletion)
     */
    suspend fun logDataDeleted(
        userId: String,
        screenName: String = ConsentAuditLogEntity.SCREEN_PROFILE
    ) = withContext(Dispatchers.IO) {
        val log = createAuditLog(
            userId = userId,
            eventType = ConsentAuditLogEntity.EVENT_DATA_DELETED,
            consentType = "ALL_DATA",
            screenName = screenName,
            consentText = "User exercised right to deletion (POPIA Section 24)"
        )
        auditDao.insert(log)
        Log.d(TAG, "Logged data deletion for user $userId")
    }

    /**
     * Check if user has accepted biometric consent
     */
    suspend fun hasBiometricConsent(userId: String): Boolean =
        withContext(Dispatchers.IO) {
            auditDao.hasBiometricConsent(userId)
        }

    /**
     * Export audit log for legal compliance
     *
     * @param userId User ID to export logs for
     * @return CSV formatted string of audit logs
     */
    suspend fun exportAuditLog(userId: String): String = withContext(Dispatchers.IO) {
        val logs = auditDao.getByUserId(userId)

        val header = "timestamp,eventType,consentType,screenName,appVersion,deviceModel,osVersion"
        val rows = logs.joinToString("\n") { log ->
            "${log.timestamp},${log.eventType},${log.consentType},${log.screenName},${log.appVersion},${log.deviceModel},${log.osVersion}"
        }

        "$header\n$rows"
    }

    /**
     * Get all audit logs for a user
     */
    suspend fun getAuditLogs(userId: String): List<ConsentAuditLogEntity> =
        withContext(Dispatchers.IO) {
            auditDao.getByUserId(userId)
        }

    /**
     * Create audit log entry with device info
     */
    private fun createAuditLog(
        userId: String,
        eventType: String,
        consentType: String,
        screenName: String,
        consentText: String
    ): ConsentAuditLogEntity {
        return ConsentAuditLogEntity(
            auditId = UUID.randomUUID().toString(),
            userId = userId,
            eventType = eventType,
            consentType = consentType,
            screenName = screenName,
            consentText = consentText,
            timestamp = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            deviceModel = Build.MODEL,
            osVersion = Build.VERSION.SDK_INT
        )
    }
}
