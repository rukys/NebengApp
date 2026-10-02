package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.domain.repository.ChatRepository
import com.disinidev.nebeng.presentation.chat.ChatMessage
import com.disinidev.nebeng.presentation.chat.ConversationItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import android.content.Context
import com.disinidev.nebeng.core.notification.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

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
    private val supabaseClient: SupabaseClient,
    @param:ApplicationContext private val context: Context
) : ChatRepository {

    @Volatile
    private var activeChatBookingId: String? = null

    override fun setActiveChat(bookingId: String?) {
        activeChatBookingId = bookingId
    }

    override suspend fun unsubscribeChat(bookingId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val channel = activeChannels.remove(bookingId)
                channel?.unsubscribe()
                Timber.d("Unsubscribed and removed Realtime channel for booking $bookingId")
            }
        }
    }

    private val localMessages = ConcurrentHashMap<String, MutableList<ChatMessage>>()
    private val activeChannels = ConcurrentHashMap<String, RealtimeChannel>()
    private val realtimeMessagesFlow = ConcurrentHashMap<String, MutableStateFlow<List<ChatMessage>>>()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Jakarta"))

    override suspend fun getMessages(
        bookingId: String,
        currentUserId: String
    ): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val list = supabaseClient.from("chat_messages").select {
                    filter { eq("booking_id", bookingId) }
                    order("created_at", Order.ASCENDING)
                }.decodeList<RemoteChatMessageDto>()

                val messages = list.map { dto -> dto.toChatMessage(currentUserId) }
                localMessages[bookingId] = messages.toMutableList()
                messages
            } catch (e: Exception) {
                Timber.e(e, "getMessages error: ${e.message}")
                localMessages[bookingId] ?: emptyList()
            }
        }
    }

    override fun observeMessages(bookingId: String, currentUserId: String): Flow<List<ChatMessage>> {
        // Get or create the realtime state flow for this booking
        val stateFlow = realtimeMessagesFlow.getOrPut(bookingId) {
            MutableStateFlow(localMessages[bookingId] ?: emptyList())
        }

        return flow {
            // 1. Emit cached messages immediately
            val cached = localMessages[bookingId] ?: emptyList()
            if (cached.isNotEmpty()) emit(cached)

            // 2. Fetch initial from Supabase
            val initial = getMessages(bookingId, currentUserId).getOrElse { emptyList() }
            stateFlow.value = initial
            emit(initial)

            // 3. Subscribe to Realtime channel if not already subscribed
            if (!activeChannels.containsKey(bookingId)) {
                try {
                    val channel = supabaseClient.channel("chat:$bookingId")
                    activeChannels[bookingId] = channel

                    val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                        table = "chat_messages"
                        filter = "booking_id=eq.$bookingId"
                    }

                    channel.subscribe(blockUntilSubscribed = false)
                    Timber.d("Subscribed to Realtime channel for booking $bookingId")

                    // 4. Collect realtime inserts and append to state
                    insertFlow.collect { action ->
                        try {
                            val record = action.record
                            val msgId = record["id"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString()
                            val senderId = record["sender_id"]?.jsonPrimitive?.content ?: ""
                            val text = record["message"]?.jsonPrimitive?.content ?: ""
                            val createdAt = record["created_at"]?.jsonPrimitive?.content
                            val timeStr = createdAt?.let {
                                runCatching { timeFormatter.format(Instant.parse(it)) }.getOrNull()
                            } ?: ""

                            val isFromMe = senderId == currentUserId
                            val newMsg = ChatMessage(
                                id = msgId,
                                text = text,
                                isFromMe = isFromMe,
                                timestamp = timeStr
                            )

                            val current = stateFlow.value.toMutableList()
                            // Avoid duplicates
                            if (current.none { it.id == msgId }) {
                                current.add(newMsg)
                                localMessages[bookingId] = current
                                stateFlow.value = current.toList()
                            }

                            // Trigger notification if not from me and user is not currently in this chat
                            if (!isFromMe && activeChatBookingId != bookingId) {
                                val senderDisplayName = record["sender_name"]?.jsonPrimitive?.content ?: "Teman Nebeng"
                                NotificationHelper.showNotification(
                                    context = context,
                                    title = "Pesan baru dari $senderDisplayName",
                                    body = text,
                                    channelId = NotificationHelper.CHANNEL_CHAT,
                                    actionUrl = "nebeng://trip/$bookingId/chat",
                                    notificationId = bookingId.hashCode()
                                )
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Realtime insert parse error: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Realtime subscribe error: ${e.message}")
                    // Fallback: collect from stateFlow only (no realtime updates)
                }
            }

            // Collect subsequent stateFlow updates
            stateFlow.collect { emit(it) }
        }.flowOn(Dispatchers.IO)
    }

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

            // Optimistic local update
            val current = localMessages.getOrPut(bookingId) { mutableListOf() }
            current.add(newMsg)
            realtimeMessagesFlow[bookingId]?.value = current.toList()

            // Persist to Supabase — Realtime will broadcast to all subscribers
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
                Timber.e(e, "sendMessage Supabase error: ${e.message}")
            }

            newMsg
        }
    }

    override suspend fun getUserConversations(userUuid: String): Result<List<ConversationItem>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val passengerBookings = supabaseClient.from("bookings").select(
                    columns = Columns.raw(
                        "id, seat_position, pickup_pin, status, created_at, " +
                        "rides(id, vehicle_model, vehicle_plate, driver_id, " +
                        "users!rides_driver_id_fkey(id, full_name, avatar_url))"
                    )
                ) {
                    filter { eq("passenger_id", userUuid) }
                    order("created_at", Order.DESCENDING)
                    limit(10)
                }.decodeList<RemoteBookingConvDto>()

                val driverBookings = supabaseClient.from("bookings").select(
                    columns = Columns.raw(
                        "id, seat_position, pickup_pin, status, created_at, " +
                        "rides!inner(id, vehicle_model, vehicle_plate, driver_id), " +
                        "users!bookings_passenger_id_fkey(id, full_name, avatar_url)"
                    )
                ) {
                    filter { eq("rides.driver_id", userUuid) }
                    order("created_at", Order.DESCENDING)
                    limit(10)
                }.decodeList<RemoteBookingConvDto>()

                val allRemote = passengerBookings + driverBookings
                if (allRemote.isNotEmpty()) {
                    return@runCatching allRemote.map { b ->
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
                        val initials = otherName.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2).joinToString("").uppercase()
                        val isActive = b.status in listOf("pending", "confirmed", "picked_up")

                        ConversationItem(
                            id = b.id,
                            title = otherName,
                            subtitle = if (isActive) "Status: ${b.status} • PIN: ${b.pickup_pin}" else "Perjalanan selesai",
                            timestamp = b.created_at?.let {
                                runCatching { timeFormatter.format(Instant.parse(it)) }.getOrNull()
                            } ?: "Hari Ini",
                            avatarInitials = initials.ifBlank { "N" },
                            isGroup = false,
                            isActiveRide = isActive,
                            driverName = otherName,
                            vehicleInfo = vehicle,
                            pin = b.pickup_pin
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "getUserConversations error: ${e.message}")
            }
            emptyList()
        }
    }

    /** Unsubscribe a booking's Realtime channel (call when chat screen closes) */
    suspend fun unsubscribeBooking(bookingId: String) {
        activeChannels.remove(bookingId)?.let { channel ->
            try {
                supabaseClient.realtime.removeChannel(channel)
            } catch (e: Exception) {
                Timber.w("unsubscribe error: ${e.message}")
            }
        }
    }

    private fun RemoteChatMessageDto.toChatMessage(currentUserId: String): ChatMessage {
        val timeStr = created_at?.let {
            runCatching { timeFormatter.format(Instant.parse(it)) }.getOrNull()
        } ?: ""
        return ChatMessage(
            id = id,
            text = message,
            isFromMe = sender_id == currentUserId,
            timestamp = timeStr
        )
    }
}
