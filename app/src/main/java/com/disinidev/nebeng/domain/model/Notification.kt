package com.disinidev.nebeng.domain.model

import java.time.Instant

data class Notification(
    val id: String,
    val userId: String,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val actionUrl: String? = null,
    val isRead: Boolean = false,
    val createdAt: Instant = Instant.now()
)

enum class NotificationCategory {
    TRIP,
    PROMO,
    REVIEW,
    SYSTEM;

    companion object {
        fun fromString(value: String): NotificationCategory = runCatching {
            valueOf(value.uppercase())
        }.getOrDefault(SYSTEM)
    }
}
