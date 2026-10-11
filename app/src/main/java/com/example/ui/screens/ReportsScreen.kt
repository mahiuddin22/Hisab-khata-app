package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.CupertinoGlassCard
import com.example.ui.components.CupertinoPillBadge
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.CalligraphyFontFamily
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PlayfairFontFamily
import com.example.util.PdfReportGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    var selectedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale("bn", "BD")).format(selectedCalendar.time)

    // Filter transactions for this month
    val calStart = (selectedCalendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
    }
    val calEnd = (selectedCalendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
    }

    val monthTransactions = remember(allTransactions, selectedCalendar) {
        allTransactions.filter { it.dateMillis in calStart.timeInMillis..calEnd.timeInMillis }
    }

    val totalExpense = monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalIncome = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "মাসিক পিডিএফ রিপোর্ট",
                    fontFamily = CalligraphyFontFamily,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "মাস শেষে স্বয়ংক্রিয় রিপোর্ট তৈরি ও ডাউনলোড করুন",
                    fontFamily = BengaliBodyFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Month Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                            generatedFile = null
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                    }

                    Text(
                        text = monthName,
                        fontSize = 15.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                            generatedFile = null
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = {
                        isGenerating = true
                        val file = PdfReportGenerator.generateAndShareMonthlyReport(
                            context = context,
                            monthYearTitle = monthName,
                            transactions = monthTransactions,
                            debts = allDebts,
                            monthlyBudget = monthlyBudget
                        )
                        isGenerating = false
                        generatedFile = file
                        if (file != null) {
                            Toast.makeText(context, "পিডিএফ রিপোর্ট তৈরি সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                            PdfReportGenerator.sharePdf(context, file)
                        } else {
                            Toast.makeText(context, "পিডিএফ তৈরি করতে ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Download PDF", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "পিডিএফ ডাউনলোড ও শেয়ার করুন",
                        fontSize = 15.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (generatedFile != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            PdfReportGenerator.viewPdf(context, generatedFile!!)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Open PDF", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "তৈরিকৃত পিডিএফটি ওপেন করুন",
                            fontSize = 14.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Document Live Preview Sheet
                Text(
                    text = "রিপোর্টের লাইভ প্রিভিউ (Statement Preview)",
                    fontSize = 15.sp,
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                // A4 Document Preview Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Banner inside preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "হিসাব খাতা • মাসিক আর্থিক বিবরণী",
                                    fontSize = 14.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "সময়কাল: $monthName",
                                    fontSize = 11.5.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 Key Stats preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "মোট খরচ", fontSize = 11.sp, fontFamily = BengaliBodyFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "৳ ${formatAmount(totalExpense)}", fontSize = 15.sp, fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                            Column {
                                Text(text = "মোট আয়", fontSize = 11.sp, fontFamily = BengaliBodyFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "৳ ${formatAmount(totalIncome)}", fontSize = 15.sp, fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Column {
                                Text(text = "অবশিষ্ট স্থিতি", fontSize = 11.sp, fontFamily = BengaliBodyFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "৳ ${formatAmount(netBalance)}",
                                    fontSize = 15.sp,
                                    fontFamily = PlayfairFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netBalance >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category preview list
                        Text(
                            text = "ক্যাটাগরি সারসংক্ষেপ:",
                            fontSize = 12.5.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val catList = monthTransactions.filter { it.type == "EXPENSE" }
                            .groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                            .toList()
                            .take(4)

                        if (catList.isEmpty()) {
                            Text(
                                text = "এই মাসে কোনো খরচের এন্ট্রি নেই।",
                                fontSize = 12.sp,
                                fontFamily = BengaliBodyFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            catList.forEach { (cat, amt) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "• $cat", fontSize = 12.sp, fontFamily = BengaliBodyFontFamily, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "৳ ${formatAmount(amt)}", fontSize = 12.sp, fontFamily = PlayfairFontFamily, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "মোট লেনদেন সংখ্যা: ${monthTransactions.size} টি",
                            fontSize = 12.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
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
