package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BazaarExpense
import com.example.ui.MessViewModel
import com.example.util.DhakaTimeHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazaarLogScreen(
    viewModel: MessViewModel,
    onBack: () -> Unit
) {
    val bazaarExpenses by viewModel.bazaarExpenses.collectAsState()
    val totalBazaar = bazaarExpenses.sumOf { it.amount }
    val isManager by viewModel.isManager.collectAsState()
    val members by viewModel.members.collectAsState()
    val currentMember = members.firstOrNull { it.userId == viewModel.currentUserId }
    val myMemberId = currentMember?.id ?: viewModel.currentUserId

    val categories = listOf("All", "Groceries", "Meat & Fish", "Vegetables", "Spices", "Misc")
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<BazaarExpense?>(null) }

    val filteredExpenses = if (selectedCategory == "All") {
        bazaarExpenses
    } else {
        bazaarExpenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bazaar & Grocery Logs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_bazaar_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bazaar Entry")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Total Banner with dual metrics and high contrast
            val myTotalBazaar = bazaarExpenses.filter { it.buyerMemberId == myMemberId }.sumOf { it.amount }
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOTAL BAZAAR EXPENDITURE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "৳${totalBazaar.toInt()}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "My purchases: ৳${myTotalBazaar.toInt()}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Chips with visual icons
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val icon = when (cat.lowercase(Locale.ROOT)) {
                        "groceries" -> Icons.Default.ShoppingCart
                        "meat & fish" -> Icons.Default.Restaurant
                        "vegetables" -> Icons.Default.Eco
                        "spices" -> Icons.Default.LocalFireDepartment
                        else -> Icons.Default.ShoppingBag
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        leadingIcon = if (cat != "All") {
                            { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No bazaar records found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredExpenses) { expense ->
                        val canEdit = isManager || expense.buyerMemberId == myMemberId
                        val canDelete = isManager || expense.buyerMemberId == myMemberId
                        BazaarExpenseCard(
                            expense = expense,
                            canEdit = canEdit,
                            canDelete = canDelete,
                            onEdit = { expenseToEdit = expense },
                            onDelete = { viewModel.deleteBazaarExpense(expense.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        if (showAddDialog) {
            AddBazaarDialog(
                defaultDate = viewModel.todayString,
                onDismiss = { showAddDialog = false },
                onAdd = { amount, description, category, date ->
                    viewModel.addBazaarExpense(amount, description, category, date)
                    showAddDialog = false
                }
            )
        }

        expenseToEdit?.let { expense ->
            EditBazaarDialog(
                expense = expense,
                onDismiss = { expenseToEdit = null },
                onSave = { updated ->
                    viewModel.updateBazaarExpense(updated)
                    expenseToEdit = null
                }
            )
        }
    }
}

fun getBazaarCategoryStyle(category: String): Triple<ImageVector, Color, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        "groceries" -> Triple(Icons.Default.ShoppingCart, Color(0xFFE65100), Color(0xFFFFF3E0))
        "meat & fish" -> Triple(Icons.Default.Restaurant, Color(0xFFC62828), Color(0xFFFFEBEE))
        "vegetables" -> Triple(Icons.Default.Eco, Color(0xFF2E7D32), Color(0xFFE8F5E9))
        "spices" -> Triple(Icons.Default.LocalFireDepartment, Color(0xFFD84315), Color(0xFFFBE9E7))
        else -> Triple(Icons.Default.ShoppingBag, Color(0xFF5E35B1), Color(0xFFEDE7F6))
    }
}

@Composable
private fun BazaarExpenseCard(
    expense: BazaarExpense,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (catIcon, catColor, _) = getBazaarCategoryStyle(expense.category)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        shadowElevation = 1.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(catColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catIcon,
                    contentDescription = expense.category,
                    tint = catColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.itemsDescription.ifBlank { "Market Purchase" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = catColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = catColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = expense.buyerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DhakaTimeHelper.formatReadableDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "৳${expense.amount.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (canEdit || canDelete) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canEdit) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Bazaar Expense",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (canDelete) {
                            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditBazaarDialog(
    expense: BazaarExpense,
    onDismiss: () -> Unit,
    onSave: (BazaarExpense) -> Unit
) {
    var amountText by remember { mutableStateOf(expense.amount.toString()) }
    var descriptionText by remember { mutableStateOf(expense.itemsDescription) }
    var categoryText by remember { mutableStateOf(expense.category) }
    var dateText by remember { mutableStateOf(expense.date) }

    val categories = listOf("Groceries", "Meat & Fish", "Vegetables", "Spices", "Misc")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Bazaar Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($ / ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_bazaar_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Items Description") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_bazaar_items_input")
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val today = DhakaTimeHelper.getTodayDhakaDate()
                    val yesterday = DhakaTimeHelper.shiftDate(today, -1)
                    FilterChip(
                        selected = dateText == today,
                        onClick = { dateText = today },
                        label = { Text("Today", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = dateText == yesterday,
                        onClick = { dateText = yesterday },
                        label = { Text("Yesterday", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = categoryText == cat,
                            onClick = { categoryText = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && descriptionText.isNotBlank()) {
                        onSave(
                            expense.copy(
                                amount = amount,
                                itemsDescription = descriptionText.trim(),
                                category = categoryText,
                                date = dateText.trim()
                            )
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && descriptionText.isNotBlank(),
                modifier = Modifier.testTag("save_bazaar_edit_button")
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
private fun AddBazaarDialog(
    defaultDate: String,
    onDismiss: () -> Unit,
    onAdd: (amount: Double, description: String, category: String, date: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("Groceries") }
    var dateText by remember { mutableStateOf(defaultDate) }

    val categories = listOf("Groceries", "Meat & Fish", "Vegetables", "Spices", "Misc")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bazaar Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($ / ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("bazaar_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Items Description") },
                    placeholder = { Text("e.g. Chicken 2kg, Rice 5kg, Cooking Oil") },
                    modifier = Modifier.fillMaxWidth().testTag("bazaar_items_input")
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val today = DhakaTimeHelper.getTodayDhakaDate()
                    val yesterday = DhakaTimeHelper.shiftDate(today, -1)
                    FilterChip(
                        selected = dateText == today,
                        onClick = { dateText = today },
                        label = { Text("Today", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = dateText == yesterday,
                        onClick = { dateText = yesterday },
                        label = { Text("Yesterday", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = categoryText == cat,
                            onClick = { categoryText = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && descriptionText.isNotBlank()) {
                        onAdd(amount, descriptionText.trim(), categoryText, dateText.trim())
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && descriptionText.isNotBlank(),
                modifier = Modifier.testTag("submit_bazaar_button")
            ) {
                Text("Add Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
