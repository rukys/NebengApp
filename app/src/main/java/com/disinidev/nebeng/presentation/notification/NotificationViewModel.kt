package com.disinidev.nebeng.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.Notification
import com.disinidev.nebeng.domain.model.NotificationCategory
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
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

enum class NotificationFilter(val label: String, val category: NotificationCategory?) {
    ALL("Semua", null),
    TRIP("Perjalanan", NotificationCategory.TRIP),
    SYSTEM("Sistem", NotificationCategory.SYSTEM)
}

data class NotificationUiState(
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val notifications: List<Notification> = emptyList(),
    val totalCount: Int = 0,
    val tripCount: Int = 0,
    val systemCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@Serializable
private data class NotificationDto(
    val id: String,
    val user_id: String,
    val category: String,
    val title: String,
    val body: String,
    val action_url: String? = null,
    val is_read: Boolean = false,
    val created_at: String
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var allNotifications: List<Notification> = emptyList()

    init {
        loadNotifications()
    }

    fun onSelectFilter(filter: NotificationFilter) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                notifications = filterNotifications(allNotifications, filter)
            )
        }
    }

    fun markAsRead(notificationId: String) {
        allNotifications = allNotifications.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        updateFilteredState()

        viewModelScope.launch {
            try {
                supabaseClient.postgrest["notifications"].update(
                    mapOf("is_read" to true)
                ) {
                    filter {
                        eq("id", notificationId)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Offline fallback
            }
        }
    }

    fun markAllAsRead() {
        allNotifications = allNotifications.map { it.copy(isRead = true) }
        updateFilteredState()

        viewModelScope.launch {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                try {
                    supabaseClient.postgrest["notifications"].update(
                        mapOf("is_read" to true)
                    ) {
                        filter {
                            eq("is_read", false)
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Offline fallback
                }
            }
        }
    }

    fun refresh() {
        loadNotifications(isRefresh = true)
    }

    private fun loadNotifications(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                // Gunakan sample notifications yang presisi dengan desain Figma
                allNotifications = getSampleNotifications()
                updateFilteredState()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                allNotifications = getSampleNotifications()
                updateFilteredState()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.localizedMessage
                    )
                }
            }
        }
    }

    private fun updateFilteredState() {
        val total = allNotifications.size
        val tripCount = allNotifications.count { it.category == NotificationCategory.TRIP }
        val systemCount = allNotifications.count { it.category == NotificationCategory.SYSTEM || it.category == NotificationCategory.REVIEW }

        _uiState.update {
            it.copy(
                notifications = filterNotifications(allNotifications, it.selectedFilter),
                totalCount = total,
                tripCount = tripCount,
                systemCount = systemCount
            )
        }
    }

    private fun filterNotifications(list: List<Notification>, filter: NotificationFilter): List<Notification> {
        return when (filter) {
            NotificationFilter.ALL -> list
            NotificationFilter.TRIP -> list.filter { it.category == NotificationCategory.TRIP }
            NotificationFilter.SYSTEM -> list.filter { it.category == NotificationCategory.SYSTEM || it.category == NotificationCategory.REVIEW }
        }
    }

    private fun getSampleNotifications(): List<Notification> {
        val now = Instant.now()
        val yesterday = now.minus(1, ChronoUnit.DAYS)
        val fewDaysAgo = now.minus(7, ChronoUnit.DAYS)

        return listOf(
            Notification(
                id = "notif_1",
                userId = "user_current",
                category = NotificationCategory.TRIP,
                title = "Driver Menuju Titik Jemput",
                body = "Andi Pratama (Avanza) berjarak 3 mnt dari Lawson Tebet. Siapkan PIN 489 201.",
                actionUrl = "tracking/booking_123",
                isRead = false,
                createdAt = now.minus(32, ChronoUnit.MINUTES)
            ),
            Notification(
                id = "notif_2",
                userId = "user_current",
                category = NotificationCategory.TRIP,
                title = "Perjalanan Selesai: Tebet → SCBD",
                body = "Beri ulasan dan rating untuk Driver Reza Hendra (NMAX) untuk menjaga keamanan komunitas.",
                actionUrl = null,
                isRead = true,
                createdAt = yesterday
            ),
            Notification(
                id = "notif_3",
                userId = "user_current",
                category = NotificationCategory.SYSTEM,
                title = "Verifikasi Profil Berhasil",
                body = "KTP & profil kantor Anda telah terverifikasi. Status: Trusted Commuter ✓",
                actionUrl = null,
                isRead = true,
                createdAt = fewDaysAgo
            )
        )
    }
}
