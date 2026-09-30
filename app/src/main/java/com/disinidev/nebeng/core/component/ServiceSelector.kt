package com.disinidev.nebeng.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

enum class ServiceType(val label: String, val icon: ImageVector) {
    CAR("Mobil", Icons.Default.DirectionsCar),
    MOTORCYCLE("Motor", Icons.Default.TwoWheeler),
    OFFER_RIDE("Beri Tebeng", Icons.Default.Commute),
    ROUTINE("Rutin", Icons.Default.CalendarToday)
}

@Composable
fun ServiceSelector(
    selectedService: ServiceType,
    onServiceSelected: (ServiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ServiceType.entries.forEach { service ->
            val isSelected = service == selectedService
            val bgColor = if (isSelected) NebengColor.Primary900 else NebengColor.Primary50
            val contentColor = if (isSelected) NebengColor.Primary0 else NebengColor.Gray800

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(bgColor)
                    .clickable { onServiceSelected(service) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = service.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
