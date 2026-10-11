package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import com.example.data.model.PersonalDebtEntity
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.PlayfairFontFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleDebtSheet(
    debt: PersonalDebtEntity,
    onDismiss: () -> Unit,
    onConfirm: (additionalPaid: Double, isFullySettled: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var payAmountText by remember { mutableStateOf(String.format(Locale.US, "%.0f", debt.remainingAmount)) }
    var isFullySettled by remember { mutableStateOf(true) }

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
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "হিসাব নিষ্পত্তি / পরিশোধ",
                    fontSize = 19.sp,
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "${debt.personName} (${if (debt.type == "RECEIVABLE") "পাওনা" else "দেনা"})",
                        fontSize = 16.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "মোট লেনদেন: ৳ ${debt.amount} • পরিশোধিত: ৳ ${debt.settledAmount}",
                        fontSize = 13.sp,
                        fontFamily = BengaliBodyFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বর্তমানে অবশিষ্ট বকেয়া: ৳ ${debt.remainingAmount}",
                        fontSize = 14.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "পরিশোধকৃত টাকার পরিমাণ *",
                fontSize = 13.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "৳",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BengaliBodyFontFamily,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                                payAmountText = it
                                val num = it.toDoubleOrNull() ?: 0.0
                                isFullySettled = (num >= debt.remainingAmount)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlayfairFontFamily
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFullySettled = !isFullySettled },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isFullySettled,
                    onCheckedChange = { isFullySettled = it }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "সম্পূর্ণ নিষ্পত্তি হিসেবে চিহ্নিত করুন (Mark Fully Settled)",
                    fontSize = 13.sp,
                    fontFamily = BengaliBodyFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val amt = payAmountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amt > 0) {
                        onConfirm(amt, isFullySettled)
                        onDismiss()
                    }
                },
                enabled = amt > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "নিষ্পত্তি নিশ্চিত করুন",
                    fontFamily = BengaliBodyFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
