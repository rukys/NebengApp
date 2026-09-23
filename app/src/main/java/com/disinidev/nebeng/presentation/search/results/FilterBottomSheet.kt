package com.disinidev.nebeng.presentation.search.results

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.presentation.search.model.DepartureTimeSlot
import com.disinidev.nebeng.presentation.search.model.DriverGender
import com.disinidev.nebeng.presentation.search.model.SearchFilterOptions
import com.disinidev.nebeng.presentation.search.model.SortBy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    filterOptions: SearchFilterOptions,
    matchingRoutesCount: Int,
    onFilterChange: (SearchFilterOptions) -> Unit,
    onResetFilter: () -> Unit,
    onApplyFilter: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NebengColor.Primary0,
        shape = RoundedCornerShape(topStart = NebengRadius.Xl, topEnd = NebengRadius.Xl),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: Title & Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Pencarian Rute",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(NebengColor.Primary50)
                        .clickable(onClick = onResetFilter)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Primary900
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Options Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. URUTKAN BERDASARKAN
                FilterSection(title = "URUTKAN BERDASARKAN") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortBy.entries.forEach { sort ->
                            val isSelected = filterOptions.sortBy == sort
                            FilterChipItem(
                                label = sort.label,
                                isSelected = isSelected,
                                onClick = { onFilterChange(filterOptions.copy(sortBy = sort)) }
                            )
                        }
                    }
                }

                // 2. JENIS KENDARAAN
                FilterSection(title = "JENIS KENDARAAN") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mobil Card
                        val isCarSelected = filterOptions.vehicleType == VehicleType.CAR || filterOptions.vehicleType == null
                        VehicleOptionCard(
                            title = "Mobil",
                            subtitle = "AC & Bagasi",
                            icon = Icons.Default.DirectionsCar,
                            isSelected = isCarSelected,
                            onClick = {
                                val next = if (filterOptions.vehicleType == VehicleType.CAR) null else VehicleType.CAR
                                onFilterChange(filterOptions.copy(vehicleType = next))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Motor Card
                        val isMotorSelected = filterOptions.vehicleType == VehicleType.MOTORCYCLE
                        VehicleOptionCard(
                            title = "Motor",
                            subtitle = "Helm & Jas Hujan",
                            icon = Icons.Default.TwoWheeler,
                            isSelected = isMotorSelected,
                            onClick = {
                                val next = if (filterOptions.vehicleType == VehicleType.MOTORCYCLE) null else VehicleType.MOTORCYCLE
                                onFilterChange(filterOptions.copy(vehicleType = next))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. GENDER PENGEMUDI (Sesuai PRD F-02)
                FilterSection(title = "GENDER PENGEMUDI") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DriverGender.entries.forEach { gender ->
                            val isSelected = filterOptions.driverGender == gender
                            FilterChipItem(
                                label = gender.label,
                                isSelected = isSelected,
                                onClick = { onFilterChange(filterOptions.copy(driverGender = gender)) }
                            )
                        }
                    }
                }

                // 4. WAKTU KEBERANGKATAN
                FilterSection(title = "WAKTU KEBERANGKATAN") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            DepartureTimeSlot.MORNING,
                            DepartureTimeSlot.AFTERNOON,
                            DepartureTimeSlot.EVENING
                        ).forEach { slot ->
                            val isSelected = filterOptions.departureTimeSlot == slot
                            FilterChipItem(
                                label = slot.label,
                                isSelected = isSelected,
                                onClick = { onFilterChange(filterOptions.copy(departureTimeSlot = slot)) }
                            )
                        }
                    }
                }

                // 4. FASILITAS & PREFERENSI
                FilterSection(title = "FASILITAS & PREFERENSI") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreferenceCheckRow(
                            label = "AC Dingin & Nyaman",
                            emoji = "❄️",
                            isChecked = filterOptions.requireAc,
                            onCheckedChange = { onFilterChange(filterOptions.copy(requireAc = it)) }
                        )
                        PreferenceCheckRow(
                            label = "Bebas Asap Rokok (Non-Smoking)",
                            emoji = "🚭",
                            isChecked = filterOptions.requireNonSmoking,
                            onCheckedChange = { onFilterChange(filterOptions.copy(requireNonSmoking = it)) }
                        )
                        PreferenceCheckRow(
                            label = "Rekan Terverifikasi Kantor",
                            emoji = "🪪",
                            isChecked = filterOptions.requireVerifiedOffice,
                            onCheckedChange = { onFilterChange(filterOptions.copy(requireVerifiedOffice = it)) }
                        )
                    }
                }

                // 5. RATING PENGEMUDI
                FilterSection(title = "RATING PENGEMUDI") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChipItem(
                            label = "★ 4.8 Ke Atas",
                            isSelected = filterOptions.minRating == 4.8,
                            onClick = { onFilterChange(filterOptions.copy(minRating = 4.8)) }
                        )
                        FilterChipItem(
                            label = "★ 4.5+",
                            isSelected = filterOptions.minRating == 4.5,
                            onClick = { onFilterChange(filterOptions.copy(minRating = 4.5)) }
                        )
                        FilterChipItem(
                            label = "Semua Rating",
                            isSelected = filterOptions.minRating == null,
                            onClick = { onFilterChange(filterOptions.copy(minRating = null)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apply Button
            NebengButton(
                text = "Terapkan Filter ($matchingRoutesCount Rute Ditemukan)",
                onClick = onApplyFilter,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Gray600
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NebengRadius.Full))
            .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary50)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900
        )
    }
}

@Composable
private fun VehicleOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(NebengColor.Primary0)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NebengColor.Primary900 else NebengColor.Gray200,
                shape = RoundedCornerShape(NebengRadius.Md)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NebengColor.Primary900,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = NebengColor.Gray600
            )
        }
        // Radio / Check Indicator
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary0)
                .border(1.dp, if (isSelected) NebengColor.Primary900 else NebengColor.Gray400, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NebengColor.Primary0,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun PreferenceCheckRow(
    label: String,
    emoji: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(NebengColor.Primary50)
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = NebengColor.Primary900,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = NebengColor.Primary900,
                checkmarkColor = NebengColor.Primary0
            )
        )
    }
}
