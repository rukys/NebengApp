package com.disinidev.nebeng.presentation.checkout

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

@Composable
fun CheckoutCarScreen(
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onConfirmBooking: (rideId: String, seatPosition: String) -> Unit = { _, _ -> }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ride = state.ride

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            CheckoutTopBar(
                title = "Pesan Tebengan Mobil",
                onBackClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                NebengButton(
                    text = "Konfirmasi & Nebeng",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    isLoading = state.isLoading,
                    onClick = {
                        viewModel.confirmBooking { bookingId ->
                            onConfirmBooking(bookingId, state.selectedSeat)
                        }
                    },
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // 1. Driver & Car Card
            DriverVehicleCard(
                driverName = ride?.driverName ?: "Andi Pratama",
                vehicleInfo = ride?.vehicleModel ?: "Toyota Avanza Silver • B 1234 ABC",
                rating = ride?.driverRating ?: 4.9,
                initials = getInitials(ride?.driverName ?: "Andi Pratama")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Pilih Kursi Penumpang Card
            CarSeatSelectionCard(
                selectedSeat = state.selectedSeat,
                onSeatSelected = viewModel::selectSeat
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun CheckoutTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Balanced spacer matching back button size to keep title centered
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
internal fun DriverVehicleCard(
    driverName: String,
    vehicleInfo: String,
    rating: Double,
    initials: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Initials
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary0
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = driverName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = vehicleInfo,
                fontSize = 12.sp,
                color = NebengColor.Gray400
            )
        }

        // Rating Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(NebengColor.Primary50)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFB800),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "%.1f".format(rating),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
        }
    }
}

@Composable
private fun CarSeatSelectionCard(
    selectedSeat: String,
    onSeatSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PILIH KURSI PENUMPANG",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Setir Kanan (ID)",
                fontSize = 12.sp,
                color = NebengColor.Gray400
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Row 1: Depan Kiri & Driver
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isDepanKiri = selectedSeat == "depan_kiri"
            SeatButton(
                text = "Depan Kiri",
                icon = Icons.Default.AirlineSeatReclineNormal,
                isSelected = isDepanKiri,
                onClick = { onSeatSelected("depan_kiri") },
                modifier = Modifier.weight(1f)
            )

            // Driver (Non-selectable)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Driver",
                        tint = NebengColor.Gray600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Driver",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Gray600,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Tengah Kiri & Tengah Kanan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isTengahKiri = selectedSeat == "tengah_kiri"
            SeatButton(
                text = "Tengah Kiri",
                icon = null,
                isSelected = isTengahKiri,
                onClick = { onSeatSelected("tengah_kiri") },
                modifier = Modifier.weight(1f)
            )

            val isTengahKanan = selectedSeat == "tengah_kanan"
            SeatButton(
                text = "Tengah Kanan",
                icon = null,
                isSelected = isTengahKanan,
                onClick = { onSeatSelected("tengah_kanan") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SeatButton(
    text: String,
    icon: ImageVector?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary50)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NebengColor.Primary0,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

internal fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "NB"
    }
}
