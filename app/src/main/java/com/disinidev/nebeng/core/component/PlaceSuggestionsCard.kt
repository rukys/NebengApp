package com.disinidev.nebeng.core.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.PlaceSuggestion

@Composable
fun PlaceSuggestionsCard(
    suggestions: List<PlaceSuggestion>,
    isSearching: Boolean,
    onSelectSuggestion: (PlaceSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp),
        shape = RoundedCornerShape(NebengRadius.Lg),
        color = NebengColor.Primary0,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, NebengColor.Gray200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SARAN LOKASI (OPENSTREETMAP)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Gray600,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Tutup",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }

            if (isSearching) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = NebengColor.Primary900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mencari alamat...",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            suggestions.forEachIndexed { index, suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .clickable { onSelectSuggestion(suggestion) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(NebengColor.Primary50),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📍", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = suggestion.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = suggestion.fullAddress,
                            fontSize = 11.sp,
                            color = NebengColor.Gray600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (index < suggestions.size - 1) {
                    HorizontalDivider(
                        color = NebengColor.Gray100,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
