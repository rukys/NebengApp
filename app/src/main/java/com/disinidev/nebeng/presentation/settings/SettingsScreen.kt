package com.disinidev.nebeng.presentation.settings

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.designsystem.NebengColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showSecuritySheet by remember { mutableStateOf(false) }
    var showDocsSheet by remember { mutableStateOf(false) }
    var showNotifSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

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
            SettingsTopBar(
                onNavigateBack = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // User Profile Card
            SettingsProfileCard(
                name = state.fullName,
                email = state.email,
                avatarInitials = state.avatarInitials,
                isVerified = state.isVerified,
                onEditClick = onNavigateToEditProfile,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: AKUN & KEAMANAN
            Text(
                text = "AKUN & KEAMANAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray600,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            // Card 1: Edit Profil & Biodata (Highlighted border black)
            SettingsMenuCard(
                icon = Icons.Default.AccountCircle,
                title = "Edit Profil & Biodata",
                subtitle = "Foto, nama, kontak, info kantor",
                highlighted = true,
                onClick = onNavigateToEditProfile
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Card 2: Keamanan & Sandi
            SettingsMenuCard(
                icon = Icons.Outlined.Lock,
                title = "Keamanan & Sandi",
                subtitle = "PIN NebengPay, ganti password",
                onClick = { showSecuritySheet = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Card 3: Verifikasi Dokumen
            SettingsMenuCard(
                icon = Icons.Outlined.Badge,
                title = "Verifikasi Dokumen",
                subtitle = "KTP, SIM & LinkedIn terverifikasi",
                trailingBadge = "AKTIF ✓",
                onClick = { showDocsSheet = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: PREFERENSI
            Text(
                text = "PREFERENSI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray600,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            // Card 4: Preferensi Notifikasi
            SettingsMenuCard(
                icon = Icons.Outlined.Notifications,
                title = "Preferensi Notifikasi",
                onClick = { showNotifSheet = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Card 5: Bahasa Aplikasi
            SettingsMenuCard(
                icon = Icons.Outlined.Translate,
                title = "Bahasa Aplikasi",
                trailingText = state.selectedLanguage,
                onClick = { showLanguageSheet = true }
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Logout Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NebengColor.Primary50)
                    .clickable { viewModel.showLogoutDialog(true) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = NebengColor.Danger600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Keluar dari Akun",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Danger600
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer note
            Text(
                text = "Nebeng App v2.4.0 • Uber Base UI System",
                fontSize = 11.sp,
                color = NebengColor.Gray400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Logout Confirmation Dialog
    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showLogoutDialog(false) },
            title = {
                Text(
                    text = "Keluar dari Akun?",
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            },
            text = {
                Text(
                    text = "Anda akan keluar dari sesi aplikasi saat ini. Anda perlu masuk kembali untuk mengakses tebengan aktif.",
                    fontSize = 13.sp,
                    color = NebengColor.Gray600,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout {
                            onLogoutSuccess()
                            onNavigateBack()
                        }
                    }
                ) {
                    Text(
                        text = "Keluar",
                        color = NebengColor.Danger600,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showLogoutDialog(false) }) {
                    Text(
                        text = "Batal",
                        color = NebengColor.Primary900
                    )
                }
            },
            containerColor = NebengColor.Primary0
        )
    }

    // Modal Bottom Sheets
    if (showEditProfileSheet) {
        EditProfileBottomSheet(
            name = state.fullName,
            email = state.email,
            onDismiss = { showEditProfileSheet = false }
        )
    }

    if (showSecuritySheet) {
        SecurityBottomSheet(
            onDismiss = { showSecuritySheet = false },
            onNavigateToChangePassword = onNavigateToChangePassword
        )
    }

    if (showDocsSheet) {
        DocumentVerificationBottomSheet(onDismiss = { showDocsSheet = false })
    }

    if (showNotifSheet) {
        NotificationPreferenceBottomSheet(
            enabled = state.isNotificationEnabled,
            onToggle = { viewModel.toggleNotification(it) },
            onDismiss = { showNotifSheet = false }
        )
    }

    if (showLanguageSheet) {
        LanguageBottomSheet(
            selectedLanguage = state.selectedLanguage,
            onSelect = {
                viewModel.setLanguage(it)
                showLanguageSheet = false
            },
            onDismiss = { showLanguageSheet = false }
        )
    }
}

@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit,
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
            text = "Pengaturan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )
    }
}

@Composable
private fun SettingsProfileCard(
    name: String,
    email: String,
    avatarInitials: String,
    isVerified: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NebengColor.Primary50)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Initials avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitials,
                color = NebengColor.Primary0,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                if (isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Terverifikasi",
                        tint = NebengColor.Primary900,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = email,
                fontSize = 12.sp,
                color = NebengColor.Gray600
            )
        }

        // Edit button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(NebengColor.Primary0)
                .border(1.dp, NebengColor.Gray200, RoundedCornerShape(8.dp))
                .clickable(onClick = onEditClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
        }
    }
}

@Composable
private fun SettingsMenuCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    highlighted: Boolean = false,
    trailingBadge: String? = null,
    trailingText: String? = null
) {
    val borderColor = if (highlighted) NebengColor.Primary900 else NebengColor.Gray200
    val borderWidth = if (highlighted) 1.5.dp else 1.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NebengColor.Primary0)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        val iconBg = if (highlighted) NebengColor.Primary900 else NebengColor.Primary50
        val iconTint = if (highlighted) NebengColor.Primary0 else NebengColor.Primary900

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = NebengColor.Gray600
                )
            }
        }

        when {
            trailingBadge != null -> {
                Text(
                    text = trailingBadge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NebengColor.Primary50)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            trailingText != null -> {
                Text(
                    text = trailingText,
                    fontSize = 12.sp,
                    color = NebengColor.Gray600
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = NebengColor.Gray400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ----------------- Bottom Sheets -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    name: String,
    email: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NebengColor.Primary0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Edit Profil & Biodata",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(16.dp)
            ) {
                Text("Nama Lengkap", fontSize = 11.sp, color = NebengColor.Gray600)
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Email", fontSize = 11.sp, color = NebengColor.Gray600)
                Text(email, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Lokasi Kantor Utama", fontSize = 11.sp, color = NebengColor.Gray600)
                Text("SCBD Lot 8, Jakarta Selatan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900)
            }

            Spacer(modifier = Modifier.height(24.dp))

            NebengButton(
                text = "Tutup",
                onClick = onDismiss,
                style = NebengButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToChangePassword: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NebengColor.Primary0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Keamanan & Sandi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(16.dp)
            ) {
                Text("Status PIN Nebeng", fontSize = 11.sp, color = NebengColor.Gray600)
                Text("PIN 6-digit Terpasang ✓", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Metode Autentikasi", fontSize = 11.sp, color = NebengColor.Gray600)
                Text("Nomor HP OTP & Google Account", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900)
            }

            Spacer(modifier = Modifier.height(24.dp))

            NebengButton(
                text = "Ubah Kata Sandi ➔",
                onClick = {
                    onDismiss()
                    onNavigateToChangePassword()
                },
                style = NebengButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            NebengButton(
                text = "Tutup",
                onClick = onDismiss,
                style = NebengButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentVerificationBottomSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NebengColor.Primary0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Verifikasi Dokumen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("e-KTP Nasional", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900, modifier = Modifier.weight(1f))
                    Text("TERVERIFIKASI ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NebengColor.Primary900)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SIM A / C Pengemudi", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900, modifier = Modifier.weight(1f))
                    Text("TERVERIFIKASI ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NebengColor.Primary900)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Email Kantor SCBD", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NebengColor.Primary900, modifier = Modifier.weight(1f))
                    Text("TERVERIFIKASI ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NebengColor.Primary900)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NebengButton(
                text = "Tutup",
                onClick = onDismiss,
                style = NebengButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationPreferenceBottomSheet(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NebengColor.Primary0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Preferensi Notifikasi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebengColor.Primary50)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifikasi Perjalanan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Primary900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Status penjemputan, chat driver, & live tracking",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NebengColor.Primary0,
                        checkedTrackColor = NebengColor.Primary900,
                        uncheckedThumbColor = NebengColor.Primary0,
                        uncheckedTrackColor = NebengColor.Gray200
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NebengButton(
                text = "Tutup",
                onClick = onDismiss,
                style = NebengButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageBottomSheet(
    selectedLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NebengColor.Primary0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Pilih Bahasa",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )

            Spacer(modifier = Modifier.height(16.dp))

            val languages = listOf("Bahasa Indonesia", "English")

            languages.forEach { lang ->
                val isSelected = lang == selectedLanguage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NebengColor.Primary50 else NebengColor.Primary0)
                        .border(
                            1.dp,
                            if (isSelected) NebengColor.Primary900 else NebengColor.Gray200,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(lang) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lang,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = NebengColor.Primary900,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NebengColor.Primary900,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            NebengButton(
                text = "Tutup",
                onClick = onDismiss,
                style = NebengButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
