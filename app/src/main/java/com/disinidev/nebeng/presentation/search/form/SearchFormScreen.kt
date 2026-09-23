package com.disinidev.nebeng.presentation.search.form

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import java.util.Locale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.R
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.PlaceSuggestion
import com.disinidev.nebeng.presentation.search.model.FavoriteLocation
import com.disinidev.nebeng.presentation.search.model.SearchHistoryItem

@Composable
fun SearchFormScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToSearchResults: (pickup: String, dropoff: String, vehicleType: String, pickupLat: Double, pickupLng: Double, departureTime: String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        topBar = {
            SearchFormTopBar(
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
                    text = "Cari Tebengan Tersedia",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        onNavigateToSearchResults(
                            state.origin,
                            state.destination,
                            state.vehicleType,
                            state.originLatitude ?: -6.2297,
                            state.originLongitude ?: 106.8580,
                            state.selectedTime
                        )
                    },
                    enabled = state.origin.isNotBlank() && state.destination.isNotBlank(),
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
            // 1. Route Input Container (Grey Box with Dropdown Popups)
            RouteInputContainer(
                origin = state.origin,
                destination = state.destination,
                selectedTime = state.selectedTime,
                vehicleType = state.vehicleType,
                activeField = state.activeField,
                suggestions = state.suggestions,
                isSearchingPlaces = state.isSearchingPlaces,
                onOriginChange = viewModel::onOriginChange,
                onDestinationChange = viewModel::onDestinationChange,
                onClearOrigin = viewModel::onClearOrigin,
                onSwapLocations = viewModel::onSwapLocations,
                onTimeChange = viewModel::onTimeChange,
                onVehicleTypeChange = viewModel::onVehicleTypeChange,
                onSelectSuggestion = viewModel::onSelectSuggestion,
                onDismissSuggestions = viewModel::onDismissSuggestions
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Lokasi Favorit & Tersimpan
            if (state.favoriteLocations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Lokasi Favorit & Tersimpan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.favoriteLocations.forEach { favorite ->
                        FavoriteChip(
                            favorite = favorite,
                            onClick = { viewModel.onSelectFavorite(favorite) }
                        )
                    }
                }
            }

            // 3. Riwayat Pencarian Rute
            if (state.recentSearches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Riwayat Pencarian Rute",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.recentSearches.forEach { history ->
                        SearchHistoryCard(
                            history = history,
                            onClick = {
                                viewModel.onSelectHistory(history)
                                onNavigateToSearchResults(
                                    history.origin,
                                    history.destination,
                                    state.vehicleType,
                                    state.originLatitude ?: -6.2297,
                                    state.originLongitude ?: 106.8580,
                                    history.departureTime.ifBlank { state.selectedTime }
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SearchFormTopBar(
    onBackClick: () -> Unit,
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
            text = "Cari & Ubah Rute",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RouteInputContainer(
    origin: String,
    destination: String,
    selectedTime: String,
    vehicleType: String,
    activeField: ActiveSearchField,
    suggestions: List<PlaceSuggestion>,
    isSearchingPlaces: Boolean,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onClearOrigin: () -> Unit,
    onSwapLocations: () -> Unit,
    onTimeChange: (String) -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onSelectSuggestion: (PlaceSuggestion) -> Unit,
    onDismissSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary50)
            .padding(14.dp)
    ) {
        // Origin Input Box with Absolute Dropdown
        var originWidthPx by remember { mutableIntStateOf(0) }
        var originHeightPx by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    originWidthPx = coordinates.size.width
                    originHeightPx = coordinates.size.height
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Primary0)
                    .border(
                        if (activeField == ActiveSearchField.ORIGIN) 1.5.dp else 1.dp,
                        if (activeField == ActiveSearchField.ORIGIN) NebengColor.Primary900 else NebengColor.Gray200,
                        RoundedCornerShape(NebengRadius.Md)
                    )
                    .padding(horizontal = 12.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Origin Dot Bullet
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary900)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = origin,
                    onValueChange = onOriginChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (origin.isEmpty()) {
                            Text(
                                text = "Titik penjemputan...",
                                fontSize = 14.sp,
                                color = NebengColor.Gray400
                            )
                        }
                        innerTextField()
                    }
                )

                if (origin.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint = NebengColor.Gray400,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onClearOrigin)
                    )
                }
            }

            // Dropdown Popup below Origin
            if (activeField == ActiveSearchField.ORIGIN && (suggestions.isNotEmpty() || isSearchingPlaces) && originWidthPx > 0) {
                val widthDp = with(LocalDensity.current) { originWidthPx.toDp() }
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(x = 0, y = originHeightPx + with(LocalDensity.current) { 6.dp.roundToPx() }),
                    onDismissRequest = onDismissSuggestions,
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Box(modifier = Modifier.width(widthDp)) {
                        SuggestionsCard(
                            suggestions = suggestions,
                            isSearching = isSearchingPlaces,
                            onSelectSuggestion = onSelectSuggestion,
                            onDismiss = onDismissSuggestions
                        )
                    }
                }
            }
        }

        // Divider Row with Swap Button (spaced inputs with gap atas and gap bawah)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Vertical connection line on left
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterStart)
                    .width(1.5.dp)
                    .height(42.dp)
                    .background(NebengColor.Gray200)
            )

            // Swap Button with gap atas and gap bawah
            Surface(
                onClick = onSwapLocations,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(28.dp),
                shape = CircleShape,
                color = NebengColor.Primary0,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, NebengColor.Gray200)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Tukar rute",
                        tint = NebengColor.Primary900,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Destination Input Box with Absolute Dropdown
        var destWidthPx by remember { mutableIntStateOf(0) }
        var destHeightPx by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    destWidthPx = coordinates.size.width
                    destHeightPx = coordinates.size.height
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Primary0)
                    .border(
                        if (activeField == ActiveSearchField.DESTINATION) 1.5.dp else 1.dp,
                        if (activeField == ActiveSearchField.DESTINATION) NebengColor.Primary900 else NebengColor.Gray200,
                        RoundedCornerShape(NebengRadius.Md)
                    )
                    .padding(horizontal = 12.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Destination Square Bullet
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(NebengColor.Primary900)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = destination,
                    onValueChange = onDestinationChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NebengColor.Primary900
                    ),
                    cursorBrush = SolidColor(NebengColor.Primary900),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (destination.isEmpty()) {
                            Text(
                                text = "Tujuan kantor, stasiun...",
                                fontSize = 14.sp,
                                color = NebengColor.Gray400
                            )
                        }
                        innerTextField()
                    }
                )

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dropdown Popup below Destination
            if (activeField == ActiveSearchField.DESTINATION && (suggestions.isNotEmpty() || isSearchingPlaces) && destWidthPx > 0) {
                val widthDp = with(LocalDensity.current) { destWidthPx.toDp() }
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(x = 0, y = destHeightPx + with(LocalDensity.current) { 6.dp.roundToPx() }),
                    onDismissRequest = onDismissSuggestions,
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Box(modifier = Modifier.width(widthDp)) {
                        SuggestionsCard(
                            suggestions = suggestions,
                            isSearching = isSearchingPlaces,
                            onSelectSuggestion = onSelectSuggestion,
                            onDismiss = onDismissSuggestions
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val context = LocalContext.current
        val timePickerDialog = remember(context, selectedTime) {
            val timePattern = Regex("""(\d{1,2}):(\d{2})""")
            val match = timePattern.find(selectedTime)
            val initialHour = match?.groupValues?.get(1)?.toIntOrNull() ?: 7
            val initialMinute = match?.groupValues?.get(2)?.toIntOrNull() ?: 30

            TimePickerDialog(
                context,
                R.style.NebengTimePickerDialogTheme,
                { _, hourOfDay, minute ->
                    val formattedHour = String.format(Locale.getDefault(), "%02d", hourOfDay)
                    val formattedMinute = String.format(Locale.getDefault(), "%02d", minute)
                    val prefix = if (selectedTime.contains(",")) selectedTime.substringBefore(",").trim() else "Hari Ini"
                    onTimeChange("$prefix, $formattedHour:$formattedMinute")
                },
                initialHour,
                initialMinute,
                true
            )
        }

        // Row: Date & Time Selector + Vehicle Type Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time Pill
            Row(
                modifier = Modifier
                    .weight(1.1f)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Primary0)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                    .clickable { timePickerDialog.show() }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pilih jam keberangkatan",
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Vehicle Type Pill with Dropdown
            var showVehicleMenu by remember { mutableStateOf(false) }
            val isMotor = vehicleType.equals("motorcycle", ignoreCase = true)

            Box(modifier = Modifier.weight(0.9f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NebengRadius.Md))
                        .background(NebengColor.Primary0)
                        .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                        .clickable { showVehicleMenu = true }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = if (isMotor) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = NebengColor.Primary900,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMotor) "Motor" else "Mobil",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NebengColor.Primary900
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Pilih tipe kendaraan",
                        tint = NebengColor.Gray400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showVehicleMenu,
                    onDismissRequest = { showVehicleMenu = false },
                    modifier = Modifier.background(NebengColor.Primary0)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Mobil",
                                fontSize = 13.sp,
                                fontWeight = if (!isMotor) FontWeight.Bold else FontWeight.Medium,
                                color = NebengColor.Primary900
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = if (!isMotor) NebengColor.Primary900 else NebengColor.Gray400,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = if (!isMotor) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NebengColor.Primary900,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            onVehicleTypeChange("car")
                            showVehicleMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Motor",
                                fontSize = 13.sp,
                                fontWeight = if (isMotor) FontWeight.Bold else FontWeight.Medium,
                                color = NebengColor.Primary900
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = if (isMotor) NebengColor.Primary900 else NebengColor.Gray400,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = if (isMotor) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NebengColor.Primary900,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            onVehicleTypeChange("motorcycle")
                            showVehicleMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteChip(
    favorite: FavoriteLocation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(NebengColor.Primary50)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = favorite.iconEmoji,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = favorite.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = NebengColor.Primary900
        )
    }
}

@Composable
private fun SearchHistoryCard(
    history: SearchHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading History Clock Icon in Circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = NebengColor.Gray600,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${history.origin} → ${history.destination}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = history.optionsSummary,
                fontSize = 12.sp,
                color = NebengColor.Gray400
            )
        }

        // Trailing Up-Right Arrow
        Icon(
            imageVector = Icons.Default.ArrowOutward,
            contentDescription = null,
            tint = NebengColor.Gray400,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SuggestionsCard(
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



