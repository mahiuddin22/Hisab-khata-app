package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.CalligraphyFontFamily
import com.example.ui.theme.LuxuryGold
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    viewModel: MainViewModel,
    onBiometricClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            triggerVibration()
            val newPin = enteredPin + digit
            enteredPin = newPin

            if (newPin.length == 4) {
                scope.launch {
                    val isValid = viewModel.verifyPin(newPin)
                    if (isValid) {
                        viewModel.unlockApp()
                    } else {
                        isError = true
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                -25f at 50
                                25f at 100
                                -20f at 150
                                20f at 200
                                -10f at 250
                                10f at 300
                                0f at 400
                            }
                        )
                        enteredPin = ""
                        isError = false
                    }
                }
            }
        }
    }

    fun onDeletePress() {
        if (enteredPin.isNotEmpty()) {
            triggerVibration()
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .border(2.dp, LuxuryGold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_hisab_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "হিসাব খাতা",
                fontFamily = CalligraphyFontFamily,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "সুরক্ষা পিন বা ফিঙ্গারপ্রিন্ট দিয়ে আনলক করুন",
                fontFamily = BengaliBodyFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4 PIN Dots with shake animation
            Row(
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            isError -> MaterialTheme.colorScheme.error
                            isFilled -> LuxuryGold
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        },
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .shadow(if (isFilled) 6.dp else 0.dp, CircleShape)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(1.5.dp, if (isFilled) LuxuryGold else MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // iOS-Style Circular Keypad
            val rows = listOf(
                listOf("1" to "১", "2" to "২", "3" to "৩"),
                listOf("4" to "৪", "5" to "৫", "6" to "৬"),
                listOf("7" to "৭", "8" to "৮", "9" to "৯")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    row.forEach { (engDigit, bngDigit) ->
                        KeypadButton(
                            primary = engDigit,
                            secondary = bngDigit,
                            onClick = { onDigitPress(engDigit) }
                        )
                        Spacer(modifier = Modifier.width(28.dp))
                    }
                }
            }

            // Bottom row: Biometric, 0, Backspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biometric Fingerprint Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            triggerVibration()
                            onBiometricClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = LuxuryGold,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // 0
                KeypadButton(
                    primary = "0",
                    secondary = "০",
                    onClick = { onDigitPress("0") }
                )

                Spacer(modifier = Modifier.width(28.dp))

                // Backspace
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDeletePress() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Reset / Forgot PIN
            Text(
                text = "পিন ভুলে গেছেন? রিসেট করুন",
                fontFamily = BengaliBodyFontFamily,
                fontSize = 13.sp,
                color = LuxuryGold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showResetDialog = true }
                    .padding(8.dp)
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "পিন রিসেট করবেন?",
                    fontFamily = BengaliBodyFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "পিন রিসেট করলে অ্যাপের লক নিষ্ক্রিয় হয়ে যাবে এবং আপনি সরাসরি প্রবেশ করতে পারবেন।",
                    fontFamily = BengaliBodyFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.disableLock()
                        showResetDialog = false
                    }
                ) {
                    Text("রিসেট ও আনলক", color = MaterialTheme.colorScheme.error, fontFamily = BengaliBodyFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("বাতিল", fontFamily = BengaliBodyFontFamily)
                }
            }
        )
    }
}

@Composable
private fun KeypadButton(
    primary: String,
    secondary: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = secondary,
                fontSize = 11.sp,
                fontFamily = BengaliBodyFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
