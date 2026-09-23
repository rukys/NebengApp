package com.disinidev.nebeng.presentation.search.results

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import com.disinidev.nebeng.presentation.search.model.VehicleFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchResultsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onEditRouteClick: () -> Unit = {},
    onNavigateToRideDetail: (String) -> Unit = {},
    onNavigateToCheckoutCar: (rideId: String) -> Unit = {},
    onNavigateToCheckoutMotor: (rideId: String) -> Unit = {},
    onNavigateToOfferRide: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            SearchResultsTopBar(
                onBackClick = onNavigateBack,
                onFilterClick = viewModel::openFilterSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        bottomBar = {
            // Sticky Footer: Butuh jam berangkat lain? Ajukan Rute Baru ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .navigationBarsPadding()
                    .border(width = 0.5.dp, color = NebengColor.Gray200)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Butuh jam berangkat lain?",
                        fontSize = 13.sp,
                        color = NebengColor.Gray600
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Ajukan Rute Baru →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900,
                        modifier = Modifier.clickable(onClick = onNavigateToOfferRide)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Route Summary Box (Clickable to edit route)
            item {
                RouteSummaryCard(
                    origin = state.pickupAddress,
                    destination = state.dropoffAddress,
                    departureTime = state.departureTime,
                    onClick = onEditRouteClick
                )
            }

            // 2. Filter Chips Row: Semua (18) | Mobil (12) | Motor (6) | Tercepat
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        VehicleFilter.ALL to "Semua (${state.totalCount})",
                        VehicleFilter.CAR to "Mobil (${state.carCount})",
                        VehicleFilter.MOTORCYCLE to "Motor (${state.motorCount})",
                        VehicleFilter.FASTEST to "Tercepat"
                    )

                    tabs.forEach { (filter, label) ->
                        val isSelected = state.selectedFilterTab == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(NebengRadius.Full))
                                .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary50)
                                .clickable { viewModel.onTabFilterSelected(filter) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900
                            )
                        }
                    }
                }
            }

            // 3. Rides List
            if (state.displayedRides.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada tebengan yang sesuai dengan filter ini",
                            fontSize = 14.sp,
                            color = NebengColor.Gray400
                        )
                    }
                }
            } else {
                items(state.displayedRides, key = { it.id }) { ride ->
                    SearchResultRideCard(
                        ride = ride,
                        onSelectClick = {
                            if (ride.vehicleType == VehicleType.CAR) {
                                onNavigateToCheckoutCar(ride.id)
                            } else {
                                onNavigateToCheckoutMotor(ride.id)
                            }
                        },
                        onClick = {
                            if (ride.vehicleType == VehicleType.CAR) {
                                onNavigateToCheckoutCar(ride.id)
                            } else {
                                onNavigateToCheckoutMotor(ride.id)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Filter Bottom Sheet
    if (state.isFilterSheetOpen) {
        FilterBottomSheet(
            sheetState = filterSheetState,
            filterOptions = state.draftFilterOptions,
            matchingRoutesCount = state.displayedRides.size,
            onFilterChange = viewModel::onDraftFilterChanged,
            onResetFilter = viewModel::onResetFilter,
            onApplyFilter = viewModel::onApplyFilter,
            onDismiss = viewModel::closeFilterSheet
        )
    }
}

@Composable
private fun SearchResultsTopBar(
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
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
            text = "Pilihan Rute Tebengan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900,
            modifier = Modifier.weight(1f)
        )

        // Filter / Tune Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RouteSummaryCard(
    origin: String,
    destination: String,
    departureTime: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary50)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        // Origin row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Primary900)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = origin,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
        }

        // Connecting vertical line
        Box(
            modifier = Modifier
                .padding(start = 3.5.dp, top = 2.dp, bottom = 2.dp)
                .width(1.dp)
                .height(14.dp)
                .background(NebengColor.Gray400)
        )

        // Destination row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(NebengColor.Primary900)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = destination,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(NebengColor.Gray200)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Departure Time & Edit CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = NebengColor.Primary900,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Jadwal: $departureTime",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Ubah Rute ✎",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
        }
    }
}

@Composable
private fun SearchResultRideCard(
    ride: RideItemUi,
    onSelectClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        // Top Row: Vehicle Icon + Driver & Schedule + Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle Square Icon
            val isCar = ride.vehicleType == VehicleType.CAR
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(if (isCar) NebengColor.Primary900 else NebengColor.Primary50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCar) Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = if (isCar) NebengColor.Primary0 else NebengColor.Primary900,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Driver & Departure Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${ride.driverName} • ${ride.vehicleModel}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Berangkat: ${ride.departureTimeFormatted} (Tiba ${ride.arrivalTimeFormatted})",
                    fontSize = 11.sp,
                    color = NebengColor.Gray400
                )
            }

            // Rating Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "%.1f".format(ride.driverRating),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Row: Details/Tags + "Pilih" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ride.facilities.joinToString(" • "),
                fontSize = 11.sp,
                color = NebengColor.Gray600,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(NebengColor.Primary900)
                    .clickable(onClick = onSelectClick)
                    .padding(horizontal = 20.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pilih",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary0
                )
            }
        }
    }
}
