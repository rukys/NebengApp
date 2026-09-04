package com.disinidev.nebeng.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.Ride
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RideCard(
    driverName: String,
    vehicleModel: String,
    rating: Float,
    totalTrips: Int,
    availableSeats: Int,
    pickupLocation: String,
    pickupTime: String,
    dropoffLocation: String,
    dropoffTime: String,
    priceFormatted: String = "Rp 20.000",
    priceUnit: String = "/kursi",
    initials: String? = null,
    onBookClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    val displayInitials = remember(driverName, initials) {
        initials ?: driverName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .then(clickableModifier)
            .padding(16.dp)
    ) {
        // --- Top Row: Avatar + Info + Seat Badge ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Driver Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Primary900),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayInitials,
                    color = NebengColor.Primary0,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + Rating & Trips
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$driverName • $vehicleModel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = " %.1f".format(rating),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Gray800
                    )
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = NebengColor.Gray400
                    )
                    Text(
                        text = "$totalTrips+ perjalanan",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600
                    )
                }
            }

            // Seat Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Sisa $availableSeats Kursi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Route Container ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(NebengColor.Primary50)
                .padding(12.dp)
        ) {
            // Pickup
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "●",
                    fontSize = 9.sp,
                    color = NebengColor.Primary900,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "$pickupLocation ($pickupTime)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dropoff
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "■",
                    fontSize = 9.sp,
                    color = NebengColor.Primary900,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "$dropoffLocation ($dropoffTime)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Bottom Row: Price + Book Button ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = priceFormatted,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                if (priceUnit.isNotBlank()) {
                    Text(
                        text = " $priceUnit",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(NebengColor.Primary900)
                    .then(
                        if (onBookClick != null) Modifier.clickable(onClick = onBookClick)
                        else clickableModifier
                    )
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pesan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary0
                )
            }
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Jakarta"))

@Composable
fun RideCard(
    ride: Ride,
    modifier: Modifier = Modifier,
    priceFormatted: String = "Rp 20.000",
    onBookClick: ((Ride) -> Unit)? = null,
    onClick: ((Ride) -> Unit)? = null
) {
    val departureTimeFormatted = remember(ride.departureTime) {
        timeFormatter.format(ride.departureTime)
    }
    val arrivalTimeFormatted = remember(ride.departureTime) {
        timeFormatter.format(ride.departureTime.plusSeconds(25 * 60))
    }

    RideCard(
        driverName = ride.driver?.fullName ?: "Driver Nebeng",
        vehicleModel = ride.vehicleInfo.model,
        rating = ride.driver?.averageRating ?: 4.9f,
        totalTrips = ride.driver?.totalTrips ?: 120,
        availableSeats = ride.availableSeats,
        pickupLocation = ride.pickupAddress,
        pickupTime = departureTimeFormatted,
        dropoffLocation = ride.dropoffAddress,
        dropoffTime = arrivalTimeFormatted,
        priceFormatted = priceFormatted,
        initials = null,
        onBookClick = onBookClick?.let { { it(ride) } },
        onClick = onClick?.let { { it(ride) } },
        modifier = modifier
    )
}
