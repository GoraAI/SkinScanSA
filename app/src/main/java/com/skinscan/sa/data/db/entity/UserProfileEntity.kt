package com.skinscan.sa.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Room entity for user profile data
 *
 * Stores user preferences, consent status, and skin profile
 *
 * Story 4.4: Extended for profile management
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
    val preferences: String? = null, // {"budget": "low", "preferredRetailers": ["CLICKS"]}

    // Story 4.4: Skin profile
    val knownConcerns: String? = null, // JSON array: ["HYPERPIGMENTATION", "ACNE"]
    val budgetRange: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val location: String? = null, // Province/region
    val knownAllergies: String? = null // Free text
)
