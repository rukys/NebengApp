package com.disinidev.nebeng.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.Notification
import com.disinidev.nebeng.domain.model.NotificationCategory
import com.disinidev.nebeng.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
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
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        allNotifications = allNotifications.map { it.copy(isRead = true) }
        updateFilteredState()

        viewModelScope.launch {
            notificationRepository.markAllAsRead()
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

            notificationRepository.getNotifications()
                .onSuccess { notifications ->
                    allNotifications = notifications
                    updateFilteredState()
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message
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
}
