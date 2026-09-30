package com.disinidev.nebeng.data.repository

import android.util.Log
import com.disinidev.nebeng.domain.model.Notification
import com.disinidev.nebeng.domain.model.NotificationCategory
import com.disinidev.nebeng.domain.repository.NotificationRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

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

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val userRepository: UserRepository
) : NotificationRepository {

    private var cachedUnreadCount: Int? = null

    override suspend fun getNotifications(): Result<List<Notification>> {
        return try {
            val userUuid = userRepository.getCurrentUserUuid()
            val remoteList = supabaseClient.postgrest["notifications"].select {
                filter {
                    eq("user_id", userUuid)
                }
                order("created_at", Order.DESCENDING)
                limit(30)
            }.decodeList<NotificationDto>()

            if (remoteList.isNotEmpty()) {
                val notifications = remoteList.map { dto ->
                    val cat = when (dto.category.lowercase()) {
                        "trip" -> NotificationCategory.TRIP
                        else -> NotificationCategory.SYSTEM
                    }
                    val createdAt = runCatching { Instant.parse(dto.created_at) }.getOrElse { Instant.now() }
                    Notification(
                        id = dto.id,
                        userId = dto.user_id,
                        category = cat,
                        title = dto.title,
                        body = dto.body,
                        actionUrl = dto.action_url,
                        isRead = dto.is_read,
                        createdAt = createdAt
                    )
                }
                cachedUnreadCount = notifications.count { !it.isRead }
                Result.success(notifications)
            } else {
                cachedUnreadCount = 0
                Result.success(emptyList())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error fetching notifications: ${e.message}", e)
            Result.success(emptyList())
        }
    }

    override suspend fun getUnreadCount(): Result<Int> {
        return try {
            val userUuid = userRepository.getCurrentUserUuid()
            val unreadList = supabaseClient.postgrest["notifications"].select {
                filter {
                    eq("user_id", userUuid)
                    eq("is_read", false)
                }
            }.decodeList<NotificationDto>()
            val count = unreadList.size
            cachedUnreadCount = count
            Result.success(count)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error getting unread count: ${e.message}", e)
            Result.success(cachedUnreadCount ?: 0)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        cachedUnreadCount = (cachedUnreadCount?.minus(1))?.coerceAtLeast(0)
        return try {
            supabaseClient.postgrest["notifications"].update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("id", notificationId)
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking notification as read: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        cachedUnreadCount = 0
        return try {
            val userUuid = userRepository.getCurrentUserUuid()
            supabaseClient.postgrest["notifications"].update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("user_id", userUuid)
                    eq("is_read", false)
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking all notifications as read: ${e.message}", e)
            Result.failure(e)
        }
    }
}
