package com.disinidev.nebeng.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ChatBubble(
    message: String,
    timestamp: String,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
    isRead: Boolean = true
) {
    val horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    val bgColor = if (isFromMe) NebengColor.Primary900 else NebengColor.Primary50
    val textColor = if (isFromMe) NebengColor.Primary0 else NebengColor.Gray800
    val timeColor = if (isFromMe) NebengColor.Gray400 else NebengColor.Gray400

    val shape = if (isFromMe) {
        RoundedCornerShape(
            topStart = NebengRadius.Lg,
            topEnd = NebengRadius.Lg,
            bottomStart = NebengRadius.Lg,
            bottomEnd = NebengRadius.Xs
        )
    } else {
        RoundedCornerShape(
            topStart = NebengRadius.Lg,
            topEnd = NebengRadius.Lg,
            bottomStart = NebengRadius.Xs,
            bottomEnd = NebengRadius.Lg
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bgColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = textColor,
                    lineHeight = 20.sp
                )
                Row(
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timestamp,
                        fontSize = 10.sp,
                        color = timeColor
                    )
                    if (isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRead) "✓✓" else "✓",
                            fontSize = 10.sp,
                            color = timeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
