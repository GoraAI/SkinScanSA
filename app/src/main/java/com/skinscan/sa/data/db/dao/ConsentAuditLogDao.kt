package com.skinscan.sa.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skinscan.sa.data.db.entity.ConsentAuditLogEntity

/**
 * Consent Audit Log DAO (Story 6.4)
 *
 * Insert-only DAO for consent audit logging.
 * No update or delete operations allowed (append-only for legal compliance).
 */
@Dao
interface ConsentAuditLogDao {

    /**
     * Insert audit log entry
     * Conflict strategy: IGNORE to prevent duplicates
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: ConsentAuditLogEntity)

    /**
     * Insert multiple audit log entries
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(logs: List<ConsentAuditLogEntity>)

    /**
     * Get all audit logs for a user (for export/legal review)
     */
    @Query("SELECT * FROM consent_audit_log WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getByUserId(userId: String): List<ConsentAuditLogEntity>

    /**
     * Get all audit logs (for compliance audit)
     */
    @Query("SELECT * FROM consent_audit_log ORDER BY timestamp ASC")
    suspend fun getAll(): List<ConsentAuditLogEntity>

    /**
     * Get audit logs by event type
     */
    @Query("SELECT * FROM consent_audit_log WHERE eventType = :eventType ORDER BY timestamp ASC")
    suspend fun getByEventType(eventType: String): List<ConsentAuditLogEntity>

    /**
     * Get audit logs in date range
     */
    @Query("SELECT * FROM consent_audit_log WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<ConsentAuditLogEntity>

    /**
     * Get count of audit logs
     */
    @Query("SELECT COUNT(*) FROM consent_audit_log")
    suspend fun getCount(): Int

    /**
     * Get count of accepted consents for a user
     */
    @Query("SELECT COUNT(*) FROM consent_audit_log WHERE userId = :userId AND eventType = 'ACCEPTED'")
    suspend fun getAcceptedConsentCount(userId: String): Int

    /**
     * Check if user has accepted biometric consent
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM consent_audit_log
        WHERE userId = :userId
        AND eventType = 'ACCEPTED'
        AND consentType = 'BIOMETRIC_PROCESSING'
    """)
    suspend fun hasBiometricConsent(userId: String): Boolean
}
