package com.disinidev.nebeng.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(
        state = state,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::sendMessage,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun ChatContent(
    state: ChatUiState,
    onInputChange: (String) -> Unit = {},
    onSendClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Automatically scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            ChatTopBar(
                driverName = state.driverName,
                vehicleInfo = state.vehicleInfo,
                pin = state.pin,
                isTripCompleted = state.isTripCompleted,
                onBackClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        },
        bottomBar = {
            if (state.isTripCompleted) {
                ChatCompletedNotice(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NebengColor.Primary0)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            } else {
                ChatInputBar(
                    inputText = state.inputMessage,
                    onInputChange = onInputChange,
                    onSendClick = onSendClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NebengColor.Primary0)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->
        // Chat messages aligned to the bottom or empty prompt
        if (state.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isTripCompleted) {
                        "Tidak ada riwayat obrolan untuk perjalanan ini."
                    } else {
                        "Mulai obrolan untuk koordinasi titik jemput dan jadwal tebengan."
                    },
                    fontSize = 13.sp,
                    color = NebengColor.Gray600,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    ChatBubbleItem(
                        message = message,
                        modifier = Modifier.animateItem()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    driverName: String,
    vehicleInfo: String,
    pin: String,
    isTripCompleted: Boolean = false,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title Column (Driver Name & Subtitle)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = driverName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                if (isTripCompleted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(NebengRadius.Full))
                            .background(NebengColor.Gray200)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Selesai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NebengColor.Primary900
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isTripCompleted) {
                    if (vehicleInfo.isNotBlank() && vehicleInfo != "-") "$vehicleInfo • Selesai" else "Perjalanan selesai"
                } else {
                    "$vehicleInfo • PIN: $pin"
                },
                fontSize = 12.sp,
                color = NebengColor.Gray400,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
        // Call button deleted per user request
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isFromMe = message.isFromMe
    val horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    val bgColor = if (isFromMe) NebengColor.Primary900 else NebengColor.Primary50
    val textColor = if (isFromMe) NebengColor.Primary0 else NebengColor.Primary900

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = textColor,
                lineHeight = 20.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

private val QUICK_REPLIES = listOf(
    "Saya sudah di titik jemput 👍",
    "Sedang jalan ke lobi / gerbang",
    "Bawa helm sendiri 🛵",
    "Tolong tunggu sekitar 2 menit ya",
    "Sudah sampai di mana ya?"
)

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Quick Reply Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(QUICK_REPLIES) { reply ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(NebengColor.Primary50)
                        .clickable { onInputChange(reply) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reply,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Primary900
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Input Field Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(NebengColor.Primary50)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = NebengColor.Primary900,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                cursorBrush = SolidColor(NebengColor.Primary900),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Ketik pesan...",
                            color = NebengColor.Gray400,
                            fontSize = 14.sp,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Send Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900)
                .clickable(onClick = onSendClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Kirim",
                tint = NebengColor.Primary0,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}
}

@Composable
private fun ChatCompletedNotice(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary50)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Gray200),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Perjalanan telah selesai",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Obrolan ditutup. Kamu tetap bisa membaca riwayat pesan di sini.",
                    fontSize = 12.sp,
                    color = NebengColor.Gray600,
                    lineHeight = 16.sp,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}
