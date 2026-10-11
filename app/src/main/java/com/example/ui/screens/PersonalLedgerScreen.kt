package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalDebtEntity
import com.example.ui.MainViewModel
import com.example.ui.components.CupertinoGlassCard
import com.example.ui.components.CupertinoPillBadge
import com.example.ui.components.CupertinoSegmentedControl
import com.example.ui.dialogs.AddDebtSheet
import com.example.ui.dialogs.SettleDebtSheet
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.CalligraphyFontFamily
import com.example.ui.theme.PlayfairFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonalLedgerScreen(
    viewModel: MainViewModel,
    onAddDebtClick: () -> Unit
) {
    val context = LocalContext.current
    val allDebts by viewModel.allDebts.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = পাবো (Receivable), 1 = দেনা (Payable), 2 = সকল
    var settlingDebt by remember { mutableStateOf<PersonalDebtEntity?>(null) }
    var debtToDelete by remember { mutableStateOf<PersonalDebtEntity?>(null) }

    val totalReceivable = allDebts.filter { it.type == "RECEIVABLE" && !it.isSettled }.sumOf { it.remainingAmount }
    val totalPayable = allDebts.filter { it.type == "PAYABLE" && !it.isSettled }.sumOf { it.remainingAmount }
    val netBalance = totalReceivable - totalPayable

    val filteredDebts = remember(allDebts, selectedTab) {
        when (selectedTab) {
            0 -> allDebts.filter { it.type == "RECEIVABLE" }
            1 -> allDebts.filter { it.type == "PAYABLE" }
            else -> allDebts
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "ব্যক্তিগত দেনা-পাওনা",
                        fontFamily = CalligraphyFontFamily,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "কার কাছে কত টাকা পাবেন বা কার কাছে কত দেনা",
                        fontFamily = BengaliBodyFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hero Stat Card
                    CupertinoGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Receivable
                                Column {
                                    Text(
                                        text = "মোট পাওনা (Receivable)",
                                        fontSize = 12.5.sp,
                                        fontFamily = BengaliBodyFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳ ${formatAmount(totalReceivable)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlayfairFontFamily,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                // Payable
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "মোট দেনা (Payable)",
                                        fontSize = 12.5.sp,
                                        fontFamily = BengaliBodyFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳ ${formatAmount(totalPayable)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlayfairFontFamily,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "নেট আর্থিক স্থিতি (পাওনা - দেনা):",
                                        fontSize = 12.sp,
                                        fontFamily = BengaliBodyFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${if (netBalance >= 0) "+" else ""}৳ ${formatAmount(netBalance)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlayfairFontFamily,
                                        color = if (netBalance >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Segmented Filter
                    CupertinoSegmentedControl(
                        items = listOf("পাবো (Receivable)", "দেনা (Payable)", "সকল"),
                        selectedIndex = selectedTab,
                        onIndexSelected = { selectedTab = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Ledger items
            if (filteredDebts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "কোন দেনা-পাওনার রেকর্ড পাওয়া যায়নি",
                            fontSize = 15.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নতুন ব্যক্তিগত ঋণ বা ধার যুক্ত করতে নিচের বাটনে ট্যাপ করুন",
                            fontSize = 13.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredDebts, key = { it.id }) { debt ->
                    DebtCardItem(
                        debt = debt,
                        onSettleClick = { settlingDebt = debt },
                        onDeleteClick = { debtToDelete = debt },
                        onCallClick = {
                            if (debt.phoneNumber.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${debt.phoneNumber}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddDebtClick,
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Debt", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "দেনা-পাওনা এন্ট্রি",
                    fontFamily = BengaliBodyFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Settle Bottom Sheet
    if (settlingDebt != null) {
        SettleDebtSheet(
            debt = settlingDebt!!,
            onDismiss = { settlingDebt = null },
            onConfirm = { additionalPaid, isFullySettled ->
                viewModel.recordDebtSettlement(settlingDebt!!.id, additionalPaid, isFullySettled)
                settlingDebt = null
            }
        )
    }

    // Delete Confirmation
    if (debtToDelete != null) {
        val debt = debtToDelete!!
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = {
                Text(
                    text = "দেনা-পাওনা মুছে ফেলবেন?",
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${debt.personName}-এর হিসাব খাতা থেকে মুছে ফেলতে চান?",
                    fontFamily = BengaliBodyFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePersonalDebt(debt.id)
                        debtToDelete = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error, fontFamily = BengaliBodyFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) {
                    Text("বাতিল", fontFamily = BengaliBodyFontFamily)
                }
            }
        )
    }
}

@Composable
private fun DebtCardItem(
    debt: PersonalDebtEntity,
    onSettleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val isReceivable = debt.type == "RECEIVABLE"
    val accentColor = if (isReceivable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val createdDateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(debt.createdDateMillis))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Person",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = debt.personName,
                            fontSize = 15.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isReceivable) "পাবো (আমার পাওনা)" else "দেনা (আমার দেওয়ার বাকি)",
                                fontSize = 11.5.sp,
                                fontFamily = BengaliBodyFontFamily,
                                color = accentColor
                            )
                            Text(text = " • ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = createdDateStr,
                                fontSize = 11.sp,
                                fontFamily = BengaliBodyFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badge
                if (debt.isSettled) {
                    CupertinoPillBadge(
                        text = "পরিশোধিত ✓",
                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        textColor = MaterialTheme.colorScheme.secondary
                    )
                } else if (debt.settledAmount > 0) {
                    CupertinoPillBadge(
                        text = "আংশিক জমা",
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        textColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    CupertinoPillBadge(
                        text = "বকেয়া",
                        backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        textColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount summary details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "মোট লেনদেন",
                        fontSize = 11.sp,
                        fontFamily = BengaliBodyFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "৳ ${formatAmount(debt.amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PlayfairFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "অবশিষ্ট বকেয়া",
                        fontSize = 11.sp,
                        fontFamily = BengaliBodyFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "৳ ${formatAmount(debt.remainingAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlayfairFontFamily,
                        color = accentColor
                    )
                }
            }

            if (debt.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "নোট: ${debt.description}",
                    fontSize = 12.sp,
                    fontFamily = BengaliBodyFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    if (debt.phoneNumber.isNotBlank()) {
                        IconButton(
                            onClick = onCallClick,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (!debt.isSettled) {
                    Button(
                        onClick = onSettleClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = "Settle", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "হিসাব মেটান / জমা",
                            fontSize = 13.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
