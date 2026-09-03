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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

@Composable
fun VoucherCard(
    title: String,
    code: String,
    modifier: Modifier = Modifier,
    actionLabel: String = "Ubah ›",
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.5.dp, NebengColor.Primary900, RoundedCornerShape(NebengRadius.Lg))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = NebengColor.Primary0,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray800
            )
            Text(
                text = "Kode: $code",
                fontSize = 11.sp,
                color = NebengColor.Gray600,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (onActionClick != null) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(start = 8.dp)
            )
        }
    }
}
