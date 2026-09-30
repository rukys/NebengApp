package com.disinidev.nebeng.core.component

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.SeatPosition
import com.disinidev.nebeng.domain.model.VehicleType

enum class SeatState {
    AVAILABLE,  // Background white, border black/gray — selectable
    SELECTED,   // Background black, text white + checkmark
    TAKEN,      // Background #E2E2E2, disabled
    DRIVER      // Background #EEEEEE, label "Supir", not selectable
}

@Composable
fun SeatSelectorGrid(
    vehicleType: VehicleType,
    seats: Map<String, SeatState>,
    selectedSeat: String?,
    onSeatSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Xl))
            .background(NebengColor.Primary50)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Xl))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Windshield / Heading Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                tint = NebengColor.Gray400,
                modifier = Modifier
                    .size(12.dp)
                    .rotate(0f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DEPAN (ARAH LAJU)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = NebengColor.Gray400
            )
        }

        if (vehicleType == VehicleType.CAR) {
            CarSeatLayout(
                seats = seats,
                selectedSeat = selectedSeat,
                onSeatSelect = onSeatSelect
            )
        } else {
            MotorcycleSeatLayout(
                seats = seats,
                selectedSeat = selectedSeat,
                onSeatSelect = onSeatSelect
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Legend
        SeatLegendRow()
    }
}

@Composable
private fun CarSeatLayout(
    seats: Map<String, SeatState>,
    selectedSeat: String?,
    onSeatSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1 (Front Row): Left = Front Left (passenger), Right = Driver (Indonesia RHD)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val frontLeftKey = SeatPosition.FRONT_LEFT.value
            val frontLeftState = if (selectedSeat == frontLeftKey) {
                SeatState.SELECTED
            } else {
                seats[frontLeftKey] ?: SeatState.AVAILABLE
            }

            SeatItem(
                label = "Depan Kiri",
                state = frontLeftState,
                onClick = { onSeatSelect(frontLeftKey) },
                modifier = Modifier.weight(1f)
            )

            // Driver Seat
            SeatItem(
                label = "Supir",
                state = SeatState.DRIVER,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2 (Rear Row): Left = Rear Left, Center = Rear Center, Right = Rear Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val rearLeftKey = SeatPosition.REAR_LEFT.value
            val rearLeftState = if (selectedSeat == rearLeftKey) {
                SeatState.SELECTED
            } else {
                seats[rearLeftKey] ?: SeatState.AVAILABLE
            }
            SeatItem(
                label = "Belakang Kiri",
                state = rearLeftState,
                onClick = { onSeatSelect(rearLeftKey) },
                modifier = Modifier.weight(1f)
            )

            val rearCenterKey = SeatPosition.REAR_CENTER.value
            val rearCenterState = if (selectedSeat == rearCenterKey) {
                SeatState.SELECTED
            } else {
                seats[rearCenterKey] ?: SeatState.AVAILABLE
            }
            SeatItem(
                label = "Tengah",
                state = rearCenterState,
                onClick = { onSeatSelect(rearCenterKey) },
                modifier = Modifier.weight(1f)
            )

            val rearRightKey = SeatPosition.REAR_RIGHT.value
            val rearRightState = if (selectedSeat == rearRightKey) {
                SeatState.SELECTED
            } else {
                seats[rearRightKey] ?: SeatState.AVAILABLE
            }
            SeatItem(
                label = "Belakang Kanan",
                state = rearRightState,
                onClick = { onSeatSelect(rearRightKey) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MotorcycleSeatLayout(
    seats: Map<String, SeatState>,
    selectedSeat: String?,
    onSeatSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Driver Front
        SeatItem(
            label = "Pengemudi",
            state = SeatState.DRIVER,
            onClick = {},
            modifier = Modifier.width(160.dp)
        )

        // Passenger Pillion
        val pillionKey = SeatPosition.PILLION.value
        val pillionState = if (selectedSeat == pillionKey) {
            SeatState.SELECTED
        } else {
            seats[pillionKey] ?: SeatState.AVAILABLE
        }
        SeatItem(
            label = "Boncengan",
            state = pillionState,
            onClick = { onSeatSelect(pillionKey) },
            modifier = Modifier.width(160.dp)
        )
    }
}

@Composable
private fun SeatItem(
    label: String,
    state: SeatState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClickable = state == SeatState.AVAILABLE || state == SeatState.SELECTED

    val (bgColor, borderColor, contentColor) = when (state) {
        SeatState.AVAILABLE -> Triple(NebengColor.Primary0, NebengColor.Gray400, NebengColor.Primary900)
        SeatState.SELECTED -> Triple(NebengColor.Primary900, NebengColor.Primary900, NebengColor.Primary0)
        SeatState.TAKEN -> Triple(Color(0xFFE2E2E2), Color(0xFFD4D4D4), NebengColor.Gray400)
        SeatState.DRIVER -> Triple(Color(0xFFEEEEEE), Color(0xFFD8D8D8), NebengColor.Gray600)
    }

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(bgColor)
            .border(
                width = if (state == SeatState.SELECTED) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(NebengRadius.Md)
            )
            .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            when (state) {
                SeatState.DRIVER -> {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Supir",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }
                SeatState.SELECTED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Dipilih",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                SeatState.TAKEN -> {
                    Icon(
                        imageVector = Icons.Default.AirlineSeatReclineNormal,
                        contentDescription = "Terisi",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Terisi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                }
                SeatState.AVAILABLE -> {
                    Icon(
                        imageVector = Icons.Default.AirlineSeatReclineNormal,
                        contentDescription = "Tersedia",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatLegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            color = NebengColor.Primary0,
            borderColor = NebengColor.Gray400,
            label = "Tersedia"
        )
        LegendItem(
            color = NebengColor.Primary900,
            borderColor = NebengColor.Primary900,
            label = "Dipilih"
        )
        LegendItem(
            color = Color(0xFFE2E2E2),
            borderColor = Color(0xFFD4D4D4),
            label = "Terisi"
        )
        LegendItem(
            color = Color(0xFFEEEEEE),
            borderColor = Color(0xFFD8D8D8),
            label = "Supir"
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    borderColor: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .border(1.dp, borderColor, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = NebengColor.Gray600
        )
    }
}
