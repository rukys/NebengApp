package com.disinidev.nebeng.presentation.profile.edit

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.designsystem.NebengColor

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditProfileTopBar(
                onNavigateBack = onNavigateBack,
                onSaveClick = { viewModel.saveProfile(onNavigateBack) },
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
                    text = "Simpan Perubahan",
                    onClick = { viewModel.saveProfile(onNavigateBack) },
                    style = NebengButtonStyle.PRIMARY,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    isLoading = state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Avatar Section with Camera Badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(NebengColor.Primary900),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.avatarInitials,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary0
                        )
                    }

                    // Camera button overlay at bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 4.dp)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(NebengColor.Primary0)
                            .border(1.5.dp, NebengColor.Primary900, CircleShape)
                            .clickable { viewModel.showMessage("Pilih foto dari galeri") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Ganti Foto",
                            tint = NebengColor.Primary900,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ubah Foto Profil",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    modifier = Modifier.clickable { viewModel.showMessage("Pilih foto dari galeri") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Field 1: Nama Lengkap (Sesuai KTP)
            EditFieldLabel(text = "NAMA LENGKAP (SESUAI KTP)")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = state.fullName,
                    onValueChange = { viewModel.onFullNameChange(it) },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Terverifikasi KTP",
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field 2: Nomor WhatsApp
            EditFieldLabel(text = "NOMOR WHATSAPP")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = state.whatsappNumber,
                    onValueChange = { viewModel.onWhatsappChange(it) },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Utama",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NebengColor.Primary0)
                        .border(1.dp, NebengColor.Gray200, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field 3: Alamat Email
            EditFieldLabel(text = "ALAMAT EMAIL")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "TERVERIFIKASI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary0,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NebengColor.Primary900)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field 4: Tempat Kerja / Gedung Kantor (Highlighted black border)
            EditFieldLabel(text = "TEMPAT KERJA / GEDUNG KANTOR")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary0)
                    .border(1.5.dp, NebengColor.Primary900, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Business,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = state.officeBuilding,
                    onValueChange = { viewModel.onOfficeChange(it) },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field 5: Bio Singkat Rekan Komuter
            EditFieldLabel(text = "BIO SINGKAT REKAN KOMUTER")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                BasicTextField(
                    value = state.bio,
                    onValueChange = { viewModel.onBioChange(it) },
                    minLines = 3,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditProfileTopBar(
    onNavigateBack: () -> Unit,
    onSaveClick: () -> Unit,
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
                .clickable(onClick = onNavigateBack),
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
            text = "Edit Profil",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Simpan",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900,
            modifier = Modifier
                .clickable(onClick = onSaveClick)
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )
    }
}

@Composable
private fun EditFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = NebengColor.Gray600,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
    )
}
