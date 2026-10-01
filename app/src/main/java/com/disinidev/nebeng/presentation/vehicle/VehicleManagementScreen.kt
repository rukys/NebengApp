package com.disinidev.nebeng.presentation.vehicle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.component.LoadingShimmer
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengTextField
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehicleManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var vehicleToDelete by remember { mutableStateOf<VehicleInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadVehicles()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebengColor.Primary0,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            VehicleTopBar(
                onNavigateBack = onNavigateBack,
                onAddClick = viewModel::openAddSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NebengColor.Primary0)
                    .statusBarsPadding()
                    .padding(horizontal = NebengSpacing.Xl, vertical = NebengSpacing.Md)
            )
        }
    ) { innerPadding ->

        val screenState = when {
            state.isLoading -> "loading"
            state.vehicles.isEmpty() -> "empty"
            else -> "content"
        }

        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "vehicleScreenStateTransition"
        ) { targetState ->
            when (targetState) {
                "loading" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = NebengSpacing.Xl)
                    ) {
                        repeat(3) {
                            Spacer(modifier = Modifier.height(NebengSpacing.Lg))
                            LoadingShimmer(
                                height = 80.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(NebengRadius.Lg))
                            )
                        }
                    }
                }
                "empty" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
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
                                imageVector = Icons.Outlined.DirectionsCar,
                                contentDescription = null,
                                tint = NebengColor.Gray400,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(NebengSpacing.Lg))
                        Text(
                            text = "Belum ada kendaraan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        )
                        Spacer(modifier = Modifier.height(NebengSpacing.Sm))
                        Text(
                            text = "Tambahkan kendaraan kamu untuk mulai menawarkan tebengan.",
                            fontSize = 13.sp,
                            color = NebengColor.Gray600,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(NebengSpacing.Xl))
                        NebengButton(
                            text = "Tambah Kendaraan",
                            onClick = viewModel::openAddSheet,
                            style = NebengButtonStyle.PRIMARY,
                            leadingIcon = Icons.Default.Add,
                            isFullWidth = false,
                            modifier = Modifier
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = NebengSpacing.Xl),
                        verticalArrangement = Arrangement.spacedBy(NebengSpacing.Md)
                    ) {
                        item { Spacer(modifier = Modifier.height(NebengSpacing.Sm)) }

                        items(state.vehicles, key = { it.id ?: it.plate }) { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                onDeleteClick = { vehicleToDelete = vehicle },
                                modifier = Modifier.animateItem()
                            )
                        }

                        item {
                            // "Add more" row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(NebengRadius.Lg))
                                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
                                    .clickable { viewModel.openAddSheet() }
                                    .padding(horizontal = NebengSpacing.Lg, vertical = NebengSpacing.Md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NebengColor.Primary50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = NebengColor.Primary900,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(NebengSpacing.Md))
                                Text(
                                    text = "Tambah Kendaraan Lain",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NebengColor.Primary900
                                )
                            }
                            Spacer(modifier = Modifier.height(NebengSpacing.Xxl))
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        vehicleToDelete?.let { vehicle ->
            AlertDialog(
                onDismissRequest = { vehicleToDelete = null },
                confirmButton = {
                    NebengButton(
                        text = "Hapus",
                        onClick = {
                            vehicle.id?.let { viewModel.deleteVehicle(it) }
                            vehicleToDelete = null
                        },
                        style = NebengButtonStyle.DANGER,
                        isFullWidth = false,
                        modifier = Modifier
                    )
                },
                dismissButton = {
                    NebengButton(
                        text = "Batal",
                        onClick = { vehicleToDelete = null },
                        style = NebengButtonStyle.SECONDARY,
                        isFullWidth = false,
                        modifier = Modifier
                    )
                },
                title = { Text("Hapus Kendaraan?", fontWeight = FontWeight.Bold, color = NebengColor.Primary900) },
                text = {
                    Text(
                        "Kendaraan ${vehicle.brand} ${vehicle.model} (${vehicle.plate}) akan dihapus permanen.",
                        color = NebengColor.Gray600,
                        fontSize = 14.sp
                    )
                },
                containerColor = NebengColor.Primary0
            )
        }

        // Add vehicle bottom sheet
        if (state.isAddSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeAddSheet,
                sheetState = sheetState,
                containerColor = NebengColor.Primary0
            ) {
                AddVehicleSheetContent(
                    state = state,
                    onBrandChange = viewModel::onBrandChange,
                    onModelChange = viewModel::onModelChange,
                    onPlateChange = viewModel::onPlateChange,
                    onColorChange = viewModel::onColorChange,
                    onYearChange = viewModel::onYearChange,
                    onTypeChange = viewModel::onTypeChange,
                    onSave = viewModel::saveVehicle
                )
            }
        }
    }
}

