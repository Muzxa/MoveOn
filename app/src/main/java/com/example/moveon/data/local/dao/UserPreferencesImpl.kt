package com.example.moveon.data.local.dao

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesImpl @Inject constructor(
    @ApplicationContext context: Context
) : UserPreferences {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun setRememberMeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME_ENABLED, enabled).apply()
    }

    override suspend fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME_ENABLED, false)
    }

    override suspend fun setRememberedEmail(email: String) {
        prefs.edit().putString(KEY_REMEMBERED_EMAIL, email).apply()
    }

    override suspend fun getRememberedEmail(): String? {
        return prefs.getString(KEY_REMEMBERED_EMAIL, null)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override suspend fun isPushNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_PUSH_NOTIFICATIONS_ENABLED, false)
    }

    override suspend fun setEmailNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EMAIL_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override suspend fun isEmailNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_EMAIL_NOTIFICATIONS_ENABLED, false)
    }

    override suspend fun setShareLiveLocationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHARE_LIVE_LOCATION_ENABLED, enabled).apply()
    }

    override suspend fun isShareLiveLocationEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHARE_LIVE_LOCATION_ENABLED, false)
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
    }

    override suspend fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)
    }

    override suspend fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, enabled).apply()
    }

    override suspend fun isAutoSyncEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)
    }

    override suspend fun setLastManualSyncAt(timestampMillis: Long) {
        prefs.edit().putLong(KEY_LAST_MANUAL_SYNC_AT, timestampMillis).apply()
    }

    override suspend fun getLastManualSyncAt(): Long? {
        val timestamp = prefs.getLong(KEY_LAST_MANUAL_SYNC_AT, 0L)
        return timestamp.takeIf { it > 0L }
    }

    override suspend fun clearRememberMeData() {
        prefs.edit()
            .remove(KEY_REMEMBERED_EMAIL)
            .putBoolean(KEY_REMEMBER_ME_ENABLED, false)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "moveon_user_preferences"
        private const val KEY_REMEMBER_ME_ENABLED = "remember_me_enabled"
        private const val KEY_REMEMBERED_EMAIL = "remembered_email"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        private const val KEY_EMAIL_NOTIFICATIONS_ENABLED = "email_notifications_enabled"
        private const val KEY_SHARE_LIVE_LOCATION_ENABLED = "share_live_location_enabled"
        private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_LAST_MANUAL_SYNC_AT = "last_manual_sync_at"
    }
}