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
    }
}