package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.CupertinoGlassCard
import com.example.ui.components.CupertinoPillBadge
import com.example.ui.dialogs.EXPENSE_CATEGORIES
import com.example.ui.dialogs.INCOME_CATEGORIES
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.CalligraphyFontFamily
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PlayfairFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onAddTransactionClick: () -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val isLockEnabled by viewModel.isLockEnabled.collectAsState()

    var selectedFilter by remember { mutableStateOf(0) } // 0 = আজকের, 1 = এই সপ্তাহ, 2 = এই মাস, 3 = সকল
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val cal = Calendar.getInstance()
    val todayStart = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val weekStart = cal.apply {
        set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    }.timeInMillis

    val monthStart = cal.apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    // Filter transactions
    val filteredTransactions = remember(allTransactions, selectedFilter) {
        when (selectedFilter) {
            0 -> allTransactions.filter { it.dateMillis >= todayStart }
            1 -> allTransactions.filter { it.dateMillis >= weekStart }
            2 -> allTransactions.filter { it.dateMillis >= monthStart }
            else -> allTransactions
        }
    }

    // Stats calculations
    val monthTransactions = allTransactions.filter { it.dateMillis >= monthStart }
    val totalExpenseMonth = monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalIncomeMonth = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val netBalanceMonth = totalIncomeMonth - totalExpenseMonth

    val todayExpense = allTransactions.filter { it.dateMillis >= todayStart && it.type == "EXPENSE" }.sumOf { it.amount }

    // Group filtered transactions by date
    val dateFormatter = SimpleDateFormat("dd MMMM, yyyy", Locale("bn", "BD"))
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { tx ->
            dateFormatter.format(Date(tx.dateMillis))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "হিসাব খাতা",
                                fontFamily = CalligraphyFontFamily,
                                fontSize = 34.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val banglaDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("bn", "BD")).format(Date())
                            Text(
                                text = banglaDate,
                                fontFamily = BengaliBodyFontFamily,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isLockEnabled) {
                            IconButton(
                                onClick = { viewModel.lockApp() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock",
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Cupertino Hero Card (Monthly Balance & Spending)
                    CupertinoGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "এই মাসের খরচ (Monthly Expense)",
                                    fontSize = 13.5.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                CupertinoPillBadge(
                                    text = "আজ: ৳ ${formatAmount(todayExpense)}",
                                    backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                    textColor = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "৳ ${formatAmount(totalExpenseMonth)}",
                                fontFamily = PlayfairFontFamily,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Income & Net Pill row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Income",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "মোট আয়", fontSize = 11.sp, fontFamily = BengaliBodyFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "৳ ${formatAmount(totalIncomeMonth)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = PlayfairFontFamily, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background((if (netBalanceMonth >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (netBalanceMonth >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = "Net",
                                            tint = if (netBalanceMonth >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "অবশিষ্ট স্থিতি", fontSize = 11.sp, fontFamily = BengaliBodyFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "৳ ${formatAmount(netBalanceMonth)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = PlayfairFontFamily,
                                            color = if (netBalanceMonth >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Filter Quick Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("আজকের", "এই সপ্তাহ", "এই মাস", "সকল").forEachIndexed { idx, label ->
                            val isSelected = selectedFilter == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                    .clickable { selectedFilter = idx }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.5.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "দৈনিক খরচের খতিয়ান (Daily Breakdown)",
                        fontSize = 16.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Grouped Transaction items
            if (groupedTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোন লেনদেন পাওয়া যায়নি",
                            fontSize = 15.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "নিচের '+' বাটনে ট্যাপ করে নতুন খরচ বা আয় যোগ করুন",
                            fontSize = 13.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                groupedTransactions.forEach { (dateGroup, transactionsForDate) ->
                    val dayExpense = transactionsForDate.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                    item(key = "header_$dateGroup") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateGroup,
                                    fontSize = 13.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (dayExpense > 0) {
                                Text(
                                    text = "মোট খরচ: ৳ ${formatAmount(dayExpense)}",
                                    fontSize = 12.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    items(transactionsForDate, key = { it.id }) { tx ->
                        TransactionRowItem(
                            tx = tx,
                            onDelete = { transactionToDelete = tx }
                        )
                    }
                }
            }
        }

        // Floating Cupertino "নতুন হিসাব +" Pill FAB
        FloatingActionButton(
            onClick = onAddTransactionClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "নতুন এন্ট্রি",
                    fontFamily = BengaliBodyFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "লেনদেন মুছে ফেলবেন?",
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "'${tx.title}' (৳ ${formatAmount(tx.amount)}) হিসাব খাতা থেকে স্থায়ীভাবে মুছে ফেলা হবে।",
                    fontFamily = BengaliBodyFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx.id)
                        transactionToDelete = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error, fontFamily = BengaliBodyFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("বাতিল", fontFamily = BengaliBodyFontFamily)
                }
            }
        )
    }
}

@Composable
private fun TransactionRowItem(
    tx: TransactionEntity,
    onDelete: () -> Unit
) {
    val isExpense = tx.type == "EXPENSE"
    val categoryItem = (if (isExpense) EXPENSE_CATEGORIES else INCOME_CATEGORIES)
        .find { it.name == tx.category }

    val iconColor = categoryItem?.color ?: if (isExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (categoryItem != null) {
                        Icon(
                            imageVector = categoryItem.icon,
                            contentDescription = tx.category,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isExpense) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = tx.category,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        fontSize = 14.5.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.category,
                            fontSize = 12.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = tx.paymentMethod,
                            fontSize = 11.5.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (tx.note.isNotBlank()) {
                        Text(
                            text = tx.note,
                            fontSize = 11.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }

            // Amount and Delete button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isExpense) "-" else "+"}৳ ${formatAmount(tx.amount)}",
                    fontFamily = PlayfairFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", amount)
    } else {
        String.format(Locale.US, "%,.2f", amount)
    }
}
