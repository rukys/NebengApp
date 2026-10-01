package com.disinidev.nebeng.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.disinidev.nebeng.domain.model.VehicleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    onNavigateToSearchResults: (pickup: String, dropoff: String, vehicleType: String, pickupLat: Double, pickupLng: Double, departureTime: String) -> Unit = { _, _, _, _, _, _ -> },
    onNavigateToOfferRide: () -> Unit = {},
    onNavigateToRoutine: () -> Unit = {},
    onNavigateToRideDetail: (String) -> Unit = {},
    onNavigateToCheckout: (String) -> Unit = {},
    onNavigateToCheckoutCar: (String) -> Unit = onNavigateToCheckout,
    onNavigateToCheckoutMotor: (String) -> Unit = onNavigateToCheckout,
    onTabSelected: (NebengTab) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUnreadNotificationCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                unreadNotificationCount = state.unreadNotificationCount,
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
                    val vehicleTypeParam = when (state.selectedService) {
                        ServiceType.MOTORCYCLE -> "motorcycle"
                        ServiceType.CAR -> "car"
                        else -> "all"
                    }
                    SearchCard(
                        popularDestinations = state.popularDestinations,
                        onClick = { onNavigateToSearch(if (state.selectedService == ServiceType.MOTORCYCLE) "motorcycle" else "car") },
                        onQuickDestinationClick = { destination ->
                            onNavigateToSearchResults(
                                state.userLocation.ifBlank { "Lokasi Sekitarmu" },
                                destination,
                                vehicleTypeParam,
                                state.userLat,
                                state.userLng,
                                "Hari Ini"
                            )
                        }
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
                                val vehicleTypeParam = when (state.selectedService) {
                                    ServiceType.MOTORCYCLE -> "motorcycle"
                                    ServiceType.CAR -> "car"
                                    else -> "all"
                                }
                                onNavigateToSearchResults(
                                    state.userLocation.ifBlank { "Lokasi Sekitarmu" },
                                    "Semua Rute Tebengan",
                                    vehicleTypeParam,
                                    state.userLat,
                                    state.userLng,
                                    "Hari Ini"
                                )
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
                        val onBook = {
                            if (ride.vehicleInfo.type == VehicleType.MOTORCYCLE) {
                                onNavigateToCheckoutMotor(ride.id)
                            } else {
                                onNavigateToCheckoutCar(ride.id)
                            }
                        }
                        RideCard(
                            ride = ride,
                            onBookClick = { onBook() },
                            onClick = { onBook() }
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
    unreadNotificationCount: Int,
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
                text = "Halo, $greeting",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = location,
                    fontSize = 13.sp,
                    color = NebengColor.Gray600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Circular Bell Button with Badge Count
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
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

            if (unreadNotificationCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-2).dp)
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                        .background(
                            color = NebengColor.Danger600,
                            shape = CircleShape
                        )
                        .border(1.5.dp, NebengColor.Primary0, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchCard(
    popularDestinations: List<String>,
    onClick: () -> Unit,
    onQuickDestinationClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val topDest1 = popularDestinations.getOrNull(0) ?: "SCBD"
    val topDest2 = popularDestinations.getOrNull(1) ?: "Sudirman"

    val tickerPlaceholders = remember(topDest1, topDest2) {
        listOf(
            "Cari rute kantor, stasiun, gedung...",
            "Cari tebengan ke $topDest1...",
            "Cari rekan kantor searah...",
            "Mau tebengan ke $topDest2?",
            "Cari tebengan mobil & motor...",
            "Cari rute stasiun KRL / MRT..."
        )
    }

    var placeholderIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(tickerPlaceholders) {
        placeholderIndex = 0
        while (isActive) {
            delay(3000L)
            placeholderIndex = (placeholderIndex + 1) % tickerPlaceholders.size
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary50)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(16.dp)
    ) {
        // Card Header: Title & Subtitle
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Mau nebeng ke mana hari ini?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Text(
                text = "Temukan tebengan searah & hemat ongkos",
                fontSize = 12.sp,
                color = NebengColor.Gray600,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clickable Search Field (Elevated Superapp input)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(NebengColor.Primary0)
                .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-contrast Search Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Primary900),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = NebengColor.Primary0,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Two-tier text content: Label + Animated Ticker
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tujuan tebengan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = NebengColor.Gray600
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = placeholderIndex,
                        transitionSpec = {
                            (slideInVertically(
                                animationSpec = tween(durationMillis = 350),
                                initialOffsetY = { it }
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 350)
                            )).togetherWith(
                                slideOutVertically(
                                    animationSpec = tween(durationMillis = 350),
                                    targetOffsetY = { -it }
                                ) + fadeOut(
                                    animationSpec = tween(durationMillis = 350)
                                )
                            )
                        },
                        label = "searchPlaceholderTicker"
                    ) { targetIndex ->
                        val safeIndex = if (targetIndex in tickerPlaceholders.indices) targetIndex else 0
                        Text(
                            text = tickerPlaceholders[safeIndex],
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NebengColor.Primary900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Forward Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Primary50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Destination Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            popularDestinations.forEach { destination ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(NebengColor.Primary0)
                        .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Full))
                        .clickable { onQuickDestinationClick(destination) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = NebengColor.Gray600,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Gray800
                    )
                }
            }
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
