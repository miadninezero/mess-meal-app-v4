package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculation.MessCalculationEngine
import com.example.data.model.BillAllocationType
import com.example.data.model.BillPaymentRecord
import com.example.data.model.BillPaymentStatus
import com.example.data.model.InAppNotification
import com.example.data.model.Member
import com.example.data.model.SharedBill
import com.example.ui.MessViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedBillsScreen(
    viewModel: MessViewModel,
    onBack: () -> Unit
) {
    val mess by viewModel.currentMess.collectAsState()
    val members by viewModel.members.collectAsState()
    val isManager by viewModel.isManager.collectAsState()
    val sharedBills by viewModel.sharedBills.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsState()

    val currentMember = members.firstOrNull { it.userId == viewModel.currentUserId }
    val currentMemberId = currentMember?.id ?: viewModel.currentUserId

    var showCreateDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<SharedBill?>(null) }
    var billToDelete by remember { mutableStateOf<SharedBill?>(null) }
    var paymentTarget by remember { mutableStateOf<Pair<SharedBill, Member>?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf("All") }

    val totalBillsAmount = sharedBills.sumOf { it.totalAmount }
    val totalCollected = sharedBills.sumOf { it.totalCollected }
    val totalOutstanding = (totalBillsAmount - totalCollected).coerceAtLeast(0.0)
    val overallProgress = if (totalBillsAmount > 0.0) (totalCollected / totalBillsAmount).toFloat().coerceIn(0f, 1f) else 0f

    val displayedBills = when (filterStatus) {
        "Due" -> sharedBills.filter { it.outstandingBalance > 0.0 }
        "Paid" -> sharedBills.filter { it.outstandingBalance <= 0.0 }
        else -> sharedBills
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Shared Bills",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Utilities & fixed costs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("notifications_bell_button")
                    ) {
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
                            Icon(Icons.Default.Notifications, contentDescription = "In-App Notifications")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master Overview Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COLLECTION OVERVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "${sharedBills.size} Bills",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("৳${totalBillsAmount.toInt()}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Collected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("৳${totalCollected.toInt()}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Outstanding", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text(
                                    "৳${totalOutstanding.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (totalOutstanding > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(overallProgress * 100).toInt()}% collected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "৳${totalOutstanding.toInt()} outstanding",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // My Personal Share & Payment Status Card
            item {
                val myAssignedShares = sharedBills.sumOf { it.getMemberShare(currentMemberId) }
                val myPaid = sharedBills.sumOf { it.getMemberPaid(currentMemberId) }
                val myDue = (myAssignedShares - myPaid).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "My Bill Status",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (myDue <= 0.0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = if (myDue <= 0.0) "ALL PAID" else "DUE: ৳${myDue.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (myDue <= 0.0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Assigned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text("৳${myAssignedShares.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Paid", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text("৳${myPaid.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Outstanding", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    "৳${myDue.toInt()}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (myDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Manager Create Bill & Filter Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Published Bills",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Fixed, equal split, or custom amounts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isManager) {
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("create_shared_bill_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Bill", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Filter Chips in LazyRow
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterStatus == "All",
                            onClick = { filterStatus = "All" },
                            label = { Text("All (${sharedBills.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterStatus == "Due",
                            onClick = { filterStatus = "Due" },
                            label = { Text("Due (${sharedBills.count { it.outstandingBalance > 0 }})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterStatus == "Paid",
                            onClick = { filterStatus = "Paid" },
                            label = { Text("Paid (${sharedBills.count { it.outstandingBalance <= 0 }})") }
                        )
                    }
                }
            }

            // Empty State or List of Bills
            if (displayedBills.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (sharedBills.isEmpty()) "No shared bills published yet" else "No bills match the filter",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isManager) "Tap 'Create Bill' above to publish chef fees, gas, or house rent." else "Your mess manager has not published any shared bills yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedBills) { bill ->
                    SharedBillCard(
                        bill = bill,
                        allMembers = members,
                        currentUserId = viewModel.currentUserId,
                        currentMemberId = currentMemberId,
                        isManager = isManager,
                        onEditBill = { billToEdit = bill },
                        onDeleteBill = { billToDelete = bill },
                        onRecordPayment = { targetMember ->
                            paymentTarget = Pair(bill, targetMember)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        CreateSharedBillDialog(
            activeMembers = members.filter { it.isActive },
            onDismiss = { showCreateDialog = false },
            onCreate = { title, allocationType, amount, deadline, notes, customShares ->
                viewModel.createSharedBill(title, allocationType, amount, deadline, notes, customShares)
                showCreateDialog = false
            }
        )
    }

    billToEdit?.let { bill ->
        EditSharedBillDialog(
            bill = bill,
            allMembers = members,
            onDismiss = { billToEdit = null },
            onSave = { updated, updatedShares ->
                viewModel.updateSharedBill(updated, updatedShares)
                billToEdit = null
            }
        )
    }

    billToDelete?.let { bill ->
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Shared Bill?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${bill.title}' (৳${bill.totalAmount})? This will immediately remove it from all member accounts.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSharedBill(bill)
                        billToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    paymentTarget?.let { (bill, member) ->
        val share = bill.getMemberShare(member.id)
        val existingPaid = bill.getMemberPaid(member.id)
        val existingPayment = bill.payments[member.id]

        RecordPaymentDialog(
            memberName = member.displayName,
            assignedShare = share,
            currentPaid = existingPaid,
            existingRecord = existingPayment,
            onDismiss = { paymentTarget = null },
            onSave = { amountPaid, method, date, notes ->
                viewModel.recordBillPayment(
                    billId = bill.id,
                    memberId = member.id,
                    memberName = member.displayName,
                    assignedShare = share,
                    amountPaid = amountPaid,
                    paymentMethod = method,
                    paymentDate = date,
                    notes = notes
                )
                paymentTarget = null
            }
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
private fun SharedBillCard(
    bill: SharedBill,
    allMembers: List<Member>,
    currentUserId: String,
    currentMemberId: String,
    isManager: Boolean,
    onEditBill: () -> Unit,
    onDeleteBill: () -> Unit,
    onRecordPayment: (Member) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val total = bill.totalAmount
    val collected = bill.totalCollected
    val outstanding = bill.outstandingBalance
    val progress = if (total > 0) (collected / total).toFloat().coerceIn(0f, 1f) else 0f
    val isFullyPaid = outstanding <= 0.0

    val allocationLabel = when (bill.allocationType) {
        BillAllocationType.FIXED_PER_MEMBER -> "Fixed ৳${bill.perMemberAmount.toInt()}"
        BillAllocationType.CUSTOM_PER_MEMBER -> "Custom"
        else -> "Equal Split"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Allocation Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = allocationLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (bill.month.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${bill.month}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFullyPaid) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isFullyPaid) "PAID" else "DUE ৳${outstanding.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isFullyPaid) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deadline & Notes Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Deadline: ${bill.deadline}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (isManager) {
                    Row {
                        IconButton(onClick = onEditBill, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Bill", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onDeleteBill, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Bill", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            if (bill.notes.isNotBlank()) {
                Text(
                    text = bill.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar & Figures
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isFullyPaid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Column Clean Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("৳${total.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Collected (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("৳${collected.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        "৳${outstanding.toInt()}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (outstanding > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Member summary counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paid: ${bill.paidMembersCount} • Partial: ${bill.partiallyPaidMembersCount} • Unpaid: ${bill.unpaidMembersCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Expandable Member Payments Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Member Shares & Payments (${bill.assignedMemberIds.size})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val assignedMembers = allMembers.filter { bill.assignedMemberIds.contains(it.id) }

                    if (assignedMembers.isEmpty()) {
                        // Fallback to assignedMemberIds if members list didn't load yet
                        bill.assignedMemberIds.forEach { memId ->
                            val name = bill.memberNames[memId] ?: "Member"
                            val pseudoMember = Member(id = memId, displayName = name)
                            MemberPaymentRow(
                                bill = bill,
                                member = pseudoMember,
                                isManager = isManager,
                                isMe = memId == currentMemberId || memId == currentUserId,
                                onRecordPayment = { onRecordPayment(pseudoMember) }
                            )
                        }
                    } else {
                        assignedMembers.forEach { member ->
                            MemberPaymentRow(
                                bill = bill,
                                member = member,
                                isManager = isManager,
                                isMe = member.id == currentMemberId || member.userId == currentUserId,
                                onRecordPayment = { onRecordPayment(member) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberPaymentRow(
    bill: SharedBill,
    member: Member,
    isManager: Boolean,
    isMe: Boolean,
    onRecordPayment: () -> Unit
) {
    val share = bill.getMemberShare(member.id)
    val paid = bill.getMemberPaid(member.id)
    val due = bill.getMemberDue(member.id)
    val status = bill.getMemberPaymentStatus(member.id)
    val payment = bill.payments[member.id]

    val badgeColor = when (status) {
        BillPaymentStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer
        BillPaymentStatus.PARTIALLY_PAID -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val badgeTextColor = when (status) {
        BillPaymentStatus.PAID -> MaterialTheme.colorScheme.onSecondaryContainer
        BillPaymentStatus.PARTIALLY_PAID -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    val statusLabel = when (status) {
        BillPaymentStatus.PAID -> "PAID"
        BillPaymentStatus.PARTIALLY_PAID -> "PARTIAL"
        else -> "UNPAID"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.displayName + if (isMe) " (You)" else "",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Share: ৳$share • Paid: ৳$paid • Due: ৳$due",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (payment != null && payment.amountPaid > 0 && payment.paymentDate.isNotBlank()) {
                    Text(
                        text = "Paid on ${payment.paymentDate} via ${payment.paymentMethod}${if (payment.notes.isNotBlank()) " (${payment.notes})" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isManager) {
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = onRecordPayment,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Record", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CreateSharedBillDialog(
    activeMembers: List<Member>,
    onDismiss: () -> Unit,
    onCreate: (title: String, allocationType: String, amount: Double, deadline: String, notes: String, customShares: Map<String, Double>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var allocationType by remember { mutableStateOf(BillAllocationType.FIXED_PER_MEMBER) }
    var amountText by remember { mutableStateOf("") }
    var bulkCustomAmount by remember { mutableStateOf("") }

    val defaultDeadline = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 7)
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
    var deadline by remember { mutableStateOf(defaultDeadline) }
    var notes by remember { mutableStateOf("") }

    // Map of memberId -> custom share text
    val memberCustomShares = remember(activeMembers) {
        mutableStateMapOf<String, String>().apply {
            activeMembers.forEach { put(it.id, "") }
        }
    }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val memberCount = if (activeMembers.isNotEmpty()) activeMembers.size else 1
    val isCustomMode = allocationType == BillAllocationType.CUSTOM_PER_MEMBER

    val totalCustomAmount = if (isCustomMode) {
        MessCalculationEngine.roundTwoDecimals(memberCustomShares.values.sumOf { it.toDoubleOrNull() ?: 0.0 })
    } else 0.0

    val previewTotal = when (allocationType) {
        BillAllocationType.CUSTOM_PER_MEMBER -> totalCustomAmount
        BillAllocationType.FIXED_PER_MEMBER -> MessCalculationEngine.roundTwoDecimals(parsedAmount * memberCount)
        else -> MessCalculationEngine.roundTwoDecimals(parsedAmount)
    }

    val previewPerMember = when (allocationType) {
        BillAllocationType.CUSTOM_PER_MEMBER -> if (memberCount > 0) MessCalculationEngine.roundTwoDecimals(totalCustomAmount / memberCount) else 0.0
        BillAllocationType.FIXED_PER_MEMBER -> MessCalculationEngine.roundTwoDecimals(parsedAmount)
        else -> MessCalculationEngine.roundTwoDecimals(parsedAmount / memberCount)
    }

    val canPublish = if (isCustomMode) {
        title.isNotBlank() && totalCustomAmount > 0.0 && deadline.isNotBlank()
    } else {
        title.isNotBlank() && parsedAmount > 0.0 && deadline.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Shared Bill", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title *") },
                    placeholder = { Text("e.g. Chef Bill, Gas Bill, WiFi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quick Title Suggestions
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val suggestions = listOf("Chef Bill", "Gas Bill", "WiFi Bill", "Electricity", "Housemaid", "Filter Water")
                    items(suggestions) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { title = item }
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Allocation Type *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Radio options
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { allocationType = BillAllocationType.FIXED_PER_MEMBER }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = allocationType == BillAllocationType.FIXED_PER_MEMBER,
                            onClick = { allocationType = BillAllocationType.FIXED_PER_MEMBER }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Fixed per Member", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text("e.g. Chef Bill ৳500 each × $memberCount members", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { allocationType = BillAllocationType.DIVIDED_EQUALLY }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = allocationType == BillAllocationType.DIVIDED_EQUALLY,
                            onClick = { allocationType = BillAllocationType.DIVIDED_EQUALLY }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Divide Equally", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text("e.g. Gas Bill ৳2,400 split evenly", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { allocationType = BillAllocationType.CUSTOM_PER_MEMBER }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = allocationType == BillAllocationType.CUSTOM_PER_MEMBER,
                            onClick = { allocationType = BillAllocationType.CUSTOM_PER_MEMBER }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Custom per Member", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text("Set different amounts for each member", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                if (isCustomMode) {
                    // Custom shares per member
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Custom Member Shares (${activeMembers.size})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Total: ৳${totalCustomAmount.toInt()}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }

                            // Quick bulk fill tool
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = bulkCustomAmount,
                                    onValueChange = { bulkCustomAmount = it },
                                    placeholder = { Text("Base ৳") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).height(48.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        val v = bulkCustomAmount.trim()
                                        if (v.isNotBlank()) {
                                            activeMembers.forEach { memberCustomShares[it.id] = v }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Set All", fontSize = 11.sp)
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                activeMembers.forEach { member ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = member.displayName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = memberCustomShares[member.id] ?: "",
                                            onValueChange = { memberCustomShares[member.id] = it },
                                            placeholder = { Text("0") },
                                            prefix = { Text("৳", style = MaterialTheme.typography.bodySmall) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.width(100.dp).height(48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = {
                            Text(
                                if (allocationType == BillAllocationType.FIXED_PER_MEMBER) "Fixed Amount Per Member (৳) *" else "Total Bill Amount (৳) *"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Calculation Preview Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Calculation Preview:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (allocationType) {
                                BillAllocationType.CUSTOM_PER_MEMBER -> "Total Bill: ৳${previewTotal.toInt()} • Configured for $memberCount members"
                                BillAllocationType.FIXED_PER_MEMBER -> "Each Member: ৳${previewPerMember.toInt()} • Total: ৳${previewTotal.toInt()} ($memberCount members)"
                                else -> "Total Bill: ৳${previewTotal.toInt()} • Each Share: ৳${previewPerMember.toInt()} ($memberCount members)"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Payment Deadline (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Optional Notes") },
                    placeholder = { Text("e.g. Chef salary for Nov 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canPublish) {
                        val customMap = if (isCustomMode) {
                            memberCustomShares.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
                        } else emptyMap()
                        onCreate(title.trim(), allocationType, if (isCustomMode) totalCustomAmount else parsedAmount, deadline.trim(), notes.trim(), customMap)
                    }
                },
                enabled = canPublish
            ) {
                Text("Publish & Notify Members")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditSharedBillDialog(
    bill: SharedBill,
    allMembers: List<Member>,
    onDismiss: () -> Unit,
    onSave: (SharedBill, Map<String, Double>?) -> Unit
) {
    var title by remember { mutableStateOf(bill.title) }
    var deadline by remember { mutableStateOf(bill.deadline) }
    var notes by remember { mutableStateOf(bill.notes) }
    var customizeShares by remember { mutableStateOf(bill.allocationType == BillAllocationType.CUSTOM_PER_MEMBER) }

    val memberSharesMap = remember(bill) {
        mutableStateMapOf<String, String>().apply {
            bill.assignedMemberIds.forEach { memId ->
                val s = bill.getMemberShare(memId)
                put(memId, if (s % 1.0 == 0.0) s.toInt().toString() else s.toString())
            }
        }
    }

    val updatedCustomTotal = MessCalculationEngine.roundTwoDecimals(memberSharesMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 })

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Shared Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Historical member allocations and past payments are preserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Payment Deadline (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Customize Member Shares", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    TextButton(onClick = { customizeShares = !customizeShares }) {
                        Text(if (customizeShares) "Hide" else "Customize")
                    }
                }

                if (customizeShares) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Set individual amounts differently for each member if needed.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            bill.assignedMemberIds.forEach { memId ->
                                val name = bill.memberNames[memId] ?: allMembers.firstOrNull { it.id == memId }?.displayName ?: "Member"
                                val paid = bill.getMemberPaid(memId)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (paid > 0) {
                                            Text("Paid: ৳${paid.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = memberSharesMap[memId] ?: "",
                                        onValueChange = { memberSharesMap[memId] = it },
                                        prefix = { Text("৳", style = MaterialTheme.typography.bodySmall) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.width(100.dp).height(48.dp)
                                    )
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("New Total Bill:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("৳${updatedCustomTotal.toInt()}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && deadline.isNotBlank()) {
                        val newShares = if (customizeShares) {
                            memberSharesMap.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
                        } else null
                        val newAllocType = if (customizeShares) BillAllocationType.CUSTOM_PER_MEMBER else bill.allocationType
                        onSave(
                            bill.copy(
                                title = title.trim(),
                                deadline = deadline.trim(),
                                notes = notes.trim(),
                                allocationType = newAllocType
                            ),
                            newShares
                        )
                    }
                },
                enabled = title.isNotBlank() && deadline.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RecordPaymentDialog(
    memberName: String,
    assignedShare: Double,
    currentPaid: Double,
    existingRecord: BillPaymentRecord?,
    onDismiss: () -> Unit,
    onSave: (amountPaid: Double, method: String, date: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (currentPaid > 0) currentPaid.toString() else assignedShare.toString()) }
    var paymentMethod by remember { mutableStateOf(existingRecord?.paymentMethod ?: "Cash") }
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var paymentDate by remember { mutableStateOf(existingRecord?.paymentDate?.ifBlank { todayDate } ?: todayDate) }
    var notes by remember { mutableStateOf(existingRecord?.notes ?: "") }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

    val previewStatus = when {
        parsedAmount >= assignedShare && assignedShare > 0.0 -> BillPaymentStatus.PAID
        parsedAmount > 0.0 -> BillPaymentStatus.PARTIALLY_PAID
        else -> BillPaymentStatus.UNPAID
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Payment for $memberName", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Assigned Share:", style = MaterialTheme.typography.bodySmall)
                        Text("৳$assignedShare", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quick shortcuts
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { amountText = assignedShare.toString() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pay Full (৳$assignedShare)", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { amountText = "0" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mark Unpaid", fontSize = 11.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (previewStatus) {
                        BillPaymentStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer
                        BillPaymentStatus.PARTIALLY_PAID -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Status will be: $previewStatus (Remaining due: ৳${(assignedShare - parsedAmount).coerceAtLeast(0.0)})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (previewStatus) {
                            BillPaymentStatus.PAID -> MaterialTheme.colorScheme.onSecondaryContainer
                            BillPaymentStatus.PARTIALLY_PAID -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Payment Method Chips
                Text("Payment Method", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Cash", "bKash", "Nagad", "Bank").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transaction Reference / Notes") },
                    placeholder = { Text("e.g. bKash TrxID #8928") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(parsedAmount, paymentMethod, paymentDate.trim(), notes.trim())
                }
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationsDialog(
    notifications: List<InAppNotification>,
    onDismiss: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("In-App Notifications", fontWeight = FontWeight.Bold)
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Text("Mark all read", fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notif ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!notif.isRead) onMarkAsRead(notif.id)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (!notif.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!notif.isRead) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Amount: ৳${notif.amount} • Due: ${notif.deadline}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = notif.timestampFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
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
}
