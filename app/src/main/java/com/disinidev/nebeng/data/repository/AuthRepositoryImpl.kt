package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.domain.repository.AuthRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class GoogleUserUpsertDto(
    val firebase_uid: String,
    val full_name: String,
    val email: String,
    val phone_number: String? = null,
    val whatsapp_number: String? = null,
    val avatar_url: String? = null
)

@Serializable
private data class UserExistCheckDto(
    val firebase_uid: String
)

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient,
    private val userRepository: UserRepository
) : AuthRepository {

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentFirebaseUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun loginWithEmail(identifier: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val email = if (identifier.contains("@")) {
                identifier.trim()
            } else {
                "${identifier.trim()}@nebeng.id"
            }
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = firebaseAuth.currentUser
            if (user != null) {
                userRepository.getOrCreateUser(
                    firebaseUid = user.uid,
                    name = user.displayName,
                    phone = user.phoneNumber,
                    email = user.email
                )
            }
            Unit
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                syncGoogleUserToSupabase(user)
                userRepository.getOrCreateUser(
                    firebaseUid = user.uid,
                    name = user.displayName,
                    phone = user.phoneNumber,
                    email = user.email
                )
            }
            Unit
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                user.updateProfile(profileUpdates).await()
                userRepository.getOrCreateUser(
                    firebaseUid = user.uid,
                    name = fullName,
                    phone = phoneNumber,
                    email = email
                )
            }
            Unit
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = firebaseAuth.currentUser ?: throw IllegalStateException("Pengguna belum masuk")
            val email = user.email ?: throw IllegalStateException("Email pengguna tidak ditemukan")
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Unit
        }
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Simulasi verifikasi OTP / Firebase Phone Auth
            kotlinx.coroutines.delay(1000)
            Unit
        }
    }

    override suspend fun resendOtp(phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Simulasi kirim ulang OTP
            kotlinx.coroutines.delay(800)
            Unit
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            firebaseAuth.signOut()
        }
    }

    private suspend fun syncGoogleUserToSupabase(user: FirebaseUser) {
        try {
            val existing = supabaseClient.postgrest["users"]
                .select {
                    filter {
                        eq("firebase_uid", user.uid)
                    }
                }
                .decodeSingleOrNull<UserExistCheckDto>()

            if (existing == null) {
                val fullName = user.displayName ?: "Pengguna Nebeng"
                val email = user.email ?: ""
                val photoUrl = user.photoUrl?.toString()

                supabaseClient.postgrest["users"]
                    .insert(
                        GoogleUserUpsertDto(
                            firebase_uid = user.uid,
                            full_name = fullName,
                            email = email,
                            phone_number = user.phoneNumber,
                            whatsapp_number = user.phoneNumber,
                            avatar_url = photoUrl
                        )
                    )
            }
        } catch (_: Exception) {
            // Offline fallback
        }
    }
}
