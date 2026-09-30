package com.disinidev.nebeng.presentation.settings.password

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengTextField
import com.disinidev.nebeng.core.designsystem.NebengColor

@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            ChangePasswordTopBar(
                onBackClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                NebengButton(
                    text = "Simpan Kata Sandi Baru",
                    onClick = { viewModel.submitChangePassword(onNavigateBack) },
                    style = NebengButtonStyle.PRIMARY,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Security Info Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NebengColor.Primary50)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary900),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = NebengColor.Primary0,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Keamanan Akun Nebeng",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gunakan minimal 6 karakter kombinasi huruf dan angka untuk menjaga keamanan akun Anda.",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message Banner
            if (state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NebengColor.Danger100)
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        fontSize = 12.sp,
                        color = NebengColor.Danger600,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Current Password
            NebengTextField(
                value = state.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                label = "Kata Sandi Saat Ini",
                placeholder = "Masukkan kata sandi saat ini",
                isPassword = true,
                leadingIcon = Icons.Outlined.Lock,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // New Password
            NebengTextField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                label = "Kata Sandi Baru",
                placeholder = "Minimal 6 karakter",
                isPassword = true,
                leadingIcon = Icons.Outlined.Lock,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Confirm Password
            NebengTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Konfirmasi Kata Sandi Baru",
                placeholder = "Ulangi kata sandi baru",
                isPassword = true,
                leadingIcon = Icons.Outlined.Lock,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChangePasswordTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Ubah Kata Sandi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )
    }
}
