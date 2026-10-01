package com.disinidev.nebeng

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.disinidev.nebeng.core.designsystem.NebengTheme
import com.disinidev.nebeng.core.navigation.NebengNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var onNewIntentListener: ((Intent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN
        ) {
            finish()
            return
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.BLACK),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.BLACK)
        )

        val initialActionUrl = extractActionUrl(intent)

        setContent {
            NebengTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    val navController = rememberNavController()
                    var pendingDeepLink by remember { mutableStateOf(initialActionUrl) }

                    DisposableEffect(navController) {
                        onNewIntentListener = { newIntent ->
                            val url = extractActionUrl(newIntent)
                            if (!url.isNullOrBlank()) {
                                runCatching {
                                    navController.navigate(Uri.parse(url))
                                }
                            } else {
                                navController.handleDeepLink(newIntent)
                            }
                        }
                        onDispose {
                            onNewIntentListener = null
                        }
                    }

                    NebengNavGraph(
                        navController = navController,
                        pendingDeepLinkUri = pendingDeepLink,
                        onDeepLinkConsumed = { pendingDeepLink = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        onNewIntentListener?.invoke(intent)
    }

    private fun extractActionUrl(intent: Intent?): String? {
        if (intent == null) return null
        val dataStr = intent.dataString
        if (!dataStr.isNullOrBlank() && dataStr.startsWith("nebeng://")) {
            return dataStr
        }
        val extraUrl = intent.getStringExtra("action_url")
            ?: intent.extras?.getString("action_url")
        if (!extraUrl.isNullOrBlank() && extraUrl.startsWith("nebeng://")) {
            return extraUrl
        }
        val action = intent.action
        if (!action.isNullOrBlank() && action.startsWith("nebeng://")) {
            return action
        }
        return null
    }
}
