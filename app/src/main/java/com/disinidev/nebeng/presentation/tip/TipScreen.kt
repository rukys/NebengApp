package com.disinidev.nebeng.presentation.tip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing

private val TIP_PRESETS = listOf(5_000, 10_000, 15_000, 20_000, 25_000, 50_000)

@Composable
fun TipScreen(
    onSkip: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TipViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            TipTopBar(
                onClose = onSkip,
                onSkip = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = NebengSpacing.Xl, vertical = NebengSpacing.Md)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .navigationBarsPadding()
                    .padding(horizontal = NebengSpacing.Xl, vertical = NebengSpacing.Lg)
            ) {
                NebengButton(
                    text = if (state.selectedAmount > 0) "Kirim Tip Rp ${formatAmount(state.selectedAmount)}" else "Lewati",
                    onClick = {
                        if (state.selectedAmount > 0) {
                            viewModel.sendTip(onDone)
                        } else {
                            onSkip()
                        }
                    },
                    style = if (state.selectedAmount > 0) NebengButtonStyle.PRIMARY else NebengButtonStyle.SECONDARY,
                    trailingIcon = if (state.selectedAmount > 0) Icons.AutoMirrored.Filled.ArrowForward else null,
                    isLoading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = state.isCompleted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TipSuccessContent(
                amount = state.selectedAmount,
                driverName = state.driverName,
                onDone = onDone,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }

        AnimatedVisibility(
            visible = !state.isCompleted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = NebengSpacing.Xl)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(NebengSpacing.Lg))

                // Heart icon header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = NebengColor.Primary900,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(NebengSpacing.Lg))

                Text(
                    text = "Beri Pengemudi Tip?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(NebengSpacing.Sm))

                Text(
                    text = "Apresiasi pengemudi ${state.driverName} yang telah mengantarkan kamu dengan selamat dan nyaman.",
                    fontSize = 13.sp,
                    color = NebengColor.Gray600,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(NebengSpacing.Xxl))

                if (!state.driverQrisUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NebengColor.Primary50)
                            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "QRIS Pengemudi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebengColor.Primary900
                            )
                            Text(
                                text = "Buka mobile banking atau e-wallet kamu untuk scan langsung",
                                fontSize = 12.sp,
                                color = NebengColor.Gray600,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NebengColor.Primary0)
                                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = state.driverQrisUrl,
                                    contentDescription = "QRIS Driver",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(NebengSpacing.Lg))
                }

                // Preset grid: 3 x 2
                TipPresetGrid(
                    presets = TIP_PRESETS,
                    selectedAmount = state.selectedAmount,
                    onSelectPreset = viewModel::onPresetSelected
                )

                Spacer(modifier = Modifier.height(NebengSpacing.Xl))

                // Custom amount input
                TipCustomInput(
                    value = state.customAmountText,
                    onValueChange = viewModel::onCustomAmountChanged,
                    isActive = state.isCustomSelected
                )

                Spacer(modifier = Modifier.height(NebengSpacing.Xxl))
            }
        }
    }
}

@Composable
private fun TipTopBar(
    onClose: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = "Beri Tip",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Text(
            text = "Lewati",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = NebengColor.Gray600,
            modifier = Modifier
                .clickable { onSkip() }
                .padding(NebengSpacing.Xs)
        )
    }
}

@Composable
private fun TipPresetGrid(
    presets: List<Int>,
    selectedAmount: Int,
    onSelectPreset: (Int) -> Unit
) {
    val rows = presets.chunked(3)
    Column(
        verticalArrangement = Arrangement.spacedBy(NebengSpacing.Md),
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(NebengSpacing.Md),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { amount ->
                    val isSelected = selectedAmount == amount
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(NebengRadius.Md))
                            .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary50)
                            .border(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = if (isSelected) NebengColor.Primary900 else NebengColor.Gray200,
                                shape = RoundedCornerShape(NebengRadius.Md)
                            )
                            .clickable { onSelectPreset(amount) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rp ${formatAmount(amount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900
                        )
                    }
                }
                // Fill empty cells if row < 3
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TipCustomInput(
    value: String,
    onValueChange: (String) -> Unit,
    isActive: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "JUMLAH LAIN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Gray600,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = NebengSpacing.Sm)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(if (isActive) NebengColor.Primary0 else NebengColor.Primary50)
                .border(
                    width = if (isActive) 1.5.dp else 1.dp,
                    color = if (isActive) NebengColor.Primary900 else NebengColor.Gray200,
                    shape = RoundedCornerShape(NebengRadius.Md)
                )
                .padding(horizontal = NebengSpacing.Lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rp",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) NebengColor.Primary900 else NebengColor.Gray400
            )
            Spacer(modifier = Modifier.width(NebengSpacing.Sm))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(NebengColor.Primary900),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = "Masukkan jumlah...",
                                fontSize = 14.sp,
                                color = NebengColor.Gray400
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun TipSuccessContent(
    amount: Int,
    driverName: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(NebengSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = NebengColor.Primary0,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Xl))

        Text(
            text = "Tip Terkirim!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Sm))

        Text(
            text = "Rp ${formatAmount(amount)} berhasil dikirim ke $driverName.\nTerima kasih sudah mengapresiasi! 🙌",
            fontSize = 14.sp,
            color = NebengColor.Gray600,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Xxxl))

        NebengButton(
            text = "Kembali ke Beranda",
            onClick = onDone,
            style = NebengButtonStyle.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatAmount(amount: Int): String {
    return when {
        amount >= 1_000_000 -> "${amount / 1_000_000}jt"
        amount >= 1_000 -> "${amount / 1_000}rb"
        else -> amount.toString()
    }
}
