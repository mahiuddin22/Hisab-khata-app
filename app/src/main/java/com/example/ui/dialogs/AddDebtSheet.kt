package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CupertinoSegmentedControl
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.PlayfairFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtSheet(
    onDismiss: () -> Unit,
    onSave: (personName: String, phoneNumber: String, amount: Double, type: String, dueDateMillis: Long?, description: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var typeIndex by remember { mutableStateOf(0) } // 0 = RECEIVABLE (পাবো), 1 = PAYABLE (দেনা)
    val isReceivable = typeIndex == 0

    var personName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

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
                    text = "নতুন ব্যক্তিগত লেনদেন (দেনা-পাওনা)",
                    fontSize = 19.sp,
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

            // Segmented Control: পাবো vs দেনা
            CupertinoSegmentedControl(
                items = listOf("পাবো (Receivable)", "দেনা (Payable)"),
                selectedIndex = typeIndex,
                onIndexSelected = { typeIndex = it },
                modifier = Modifier.fillMaxWidth(),
                selectedColor = if (isReceivable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Field
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
                        color = if (isReceivable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
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

            // Person Name
            Text(
                text = "ব্যক্তির নাম *",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                placeholder = { Text("যেমন: তানভীর আহমেদ, সাজিদ ভাই", fontFamily = BengaliBodyFontFamily) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number
            Text(
                text = "মোবাইল নম্বর (ঐচ্ছিক)",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = { Text("017XXXXXXXX", fontFamily = BengaliBodyFontFamily) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note / Reason
            Text(
                text = "লেনদেনের কারণ / নোট (ঐচ্ছিক)",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("যেমন: জরুরি প্রয়োজনে ধার, বিলের বকেয়া অংশ", fontFamily = BengaliBodyFontFamily) },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))

            val amountValue = amountText.toDoubleOrNull() ?: 0.0
            val isValid = amountValue > 0 && personName.isNotBlank()

            Button(
                onClick = {
                    if (isValid) {
                        onSave(
                            personName,
                            phoneNumber,
                            amountValue,
                            if (isReceivable) "RECEIVABLE" else "PAYABLE",
                            System.currentTimeMillis() + 86400000L * 30, // Default 30 days due
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
                    containerColor = if (isReceivable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isReceivable) "পাওনা রেকর্ড সংরক্ষণ করুন" else "দেনা রেকর্ড সংরক্ষণ করুন",
                    fontFamily = BengaliBodyFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
