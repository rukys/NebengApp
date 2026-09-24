package com.disinidev.nebeng.presentation.driver.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.repository.DriverBookingRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRequestsBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverRequestsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NebengColor.Primary0,
        shape = RoundedCornerShape(topStart = NebengRadius.Xl, topEnd = NebengRadius.Xl),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Permintaan Penumpang",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NebengColor.Primary900)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${state.requests.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary0
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary50)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = NebengColor.Primary900,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NebengColor.Gray200,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum ada permintaan tebengan baru",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = NebengColor.Gray400
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    state.requests.forEach { request ->
                        PassengerRequestCard(
                            request = request,
                            onAccept = { viewModel.acceptRequest(request.bookingId) },
                            onReject = { viewModel.rejectRequest(request.bookingId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PassengerRequestCard(
    request: DriverBookingRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
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
        // Passenger Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Primary900),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = request.passengerName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    color = NebengColor.Primary0,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.passengerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                Text(
                    text = "Posisi: ${request.seatPosition}",
                    fontSize = 12.sp,
                    color = NebengColor.Gray600
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Route details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(NebengColor.Primary50)
                .padding(10.dp)
        ) {
            Text(
                text = "📍 Jemput: ${request.pickupAddress}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🏁 Tujuan: ${request.dropoffAddress}",
                fontSize = 12.sp,
                color = NebengColor.Gray600
            )
            if (!request.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💬 \"${request.notes}\"",
                    fontSize = 12.sp,
                    color = NebengColor.Gray600,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row (Tolak vs Terima)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tolak Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Primary0)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                    .clickable(onClick = onReject),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tolak",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Gray600
                )
            }

            // Terima Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Primary900)
                    .clickable(onClick = onAccept),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NebengColor.Primary0,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Terima",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary0
                    )
                }
            }
        }
    }
}
