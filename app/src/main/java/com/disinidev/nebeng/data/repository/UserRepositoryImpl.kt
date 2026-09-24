package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class UserRowDto(
    val id: String,
    val firebase_uid: String,
    val full_name: String? = null,
    val phone_number: String? = null,
    val email: String? = null
)

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    private val cacheFirebaseToUuid = ConcurrentHashMap<String, String>()

    override suspend fun getCurrentUserUuid(): String = withContext(Dispatchers.IO) {
        val fbUser = firebaseAuth.currentUser
        val fbUid = fbUser?.uid ?: "anonymous_user"
        val name = fbUser?.displayName ?: "Pengguna Nebeng"
        val phone = fbUser?.phoneNumber
        val email = fbUser?.email

        getOrCreateUser(
            firebaseUid = fbUid,
            name = name,
            phone = phone,
            email = email
        )
    }

    override suspend fun getOrCreateUser(
        firebaseUid: String,
        name: String?,
        phone: String?,
        email: String?
    ): String = withContext(Dispatchers.IO) {
        cacheFirebaseToUuid[firebaseUid]?.let { return@withContext it }

        try {
            // 1. Check if user already exists in Supabase users table
            val existing = supabaseClient.postgrest["users"].select {
                filter {
                    eq("firebase_uid", firebaseUid)
                }
            }.decodeSingleOrNull<UserRowDto>()

            if (existing != null) {
                cacheFirebaseToUuid[firebaseUid] = existing.id
                return@withContext existing.id
            }

            // 2. User not found, create new row in Supabase users table
            val newUuid = UUID.randomUUID().toString()
            val insertPayload = mutableMapOf<String, Any>(
                "id" to newUuid,
                "firebase_uid" to firebaseUid,
                "full_name" to (name ?: "Pengguna Nebeng"),
                "role" to "both"
            )
            phone?.takeIf { it.isNotBlank() }?.let { insertPayload["phone_number"] = it }
            email?.takeIf { it.isNotBlank() }?.let { insertPayload["email"] = it }

            supabaseClient.postgrest["users"].insert(insertPayload)
            cacheFirebaseToUuid[firebaseUid] = newUuid
            newUuid
        } catch (e: Exception) {
            // Offline fallback: generate deterministic UUID based on firebaseUid
            val fallbackUuid = UUID.nameUUIDFromBytes(firebaseUid.toByteArray()).toString()
            cacheFirebaseToUuid[firebaseUid] = fallbackUuid
            fallbackUuid
        }
    }
}
