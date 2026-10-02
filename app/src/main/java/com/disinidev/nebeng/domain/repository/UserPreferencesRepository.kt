package com.disinidev.nebeng.domain.repository

interface UserPreferencesRepository {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean = true)
    fun isNotificationEnabled(): Boolean
    fun setNotificationEnabled(enabled: Boolean)
}
