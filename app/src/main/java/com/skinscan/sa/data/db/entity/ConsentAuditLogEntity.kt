package com.skinscan.sa.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Consent Audit Log Entity (Story 6.4)
 *
 * Immutable audit trail for all consent interactions.
 * Used to prove POPIA compliance in case of legal dispute.
 *
 * Note: This table is append-only - no updates or deletes allowed.
 * Even after user account deletion, audit logs are retained (legal requirement).
 */
@Entity(tableName = "consent_audit_log")
data class ConsentAuditLogEntity(
    @PrimaryKey
    val auditId: String,

    /** User identifier */
    val userId: String,

    /** Event type: SHOWN, ACCEPTED, DECLINED, REVOKED, DATA_DELETED */
    val eventType: String,

    /** Consent type: BIOMETRIC_PROCESSING, ANALYTICS */
    val consentType: String,

    /** Screen where event occurred: POPIA_CONSENT_SCREEN, SETTINGS */
    val screenName: String,

    /** Full consent text shown to user (for legal proof) */
    val consentText: String,

    /** Unix timestamp in milliseconds */
    val timestamp: Long,

    /** App version at time of event */
    val appVersion: String,

    /** Device model (Build.MODEL) */
    val deviceModel: String,

    /** Android SDK version (Build.VERSION.SDK_INT) */
    val osVersion: Int
) {
    companion object {
        // Event types
        const val EVENT_SHOWN = "SHOWN"
        const val EVENT_ACCEPTED = "ACCEPTED"
        const val EVENT_DECLINED = "DECLINED"
        const val EVENT_REVOKED = "REVOKED"
        const val EVENT_DATA_DELETED = "DATA_DELETED"

        // Consent types
        const val CONSENT_BIOMETRIC_PROCESSING = "BIOMETRIC_PROCESSING"
        const val CONSENT_ANALYTICS = "ANALYTICS"

        // Screen names
        const val SCREEN_POPIA_CONSENT = "POPIA_CONSENT_SCREEN"
        const val SCREEN_SETTINGS = "SETTINGS"
        const val SCREEN_PROFILE = "PROFILE"
    }
}
