package com.disinidev.nebeng.presentation.driver.offer

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.PlaceSuggestion

@Composable
fun OfferRideScreen(
    modifier: Modifier = Modifier,
    viewModel: OfferRideViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onPublishSuccess: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            OfferRideTopBar(
                onBackClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                NebengButton(
                    text = if (state.isPublishing) "Mempublikasikan..." else "Publikasikan Tebengan",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        viewModel.publishRide(onSuccess = onPublishSuccess)
                    },
                    enabled = !state.isPublishing &&
                        state.pickupAddress.isNotBlank() &&
                        state.dropoffAddress.isNotBlank() &&
                        state.vehiclePlate.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Error banner if any
            if (state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NebengRadius.Md))
                        .background(NebengColor.Danger100)
                        .border(1.dp, NebengColor.Danger600.copy(alpha = 0.5f), RoundedCornerShape(NebengRadius.Md))
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        fontSize = 13.sp,
                        color = NebengColor.Danger600
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 1. Vehicle Type Selector (Mobil vs Motor)
            Text(
                text = "Pilih Jenis Kendaraan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VehicleTypeOption(
                    title = "Mobil",
                    subtitle = "Maks 4 Penumpang",
                    icon = Icons.Default.DirectionsCar,
                    isSelected = state.vehicleType == "car",
                    onClick = { viewModel.onVehicleTypeChange("car") },
                    modifier = Modifier.weight(1f)
                )
                VehicleTypeOption(
                    title = "Motor",
                    subtitle = "1 Penumpang (Bonceng)",
                    icon = Icons.Default.TwoWheeler,
                    isSelected = state.vehicleType == "motorcycle",
                    onClick = { viewModel.onVehicleTypeChange("motorcycle") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Route Inputs (Titik Berangkat & Tujuan)
            Text(
                text = "Rute Perjalanan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(8.dp))
            DriverRouteInputCard(
                pickupAddress = state.pickupAddress,
                dropoffAddress = state.dropoffAddress,
                onPickupChange = viewModel::onPickupChange,
                onDropoffChange = viewModel::onDropoffChange,
                suggestions = state.suggestions,
                isSearching = state.isSearchingPlaces,
                onSelectSuggestion = viewModel::selectSuggestion,
                onDismissSuggestions = viewModel::closeSuggestions
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Schedule & Available Seats
            Text(
                text = "Jadwal & Kursi Tersedia",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(NebengColor.Primary50)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
                    .padding(16.dp)
            ) {
                // Jam Berangkat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NebengColor.Primary900,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Waktu Berangkat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Primary900,
                        modifier = Modifier.weight(1f)
                    )
                    BasicTextField(
                        value = state.departureTime,
                        onValueChange = viewModel::onDepartureTimeChange,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .clip(RoundedCornerShape(NebengRadius.Md))
                            .background(NebengColor.Primary0)
                            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = NebengColor.Gray200
                )

                // Kursi Tersedia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kursi Kosong",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = NebengColor.Primary900
                        )
                        Text(
                            text = if (state.vehicleType == "motorcycle") "1 Helm penumpang" else "Kapasitas penumpang",
                            fontSize = 12.sp,
                            color = NebengColor.Gray400
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NebengColor.Primary0)
                                .border(1.dp, NebengColor.Gray200, CircleShape)
                                .clickable(enabled = state.availableSeats > 1, onClick = viewModel::decrementSeats),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Kurang",
                                tint = if (state.availableSeats > 1) NebengColor.Primary900 else NebengColor.Gray400,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "${state.availableSeats} Kursi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NebengColor.Primary0)
                                .border(1.dp, NebengColor.Gray200, CircleShape)
                                .clickable(
                                    enabled = (state.vehicleType == "car" && state.availableSeats < 6),
                                    onClick = viewModel::incrementSeats
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah",
                                tint = if (state.vehicleType == "car" && state.availableSeats < 6) NebengColor.Primary900 else NebengColor.Gray400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Vehicle Details (Model & Plat Nomor)
            Text(
                text = "Informasi Kendaraan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(NebengColor.Primary50)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Model Kendaraan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Gray600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = state.vehicleModel,
                        onValueChange = viewModel::onVehicleModelChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NebengColor.Primary900
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NebengRadius.Md))
                            .background(NebengColor.Primary0)
                            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                            .padding(12.dp)
                    )
                }

                Column {
                    Text(
                        text = "Nomor Plat Polisi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Gray600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = state.vehiclePlate,
                        onValueChange = viewModel::onVehiclePlateChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NebengRadius.Md))
                            .background(NebengColor.Primary0)
                            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Notes / Preferences
            Text(
                text = "Catatan untuk Penumpang (Opsional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(8.dp))
            BasicTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = NebengColor.Primary900
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (state.notes.isEmpty()) {
                            Text(
                                text = "Contoh: Mobil AC, dilarang merokok, titik kumpul di samping lobi kantor.",
                                fontSize = 13.sp,
                                color = NebengColor.Gray400
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(NebengColor.Primary50)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
                    .padding(14.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfferRideTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Buka Rute Tebengan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )
    }
}

@Composable
private fun VehicleTypeOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) NebengColor.Primary900 else NebengColor.Primary50
    val contentColor = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900
    val subColor = if (isSelected) NebengColor.Gray400 else NebengColor.Gray600
    val borderColor = if (isSelected) NebengColor.Primary900 else NebengColor.Gray200

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(NebengRadius.Lg))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = subColor
                )
            }
        }
    }
}

@Composable
private fun DriverRouteInputCard(
    pickupAddress: String,
    dropoffAddress: String,
    onPickupChange: (String) -> Unit,
    onDropoffChange: (String) -> Unit,
    suggestions: List<PlaceSuggestion>,
    isSearching: Boolean,
    onSelectSuggestion: (PlaceSuggestion) -> Unit,
    onDismissSuggestions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary50)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(14.dp)
    ) {
        // 1. Pickup Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)) // Green dot
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = pickupAddress,
                onValueChange = onPickupChange,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (pickupAddress.isEmpty()) {
                            Text(
                                text = "Titik Keberangkatan / Jemput",
                                fontSize = 14.sp,
                                color = NebengColor.Gray400
                            )
                        }
                        innerTextField()
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 0.5.dp,
            color = NebengColor.Gray200
        )

        // 2. Dropoff Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NebengColor.Primary900) // Black square
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = dropoffAddress,
                onValueChange = onDropoffChange,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (dropoffAddress.isEmpty()) {
                            Text(
                                text = "Titik Tujuan Akhir",
                                fontSize = 14.sp,
                                color = NebengColor.Gray400
                            )
                        }
                        innerTextField()
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Suggestions Dropdown
        AnimatedVisibility(visible = suggestions.isNotEmpty() || isSearching) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Primary0)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                    .padding(8.dp)
            ) {
                if (isSearching) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = NebengColor.Primary900
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Mencari alamat...",
                            fontSize = 12.sp,
                            color = NebengColor.Gray400
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saran Alamat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Gray400,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = NebengColor.Gray400,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onDismissSuggestions)
                        )
                    }

                    suggestions.take(4).forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectSuggestion(suggestion) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = NebengColor.Primary900,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = suggestion.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NebengColor.Primary900
                                )
                                Text(
                                    text = suggestion.fullAddress,
                                    fontSize = 11.sp,
                                    color = NebengColor.Gray400,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
