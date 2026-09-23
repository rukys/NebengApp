package com.disinidev.nebeng.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@Serializable
private data class UserProfileDto(
    val id: String? = null,
    val full_name: String? = null,
    val phone_number: String? = null,
    val email: String? = null,
    val avatar_url: String? = null,
    val average_rating: Float? = null,
    val total_trips: Int? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun toggleRole(role: ProfileRole) {
        _uiState.update { current ->
            if (role == ProfileRole.PASSENGER) {
                current.copy(
                    selectedRole = ProfileRole.PASSENGER,
                    rating = 4.9f,
                    tripCount = 15,
                    co2SavedKg = 42
                )
            } else {
                current.copy(
                    selectedRole = ProfileRole.DRIVER,
                    rating = 5.0f,
                    tripCount = 8,
                    co2SavedKg = 68
                )
            }
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(message = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            viewModelScope.launch {
                try {
                    val profile = supabaseClient.postgrest["users"]
                        .select {
                            filter {
                                eq("firebase_uid", uid)
                            }
                        }
                        .decodeSingleOrNull<UserProfileDto>()

                    if (profile != null) {
                        val fullName = profile.full_name ?: currentUser.displayName ?: "Budi Santoso"
                        val initials = fullName.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .uppercase()

                        _uiState.update { current ->
                            current.copy(
                                fullName = fullName,
                                phoneNumber = profile.phone_number ?: currentUser.phoneNumber ?: current.phoneNumber,
                                email = profile.email ?: currentUser.email ?: current.email,
                                avatarUrl = profile.avatar_url,
                                avatarInitials = if (initials.isNotBlank()) initials else "B",
                                rating = profile.average_rating ?: current.rating,
                                tripCount = profile.total_trips ?: current.tripCount
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Fallback to default state
                }
            }
        }
    }
}
