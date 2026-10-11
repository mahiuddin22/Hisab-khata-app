package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.components.CupertinoGlassCard
import com.example.ui.components.CupertinoSegmentedControl
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.CalligraphyFontFamily
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PlayfairFontFamily

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val isLockEnabled by viewModel.isLockEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf("") }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInputText by remember { mutableStateOf(monthlyBudget.toLong().toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp)
    ) {
        // Title
        item {
            Text(
                text = "অ্যাপ সেটিংস",
                fontFamily = CalligraphyFontFamily,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "ডার্ক মোড, বায়োমেট্রিক নিরাপত্তা ও পার্সোনালাইজেশন",
                fontFamily = BengaliBodyFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Profile Card
            CupertinoGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .border(1.5.dp, LuxuryGold, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hisab_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "হিসাব খাতা (Hisab Khata)",
                            fontSize = 17.sp,
                            fontFamily = BengaliBodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "আইফোন স্টাইল নান্দনিক ব্যয় ব্যবস্থাপনা",
                            fontSize = 12.5.sp,
                            fontFamily = BengaliBodyFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 1: Theme & Dark Mode
            Text(
                text = "থিম ও ডিসপ্লে (Dark Mode)",
                fontSize = 14.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            val themeIndex = when (themeMode) {
                "DARK" -> 0
                "LIGHT" -> 1
                else -> 2
            }

            CupertinoSegmentedControl(
                items = listOf("ডার্ক মোড (Dark)", "লাইট মোড (Light)", "সিস্টেম"),
                selectedIndex = themeIndex,
                onIndexSelected = {
                    val mode = when (it) {
                        0 -> "DARK"
                        1 -> "LIGHT"
                        else -> "SYSTEM"
                    }
                    viewModel.setTheme(mode)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: Security & Password
            Text(
                text = "নিরাপত্তা ও পাসওয়ার্ড (Security & PIN)",
                fontSize = 14.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // PIN Lock Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = LuxuryGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "অ্যাপ পাসওয়ার্ড / পিন লক",
                                    fontSize = 14.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isLockEnabled) "লক সক্রিয় রয়েছে" else "বর্তমানে নিষ্ক্রিয়",
                                    fontSize = 12.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showPinDialog = true
                                } else {
                                    viewModel.disableLock()
                                    Toast.makeText(context, "পিন লক নিষ্ক্রিয় করা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (isLockEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Change PIN Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showPinDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Pin, contentDescription = "Change PIN", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "পিন পরিবর্তন করুন (Change PIN)",
                                    fontSize = 13.5.sp,
                                    fontFamily = BengaliBodyFontFamily,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(text = "পরিবর্তন >", fontSize = 12.sp, fontFamily = BengaliBodyFontFamily, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Biometric Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Fingerprint", tint = LuxuryGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ফিঙ্গারপ্রিন্ট / বায়োমেট্রিক আনলক",
                                        fontSize = 14.sp,
                                        fontFamily = BengaliBodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "আঙুলের ছাপে দ্রুত অ্যাপে প্রবেশ",
                                        fontSize = 12.sp,
                                        fontFamily = BengaliBodyFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { viewModel.setBiometricToggle(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Lock Now action
                        Button(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Lock", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "এখনই অ্যাপ লক করুন (Lock Now)",
                                fontFamily = BengaliBodyFontFamily,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: Budget Setting
            Text(
                text = "বাজেট ব্যবস্থাপনা (Monthly Budget)",
                fontSize = 14.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        budgetInputText = monthlyBudget.toLong().toString()
                        showBudgetDialog = true
                    },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "Budget", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "মাসিক খরচের লক্ষ্যমাত্রা",
                                fontSize = 14.sp,
                                fontFamily = BengaliBodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "বাজেট অতিক্রম করলে গ্রাফে লাল সতর্ক সংকেত",
                                fontSize = 12.sp,
                                fontFamily = BengaliBodyFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "৳ ${monthlyBudget.toLong()}",
                        fontSize = 16.sp,
                        fontFamily = PlayfairFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 4: Calligraphy Font Showcase
            Text(
                text = "ক্যালিওগ্রাফি ও ফন্ট স্টাইল (Typography)",
                fontSize = 14.sp,
                fontFamily = BengaliBodyFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            CupertinoGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "হিসাব খাতা ও নান্দনিক ক্যালিওগ্রাফি",
                        fontFamily = CalligraphyFontFamily,
                        fontSize = 24.sp,
                        color = LuxuryGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "বাংলা ক্যালিওগ্রাফির জন্য Galada ফন্ট এবং স্পষ্ট সংখ্যার জন্য Hind Siliguri ও Playfair Display সংযুক্ত করা হয়েছে।",
                        fontFamily = BengaliBodyFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Set PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    text = "৪-ডিজিট পিন কোড সেট করুন",
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "অ্যাপ সুরক্ষার জন্য নতুন ৪ সংখ্যার একটি পিন প্রবেশ করান:",
                        fontFamily = BengaliBodyFontFamily,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinText = it },
                        placeholder = { Text("নতুন পিন (যেমন: 1234)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPinText,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinText = it },
                        placeholder = { Text("পিন পুনরায় লিখুন") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinErrorText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pinErrorText,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontFamily = BengaliBodyFontFamily
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length != 4) {
                            pinErrorText = "পিন অবশ্যই ৪ সংখ্যার হতে হবে।"
                        } else if (newPinText != confirmPinText) {
                            pinErrorText = "দুইটি পিন মেলেনি, আবার লিখুন।"
                        } else {
                            viewModel.setupPin(newPinText)
                            showPinDialog = false
                            newPinText = ""
                            confirmPinText = ""
                            pinErrorText = ""
                            Toast.makeText(context, "পিন সফলভাবে সেট হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("সংরক্ষণ", fontFamily = BengaliBodyFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("বাতিল", fontFamily = BengaliBodyFontFamily)
                }
            }
        )
    }

    // Budget Editor Dialog
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = {
                Text(
                    text = "মাসিক বাজেট পরিবর্তন",
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "আপনার প্রতি মাসের সর্বোচ্চ খরচের বাজেট সীমা কত?",
                        fontFamily = BengaliBodyFontFamily,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetInputText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) budgetInputText = it },
                        prefix = { Text("৳ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInputText.toDoubleOrNull() ?: 25000.0
                        viewModel.setBudgetLimit(amount)
                        showBudgetDialog = false
                        Toast.makeText(context, "বাজেট সীমা আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("সংরক্ষণ", fontFamily = BengaliBodyFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("বাতিল", fontFamily = BengaliBodyFontFamily)
                }
            }
        )
    }
}
