package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CupertinoSegmentedControl
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.CategoryBills
import com.example.ui.theme.CategoryEducation
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryTransport
import com.example.ui.theme.PlayfairFontFamily
import java.util.Calendar

data class CategoryItem(val name: String, val icon: ImageVector, val color: Color)

val EXPENSE_CATEGORIES = listOf(
    CategoryItem("খাবার ও বাজার", Icons.Default.Fastfood, CategoryFood),
    CategoryItem("বিল ও ইউটিলিটি", Icons.Default.Receipt, CategoryBills),
    CategoryItem("যাতায়াত ও ভাড়া", Icons.Default.DirectionsBus, CategoryTransport),
    CategoryItem("চিকিৎসা ও ওষুধ", Icons.Default.MedicalServices, CategoryHealth),
    CategoryItem("শিক্ষা ও বই", Icons.Default.School, CategoryEducation),
    CategoryItem("শপিং ও পোশাক", Icons.Default.ShoppingBag, CategoryShopping),
    CategoryItem("ব্যক্তিগত খরচ", Icons.Default.Category, CategoryPersonal),
    CategoryItem("অন্যান্য খরচ", Icons.Default.AttachMoney, Color(0xFF64748B))
)

val INCOME_CATEGORIES = listOf(
    CategoryItem("বেতন ও আয়", Icons.Default.Work, Color(0xFF10B981)),
    CategoryItem("ব্যবসা ও মুনাফা", Icons.Default.AttachMoney, Color(0xFF059669)),
    CategoryItem("ফ্রিল্যান্সিং", Icons.Default.CreditCard, Color(0xFF3B82F6)),
    CategoryItem("উপহার ও অন্যান্য", Icons.Default.Category, Color(0xFF8B5CF6))
)

val PAYMENT_METHODS = listOf(
    "নগদ (Cash)",
    "বিকাশ (bKash)",
    "নগদ (Nagad)",
    "ব্যাংক / কার্ড",
    "অন্যান্য"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: String, category: String, dateMillis: Long, paymentMethod: String, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var typeIndex by remember { mutableStateOf(0) } // 0 = Expense (খরচ), 1 = Income (আয়)
    val isExpense = typeIndex == 0

    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val categories = if (isExpense) EXPENSE_CATEGORIES else INCOME_CATEGORIES
    var selectedCategory by remember(isExpense) { mutableStateOf(categories.first().name) }
    var selectedPaymentMethod by remember { mutableStateOf(PAYMENT_METHODS.first()) }

    var selectedDayOption by remember { mutableStateOf("আজ") } // "আজ", "গতকাল", "২ দিন আগে"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpense) "নতুন খরচের হিসাব" else "নতুন আয়ের হিসাব",
                    fontSize = 20.sp,
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // iOS-style Segmented Type Toggle
            CupertinoSegmentedControl(
                items = listOf("খরচ (Expense)", "আয় (Income)"),
                selectedIndex = typeIndex,
                onIndexSelected = { typeIndex = it },
                modifier = Modifier.fillMaxWidth(),
                selectedColor = if (isExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Big Amount Input (Apple Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "৳",
                        fontSize = 32.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) amountText = it },
                        placeholder = { Text("0.00", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlayfairFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title Field
            Text(
                text = "বিবরণ বা শিরোনাম *",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                placeholder = { Text("যেমন: কাঁচাবাজার, ইন্টারনেট বিল", fontFamily = BengaliBodyFontFamily) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selector Chips
            Text(
                text = "ক্যাটাগরি নির্বাচন *",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat.name
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) cat.color else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) cat.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat.name }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.name,
                            tint = if (isSelected) Color.White else cat.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            fontSize = 13.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Date Quick Picker
            Text(
                text = "তারিখ *",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("আজ", "গতকাল", "২ দিন আগে").forEach { day ->
                    val isSelected = selectedDayOption == day
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedDayOption = day }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontSize = 13.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Payment Method Selector
            Text(
                text = "লেনদেনের মাধ্যম *",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PAYMENT_METHODS.forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPaymentMethod = method }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = method,
                            fontSize = 13.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Optional Note
            Text(
                text = "অতিরিক্ত নোট (ঐচ্ছিক)",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("যেমন: রসিদ নং বা অতিরিক্ত তথ্য", fontFamily = BengaliBodyFontFamily) },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Save Button
            val amountValue = amountText.toDoubleOrNull() ?: 0.0
            val isValid = amountValue > 0 && titleText.isNotBlank()

            Button(
                onClick = {
                    if (isValid) {
                        val cal = Calendar.getInstance()
                        when (selectedDayOption) {
                            "গতকাল" -> cal.add(Calendar.DAY_OF_YEAR, -1)
                            "২ দিন আগে" -> cal.add(Calendar.DAY_OF_YEAR, -2)
                        }
                        onSave(
                            titleText,
                            amountValue,
                            if (isExpense) "EXPENSE" else "INCOME",
                            selectedCategory,
                            cal.timeInMillis,
                            selectedPaymentMethod,
                            noteText
                        )
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "হিসাব সংরক্ষণ করুন",
                    fontFamily = BengaliBodyFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
