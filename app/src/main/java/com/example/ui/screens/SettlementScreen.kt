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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculation.MemberSettlement
import com.example.data.model.UtilityBill
import com.example.ui.MessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementScreen(
    viewModel: MessViewModel,
    onNavigateToSharedBills: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val mess by viewModel.currentMess.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val utilityBills by viewModel.utilityBills.collectAsState()
    val sharedBills by viewModel.sharedBills.collectAsState()
    val isManager by viewModel.isManager.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showAddBillDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<UtilityBill?>(null) }
    var filterStatus by remember { mutableStateOf("All") }
    var showAuditSection by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<UtilityBill?>(null) }

    fun copyReportToClipboard() {
        val report = buildString {
            appendLine("════════════════════════════════")
            appendLine("📋 ${mess?.name ?: "MESS MEAL"} - FINAL SETTLEMENT")
            appendLine("Month: ${mess?.activeMonth ?: "Current"}")
            appendLine("════════════════════════════════")
            appendLine("• Total Bazaar (Groceries): ${summary.totalBazaarCost}")
            appendLine("• Total Confirmed Meal Units: ${summary.totalMeals} units")
            appendLine("• Final Meal Rate: ${summary.mealRate} / unit")
            appendLine("• Shared Bills & Fixed Costs: ৳${summary.totalSharedBills} (Collected: ৳${summary.totalSharedBillsCollected}, Outstanding: ৳${summary.totalSharedBillsUnpaid})")
            appendLine("• Legacy Utilities: ${summary.totalUtilities}")
            appendLine("• Total Member Deposits: ${summary.totalDeposits}")
            appendLine("• Mess Reserve Balance: ${summary.remainingFund}")
            appendLine("────────────────────────────────")
            appendLine("MEMBER RECONCILIATION:")
            summary.memberSettlements.forEach { s ->
                val status = if (s.netBalance >= 0) "REFUND: +${s.netBalance}" else "DUE: ${s.netBalance}"
                appendLine("👤 ${s.memberName}")
                val breakdown = if (s.guestUnits > 0) " (Member: ${s.memberUnits}, Guest: ${s.guestUnits})" else ""
                appendLine("   Confirmed Meal Units: ${s.totalMeals} units$breakdown | Meal Cost: ${s.mealCost}")
                if (s.sharedBillsTotalShare > 0) {
                    appendLine("   Shared Bills: Share ৳${s.sharedBillsTotalShare} | Paid ৳${s.sharedBillsPaid} | Unpaid ৳${s.sharedBillsUnpaid}")
                }
                appendLine("   Total Expense: ${s.totalCost} | Deposit: ${s.totalDeposit}")
                appendLine("   Status: $status")
                appendLine("")
            }
            appendLine("════════════════════════════════")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mess Settlement Report", report)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Settlement report copied to clipboard!", Toast.LENGTH_LONG).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mess Balance & Settlement", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { copyReportToClipboard() },
                        modifier = Modifier.testTag("share_settlement_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export Report")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Metrics Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "MONTHLY MESS BALANCE SHEET",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Final Meal Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("${summary.mealRate}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("${summary.totalBazaarCost + summary.totalUtilities}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Confirmed Meal Units:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${summary.totalMeals} units", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(
                            text = "Rules: Normal day (Both=2, Lunch=1.5, Night=1) • Friday (Lunch=1) • Single served=1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Bazaar:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${summary.totalBazaarCost}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Shared Bills Total:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("৳${summary.totalSharedBills.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Shared Bills Due:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                if (summary.totalSharedBillsUnpaid <= 0) "All Paid" else "৳${summary.totalSharedBillsUnpaid.toInt()} Unpaid",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (summary.totalSharedBillsUnpaid <= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                            )
                        }
                        if (summary.totalUtilities > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Legacy Utilities:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("${summary.totalUtilities}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Member Deposits:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${summary.totalDeposits}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Cash in Hand / Reserve:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "${summary.remainingFund}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (summary.remainingFund >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Shared Utility Bills Header & Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Shared Bills",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Chef, gas, wifi & utilities (separated from grocery)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onNavigateToSharedBills,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Manage Bills (${sharedBills.size})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            if (utilityBills.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "No shared bills recorded this month",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap 'Add Bill' above to record cook salary, gas, or wifi costs.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(utilityBills) { bill ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bill.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                                Text(
                                    text = "Month: ${bill.month} • Shared across ${summary.memberSettlements.size.coerceAtLeast(1)} members",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${bill.amount}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                if (isManager) {
                                    IconButton(onClick = { billToEdit = bill }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Bill",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = { billToDelete = bill }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Bill",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Individual Member Final Accounts Header & Filter
            item {
                Column {
                    Text(
                        text = "Individual Member Balances",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterStatus == "All",
                            onClick = { filterStatus = "All" },
                            label = { Text("All (${summary.memberSettlements.size})") }
                        )
                        FilterChip(
                            selected = filterStatus == "Refund",
                            onClick = { filterStatus = "Refund" },
                            label = { Text("Refund Due (${summary.memberSettlements.count { it.netBalance >= 0 }})") }
                        )
                        FilterChip(
                            selected = filterStatus == "Due",
                            onClick = { filterStatus = "Due" },
                            label = { Text("Payment Due (${summary.memberSettlements.count { it.netBalance < 0 }})") }
                        )
                    }
                }
            }

            val displayedSettlements = when (filterStatus) {
                "Refund" -> summary.memberSettlements.filter { it.netBalance >= 0 }
                "Due" -> summary.memberSettlements.filter { it.netBalance < 0 }
                else -> summary.memberSettlements
            }

            items(displayedSettlements) { settlement ->
                MemberSettlementCard(settlement = settlement)
            }

            // Audit Trail Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Audit History & Governance",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Immutable ledger of financial & attendance overrides",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            TextButton(onClick = { showAuditSection = !showAuditSection }) {
                                Text(if (showAuditSection) "Hide (${auditLogs.size})" else "Show (${auditLogs.size})")
                            }
                        }

                        if (showAuditSection) {
                            Spacer(modifier = Modifier.height(10.dp))
                            if (auditLogs.isEmpty()) {
                                Text(
                                    text = "No audit entries recorded yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    auditLogs.take(15).forEach { log ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = log.action.replace("_", " "),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = log.timestampFormatted,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = log.details,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "By: ${log.performedByName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Export Action Button
            item {
                Button(
                    onClick = { copyReportToClipboard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy & Export Statement to Clipboard", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        if (showAddBillDialog) {
            AddUtilityBillDialog(
                onDismiss = { showAddBillDialog = false },
                onAdd = { title, amount ->
                    viewModel.addUtilityBill(title, amount)
                    showAddBillDialog = false
                }
            )
        }

        billToEdit?.let { bill ->
            EditUtilityBillDialog(
                bill = bill,
                onDismiss = { billToEdit = null },
                onSave = { updated ->
                    viewModel.updateUtilityBill(updated)
                    billToEdit = null
                }
            )
        }

        val bill = billToDelete
        if (bill != null) {
            AlertDialog(
                onDismissRequest = { billToDelete = null },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete Utility Bill?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete '${bill.title}' (৳${bill.amount})? This will immediately remove it from mess shared expenses and recalculate member balances.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteUtilityBill(bill)
                            billToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { billToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun MemberSettlementCard(settlement: MemberSettlement) {
    val isRefund = settlement.isRefund
    val statusColor = if (isRefund) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    val statusBadgeBg = if (isRefund) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
    val statusBadgeText = if (isRefund) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer

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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = settlement.memberName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBadgeBg
                ) {
                    Text(
                        text = if (isRefund) "REFUND: +${settlement.netBalance}" else "DUE: ${settlement.netBalance}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = statusBadgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Confirmed Meal Units:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${settlement.totalMeals} units", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            }
            if (settlement.guestUnits > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  ↳ Portions:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("Member: ${settlement.memberUnits} | Guests: ${settlement.guestUnits}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.outline)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Meal Cost (${settlement.totalMeals} × rate):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${settlement.mealCost}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            }
            if (settlement.utilityCost > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Legacy Utility Share:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${settlement.utilityCost}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
            }
            if (settlement.sharedBillsTotalShare > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Shared Bills Assigned:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("৳${settlement.sharedBillsTotalShare}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  ↳ Paid Directly:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("৳${settlement.sharedBillsPaid}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                }
                if (settlement.sharedBillsUnpaid > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("  ↳ Due in Settlement:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        Text("৳${settlement.sharedBillsUnpaid}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Member Expense:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text("${settlement.totalCost}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Deposit Paid:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Text("${settlement.totalDeposit}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun AddUtilityBillDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Shared Utility Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Bill Title") },
                    placeholder = { Text("e.g. Cook Salary, Gas, WiFi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total Bill Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (titleText.isNotBlank() && amount > 0) {
                        onAdd(titleText.trim(), amount)
                    }
                },
                enabled = titleText.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add Bill")
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
private fun EditUtilityBillDialog(
    bill: UtilityBill,
    onDismiss: () -> Unit,
    onSave: (UtilityBill) -> Unit
) {
    var titleText by remember { mutableStateOf(bill.title) }
    var amountText by remember { mutableStateOf(bill.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Shared Utility Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Bill Title") },
                    placeholder = { Text("e.g. Cook Salary, Gas, WiFi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total Bill Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (titleText.isNotBlank() && amount > 0) {
                        onSave(bill.copy(title = titleText.trim(), amount = amount))
                    }
                },
                enabled = titleText.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
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
