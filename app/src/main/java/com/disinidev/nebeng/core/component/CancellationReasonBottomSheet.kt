package com.disinidev.nebeng.core.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing
import com.disinidev.nebeng.domain.model.CancellationReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancellationReasonBottomSheet(
    onDismiss: () -> Unit,
    onConfirmCancel: (reasonText: String) -> Unit,
    isCancelling: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedReason by remember { mutableStateOf<CancellationReason?>(null) }
    var customNotes by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NebengColor.Primary0,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Row: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batalkan Tebengan?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = NebengColor.Gray600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Bantu kami memahami alasan pembatalan untuk menjaga kenyamanan komunitas Nebeng.",
                fontSize = 13.sp,
                color = NebengColor.Gray600,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Reason Options
            CancellationReason.entries.forEach { reason ->
                val isSelected = selectedReason == reason
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(NebengRadius.Md))
                        .background(if (isSelected) NebengColor.Primary50 else NebengColor.Primary0)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NebengColor.Primary900 else NebengColor.Gray200,
                            shape = RoundedCornerShape(NebengRadius.Md)
                        )
                        .clickable { selectedReason = reason }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Radio Circle
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 6.dp else 1.5.dp,
                                color = if (isSelected) NebengColor.Primary900 else NebengColor.Gray400,
                                shape = CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = reason.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) NebengColor.Primary900 else NebengColor.Gray800,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Animated Extra Input for 'OTHER'
            AnimatedVisibility(
                visible = selectedReason == CancellationReason.OTHER,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    NebengTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        placeholder = "Tulis alasan pembatalan Anda di sini...",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NebengButton(
                    text = "Tetap Nebeng",
                    onClick = onDismiss,
                    style = NebengButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                    enabled = !isCancelling
                )

                NebengButton(
                    text = if (isCancelling) "Membatalkan..." else "Ya, Batalkan",
                    onClick = {
                        val finalReason = when (selectedReason) {
                            CancellationReason.OTHER -> customNotes.ifBlank { "Alasan lainnya" }
                            null -> "Dibatalkan oleh penumpang"
                            else -> selectedReason!!.label
                        }
                        onConfirmCancel(finalReason)
                    },
                    style = NebengButtonStyle.DANGER,
                    isLoading = isCancelling,
                    enabled = selectedReason != null && !isCancelling,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
