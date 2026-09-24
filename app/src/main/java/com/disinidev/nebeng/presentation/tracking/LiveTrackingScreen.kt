package com.disinidev.nebeng.presentation.tracking

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.R
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.VehicleType

@Composable
fun LiveTrackingScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveTrackingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToChat: (driverName: String, bookingId: String) -> Unit = { _, _ -> },
    onTripFinished: (bookingId: String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Interactive OpenStreetMap (MapLibre Native)
        TrackingMapView(
            modifier = Modifier.fillMaxSize()
        )

        // 2. Pointing Tracking Map Overlay (Route Polylines, Needles & Pulse Radar)
        LiveTrackingMapOverlay(
            state = state,
            onRecenterClick = viewModel::startLiveTrackingSimulation,
            onGpsBadgeClick = viewModel::broadcastCurrentDeviceGps,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Top Navigation Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (White Circle)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(NebengColor.Primary0)
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

                Spacer(modifier = Modifier.weight(1f))

                // ETA Pill (Center - clickable to trigger trip completion)
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(NebengRadius.Full))
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(NebengColor.Primary900)
                        .clickable { onTripFinished(state.bookingId) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NebengColor.Primary0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isArrived) "Driver Tiba • Siap Berangkat" else "Tiba dlm ${state.etaMinutes} mnt • ${state.distanceMeters}m",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary0,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Safety / SOS Button (White Circle with Red Shield)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(NebengColor.Primary0)
                        .clickable { viewModel.showEmergencyDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Pusat Bantuan & Keamanan",
                        tint = NebengColor.Danger600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Bottom Floating Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(NebengColor.Primary0)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                // Header Row: Driver Status & PIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.statusText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.isArrived) "Tunjukkan PIN jemput kepada pengemudi" else "${state.vehicleModel} • ${state.vehiclePlate}",
                            fontSize = 12.sp,
                            color = if (state.isArrived) NebengColor.Primary900 else NebengColor.Gray400,
                            fontWeight = if (state.isArrived) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // PIN Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NebengColor.Primary900)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PIN: ${state.bookingPin}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary0,
                            letterSpacing = 0.5.sp,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row: Bagikan Rute & Chat Driver / Mulai Perjalanan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button: Bagikan Rute
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(NebengRadius.Lg))
                            .background(NebengColor.Primary50)
                            .clickable { shareTripDetails(context, state) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = NebengColor.Primary900,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bagikan Rute",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebengColor.Primary900,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }

                    // Button: Chat Driver OR Mulai Perjalanan (if arrived)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(NebengRadius.Lg))
                            .background(NebengColor.Primary900)
                            .clickable {
                                if (state.isArrived) {
                                    onTripFinished(state.bookingId)
                                } else {
                                    onNavigateToChat(state.driverName, state.bookingId)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (state.isArrived) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = NebengColor.Primary0,
                                modifier = Modifier
                                    .size(16.dp)
                                    .then(if (state.isArrived) Modifier.clip(CircleShape) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isArrived) "Mulai Perjalanan ➔" else "Chat Driver",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebengColor.Primary0,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. Emergency SOS Dialog
        if (state.isEmergencyDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.showEmergencyDialog(false) },
                title = {
                    Text(
                        text = "Pusat Bantuan & Keamanan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NebengColor.Primary900
                    )
                },
                text = {
                    Text(
                        text = "Jika terjadi situasi darurat selama perjalanan, Anda dapat menghubungi pusat bantuan darurat 112 atau bagikan lokasi live ke kontak darurat.",
                        fontSize = 13.sp,
                        color = NebengColor.Gray600,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.showEmergencyDialog(false)
                            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:112")
                            }
                            context.startActivity(callIntent)
                        }
                    ) {
                        Text(
                            text = "Hubungi 112",
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Danger600
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showEmergencyDialog(false) }) {
                        Text(
                            text = "Tutup",
                            color = NebengColor.Primary900
                        )
                    }
                },
                containerColor = NebengColor.Primary0,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

private fun shareTripDetails(context: Context, state: LiveTrackingUiState) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Saya sedang nebeng bersama ${state.driverName} (${state.vehicleModel} • ${state.vehiclePlate}) menuju ${state.destinationLocation}. Lacak perjalanan saya di Nebeng!"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Rute Perjalanan")
    context.startActivity(shareIntent)
}
