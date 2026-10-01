package com.disinidev.nebeng.presentation.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengTextField
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing
import com.disinidev.nebeng.domain.model.RoutineCommute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCommuteScreen(
    onNavigateBack: () -> Unit,
    onSearchRide: (origin: String, destination: String, vehicleType: String) -> Unit,
    viewModel: RoutineCommuteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var routineToDelete by remember { mutableStateOf<RoutineCommute?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NebengColor.BackgroundPrimary)
            .statusBarsPadding(),
        topBar = {
            RoutineTopBar(onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.routines.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NebengColor.Primary0)
                        .navigationBarsPadding()
                        .padding(horizontal = NebengSpacing.Md, vertical = NebengSpacing.Sm)
                ) {
                    NebengButton(
                        text = "+ Tambah Jadwal Rutin",
                        onClick = { viewModel.showAddSheet(true) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NebengColor.BackgroundPrimary)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NebengColor.Primary900)
                    }
                }

                uiState.routines.isEmpty() -> {
                    RoutineEmptyState(
                        onAddClicked = { viewModel.showAddSheet(true) }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = NebengSpacing.Md),
                        verticalArrangement = Arrangement.spacedBy(NebengSpacing.Sm)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(NebengSpacing.Xs))
                            RoutineHeaderBanner()
                            Spacer(modifier = Modifier.height(NebengSpacing.Xs))
                        }

                        items(uiState.routines, key = { it.id }) { routine ->
                            RoutineCard(
                                routine = routine,
                                onToggle = { isEnabled ->
                                    viewModel.toggleRoutine(routine.id, isEnabled)
                                },
                                onSearchToday = {
                                    onSearchRide(routine.originName, routine.destinationName, routine.vehicleType)
                                },
                                onDelete = {
                                    routineToDelete = routine
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(NebengSpacing.Xl))
                        }
                    }
                }
            }
        }
    }

    if (uiState.isAddSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showAddSheet(false) },
            sheetState = sheetState,
            containerColor = NebengColor.Primary0
        ) {
            AddRoutineSheetContent(
                uiState = uiState,
                onOriginChange = viewModel::updateOrigin,
                onDestinationChange = viewModel::updateDestination,
                onTimeChange = viewModel::updateDepartureTime,
                onToggleDay = viewModel::toggleDay,
                onVehicleTypeChange = viewModel::updateVehicleType,
                onAutoBookToggle = viewModel::toggleAutoBook,
                onSave = viewModel::saveRoutine,
                onDismiss = { viewModel.showAddSheet(false) }
            )
        }
    }

    routineToDelete?.let { routine ->
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            title = {
                Text(
                    text = "Hapus Jadwal Rutin?",
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            },
            text = {
                Text(
                    text = "Jadwal ${routine.originName} ke ${routine.destinationName} akan dihapus secara permanen.",
                    color = NebengColor.Gray600
                )
            },
            confirmButton = {
                NebengButton(
                    text = "Hapus",
                    onClick = {
                        viewModel.deleteRoutine(routine.id)
                        routineToDelete = null
                    },
                    style = NebengButtonStyle.DANGER
                )
            },
            dismissButton = {
                NebengButton(
                    text = "Batal",
                    onClick = { routineToDelete = null },
                    style = NebengButtonStyle.GHOST
                )
            },
            containerColor = NebengColor.Primary0
        )
    }
}

@Composable
private fun RoutineTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NebengColor.Primary0)
            .padding(horizontal = NebengSpacing.Md, vertical = NebengSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onNavigateBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = NebengColor.Primary900
            )
        }
        Spacer(modifier = Modifier.width(NebengSpacing.Sm))
        Column {
            Text(
                text = "Tebengan Rutin",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Text(
                text = "Otomasi rute harian rumah-kantor",
                fontSize = 12.sp,
                color = NebengColor.Gray600
            )
        }
    }
}

