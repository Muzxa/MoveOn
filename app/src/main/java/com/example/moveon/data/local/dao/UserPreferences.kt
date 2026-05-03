package com.example.moveon.data.local.dao

interface UserPreferences {
	suspend fun setRememberMeEnabled(enabled: Boolean)
	suspend fun isRememberMeEnabled(): Boolean
	suspend fun setRememberedEmail(email: String)
	suspend fun getRememberedEmail(): String?
	suspend fun setOnboardingCompleted(completed: Boolean)
	suspend fun isOnboardingCompleted(): Boolean
	suspend fun setPushNotificationsEnabled(enabled: Boolean)
	suspend fun isPushNotificationsEnabled(): Boolean
	suspend fun setEmailNotificationsEnabled(enabled: Boolean)
	suspend fun isEmailNotificationsEnabled(): Boolean
	suspend fun setShareLiveLocationEnabled(enabled: Boolean)
	suspend fun isShareLiveLocationEnabled(): Boolean
	suspend fun setDarkModeEnabled(enabled: Boolean)
	suspend fun isDarkModeEnabled(): Boolean
	suspend fun setAutoSyncEnabled(enabled: Boolean)
	suspend fun isAutoSyncEnabled(): Boolean
	suspend fun setLastManualSyncAt(timestampMillis: Long)
	suspend fun getLastManualSyncAt(): Long?
	suspend fun clearRememberMeData()

}