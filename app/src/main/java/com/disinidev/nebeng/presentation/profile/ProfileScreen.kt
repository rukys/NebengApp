package com.disinidev.nebeng.presentation.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengBottomNav
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengTab
import com.disinidev.nebeng.core.designsystem.NebengColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onTabSelected: (NebengTab) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSosSheet by remember { mutableStateOf(false) }
    var showFaqSheet by remember { mutableStateOf(false) }
    var showVehicleSheet by remember { mutableStateOf(false) }
    var showScheduleSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

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
            ProfileTopBar(
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        },
        bottomBar = {
            NebengBottomNav(
                selectedTab = NebengTab.AKUN,
                onTabSelected = onTabSelected
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

            // Role Switcher Pill (Penumpang vs Pengemudi)
            RoleSelectorPill(
                selectedRole = state.selectedRole,
                onRoleSelected = { viewModel.toggleRole(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Info Card
            UserInfoCard(
                name = state.fullName,
                contactInfo = "${state.phoneNumber} • ${state.email}",
                avatarInitial = state.avatarInitials,
                onClick = onNavigateToEditProfile,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Card (Rating, Tebeng, Hemat CO2)
            StatsCard(
                rating = state.rating,
                tripCount = state.tripCount,
                co2Saved = state.co2SavedKg,
                isPassenger = state.selectedRole == ProfileRole.PASSENGER,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Menu Items List (Voucher ditiadakan)
            if (state.selectedRole == ProfileRole.PASSENGER) {
                ProfileMenuItem(
                    icon = Icons.Outlined.Security,
                    title = "Kontak Darurat & SOS",
                    onClick = { showSosSheet = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileMenuItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Pusat Bantuan & FAQ",
                    onClick = { showFaqSheet = true }
                )
            } else {
                ProfileMenuItem(
                    icon = Icons.Outlined.DirectionsCar,
                    title = "Kendaraan Saya",
                    onClick = { showVehicleSheet = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileMenuItem(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Jadwal Tebengan Saya",
                    onClick = { showScheduleSheet = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileMenuItem(
                    icon = Icons.Outlined.Security,
                    title = "Kontak Darurat & SOS",
                    onClick = { showSosSheet = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileMenuItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Pusat Bantuan & FAQ",
                    onClick = { showFaqSheet = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheets for Actions
    if (showSosSheet) {
        SosBottomSheet(onDismiss = { showSosSheet = false })
    }

    if (showFaqSheet) {
        FaqBottomSheet(onDismiss = { showFaqSheet = false })
    }

    if (showVehicleSheet) {
        VehicleBottomSheet(onDismiss = { showVehicleSheet = false })
    }

    if (showScheduleSheet) {
        ScheduleBottomSheet(onDismiss = { showScheduleSheet = false })
    }

    if (showSettingsSheet) {
        SettingsBottomSheet(
            onDismiss = { showSettingsSheet = false },
            onLogout = {
                showSettingsSheet = false
                viewModel.showMessage("Fitur logout akan dialihkan ke layar Auth")
            }
        )
    }
}

@Composable
private fun ProfileTopBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Profil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Pengaturan",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RoleSelectorPill(
    selectedRole: ProfileRole,
    onRoleSelected: (ProfileRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(NebengColor.Primary50)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            val isPassenger = selectedRole == ProfileRole.PASSENGER

            val passengerBg by animateColorAsState(
                targetValue = if (isPassenger) NebengColor.Primary900 else Color.Transparent,
                animationSpec = tween(200),
                label = "passengerBg"
            )
            val passengerContentColor by animateColorAsState(
                targetValue = if (isPassenger) NebengColor.Primary0 else NebengColor.Gray600,
                animationSpec = tween(200),
                label = "passengerColor"
            )

            val driverBg by animateColorAsState(
                targetValue = if (!isPassenger) NebengColor.Primary900 else Color.Transparent,
                animationSpec = tween(200),
                label = "driverBg"
            )
            val driverContentColor by animateColorAsState(
                targetValue = if (!isPassenger) NebengColor.Primary0 else NebengColor.Gray600,
                animationSpec = tween(200),
                label = "driverColor"
            )

            // Tab Penumpang
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(passengerBg)
                    .clickable { onRoleSelected(ProfileRole.PASSENGER) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = passengerContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Penumpang",
                        fontSize = 13.sp,
                        fontWeight = if (isPassenger) FontWeight.Bold else FontWeight.Medium,
                        color = passengerContentColor
                    )
                }
            }

            // Tab Pengemudi
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(driverBg)
                    .clickable { onRoleSelected(ProfileRole.DRIVER) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SteeringWheelCanvasIcon(
                        tint = driverContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pengemudi",
                        fontSize = 13.sp,
                        fontWeight = if (!isPassenger) FontWeight.Bold else FontWeight.Medium,
                        color = driverContentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SteeringWheelCanvasIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.6.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer ring
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        // Center hub
        drawCircle(
            color = tint,
            radius = radius * 0.28f,
            center = center,
            style = Fill
        )
        // Horizontal spoke left
        drawLine(
            color = tint,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x - radius * 0.28f, center.y),
            strokeWidth = strokeWidth
        )
        // Horizontal spoke right
        drawLine(
            color = tint,
            start = Offset(center.x + radius * 0.28f, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth
        )
        // Bottom spoke
        drawLine(
            color = tint,
            start = Offset(center.x, center.y + radius * 0.28f),
            end = Offset(center.x, center.y + radius),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun UserInfoCard(
    name: String,
    contactInfo: String,
    avatarInitial: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NebengColor.Primary50)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle Avatar with Initial
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitial,
                color = NebengColor.Primary0,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = contactInfo,
                fontSize = 12.sp,
                color = NebengColor.Gray600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatsCard(
    rating: Float,
    tripCount: Int,
    co2Saved: Int,
    isPassenger: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rating Column
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = String.format("%.1f", rating),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Rating",
                fontSize = 12.sp,
                color = NebengColor.Gray400
            )
        }

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(28.dp)
                .background(NebengColor.Gray200)
        )

        // Trips Column
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${tripCount}x",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (isPassenger) "Tebeng" else "Beri Tebeng",
                fontSize = 12.sp,
                color = NebengColor.Gray400
            )
        }

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(28.dp)
                .background(NebengColor.Gray200)
        )

        // CO2 Column
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$co2Saved kg",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Hemat CO₂",
                fontSize = 12.sp,
                color = NebengColor.Gray400
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NebengColor.Primary50)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NebengColor.Primary900,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = NebengColor.Primary900,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = NebengColor.Gray400,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ----------------- Bottom Sheets -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SosBottomSheet(onDismiss: () -> Unit) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = NebengColor.Danger600,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Kontak Darurat & SOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Kontak berikut akan otomatis menerima koordinat GPS dan detail tebengan kamu saat tombol SOS ditekan selama perjalanan aktif.",
                fontSize = 13.sp,
                color = NebengColor.Gray600,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            EmergencyContactItem(
                name = "Keluarga / Orang Tua",
                number = "+62 812-3344-5566",
                tag = "Prioritas 1"
            )

            Spacer(modifier = Modifier.height(8.dp))

            EmergencyContactItem(
                name = "Rekan Kerja SCBD",
                number = "+62 811-9988-7766",
                tag = "Prioritas 2"
            )

            Spacer(modifier = Modifier.height(8.dp))

            EmergencyContactItem(
                name = "Layanan Darurat Polisi",
                number = "110",
                tag = "Nasional"
            )

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

@Composable
private fun EmergencyContactItem(
    name: String,
    number: String,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NebengColor.Primary50)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = null,
            tint = NebengColor.Primary900,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NebengColor.Primary900
            )
            Text(
                text = number,
                fontSize = 12.sp,
                color = NebengColor.Gray600
            )
        }
        Text(
            text = tag,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = NebengColor.Gray600,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(NebengColor.Gray200)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaqBottomSheet(onDismiss: () -> Unit) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pusat Bantuan & FAQ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FaqItem(
                question = "Apakah Nebeng benar-benar 100% gratis?",
                answer = "Ya, Nebeng dibuat untuk gerakan komuter ramah lingkungan tanpa tarif komersial dan tanpa komisi platform."
            )

            Spacer(modifier = Modifier.height(12.dp))

            FaqItem(
                question = "Bagaimana jika pengemudi meminta bayaran?",
                answer = "Nebeng melarang penarikan ongkos komersial. Jika terjadi pemaksaan, laporkan nomor plat pengemudi melalui menu kontak darurat."
            )

            Spacer(modifier = Modifier.height(12.dp))

            FaqItem(
                question = "Apakah saya bisa memberikan apresiasi tip?",
                answer = "Secara sukarela setelah perjalanan selesai, Anda dapat memindai QRIS driver sendiri secara mandiri tanpa potongan platform."
            )

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

@Composable
private fun FaqItem(question: String, answer: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NebengColor.Primary50)
            .padding(14.dp)
    ) {
        Text(
            text = question,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = answer,
            fontSize = 12.sp,
            color = NebengColor.Gray600,
            lineHeight = 18.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleBottomSheet(onDismiss: () -> Unit) {
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
                text = "Kendaraan Saya",
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
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Toyota Avanza Silver",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "B 1234 ABC • Kapasitas 4 Penumpang",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600
                    )
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
private fun ScheduleBottomSheet(onDismiss: () -> Unit) {
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
                text = "Jadwal Tebengan Rutin",
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
                Text(
                    text = "Senin – Jumat, 07:00 WIB",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 Tebet, Jaksel ➔ SCBD Lot 8",
                    fontSize = 13.sp,
                    color = NebengColor.Gray800
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3 Kursi Tersedia • Toyota Avanza Silver",
                    fontSize = 12.sp,
                    color = NebengColor.Gray600
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
private fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    onLogout: () -> Unit
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
                text = "Pengaturan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nebeng App v1.0.0 (Build 14)\nKomunitas Nebeng Komuter Jabodetabek",
                fontSize = 13.sp,
                color = NebengColor.Gray600,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            NebengButton(
                text = "Keluar dari Akun",
                onClick = onLogout,
                style = NebengButtonStyle.DANGER,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            NebengButton(
                text = "Tutup",
                onClick = onDismiss,
                style = NebengButtonStyle.GHOST,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
