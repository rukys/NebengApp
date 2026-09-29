package com.disinidev.nebeng.data.repository

import android.util.Log
import com.disinidev.nebeng.domain.repository.ChatRepository
import com.disinidev.nebeng.presentation.chat.ChatMessage
import com.disinidev.nebeng.presentation.chat.ConversationItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class RemoteChatMessageDto(
    val id: String,
    val booking_id: String,
    val sender_id: String,
    val sender_name: String? = null,
    val message: String,
    val created_at: String? = null
)

@Serializable
private data class RemoteBookingConvDto(
    val id: String,
    val seat_position: String,
    val pickup_pin: String,
    val status: String,
    val created_at: String? = null,
    val rides: RemoteConvRideDto? = null,
    val users: RemoteConvUserDto? = null
)

@Serializable
private data class RemoteConvRideDto(
    val id: String,
    val vehicle_model: String,
    val vehicle_plate: String,
    val driver_id: String,
    val users: RemoteConvUserDto? = null
)

@Serializable
private data class RemoteConvUserDto(
    val id: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null
)

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ChatRepository {

    private val localMessages = ConcurrentHashMap<String, MutableList<ChatMessage>>()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Jakarta"))

    override suspend fun getMessages(
        bookingId: String,
        currentUserId: String
    ): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val list = supabaseClient.from("chat_messages").select {
                    filter {
                        eq("booking_id", bookingId)
                    }
                    order("created_at", Order.ASCENDING)
                }.decodeList<RemoteChatMessageDto>()

                if (list.isNotEmpty()) {
                    val messages = list.map { dto ->
                        val timeStr = dto.created_at?.let {
                            runCatching { timeFormatter.format(Instant.parse(it)) }.getOrNull()
                        } ?: ""
                        ChatMessage(
                            id = dto.id,
                            text = dto.message,
                            isFromMe = dto.sender_id == currentUserId,
                            timestamp = timeStr
                        )
                    }
                    localMessages[bookingId] = messages.toMutableList()
                    return@runCatching messages
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "getMessages Supabase error: ${e.message}", e)
            }

            // Fallback to local cache or empty
            localMessages[bookingId] ?: emptyList()
        }
    }

    override fun observeMessages(bookingId: String, currentUserId: String): Flow<List<ChatMessage>> = flow {
        // Emit initial
        val initial = getMessages(bookingId, currentUserId).getOrElse { localMessages[bookingId] ?: emptyList() }
        emit(initial)

        // Poll every 3 seconds for new messages
        while (true) {
            delay(3000L)
            val updated = getMessages(bookingId, currentUserId).getOrNull()
            if (updated != null) {
                emit(updated)
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendMessage(
        bookingId: String,
        senderId: String,
        senderName: String,
        text: String
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        runCatching {
            val msgId = UUID.randomUUID().toString()
            val now = Instant.now()
            val timeStr = timeFormatter.format(now)

            val newMsg = ChatMessage(
                id = msgId,
                text = text,
                isFromMe = true,
                timestamp = timeStr
            )

            // Update local memory first
            val currentList = localMessages.getOrPut(bookingId) { mutableListOf() }
            currentList.add(newMsg)

            // Sync to Supabase
            try {
                val payload = mapOf(
                    "id" to msgId,
                    "booking_id" to bookingId,
                    "sender_id" to senderId,
                    "sender_name" to senderName,
                    "message" to text,
                    "created_at" to now.toString()
                )
                supabaseClient.from("chat_messages").insert(payload)
            } catch (e: Exception) {
                Log.e("ChatRepository", "sendMessage Supabase error: ${e.message}", e)
            }

            newMsg
        }
    }

    override suspend fun getUserConversations(userUuid: String): Result<List<ConversationItem>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                // 1. Fetch user bookings as passenger
                val passengerBookings = supabaseClient.from("bookings").select(
                    columns = Columns.raw(
                        "id, seat_position, pickup_pin, status, created_at, " +
                        "rides(id, vehicle_model, vehicle_plate, driver_id, " +
                        "users!rides_driver_id_fkey(id, full_name, avatar_url))"
                    )
                ) {
                    filter {
                        eq("passenger_id", userUuid)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(10)
                }.decodeList<RemoteBookingConvDto>()

                // 2. Fetch driver bookings
                val driverBookings = supabaseClient.from("bookings").select(
                    columns = Columns.raw(
                        "id, seat_position, pickup_pin, status, created_at, " +
                        "rides!inner(id, vehicle_model, vehicle_plate, driver_id), " +
                        "users!bookings_passenger_id_fkey(id, full_name, avatar_url)"
                    )
                ) {
                    filter {
                        eq("rides.driver_id", userUuid)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(10)
                }.decodeList<RemoteBookingConvDto>()

                val allRemote = passengerBookings + driverBookings
                if (allRemote.isNotEmpty()) {
                    val items = allRemote.map { b ->
                        val isDriverRole = b.rides?.driver_id == userUuid
                        val otherName = if (isDriverRole) {
                            b.users?.full_name ?: "Penumpang"
                        } else {
                            b.rides?.users?.full_name ?: "Pengemudi"
                        }
                        val vehicle = listOfNotNull(b.rides?.vehicle_model, b.rides?.vehicle_plate)
                            .filter { it.isNotBlank() }
                            .joinToString(" • ")
                            .ifBlank { "Kendaraan" }
                        val initials = otherName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                        val isActive = b.status in listOf("pending", "confirmed", "picked_up")

                        ConversationItem(
                            id = b.id,
                            title = otherName,
                            subtitle = if (isActive) "Status: ${b.status} • PIN: ${b.pickup_pin}" else "Perjalanan selesai",
                            timestamp = b.created_at?.let { runCatching { timeFormatter.format(Instant.parse(it)) }.getOrNull() } ?: "Hari Ini",
                            avatarInitials = initials.ifBlank { "N" },
                            isGroup = false,
                            isActiveRide = isActive,
                            driverName = otherName,
                            vehicleInfo = vehicle,
                            pin = b.pickup_pin
                        )
                    }
                    return@runCatching items
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "getUserConversations Supabase error: ${e.message}", e)
            }

            emptyList()
        }
    }
}
