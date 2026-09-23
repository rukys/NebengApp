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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

@Composable
fun CheckoutMotorScreen(
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onConfirmBooking: (rideId: String, helmetChoice: String) -> Unit = { _, _ -> }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ride = state.ride

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            CheckoutTopBar(
                title = "Pesan Tebengan Motor",
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
                    onClick = {
                        viewModel.confirmBooking()
                        ride?.let {
                            onConfirmBooking(
                                it.id,
                                if (state.helmetOption == HelmetOption.DRIVER_HELMET) "driver_helmet" else "bring_own"
                            )
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
            // 1. Driver & Motor Card
            DriverVehicleCard(
                driverName = ride?.driverName ?: "Reza Hendra",
                vehicleInfo = ride?.vehicleModel ?: "Yamaha NMAX Hitam • B 5678 XYZ",
                rating = ride?.driverRating ?: 4.8,
                initials = getInitials(ride?.driverName ?: "Reza Hendra")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Perlengkapan Helm & Jas Hujan Card
            MotorEquipmentCard(
                selectedOption = state.helmetOption,
                onOptionSelected = viewModel::selectHelmetOption
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MotorEquipmentCard(
    selectedOption: HelmetOption,
    onOptionSelected: (HelmetOption) -> Unit,
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
        Text(
            text = "PERLENGKAPAN HELM & JAS HUJAN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Options Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isDriverHelmet = selectedOption == HelmetOption.DRIVER_HELMET
            HelmetButton(
                text = "Pakai Helm Driver",
                isSelected = isDriverHelmet,
                onClick = { onOptionSelected(HelmetOption.DRIVER_HELMET) },
                modifier = Modifier.weight(1f)
            )

            val isBringOwn = selectedOption == HelmetOption.BRING_OWN
            HelmetButton(
                text = "Bawa Sendiri",
                isSelected = isBringOwn,
                onClick = { onOptionSelected(HelmetOption.BRING_OWN) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Disediakan pelindung kepala steril & jas hujan jika cuaca mendung.",
            fontSize = 11.sp,
            color = NebengColor.Gray400,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun HelmetButton(
    text: String,
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
