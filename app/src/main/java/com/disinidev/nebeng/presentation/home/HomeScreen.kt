package com.disinidev.nebeng.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.disinidev.nebeng.core.component.LoadingShimmer
import com.disinidev.nebeng.core.component.NebengBottomNav
import com.disinidev.nebeng.core.component.NebengTab
import com.disinidev.nebeng.core.component.RideCard
import com.disinidev.nebeng.core.component.ServiceSelector
import com.disinidev.nebeng.core.component.ServiceType
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    onNavigateToOfferRide: () -> Unit = {},
    onNavigateToRoutine: () -> Unit = {},
    onNavigateToRideDetail: (String) -> Unit = {},
    onNavigateToCheckout: (String) -> Unit = {},
    onTabSelected: (NebengTab) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            viewModel.fetchCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            HomeHeader(
                greeting = state.userGreeting,
                location = state.userLocation,
                initials = state.userAvatarInitials,
                onNotificationClick = onNavigateToNotifications,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        bottomBar = {
            NebengBottomNav(
                selectedTab = NebengTab.BERANDA,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshRides,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Search Card
                item {
                    SearchCard(
                        onClick = { onNavigateToSearch(if (state.selectedService == ServiceType.MOTORCYCLE) "motorcycle" else "car") }
                    )
                }

                // 3. Service Shortcuts (Mobil, Motor, Beri Tebeng, Rutin)
                item {
                    ServiceSelector(
                        selectedService = state.selectedService,
                        onServiceSelected = { service ->
                            viewModel.onServiceSelected(service)
                            when (service) {
                                ServiceType.CAR -> onNavigateToSearch("car")
                                ServiceType.MOTORCYCLE -> onNavigateToSearch("motorcycle")
                                ServiceType.OFFER_RIDE -> onNavigateToOfferRide()
                                ServiceType.ROUTINE -> onNavigateToRoutine()
                            }
                        }
                    )
                }

                // 4. Section Title: Tebengan Populer Sekitarmu + Lihat Semua
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tebengan Populer Sekitarmu",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Lihat Semua (${state.totalRidesCount})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900,
                            modifier = Modifier.clickable {
                                onNavigateToSearch(if (state.selectedService == ServiceType.MOTORCYCLE) "motorcycle" else "car")
                            }
                        )
                    }
                }

                // 5. Popular Rides List
                if (state.isLoading && !state.isRefreshing) {
                    items(3) {
                        LoadingShimmer(height = 160.dp, cornerRadius = NebengRadius.Lg)
                    }
                } else if (state.popularRides.isEmpty()) {
                    item {
                        EmptyStateView()
                    }
                } else {
                    items(state.popularRides, key = { it.id }) { ride ->
                        RideCard(
                            ride = ride,
                            onBookClick = { onNavigateToCheckout(ride.id) },
                            onClick = { onNavigateToRideDetail(ride.id) }
                        )
                    }
                }

                // Bottom padding spacer
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    location: String,
    initials: String,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Initials Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = NebengColor.Primary0,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Greeting & Location
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Halo, $greeting 👋",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "📍 ",
                    fontSize = 11.sp
                )
                Text(
                    text = location,
                    fontSize = 13.sp,
                    color = NebengColor.Gray600
                )
            }
        }

        // Circular Bell Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
                .clickable(onClick = onNotificationClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifikasi",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SearchCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary50)
            .padding(16.dp)
    ) {
        // Question Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "●",
                fontSize = 10.sp,
                color = NebengColor.Primary900,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Mau nebeng ke mana hari ini?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clickable Search Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(NebengColor.Primary0)
                .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = NebengColor.Gray400,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Cari rute kantor, stasiun, gedung...",
                fontSize = 14.sp,
                color = NebengColor.Gray400
            )
        }
    }
}

@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Belum ada tebengan populer di sekitarmu",
            fontSize = 14.sp,
            color = NebengColor.Gray600
        )
    }
}
