package com.disinidev.nebeng.domain.repository

data class UserProfileData(
    val id: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val officeAddress: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val qrisUrl: String? = null,
    val ktpUrl: String? = null,
    val rating: Float = 5.0f,
    val totalTrips: Int = 0,
    val role: String = "both",
    val isKtpVerified: Boolean = false
)

interface UserRepository {
    suspend fun getCurrentUserUuid(): String
    suspend fun getCurrentUserName(): String
    suspend fun getOrCreateUser(
        firebaseUid: String,
        name: String? = null,
        phone: String? = null,
        email: String? = null
    ): String
    suspend fun getUserProfile(): Result<UserProfileData>
    suspend fun updateUserProfile(
        fullName: String,
        officeAddress: String,
        bio: String,
        phoneNumber: String? = null
    ): Result<Unit>
    suspend fun setupUserProfile(
        fullName: String,
        phoneNumber: String?,
        email: String?,
        officeAddress: String?,
        bio: String?,
        avatarUrl: String?
    ): Result<Unit>
    suspend fun updateUserRole(role: String): Result<Unit>
    suspend fun getUserRoleStats(role: String): Result<Pair<Int, Int>>
    suspend fun uploadAvatar(imageBytes: ByteArray, extension: String = "jpg"): Result<String>
    suspend fun uploadKtp(imageBytes: ByteArray, extension: String = "jpg"): Result<String>
    suspend fun uploadQris(imageBytes: ByteArray, extension: String = "jpg"): Result<String>
    suspend fun getDriverQrisUrl(driverId: String): Result<String?>
    suspend fun syncFcmToken(token: String): Result<Unit>
    suspend fun syncFcmToken(): Result<Unit>
    suspend fun logout(): Result<Unit>
}
