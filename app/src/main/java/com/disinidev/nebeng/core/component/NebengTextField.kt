package com.disinidev.nebeng.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing

@Composable
fun NebengTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    isSearch: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isError = !errorMessage.isNullOrEmpty()

    val backgroundColor = if (isFocused) NebengColor.Primary0 else NebengColor.Primary50
    val borderColor = when {
        isError -> NebengColor.Danger600
        isFocused -> NebengColor.Primary900
        else -> NebengColor.Primary50
    }
    val borderWidth = if (isFocused || isError) 1.5.dp else 1.dp

    Column(modifier = modifier.fillMaxWidth()) {
        if (!label.isNullOrEmpty()) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NebengColor.Gray800,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(backgroundColor)
                .border(borderWidth, borderColor, RoundedCornerShape(NebengRadius.Md)),
            enabled = enabled,
            singleLine = singleLine,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = if (enabled) NebengColor.Gray800 else NebengColor.Gray400
            ),
            cursorBrush = SolidColor(NebengColor.Primary900),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NebengSpacing.Lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isFocused) NebengColor.Primary900 else NebengColor.Gray400
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = NebengColor.Gray400,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }

                    when {
                        isPassword -> {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = NebengColor.Gray400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        isSearch && value.isNotEmpty() -> {
                            IconButton(
                                onClick = { onValueChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = NebengColor.Gray400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        trailingIcon != null -> {
                            IconButton(
                                onClick = { onTrailingIconClick?.invoke() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = trailingIcon,
                                    contentDescription = null,
                                    tint = NebengColor.Gray400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        )

        if (isError) {
            Text(
                text = errorMessage.orEmpty(),
                fontSize = 12.sp,
                color = NebengColor.Danger600,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
