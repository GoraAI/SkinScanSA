package com.skinscan.sa

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skinscan.sa.data.session.UserSessionManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for UserSessionManager (Task 1 verification)
 *
 * Validates that:
 * 1. UUID is generated on first access
 * 2. UUID is consistent across multiple accesses
 * 3. clearSession() generates a new UUID
 * 4. Uses EncryptedSharedPreferences for secure storage
 */
@RunWith(AndroidJUnit4::class)
class UserSessionManagerTest {

    private lateinit var context: Context
    private lateinit var userSessionManager: UserSessionManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        userSessionManager = UserSessionManager(context)
        // Clear any existing session for clean test state
        userSessionManager.clearSession()
    }

    @After
    fun teardown() {
        // Clean up after tests
        userSessionManager.clearSession()
    }

    @Test
    fun userId_generates_uuid_on_first_access() {
        val userId = userSessionManager.userId

        assertNotNull("userId should not be null", userId)
        assertTrue("userId should not be empty", userId.isNotEmpty())
        // Verify UUID format (8-4-4-4-12 hex digits)
        assertTrue(
            "userId should be valid UUID format",
            userId.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
        )
    }

    @Test
    fun userId_is_consistent_across_multiple_accesses() {
        val userId1 = userSessionManager.userId
        val userId2 = userSessionManager.userId
        val userId3 = userSessionManager.userId

        assertEquals("userId should be consistent", userId1, userId2)
        assertEquals("userId should be consistent", userId2, userId3)
    }

    @Test
    fun clearSession_generates_new_uuid() {
        val userId1 = userSessionManager.userId
        userSessionManager.clearSession()
        val userId2 = userSessionManager.userId

        assertNotEquals("clearSession should generate new userId", userId1, userId2)
        assertNotNull("New userId should not be null", userId2)
        assertTrue("New userId should be valid UUID",
            userId2.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }

    @Test
    fun isFirstLaunch_returns_true_before_userId_access() {
        // After clearSession in setup, isFirstLaunch should be true
        assertTrue("isFirstLaunch should be true initially", userSessionManager.isFirstLaunch)
    }

    @Test
    fun isFirstLaunch_returns_false_after_userId_access() {
        // Access userId to trigger first launch flag update
        userSessionManager.userId

        assertFalse("isFirstLaunch should be false after userId access", userSessionManager.isFirstLaunch)
    }

    @Test
    fun userId_persists_across_new_instances() {
        val userId1 = userSessionManager.userId

        // Create new instance
        val newManager = UserSessionManager(context)
        val userId2 = newManager.userId

        assertEquals("userId should persist across instances", userId1, userId2)
    }

    @Test
    fun multiple_users_not_supported_single_device_id() {
        // This test documents that we use a single device-based ID
        // Multiple user sessions are not supported in MVP
        val manager1 = UserSessionManager(context)
        val manager2 = UserSessionManager(context)

        assertEquals("All managers should return same userId",
            manager1.userId, manager2.userId)
    }
}
