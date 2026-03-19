package com.example.moveon.data.local.dao

interface UserPreferences {
	suspend fun setRememberMeEnabled(enabled: Boolean)
	suspend fun isRememberMeEnabled(): Boolean
	suspend fun setRememberedEmail(email: String)
	suspend fun getRememberedEmail(): String?
	suspend fun setOnboardingCompleted(completed: Boolean)
	suspend fun isOnboardingCompleted(): Boolean
	suspend fun clearRememberMeData()

}