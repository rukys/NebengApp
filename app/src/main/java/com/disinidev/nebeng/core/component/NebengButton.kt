package com.disinidev.nebeng.core.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

enum class NebengButtonStyle {
    PRIMARY,
    PILL,
    SECONDARY,
    GHOST,
    DANGER
}

enum class NebengButtonSize(val height: Dp, val fontSize: Int, val horizontalPadding: Dp) {
    LARGE(52.dp, 15, 24.dp),
    MEDIUM(44.dp, 14, 16.dp),
    SMALL(36.dp, 12, 12.dp)
}

@Composable
fun NebengButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NebengButtonStyle = NebengButtonStyle.PRIMARY,
    size: NebengButtonSize = NebengButtonSize.LARGE,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    isFullWidth: Boolean = true
) {
    val shape = when (style) {
        NebengButtonStyle.PILL -> RoundedCornerShape(NebengRadius.Full)
        else -> RoundedCornerShape(NebengRadius.Lg)
    }

    val (containerColor, contentColor, border) = when (style) {
        NebengButtonStyle.PRIMARY -> Triple(
            NebengColor.Primary900,
            NebengColor.Primary0,
            null
        )
        NebengButtonStyle.PILL -> Triple(
            NebengColor.Primary0,
            NebengColor.Primary900,
            BorderStroke(1.5.dp, NebengColor.Primary900)
        )
        NebengButtonStyle.SECONDARY -> Triple(
            NebengColor.Primary50,
            NebengColor.Primary900,
            null
        )
        NebengButtonStyle.GHOST -> Triple(
            Color.Transparent,
            NebengColor.Primary900,
            null
        )
        NebengButtonStyle.DANGER -> Triple(
            NebengColor.Danger600,
            NebengColor.Primary0,
            null
        )
    }

    val widthModifier = if (isFullWidth) modifier.fillMaxWidth() else modifier

    Button(
        onClick = onClick,
        modifier = widthModifier.height(size.height),
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (style == NebengButtonStyle.PRIMARY) NebengColor.DisabledGray else NebengColor.Primary50,
            disabledContentColor = if (style == NebengButtonStyle.PRIMARY) NebengColor.Primary0 else NebengColor.Gray400
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        elevation = null
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text,
                    fontSize = size.fontSize.sp,
                    fontWeight = FontWeight.Bold
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (trailingText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trailingText,
                        fontSize = size.fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NebengMiniButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 56.dp, minHeight = 28.dp),
        shape = RoundedCornerShape(NebengRadius.Full),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) NebengColor.Primary900 else NebengColor.Primary50,
            contentColor = if (isPrimary) NebengColor.Primary0 else NebengColor.Gray800
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        elevation = null
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
