package com.disinidev.nebeng.presentation.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_whenTripCompleted_displaysNoticeAndHidesInputBar() {
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(
                    driverName = "Budi Santoso",
                    vehicleInfo = "Avanza • B 1234 CD",
                    pin = "482 190",
                    isTripCompleted = true
                )
            )
        }

        // Verify UX notice is displayed
        composeTestRule.onNodeWithText("Perjalanan telah selesai").assertIsDisplayed()
        composeTestRule.onNodeWithText("Obrolan ditutup. Kamu tetap bisa membaca riwayat pesan di sini.").assertIsDisplayed()

        // Verify input bar is hidden
        composeTestRule.onNodeWithText("Ketik pesan...").assertDoesNotExist()
    }

    @Test
    fun test_whenTripActive_displaysInputBar() {
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(
                    driverName = "Budi Santoso",
                    vehicleInfo = "Avanza • B 1234 CD",
                    pin = "482 190",
                    isTripCompleted = false
                )
            )
        }

        // Verify input bar is available
        composeTestRule.onNodeWithText("Ketik pesan...").assertIsDisplayed()

        // Verify completed notice is absent
        composeTestRule.onNodeWithText("Perjalanan telah selesai").assertDoesNotExist()
    }

    @Test
    fun test_whenMessagesPresent_displaysChatHistory() {
        val testMessage = "Halo, saya sudah sampai di titik jemput ya!"
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(
                    driverName = "Budi Santoso",
                    vehicleInfo = "Avanza • B 1234 CD",
                    isTripCompleted = true,
                    messages = listOf(
                        ChatMessage(
                            id = "msg_1",
                            text = testMessage,
                            isFromMe = false
                        )
                    )
                )
            )
        }

        // Verify message history is visible even when completed
        composeTestRule.onNodeWithText(testMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Perjalanan telah selesai").assertIsDisplayed()
    }
}
