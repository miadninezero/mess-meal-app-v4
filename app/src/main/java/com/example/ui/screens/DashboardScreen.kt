package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.data.model.Member
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BazaarExpense
import com.example.ui.MessViewModel
import com.example.util.DhakaTimeHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MessViewModel,
    onNavigateToMeals: () -> Unit,
    onNavigateToBazaar: () -> Unit,
    onNavigateToSharedBills: () -> Unit,
    onNavigateToDeposits: () -> Unit,
    onNavigateToSettlement: () -> Unit,
    onChangeMess: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val mess by viewModel.currentMess.collectAsState()
    val members by viewModel.members.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val dailyMeals by viewModel.dailyMeals.collectAsState()
    val bazaarExpenses by viewModel.bazaarExpenses.collectAsState()
    val sharedBills by viewModel.sharedBills.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsState()

    val currentMember = members.firstOrNull { it.userId == viewModel.currentUserId }
    val memberId = currentMember?.id ?: viewModel.currentUserId
    val todayRecord = dailyMeals.firstOrNull { it.id == viewModel.todayDhakaDate || it.date == viewModel.todayDhakaDate }
    val myBooking = todayRecord?.bookings?.get(memberId)
    val kitchenSummary by viewModel.kitchenSummary.collectAsState()
    val isFriday = todayRecord?.isFriday ?: DhakaTimeHelper.isFriday(viewModel.todayDhakaDate)
    val isManager by viewModel.isManager.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showRolesDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = mess?.name?.ifBlank { "Mess Meal" } ?: "Mess Meal",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Mess ID", mess?.id ?: "")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Mess Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Code: ${mess?.id?.take(8) ?: "..."}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text("$unreadNotifsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mess Members & Roles") },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showRolesDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Switch / Join Mess") },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onChangeMess()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sign Out") },
                                leadingIcon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onSignOut()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Live Meal Rate Hero Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT MEAL RATE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format("%.2f", summary.mealRate),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = " / meal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Formula: Total Bazaar (${summary.totalBazaarCost}) ÷ Confirmed Meal Units (${summary.totalMeals})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 4-Grid Key Financial Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Meal Units",
                    value = "${summary.totalMeals}",
                    icon = Icons.Default.Restaurant,
                    tint = MaterialTheme.colorScheme.primary
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Bazaar",
                    value = "${summary.totalBazaarCost}",
                    icon = Icons.Default.ShoppingBag,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Deposits",
                    value = "${summary.totalDeposits}",
                    icon = Icons.Default.Payments,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Reserve Fund",
                    value = "${summary.remainingFund}",
                    icon = Icons.Default.AccountBalanceWallet,
                    tint = if (summary.remainingFund >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }
        }

        // Today's Booking & Kitchen Orders Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Meals (${viewModel.todayDhakaDate})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Asia/Dhaka Timezone • Strict Cutoffs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (isFriday) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Friday (Lunch Only)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // My Booking Status Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val lunchStatus = myBooking?.statusLunch ?: "NO_RESPONSE"
                        val nightStatus = if (isFriday) "DISABLED" else (myBooking?.statusNight ?: "NO_RESPONSE")
                        val totalGuests = (myBooking?.guestLunch ?: 0) + (myBooking?.guestNight ?: 0)

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("My Lunch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lunchStatus.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (lunchStatus == "BOOKED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("My Night", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = nightStatus.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isFriday) MaterialTheme.colorScheme.outline else if (nightStatus == "BOOKED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Guest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalGuests",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kitchen Order Tally
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🍳 Kitchen Summary:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Lunch: ${kitchenSummary.bookedLunchCount} | Night: ${kitchenSummary.bookedNightCount}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToMeals,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Bookings")
                    }
                }
            }
        }

        // Quick Navigation Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mess Operations",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToSharedBills) {
                    Text("View All Bills", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Shared Bills & Fixed Costs Dedicated Feature Card
        item {
            val totalBills = sharedBills.size
            val totalBillAmount = sharedBills.sumOf { it.totalAmount }
            val totalCollected = sharedBills.sumOf { it.totalCollected }
            val outstandingDue = (totalBillAmount - totalCollected).coerceAtLeast(0.0)

            OutlinedCard(
                onClick = onNavigateToSharedBills,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Shared Bills",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Chef, gas, wifi & utilities",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (outstandingDue <= 0.0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (outstandingDue <= 0.0) "ALL PAID" else "DUE: ৳${outstandingDue.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (outstandingDue <= 0.0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$totalBills Bills Published",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Collected: ৳${totalCollected.toInt()} / ৳${totalBillAmount.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DashboardQuickAction(
                    label = "Meals",
                    icon = Icons.Default.Restaurant,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToMeals,
                    modifier = Modifier.weight(1f)
                )
                DashboardQuickAction(
                    label = "Bazaar",
                    icon = Icons.Default.ShoppingBag,
                    tint = Color(0xFFE65100),
                    onClick = onNavigateToBazaar,
                    modifier = Modifier.weight(1f)
                )
                DashboardQuickAction(
                    label = "Bills",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToSharedBills,
                    modifier = Modifier.weight(1f)
                )
                DashboardQuickAction(
                    label = "Deposit",
                    icon = Icons.Default.Payments,
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = onNavigateToDeposits,
                    modifier = Modifier.weight(1f)
                )
                DashboardQuickAction(
                    label = "Balance",
                    icon = Icons.Default.Assessment,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToSettlement,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Members & Roles Quick Action Card
        item {
            OutlinedCard(
                onClick = { showRolesDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Mess Members & Roles", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "${members.size} Members • ${members.count { it.role == "MANAGER" || mess?.isManager(it.userId) == true }} Managers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isManager) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isManager) "Manager" else "Member",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isManager) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Recent Market Expenses
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Bazaar Purchases",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedButton(
                    onClick = onNavigateToBazaar,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View All", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (bazaarExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "No bazaar records yet this month. Tap 'Bazaar Log' to add grocery purchases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(bazaarExpenses.take(3)) { expense ->
                BazaarMiniCard(expense = expense)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showRolesDialog) {
        ManageRolesDialog(
            members = members,
            currentUserId = viewModel.currentUserId,
            isCurrentUserManager = isManager,
            isMessManager = { uid -> mess?.isManager(uid) == true },
            onPromoteToManager = { member -> viewModel.promoteMemberToManager(member) },
            onDemoteToMember = { member -> viewModel.demoteManagerToMember(member) },
            onResignAsManager = { successor -> viewModel.resignAsManager(successor) },
            onDismiss = { showRolesDialog = false }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false },
            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() }
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MealStepper(
    label: String,
    count: Double,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
            }
            Text(
                text = if (count % 1.0 == 0.0) count.toInt().toString() else count.toString(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardQuickAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BazaarMiniCard(expense: BazaarExpense) {
    val catIcon = when (expense.category.lowercase(Locale.ROOT)) {
        "groceries" -> Icons.Default.ShoppingCart
        "meat & fish" -> Icons.Default.Restaurant
        "vegetables" -> Icons.Default.Eco
        "spices" -> Icons.Default.LocalFireDepartment
        else -> Icons.Default.ShoppingBag
    }
    val catColor = when (expense.category.lowercase(Locale.ROOT)) {
        "groceries" -> Color(0xFFE65100)
        "meat & fish" -> Color(0xFFC62828)
        "vegetables" -> Color(0xFF2E7D32)
        "spices" -> Color(0xFFD84315)
        else -> Color(0xFF5E35B1)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(catColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(catIcon, contentDescription = null, tint = catColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.itemsDescription.ifBlank { "Bazaar Purchase" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${expense.buyerName} • ${DhakaTimeHelper.formatReadableDate(expense.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "৳${expense.amount.toInt()}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ManageRolesDialog(
    members: List<Member>,
    currentUserId: String,
    isCurrentUserManager: Boolean,
    isMessManager: (String) -> Boolean,
    onPromoteToManager: (Member) -> Unit,
    onDemoteToMember: (Member) -> Unit,
    onResignAsManager: (successor: Member?) -> Unit,
    onDismiss: () -> Unit
) {
    var memberToPromote by remember { mutableStateOf<Member?>(null) }
    var memberToDemote by remember { mutableStateOf<Member?>(null) }
    var showResignFlow by remember { mutableStateOf(false) }
    var selectedSuccessorId by remember { mutableStateOf("") }

    val managersCount = members.count { it.role == "MANAGER" || isMessManager(it.userId) }
    val isSoleManager = isCurrentUserManager && managersCount <= 1
    val otherMembers = members.filter { it.userId != currentUserId }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        },
        title = {
            Text("Mess Members & Roles", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                Text(
                    text = "Managers can configure booking deadlines, override bookings, record shared utility bills, and assign other managers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members) { member ->
                        val isUser = member.userId == currentUserId
                        val isThisMemberManager = member.role == "MANAGER" || isMessManager(member.userId)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isThisMemberManager) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isThisMemberManager) {
                                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text(
                                                text = member.displayName.take(1).uppercase(),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = member.displayName + if (isUser) " (You)" else "",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (member.room.isNotBlank()) {
                                                Text("Room ${member.room} • ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                            Text(
                                                text = if (isThisMemberManager) "Manager" else "Member",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isThisMemberManager) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }

                                if (isCurrentUserManager && !isUser) {
                                    if (isThisMemberManager) {
                                        OutlinedButton(
                                            onClick = { memberToDemote = member },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Demote", fontSize = 11.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = { memberToPromote = member },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Make Manager", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isCurrentUserManager) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showResignFlow = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resign from Manager Role")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Promote Member Confirmation
    if (memberToPromote != null) {
        val target = memberToPromote!!
        AlertDialog(
            onDismissRequest = { memberToPromote = null },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Promote to Manager?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to promote '${target.displayName}' to Manager? They will have full administrative authority to configure deadlines, edit bills, and manage bookings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPromoteToManager(target)
                        memberToPromote = null
                    }
                ) {
                    Text("Promote to Manager")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToPromote = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Demote Member Confirmation
    if (memberToDemote != null) {
        val target = memberToDemote!!
        AlertDialog(
            onDismissRequest = { memberToDemote = null },
            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Manager Role?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Change '${target.displayName}' from Manager back to regular Member?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDemoteToMember(target)
                        memberToDemote = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove Manager")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDemote = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Resign Flow
    if (showResignFlow) {
        if (!isSoleManager) {
            // There are other managers
            AlertDialog(
                onDismissRequest = { showResignFlow = false },
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Resign as Manager?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to step down from Manager role? Other mess managers will continue managing the mess, and you will become a regular member.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onResignAsManager(null)
                            showResignFlow = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm Resignation")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResignFlow = false }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            // Sole manager: must select successor
            val successorCandidate = otherMembers.firstOrNull { it.id == selectedSuccessorId || it.userId == selectedSuccessorId }
            AlertDialog(
                onDismissRequest = { showResignFlow = false },
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Transfer & Resign", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "You are currently the sole manager of this mess. To resign, please select a member to transfer the manager role to:",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (otherMembers.isEmpty()) {
                            Text(
                                text = "There are no other members in this mess to transfer to. Add or invite members first.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                otherMembers.forEach { m ->
                                    val isSelected = selectedSuccessorId == m.id || selectedSuccessorId == m.userId
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedSuccessorId = m.id.ifBlank { m.userId } }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(m.displayName, fontWeight = FontWeight.Medium)
                                            if (m.room.isNotBlank()) {
                                                Text("Room: ${m.room}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (successorCandidate != null) {
                                onResignAsManager(successorCandidate)
                                showResignFlow = false
                                onDismiss()
                            }
                        },
                        enabled = successorCandidate != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Transfer & Resign")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResignFlow = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

