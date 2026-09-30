package com.disinidev.nebeng.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

enum class BadgeVariant {
    DISCOUNT,     // Black bg, yellow lightning
    VERIFIED,     // Green bg, white check
    QRIS,         // Red bg, white bold text
    INFO,         // Light gray bg, dark text (e.g. Sisa Kursi)
    OUTLINE       // White bg, outline
}

@Composable
fun NebengBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.INFO,
    icon: ImageVector? = null
) {
    val (bgColor, textColor, defaultIcon, iconTint) = when (variant) {
        BadgeVariant.DISCOUNT -> Quadruple(
            NebengColor.Primary900,
            NebengColor.Primary0,
            Icons.Default.Bolt,
            Color(0xFFFFD700) // Gold/Yellow
        )
        BadgeVariant.VERIFIED -> Quadruple(
            NebengColor.Success700,
            NebengColor.Primary0,
            Icons.Default.Check,
            NebengColor.Primary0
        )
        BadgeVariant.QRIS -> Quadruple(
            NebengColor.QrisRed,
            NebengColor.Primary0,
            null,
            Color.Unspecified
        )
        BadgeVariant.INFO -> Quadruple(
            NebengColor.Primary50,
            NebengColor.Gray800,
            null,
            Color.Unspecified
        )
        BadgeVariant.OUTLINE -> Quadruple(
            NebengColor.Primary0,
            NebengColor.Gray800,
            null,
            Color.Unspecified
        )
    }

    val activeIcon = icon ?: defaultIcon
    val shape = RoundedCornerShape(NebengRadius.Sm)
    val borderModifier = if (variant == BadgeVariant.OUTLINE) {
        Modifier.border(1.dp, NebengColor.Gray200, shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .then(borderModifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (activeIcon != null) {
                Icon(
                    imageVector = activeIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