@Composable
private fun VehicleTopBar(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit,
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
                .clickable(onClick = onNavigateBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = NebengColor.Primary900,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(NebengSpacing.Lg))
        Text(
            text = "Kendaraan Saya",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah kendaraan",
                tint = NebengColor.Primary0,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleInfo,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary50)
            .padding(horizontal = NebengSpacing.Lg, vertical = NebengSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (vehicle.type == VehicleType.MOTORCYCLE) Icons.Outlined.TwoWheeler else Icons.Outlined.DirectionsCar,
                contentDescription = null,
                tint = NebengColor.Primary0,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(NebengSpacing.Md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${vehicle.brand} ${vehicle.model}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = vehicle.plate,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NebengColor.Gray600
            )
            if (!vehicle.color.isNullOrBlank()) {
                Text(
                    text = buildString {
                        append(vehicle.color)
                        vehicle.year?.let { append(" • $it") }
                    },
                    fontSize = 12.sp,
                    color = NebengColor.Gray400
                )
            }
        }

        // Verified badge
        if (vehicle.isVerified) {
            Text(
                text = "TERVERIFIKASI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary0,
                modifier = Modifier
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(NebengColor.Primary900)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.width(NebengSpacing.Sm))
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NebengColor.Danger100)
                .clickable(onClick = onDeleteClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus",
                tint = NebengColor.Danger600,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddVehicleSheetContent(
    state: VehicleManagementUiState,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onTypeChange: (VehicleType) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NebengSpacing.Xl)
            .padding(bottom = NebengSpacing.Xl)
    ) {
        Text(
            text = "Tambah Kendaraan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Xl))

        // Vehicle type selector
        SectionLabel("TIPE KENDARAAN")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NebengSpacing.Md)
        ) {
            VehicleTypeChip(
                label = "Mobil",
                icon = Icons.Outlined.DirectionsCar,
                isSelected = state.formType == VehicleType.CAR,
                onClick = { onTypeChange(VehicleType.CAR) },
                modifier = Modifier.weight(1f)
            )
            VehicleTypeChip(
                label = "Motor",
                icon = Icons.Outlined.TwoWheeler,
                isSelected = state.formType == VehicleType.MOTORCYCLE,
                onClick = { onTypeChange(VehicleType.MOTORCYCLE) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Lg))

        NebengTextField(
            value = state.formBrand,
            onValueChange = onBrandChange,
            label = "Merek*",
            placeholder = "Toyota, Honda, Yamaha..."
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        NebengTextField(
            value = state.formModel,
            onValueChange = onModelChange,
            label = "Model / Tipe*",
            placeholder = "Avanza, Brio, Beat..."
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        NebengTextField(
            value = state.formPlate,
            onValueChange = onPlateChange,
            label = "Nomor Polisi*",
            placeholder = "B 1234 ABC"
        )

        Spacer(modifier = Modifier.height(NebengSpacing.Md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NebengSpacing.Md)
        ) {
            NebengTextField(
                value = state.formColor,
                onValueChange = onColorChange,
                label = "Warna",
                placeholder = "Putih...",
                modifier = Modifier.weight(1f)
            )
            NebengTextField(
                value = state.formYear,
                onValueChange = onYearChange,
                label = "Tahun",
                placeholder = "2022",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        // Error message
        AnimatedVisibility(visible = !state.formError.isNullOrBlank()) {
            Text(
                text = state.formError.orEmpty(),
                fontSize = 13.sp,
                color = NebengColor.Danger600,
                modifier = Modifier.padding(top = NebengSpacing.Sm)
            )
        }

        Spacer(modifier = Modifier.height(NebengSpacing.Xl))

        NebengButton(
            text = "Simpan Kendaraan",
            onClick = onSave,
            style = NebengButtonStyle.PRIMARY,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            isLoading = state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun VehicleTypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary50)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = NebengColor.Gray200,
                shape = RoundedCornerShape(NebengRadius.Md)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = NebengSpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(NebengSpacing.Sm))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = NebengColor.Gray600,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = NebengSpacing.Sm)
    )
}
