package com.disinidev.nebeng.presentation.auth.setup

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun SetupProfileScreen(
    phoneNumber: String,
    onNavigateBack: () -> Unit,
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetupProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val initials = remember(uiState.fullName) {
        uiState.fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
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
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top Bar ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = NebengColor.Primary900
                )
            }

            Text(
                text = "Edit Profil",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = "Simpan",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NebengColor.Primary900,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::saveProfile
                    )
            )
        }

        // --- Scrollable Form Content ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Avatar with Camera Badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(NebengColor.Primary900),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Camera Icon Badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, NebengColor.Primary900, CircleShape)
                            .clickable { /* Select Photo */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Ganti Foto",
                            tint = NebengColor.Primary900,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ubah Foto Profil",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Field: Nama Lengkap
            FormLabel(text = "NAMA LENGKAP (SESUAI KTP)")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = uiState.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900
                )

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary900)
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Field: Nomor WhatsApp
            FormLabel(text = "NOMOR WHATSAPP")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "WhatsApp",
                        tint = NebengColor.Primary900,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.phoneNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Primary900
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(Color.White)
                        .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Full))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "Utama",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Gray800
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Field: Alamat Email
            FormLabel(text = "ALAMAT EMAIL")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = uiState.email,
                    fontSize = 14.sp,
                    color = NebengColor.Primary900
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NebengColor.Primary900)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "TERVERIFIKASI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Field: Tempat Kerja / Gedung Kantor
            FormLabel(text = "TEMPAT KERJA / GEDUNG KANTOR")
            NebengTextField(
                value = uiState.workplace,
                onValueChange = viewModel::onWorkplaceChange,
                placeholder = "Nama perusahaan atau gedung kantor",
                leadingIcon = Icons.Default.Apartment
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Field: Bio Singkat
            FormLabel(text = "BIO SINGKAT REKAN KOMUTER")
            NebengTextField(
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                placeholder = "Ceritakan rutinitas komuter Anda...",
                singleLine = false,
                minLines = 3,
                maxLines = 4
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    fontSize = 12.sp,
                    color = NebengColor.Danger600
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Bottom CTA Button ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp)
        ) {
            NebengButton(
                text = "Simpan Perubahan",
                trailingText = "→",
                onClick = viewModel::saveProfile,
                isLoading = uiState.isLoading,
                style = NebengButtonStyle.PRIMARY
            )
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = NebengColor.Gray600,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}
