package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.CupertinoGlassCard
import com.example.ui.components.CupertinoPillBadge
import com.example.ui.dialogs.EXPENSE_CATEGORIES
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.CalligraphyFontFamily
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PlayfairFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    var selectedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    val selectedYear = selectedCalendar.get(Calendar.YEAR)
    val selectedMonth = selectedCalendar.get(Calendar.MONTH)

    // Calculate start and end of this selected month
    val calStart = (selectedCalendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val calEnd = (selectedCalendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }

    val maxDaysInMonth = selectedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Filter transactions for this month
    val monthTransactions = remember(allTransactions, selectedYear, selectedMonth) {
        allTransactions.filter {
            it.dateMillis in calStart.timeInMillis..calEnd.timeInMillis
        }
    }

    val expenseTransactions = monthTransactions.filter { it.type == "EXPENSE" }
    val totalExpense = expenseTransactions.sumOf { it.amount }
    val totalIncome = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }

    // Daily breakdown map: Day (1..maxDaysInMonth) -> Total Expense
    val dailyExpenseMap = remember(expenseTransactions, maxDaysInMonth) {
        val map = mutableMapOf<Int, Double>()
        for (day in 1..maxDaysInMonth) {
            map[day] = 0.0
        }
        val cal = Calendar.getInstance()
        for (tx in expenseTransactions) {
            cal.timeInMillis = tx.dateMillis
            val day = cal.get(Calendar.DAY_OF_MONTH)
            map[day] = (map[day] ?: 0.0) + tx.amount
        }
        map
    }

    // Category breakdown
    val categoryExpenseMap = remember(expenseTransactions) {
        expenseTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Stats
    val highestDay = dailyExpenseMap.maxByOrNull { it.value }
    val daysWithExpenseCount = dailyExpenseMap.count { it.value > 0 }
    val avgDailySpend = if (daysWithExpenseCount > 0) totalExpense / daysWithExpenseCount else 0.0

    // Interactive selected day on chart
    var inspectedDay by remember { mutableStateOf<Int?>(null) }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale("bn", "BD")).format(selectedCalendar.time)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "গ্রাফ ও ব্যয় বিশ্লেষণ",
                        fontFamily = CalligraphyFontFamily,
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "স্বয়ংক্রিয় মাসিক ও দৈনিক খরচের হিসাব",
                        fontFamily = BengaliBodyFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Month Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                            inspectedDay = null
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Prev Month",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = monthName,
                        fontSize = 13.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = {
                            selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                            inspectedDay = null
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3 Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Total
                SummaryMiniCard(
                    title = "মোট খরচ",
                    value = "৳ ${formatAmount(totalExpense)}",
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Highest Day
                SummaryMiniCard(
                    title = "সর্বোচ্চ দিন",
                    value = if (highestDay != null && highestDay.value > 0) "${highestDay.key} তারিখ\n৳ ${formatAmount(highestDay.value)}" else "রেকর্ড নেই",
                    accentColor = LuxuryGold,
                    modifier = Modifier.weight(1f)
                )

                // Card 3: Avg Spend
                SummaryMiniCard(
                    title = "দৈনিক গড়",
                    value = "৳ ${formatAmount(avgDailySpend)}",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Budget Progress Card
            CupertinoGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val budgetRatio = if (monthlyBudget > 0) (totalExpense / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
                    val isExceeded = totalExpense > monthlyBudget

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মাসিক বাজেট ব্যবহার (Budget Tracker)",
                            fontSize = 14.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f%%", (totalExpense / monthlyBudget * 100)),
                            fontSize = 14.sp,
                            fontFamily = PlayfairFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isExceeded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { budgetRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = if (isExceeded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "খরচ: ৳ ${formatAmount(totalExpense)}",
                            fontSize = 12.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "বাজেট সীমা: ৳ ${formatAmount(monthlyBudget)}",
                            fontSize = 12.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isExceeded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Alert", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "সতর্কবার্তা: নির্ধারিত মাসিক বাজেট সীমা অতিক্রম করেছে!",
                                fontSize = 11.5.sp,
                                fontFamily = BengaliBodyFontFamily,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 1: Daily Spending Bar Graph
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BarChart, contentDescription = "Graph", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "দৈনিক খরচের গ্রাফ (১ - $maxDaysInMonth তারিখ)",
                        fontSize = 16.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "ট্যাপ করে বিস্তারিত দেখুন",
                    fontSize = 11.sp,
                    fontFamily = BengaliBodyFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Bar Graph Card
            CupertinoGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val maxVal = (dailyExpenseMap.values.maxOrNull() ?: 1.0).coerceAtLeast(100.0)

                    // Inspected Day detail banner
                    if (inspectedDay != null) {
                        val dayVal = dailyExpenseMap[inspectedDay!!] ?: 0.0
                        val dayTxs = expenseTransactions.filter {
                            val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                            c.get(Calendar.DAY_OF_MONTH) == inspectedDay
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$inspectedDay $monthName",
                                        fontFamily = BengaliBodyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "মোট খরচ: ৳ ${formatAmount(dayVal)}",
                                        fontFamily = PlayfairFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                if (dayTxs.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "বিভাগ: " + dayTxs.joinToString(", ") { "${it.category} (৳${formatAmount(it.amount)})" },
                                        fontSize = 12.sp,
                                        fontFamily = BengaliBodyFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Canvas Bar Chart
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val highlightColor = MaterialTheme.colorScheme.tertiary
                    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(maxDaysInMonth) {
                                    detectTapGestures { offset ->
                                        val barAreaWidth = size.width / maxDaysInMonth
                                        val tappedDay = ((offset.x / barAreaWidth).toInt() + 1).coerceIn(1, maxDaysInMonth)
                                        inspectedDay = tappedDay
                                    }
                                }
                        ) {
                            val barWidth = (size.width / maxDaysInMonth) * 0.7f
                            val spacing = (size.width / maxDaysInMonth) * 0.3f
                            val chartHeight = size.height - 25f

                            for (day in 1..maxDaysInMonth) {
                                val amount = dailyExpenseMap[day] ?: 0.0
                                val barHeight = if (maxVal > 0) ((amount / maxVal) * chartHeight).toFloat().coerceAtLeast(6f) else 6f
                                val left = (day - 1) * (barWidth + spacing) + spacing / 2
                                val top = chartHeight - barHeight

                                val isSelected = inspectedDay == day
                                val isPeak = amount > 0 && amount == (highestDay?.value ?: -1.0)

                                val barColor = when {
                                    isSelected -> LuxuryGold
                                    isPeak -> highlightColor
                                    amount > 0 -> primaryColor
                                    else -> emptyColor
                                }

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(left, top),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }

                    // X-axis day indicators (1, 5, 10, 15, 20, 25, 30)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(1, 5, 10, 15, 20, 25, maxDaysInMonth).forEach { day ->
                            Text(
                                text = "$day",
                                fontSize = 10.sp,
                                fontFamily = PlayfairFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 2: Category Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PieChart, contentDescription = "Pie", tint = LuxuryGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "বিষয়ভিত্তিক খরচের শতকরা হার (Category Breakdown)",
                        fontSize = 16.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (categoryExpenseMap.isEmpty()) {
                CupertinoGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "এই মাসে খরচের কোনো রেকর্ড নেই।",
                            fontSize = 13.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                CupertinoGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        categoryExpenseMap.forEach { (categoryName, amount) ->
                            val catItem = EXPENSE_CATEGORIES.find { it.name == categoryName }
                            val catColor = catItem?.color ?: MaterialTheme.colorScheme.primary
                            val percentage = if (totalExpense > 0) (amount / totalExpense) else 0.0

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = categoryName,
                                            fontSize = 13.5.sp,
                                            fontFamily = BengaliBodyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "৳ ${formatAmount(amount)}",
                                            fontFamily = PlayfairFontFamily,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = String.format(Locale.US, "(%.1f%%)", percentage * 100),
                                            fontSize = 12.sp,
                                            fontFamily = BengaliBodyFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { percentage.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = catColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
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
private fun SummaryMiniCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontFamily = BengaliBodyFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = PlayfairFontFamily,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                lineHeight = 18.sp
            )
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
