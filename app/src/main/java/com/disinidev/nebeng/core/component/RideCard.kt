package com.disinidev.nebeng.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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

@Composable
fun RideCard(
    driverName: String,
    vehicleModel: String,
    departureTime: String,
    rating: Float,
    priceFormatted: String,
    modifier: Modifier = Modifier,
    initials: String? = null,
    availableSeats: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    val displayInitials = remember(driverName, initials) {
        initials ?: driverName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .then(clickableModifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Driver Initials Avatar
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

        // Trip Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$driverName • $vehicleModel",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray800
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = departureTime,
                    fontSize = 12.sp,
                    color = NebengColor.Gray600
                )
                Text(
                    text = " • ",
                    fontSize = 12.sp,
                    color = NebengColor.Gray400
                )
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
                if (availableSeats != null) {
                    Text(
                        text = " • $availableSeats kursi",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600
                    )
                }
            }
        }

        // Price
        Text(
            text = priceFormatted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Gray800
        )
    }
}
