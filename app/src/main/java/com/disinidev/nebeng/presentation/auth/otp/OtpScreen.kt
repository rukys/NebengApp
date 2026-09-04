package com.disinidev.nebeng.presentation.auth.otp

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengOtpField
import com.disinidev.nebeng.core.designsystem.NebengColor

@Composable
fun OtpScreen(
    phoneNumber: String,
    onNavigateBack: () -> Unit,
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(phoneNumber) {
        if (phoneNumber.isNotBlank()) {
            viewModel.setPhoneNumber(phoneNumber)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToNext()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NebengColor.Primary0)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // App Bar with Back Arrow and Centered Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NebengColor.Primary900
                    )
                }

                Text(
                    text = "Verifikasi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Gray800,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Masukkan 6-digit kode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray800
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle & WhatsApp Phone Number
            Text(
                text = "Kode keamanan telah dikirim via WhatsApp ke nomor",
                fontSize = 14.sp,
                color = NebengColor.Gray600,
                lineHeight = 20.sp
            )
            Text(
                text = uiState.phoneNumber,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 6-digit OTP Field
            NebengOtpField(
                otpText = uiState.otpCode,
                onOtpChange = viewModel::onOtpChange,
                otpCount = 6,
                onComplete = viewModel::verifyOtp
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    fontSize = 12.sp,
                    color = NebengColor.Danger600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Countdown Timer & Resend
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val formattedTime = "00:%02d".format(uiState.countdownSeconds)
                Text(
                    text = if (uiState.countdownSeconds > 0) {
                        "Kirim ulang kode dalam $formattedTime"
                    } else {
                        "Kirim ulang kode via WhatsApp"
                    },
                    fontSize = 13.sp,
                    color = if (uiState.canResend) NebengColor.Primary900 else NebengColor.Gray600,
                    fontWeight = if (uiState.canResend) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable(enabled = uiState.canResend) {
                        viewModel.resendOtp(viaSms = false)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Kirim via SMS Reguler",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Gray800,
                    modifier = Modifier.clickable {
                        viewModel.resendOtp(viaSms = true)
                    }
                )
            }
        }

        // Bottom CTA Button: Verifikasi & Masuk →
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            NebengButton(
                text = "Verifikasi & Masuk",
                trailingText = "→",
                onClick = viewModel::verifyOtp,
                isLoading = uiState.isLoading,
                enabled = uiState.otpCode.length == 6,
                style = NebengButtonStyle.PRIMARY
            )
        }
    }
}
