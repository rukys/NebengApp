package com.disinidev.nebeng.domain.repository

interface UserRepository {
    suspend fun getCurrentUserUuid(): String
    suspend fun getOrCreateUser(
        firebaseUid: String,
        name: String? = null,
        phone: String? = null,
        email: String? = null
    ): String
}
