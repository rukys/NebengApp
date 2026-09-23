package com.disinidev.nebeng.presentation.tripdone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.designsystem.NebengColor

@Composable
fun TripDoneScreen(
    onNavigateBack: () -> Unit,
    onSkip: () -> Unit,
    onSubmitComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripDoneViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            TripDoneTopBar(
                onClose = onNavigateBack,
                onSkip = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                NebengButton(
                    text = "Kirim Ulasan & Selesai",
                    onClick = { viewModel.submitRating(onSubmitComplete) },
                    style = NebengButtonStyle.PRIMARY,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    isLoading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Arrival Status Card
            ArrivalStatusCard(
                title = state.title,
                driverName = state.driverName,
                vehicleModel = state.vehicleModel,
                licensePlate = state.licensePlate,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 5-Star Interactive Rating
            StarRatingRow(
                rating = state.rating,
                onRatingChanged = viewModel::onRatingChanged
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Review / Feedback Input (Apresiasi & Tip di-takeout)
            ReviewInputCard(
                reviewText = state.reviewText,
                onReviewTextChanged = viewModel::onReviewTextChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TripDoneTopBar(
    onClose: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = "Penilaian",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Text(
            text = "Lewati",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = NebengColor.Gray600,
            modifier = Modifier
                .clickable { onSkip() }
                .padding(4.dp)
        )
    }
}

@Composable
private fun ArrivalStatusCard(
    title: String,
    driverName: String,
    vehicleModel: String,
    licensePlate: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NebengColor.Primary50)
            .padding(vertical = 28.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = NebengColor.Primary0,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "$driverName • $vehicleModel $licensePlate",
            fontSize = 13.sp,
            color = NebengColor.Gray600
        )
    }
}

@Composable
private fun StarRatingRow(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val isFilled = i <= rating
                Icon(
                    imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Rating $i",
                    tint = if (isFilled) NebengColor.Primary900 else NebengColor.Gray200,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onRatingChanged(i) }
                )
            }
        }
    }
}

@Composable
private fun ReviewInputCard(
    reviewText: String,
    onReviewTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "ULASAN PENGEMUDI (OPSIONAL)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Gray800,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(NebengColor.Primary50)
                .border(1.dp, NebengColor.Gray200, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            if (reviewText.isEmpty()) {
                Text(
                    text = "Tulis pengalaman atau catatan perjalananmu...",
                    fontSize = 13.sp,
                    color = NebengColor.Gray400
                )
            }
            BasicTextField(
                value = reviewText,
                onValueChange = onReviewTextChanged,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = NebengColor.Primary900,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(NebengColor.Primary900),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
