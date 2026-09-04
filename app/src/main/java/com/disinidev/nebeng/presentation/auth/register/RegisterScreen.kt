package com.disinidev.nebeng.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengTextField
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

@Composable
fun RegisterScreen(
    onNavigateToOtp: (phoneNumber: String, fullName: String, email: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToOtp(uiState.phoneNumber, uiState.fullName, uiState.email)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NebengColor.Primary0)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo Header: Black box with "N" + "Nebeng"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NebengColor.Primary900),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Nebeng",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title & Subtitle
            Text(
                text = "Buat akun baru",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray800
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mulai perjalanan hemat dan kurangi macet harian.",
                fontSize = 14.sp,
                color = NebengColor.Gray600
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Field
            NebengTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Nama Lengkap",
                placeholder = "Nama sesuai KTP",
                leadingIcon = Icons.Default.PersonOutline
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email Field
            NebengTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                placeholder = "nama@email.com",
                leadingIcon = Icons.Default.MailOutline,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // WhatsApp Phone Field
            NebengTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                label = "Nomor WhatsApp",
                placeholder = "0812 3456 7890",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            NebengTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Kata Sandi",
                placeholder = "Min. 8 karakter",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms Agreement Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { viewModel.onTermsAgreedChange(!uiState.termsAgreed) }
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (uiState.termsAgreed) NebengColor.Primary900 else NebengColor.Primary0)
                        .border(
                            1.5.dp,
                            if (uiState.termsAgreed) NebengColor.Primary900 else NebengColor.Gray400,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.termsAgreed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Saya setuju dengan Syarat Layanan & Kebijakan Privasi.",
                    fontSize = 12.sp,
                    color = NebengColor.Gray800
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    fontSize = 12.sp,
                    color = NebengColor.Danger600
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Register CTA Button
            NebengButton(
                text = "Daftar Akun",
                trailingText = "→",
                onClick = viewModel::register,
                isLoading = uiState.isLoading,
                style = NebengButtonStyle.PRIMARY
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Google Register Button
            Button(
                onClick = { /* Google Sign In */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.5.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg)),
                shape = RoundedCornerShape(NebengRadius.Lg),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NebengColor.Primary0,
                    contentColor = NebengColor.Primary900
                ),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Daftar dengan Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    )
                }
            }
        }

        // Bottom Footer: Sudah memiliki akun? Masuk
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah memiliki akun? ",
                fontSize = 13.sp,
                color = NebengColor.Gray600
            )
            Text(
                text = "Masuk",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNavigateToLogin
                )
            )
        }
    }
}
