package com.disinidev.nebeng.domain.repository

interface AuthRepository {
    fun isLoggedIn(): Boolean
    fun getCurrentFirebaseUid(): String?
    suspend fun loginWithEmail(identifier: String, password: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<Unit>
    suspend fun registerWithEmail(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String
    ): Result<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun verifyOtp(phone: String, code: String): Result<Unit>
    suspend fun resendOtp(phone: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
