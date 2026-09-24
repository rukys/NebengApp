package com.disinidev.nebeng.core.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

@AndroidEntryPoint
class NebengFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var supabaseClient: SupabaseClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = firebaseAuth.currentUser?.uid ?: return
        serviceScope.launch {
            runCatching {
                supabaseClient.postgrest["users"].update(
                    mapOf("fcm_token" to token)
                ) {
                    filter {
                        eq("firebase_uid", userId)
                    }
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Nebeng"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Update baru pada tebengan Anda"
        val channelId = remoteMessage.data["channel_id"] ?: NotificationHelper.CHANNEL_TRIP

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            channelId = channelId
        )
    }
}
