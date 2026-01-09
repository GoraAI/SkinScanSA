package com.skinscan.sa.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Room entity for user profile data
 *
 * Stores user preferences and consent status
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String = UUID.randomUUID().toString(),

    val createdAt: Date = Date(),

    val updatedAt: Date = Date(),

    // POPIA consent status
    val popiaConsentGiven: Boolean = false,
    val popiaConsentDate: Date? = null,

    // Analytics consent (optional)
    val analyticsConsentGiven: Boolean = false,

    // User preferences (JSON object)
    val preferences: String? = null // {"budget": "low", "preferredRetailers": ["CLICKS"]}
)