@Composable
private fun RoutineHeaderBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(NebengColor.Primary50)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
            .padding(NebengSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(NebengColor.Success700)
        )
        Spacer(modifier = Modifier.width(NebengSpacing.Sm))
        Text(
            text = "Jadwal otomatis membantu mencocokkan tebengan tepat waktu setiap pagi.",
            fontSize = 13.sp,
            color = NebengColor.Primary900,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun RoutineCard(
    routine: RoutineCommute,
    onToggle: (Boolean) -> Unit,
    onSearchToday: () -> Unit,
    onDelete: () -> Unit
) {
    val isCar = routine.vehicleType == "car"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(
                1.dp,
                if (routine.isEnabled) NebengColor.Primary900.copy(alpha = 0.3f) else NebengColor.Gray200,
                RoundedCornerShape(NebengRadius.Lg)
            )
            .padding(NebengSpacing.Md)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (routine.isEnabled) NebengColor.Primary50
                            else NebengColor.Gray100
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCar) Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = if (routine.isEnabled) NebengColor.Primary900 else NebengColor.Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(NebengSpacing.Xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(NebengColor.Primary50)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NebengColor.Gray600,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${routine.departureTime} WIB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Primary900
                    )
                }
            }

            Switch(
                checked = routine.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NebengColor.Primary0,
                    checkedTrackColor = NebengColor.Primary900,
                    uncheckedThumbColor = NebengColor.Primary0,
                    uncheckedTrackColor = NebengColor.Gray200
                )
            )
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Sm))

        // Route details
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary900)
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(NebengColor.Gray200)
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = NebengColor.Danger600,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(NebengSpacing.Sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.originName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NebengColor.Primary900
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = routine.destinationName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NebengColor.Primary900
                )
            }
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Sm))

        // Days badge
        Text(
            text = "📅 ${routine.getFormattedDays()}",
            fontSize = 12.sp,
            color = NebengColor.Gray600
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NebengButton(
                text = "Cari Hari Ini",
                onClick = onSearchToday,
                style = NebengButtonStyle.SECONDARY,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(NebengSpacing.Sm))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Danger100)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = NebengColor.Danger600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RoutineEmptyState(onAddClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NebengSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = NebengColor.Primary900,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(NebengSpacing.Md))
        Text(
            text = "Belum Ada Jadwal Rutin",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )
        Spacer(modifier = Modifier.height(NebengSpacing.Xs))
        Text(
            text = "Atur rute rumah ke tempat kerja agar sistem otomatis mencari tebengan tepat waktu setiap pagi.",
            fontSize = 14.sp,
            color = NebengColor.Gray600,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = NebengSpacing.Md)
        )
        Spacer(modifier = Modifier.height(NebengSpacing.Lg))
        NebengButton(
            text = "+ Tambah Jadwal Rutin",
            onClick = onAddClicked
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddRoutineSheetContent(
    uiState: RoutineCommuteUiState,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onToggleDay: (Int) -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onAutoBookToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = NebengSpacing.Lg, vertical = NebengSpacing.Md)
    ) {
        Text(
            text = "Tambah Jadwal Tebengan Rutin",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )
        Text(
            text = "Simpan rute harian untuk otomatisasi dan pencarian instan",
            fontSize = 12.sp,
            color = NebengColor.Gray600
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        NebengTextField(
            value = uiState.originInput,
            onValueChange = onOriginChange,
            label = "Titik Berangkat / Rumah",
            placeholder = "Misal: Kost BSD Serpong",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Sm))

        NebengTextField(
            value = uiState.destinationInput,
            onValueChange = onDestinationChange,
            label = "Tujuan / Kantor / Kampus",
            placeholder = "Misal: Menara Sudirman, Jakarta",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Sm))

        NebengTextField(
            value = uiState.departureTimeInput,
            onValueChange = onTimeChange,
            label = "Jam Berangkat (WIB)",
            placeholder = "07:30",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        Text(
            text = "Hari Berangkat",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = NebengColor.Primary900
        )
        Spacer(modifier = Modifier.height(NebengSpacing.Xs))

        val days = listOf(
            1 to "Sen",
            2 to "Sel",
            3 to "Rab",
            4 to "Kam",
            5 to "Jum",
            6 to "Sab",
            7 to "Min"
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { (dayId, label) ->
                val isSelected = uiState.selectedDays.contains(dayId)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(
                            if (isSelected) NebengColor.Primary900 else NebengColor.Primary50
                        )
                        .border(
                            1.dp,
                            if (isSelected) NebengColor.Primary900 else NebengColor.Gray200,
                            RoundedCornerShape(NebengRadius.Sm)
                        )
                        .clickable { onToggleDay(dayId) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        Text(
            text = "Jenis Kendaraan Pilihan",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = NebengColor.Primary900
        )
        Spacer(modifier = Modifier.height(NebengSpacing.Xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NebengSpacing.Sm)
        ) {
            val isCar = uiState.selectedVehicleType == "car"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(if (isCar) NebengColor.Primary900 else NebengColor.Primary50)
                    .border(
                        1.dp,
                        if (isCar) NebengColor.Primary900 else NebengColor.Gray200,
                        RoundedCornerShape(NebengRadius.Md)
                    )
                    .clickable { onVehicleTypeChange("car") }
                    .padding(NebengSpacing.Sm),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = if (isCar) NebengColor.Primary0 else NebengColor.Primary900,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mobil",
                        fontWeight = if (isCar) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCar) NebengColor.Primary0 else NebengColor.Primary900
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(if (!isCar) NebengColor.Primary900 else NebengColor.Primary50)
                    .border(
                        1.dp,
                        if (!isCar) NebengColor.Primary900 else NebengColor.Gray200,
                        RoundedCornerShape(NebengRadius.Md)
                    )
                    .clickable { onVehicleTypeChange("motorcycle") }
                    .padding(NebengSpacing.Sm),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = if (!isCar) NebengColor.Primary0 else NebengColor.Primary900,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Motor",
                        fontWeight = if (!isCar) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isCar) NebengColor.Primary0 else NebengColor.Primary900
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(NebengColor.Primary50)
                .padding(NebengSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifikasi Otomatis",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Primary900
                )
                Text(
                    text = "Beri tahu jika ada driver dengan rute serupa",
                    fontSize = 12.sp,
                    color = NebengColor.Gray600
                )
            }
            Switch(
                checked = uiState.autoBook,
                onCheckedChange = onAutoBookToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NebengColor.Primary0,
                    checkedTrackColor = NebengColor.Primary900,
                    uncheckedThumbColor = NebengColor.Primary0,
                    uncheckedTrackColor = NebengColor.Gray200
                )
            )
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Lg))

        NebengButton(
            text = if (uiState.isSaving) "Menyimpan..." else "Simpan Jadwal Rutin",
            onClick = onSave,
            enabled = !uiState.isSaving && uiState.originInput.isNotBlank() && uiState.destinationInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Md))
    }
}
