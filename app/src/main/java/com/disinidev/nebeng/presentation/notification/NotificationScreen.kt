package com.disinidev.nebeng.presentation.notification

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.Notification
import com.disinidev.nebeng.domain.model.NotificationCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToLiveTracking: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val groupedNotifications = remember(state.notifications) {
        groupNotificationsByDate(state.notifications)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = NebengColor.Primary0,
        topBar = {
            NotificationTopBar(
                onBackClick = onNavigateBack,
                onMarkAllReadClick = viewModel::markAllAsRead
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Filter Chips Row
                item {
                    NotificationFilterRow(
                        selectedFilter = state.selectedFilter,
                        totalCount = state.totalCount,
                        tripCount = state.tripCount,
                        systemCount = state.systemCount,
                        onFilterSelected = viewModel::onSelectFilter
                    )
                }

                // 2. Notifications List or Empty State
                if (state.notifications.isEmpty()) {
                    item {
                        EmptyNotificationView()
                    }
                } else {
                    groupedNotifications.forEach { (dateHeader, notifs) ->
                        item {
                            Text(
                                text = dateHeader,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebengColor.Gray600,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(notifs, key = { it.id }) { notif ->
                            NotificationCard(
                                notification = notif,
                                onClick = { viewModel.markAsRead(notif.id) },
                                onActionClick = {
                                    viewModel.markAsRead(notif.id)
                                    onNavigateToLiveTracking("booking_current")
                                }
                            )
                        }
                    }
                }

                // 3. Footer Preferences
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = NebengColor.Gray400,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Atur preferensi notifikasi di menu Pengaturan Akun",
                            fontSize = 12.sp,
                            color = NebengColor.Gray600
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationTopBar(
    onBackClick: () -> Unit,
    onMarkAllReadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

        Spacer(modifier = Modifier.width(14.dp))

        // Title
        Text(
            text = "Notifikasi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NebengColor.Primary900
        )

        Spacer(modifier = Modifier.weight(1f))

        // "Tandai Dibaca" Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(NebengColor.Primary50)
                .clickable(onClick = onMarkAllReadClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tandai Dibaca",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }
        }
    }
}

@Composable
private fun NotificationFilterRow(
    selectedFilter: NotificationFilter,
    totalCount: Int,
    tripCount: Int,
    systemCount: Int,
    onFilterSelected: (NotificationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val count = when (filter) {
                NotificationFilter.ALL -> totalCount
                NotificationFilter.TRIP -> tripCount
                NotificationFilter.SYSTEM -> systemCount
            }
            val label = "${filter.label} ($count)"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary50)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) NebengColor.Primary0 else NebengColor.Primary900
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (!notification.isRead) NebengColor.Primary900 else NebengColor.Gray200
    val borderWidth = if (!notification.isRead) 1.5.dp else 1.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(borderWidth, borderColor, RoundedCornerShape(NebengRadius.Lg))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            val (iconVector, iconBg, iconTint) = when (notification.category) {
                NotificationCategory.TRIP -> {
                    if (!notification.isRead) {
                        Triple(Icons.Default.DirectionsCar, NebengColor.Primary900, NebengColor.Primary0)
                    } else {
                        Triple(Icons.Default.RateReview, NebengColor.Primary50, NebengColor.Primary900)
                    }
                }
                NotificationCategory.REVIEW -> Triple(Icons.Default.RateReview, NebengColor.Primary50, NebengColor.Primary900)
                NotificationCategory.SYSTEM -> Triple(Icons.Default.VerifiedUser, NebengColor.Primary50, NebengColor.Primary900)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Text(
                            text = "●",
                            fontSize = 10.sp,
                            color = NebengColor.Primary900,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    } else {
                        Text(
                            text = formatNotificationTime(notification.createdAt),
                            fontSize = 11.sp,
                            color = NebengColor.Gray400,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    fontSize = 13.sp,
                    color = NebengColor.Gray600,
                    lineHeight = 18.sp
                )
            }
        }

        // Action button row (for trip/tracking action)
        if (!notification.actionUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatNotificationHour(notification.createdAt)} • Perjalanan",
                    fontSize = 11.sp,
                    color = NebengColor.Gray600
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(NebengColor.Primary900)
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lihat Live Tracking →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary0
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tidak ada notifikasi",
            fontSize = 14.sp,
            color = NebengColor.Gray600
        )
    }
}

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm 'WIB'").withZone(ZoneId.of("Asia/Jakarta"))
private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMM").withZone(ZoneId.of("Asia/Jakarta"))

private fun formatNotificationHour(instant: Instant): String {
    return timeFormat.format(instant)
}

private fun formatNotificationTime(instant: Instant): String {
    val zone = ZoneId.of("Asia/Jakarta")
    val notifDate = instant.atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)

    return when {
        notifDate.isEqual(today) -> timeFormat.format(instant)
        notifDate.isEqual(today.minusDays(1)) -> "Kemarin"
        else -> dayMonthFormat.format(instant)
    }
}

private fun groupNotificationsByDate(notifications: List<Notification>): Map<String, List<Notification>> {
    val zone = ZoneId.of("Asia/Jakarta")
    val today = LocalDate.now(zone)
    val yesterday = today.minusDays(1)

    return notifications.groupBy { notif ->
        val notifDate = notif.createdAt.atZone(zone).toLocalDate()
        when {
            notifDate.isEqual(today) -> "HARI INI"
            notifDate.isEqual(yesterday) -> "KEMARIN"
            else -> notifDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")).uppercase()
        }
    }
}
