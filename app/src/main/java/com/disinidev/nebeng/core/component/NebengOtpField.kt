package com.disinidev.nebeng.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

@Composable
fun NebengOtpField(
    otpText: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpCount: Int = 6,
    onComplete: (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = TextFieldValue(text = otpText, selection = TextRange(otpText.length)),
        onValueChange = { newValue ->
            if (newValue.text.length <= otpCount && newValue.text.all { it.isDigit() }) {
                onOtpChange(newValue.text)
                if (newValue.text.length == otpCount) {
                    onComplete?.invoke()
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(0.dp)) {
                    innerTextField()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                repeat(otpCount) { index ->
                    val char = when {
                        index < otpText.length -> otpText[index].toString()
                        else -> ""
                    }
                    val isCurrent = index == otpText.length
                    val isFilled = index < otpText.length

                    val borderColor = when {
                        isCurrent -> NebengColor.Primary900
                        isFilled -> NebengColor.Primary900
                        else -> NebengColor.Gray200
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(NebengRadius.Md))
                            .background(NebengColor.Primary0)
                            .border(1.5.dp, borderColor, RoundedCornerShape(NebengRadius.Md)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebengColor.Gray800,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
)
}
