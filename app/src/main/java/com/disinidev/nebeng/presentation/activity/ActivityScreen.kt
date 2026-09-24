package com.disinidev.nebeng.presentation.activity

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengBottomNav
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengTab
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

@Composable
fun ActivityScreen(
    onTabSelected: (NebengTab) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToLiveTracking: (String) -> Unit = {},
    onNavigateToTripDone: (String) -> Unit = {},
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var isRequestsSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            ActivityTopBar(
                isSearchActive = state.isSearchActive,
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onToggleSearch = viewModel::toggleSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        },
        bottomBar = {
            NebengBottomNav(
                selectedTab = NebengTab.AKTIVITAS,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Filter Chips Row
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ActivityFilterChips(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = viewModel::onFilterSelected
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Ongoing State: Active Trip Card & Driver Requests
            if (state.selectedFilter == ActivityFilter.ONGOING) {
                // Driver Requests Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NebengRadius.Lg))
                            .background(NebengColor.Primary50)
                            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
                            .clickable { isRequestsSheetOpen = true }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NebengColor.Primary900),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NebengColor.Primary0,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permintaan Penumpang Masuk",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NebengColor.Primary900
                                )
                                Text(
                                    text = "Review & konfirmasi calon penumpang",
                                    fontSize = 11.sp,
                                    color = NebengColor.Gray600
                                )
                            }
                            Text(
                                text = "Kelola ➔",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebengColor.Primary900
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    val activeTrip = state.activeTrip
                    if (activeTrip != null) {
                        ActiveTripCard(
                            activeTrip = activeTrip,
                            onTrackClick = { onNavigateToLiveTracking(activeTrip.bookingId) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // RIWAYAT SELESAI Section Header
            if (state.selectedFilter == ActivityFilter.ONGOING || state.selectedFilter == ActivityFilter.COMPLETED) {
                item {
                    Text(
                        text = "RIWAYAT SELESAI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Gray800,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                val filteredTrips = state.completedTrips.filter {
                    state.searchQuery.isBlank() ||
                            it.origin.contains(state.searchQuery, ignoreCase = true) ||
                            it.destination.contains(state.searchQuery, ignoreCase = true)
                }

                items(filteredTrips) { trip ->
                    CompletedTripCard(
                        trip = trip,
                        onClick = { onNavigateToTripDone(trip.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Canceled Trips List
            if (state.selectedFilter == ActivityFilter.CANCELED) {
                val filteredCanceled = state.canceledTrips.filter {
                    state.searchQuery.isBlank() ||
                            it.origin.contains(state.searchQuery, ignoreCase = true) ||
                            it.destination.contains(state.searchQuery, ignoreCase = true)
                }

                if (filteredCanceled.isEmpty()) {
                    item {
                        EmptyActivityState(message = "Tidak ada perjalanan yang dibatalkan")
                    }
                } else {
                    items(filteredCanceled) { trip ->
                        CanceledTripCard(trip = trip)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (isRequestsSheetOpen) {
        com.disinidev.nebeng.presentation.driver.requests.DriverRequestsBottomSheet(
            onDismiss = { isRequestsSheetOpen = false }
        )
    }
}

@Composable
private fun ActivityTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSearchActive) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(NebengColor.Primary50)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = NebengColor.Gray600,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.weight(1f)
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus pencarian",
                        tint = NebengColor.Gray600,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onSearchQueryChange("") }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Batal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900,
                modifier = Modifier
                    .clickable { onToggleSearch(false) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Aktivitas",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Primary50)
                    .clickable { onToggleSearch(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari Aktivitas",
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityFilterChips(
    selectedFilter: ActivityFilter,
    onFilterSelected: (ActivityFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(ActivityFilter.entries) { filter ->
            val isSelected = filter == selectedFilter
            val bg = if (isSelected) NebengColor.Primary900 else NebengColor.Primary50
            val textColor = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun ActiveTripCard(
    activeTrip: ActiveTrip,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NebengColor.Primary0)
            .border(1.5.dp, NebengColor.Primary900, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Status header & PIN
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary900)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activeTrip.statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NebengColor.Primary900)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PIN: ${activeTrip.pin}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary0
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Driver & Vehicle
        Text(
            text = "${activeTrip.driverName} • ${activeTrip.vehicleModel} ${activeTrip.licensePlate}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Route
        Text(
            text = "${activeTrip.pickupAddress} ➔ ${activeTrip.dropoffAddress}",
            fontSize = 13.sp,
            color = NebengColor.Gray600
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Track Button
        NebengButton(
            text = "Lacak Posisi Driver",
            onClick = onTrackClick,
            style = NebengButtonStyle.PRIMARY,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompletedTripCard(
    trip: TripHistoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NebengColor.Primary50)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${trip.origin} ➔ ${trip.destination}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(4.dp))
            val subtitle = "${trip.timeText} • ${trip.vehicleType}"
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = NebengColor.Gray600
            )
        }
        // "Pesan Lagi" is completely removed as instructed
    }
}

@Composable
private fun CanceledTripCard(
    trip: TripHistoryItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NebengColor.Primary50)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${trip.origin} ➔ ${trip.destination}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(4.dp))
            val subtitle = "${trip.timeText} • ${trip.vehicleType}"
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = NebengColor.Gray600
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(NebengColor.Danger100)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Dibatalkan",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Danger600
            )
        }
    }
}

@Composable
private fun EmptyActivityState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = NebengColor.Gray400,
            fontWeight = FontWeight.Medium
        )
    }
}
