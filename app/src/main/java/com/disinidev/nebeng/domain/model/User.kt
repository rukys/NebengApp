package com.disinidev.nebeng.domain.model

import java.time.Instant

data class User(
    val id: String,
    val firebaseUid: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val whatsappNumber: String? = null,
    val officeAddress: String? = null,
    val officeLat: Double? = null,
    val officeLng: Double? = null,
    val bio: String? = null,
    val qrisImageUrl: String? = null,
    val ktpVerified: Boolean = false,
    val officeVerified: Boolean = false,
    val averageRating: Float = 0.0f,
    val totalTrips: Int = 0,
    val role: UserRole = UserRole.PASSENGER,
    val fcmToken: String? = null,
    val createdAt: Instant = Instant.now()
)

enum class UserRole {
    PASSENGER,
    DRIVER,
    BOTH;

    companion object {
        fun fromString(value: String): UserRole = when (value.lowercase()) {
            "driver" -> DRIVER
            "both" -> BOTH
            else -> PASSENGER
        }
    }
}
