package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.dialogs.AddDebtSheet
import com.example.ui.dialogs.AddTransactionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.PersonalLedgerScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.HisabKhataTheme

class MainActivity : FragmentActivity() {

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "ডিভাইসে কোনো বায়োমেট্রিক ফিঙ্গারপ্রিন্ট সেট নেই", Toast.LENGTH_SHORT).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Fall back gracefully to PIN
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("হিসাব খাতা আনলক করুন")
            .setSubtitle("বায়োমেট্রিক ফিঙ্গারপ্রিন্ট সেন্সরে আঙুল স্পর্শ করুন")
            .setNegativeButtonText("পিন ব্যবহার করুন")
            .build()

        prompt.authenticate(promptInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()
            val isAppLocked by mainViewModel.isAppLocked.collectAsState()
            val isBiometricEnabled by mainViewModel.isBiometricEnabled.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            HisabKhataTheme(darkTheme = isDarkTheme) {
                // If app is locked, auto-trigger biometric once if enabled
                LaunchedEffect(isAppLocked) {
                    if (isAppLocked && isBiometricEnabled) {
                        showBiometricPrompt {
                            mainViewModel.unlockApp()
                        }
                    }
                }

                if (isAppLocked) {
                    LockScreen(
                        viewModel = mainViewModel,
                        onBiometricClick = {
                            showBiometricPrompt {
                                mainViewModel.unlockApp()
                            }
                        }
                    )
                } else {
                    MainAppScaffold(viewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showAddTxSheet by remember { mutableStateOf(false) }
    var showAddDebtSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            CupertinoBottomBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onAddTransactionClick = { showAddTxSheet = true }
                )
                1 -> PersonalLedgerScreen(
                    viewModel = viewModel,
                    onAddDebtClick = { showAddDebtSheet = true }
                )
                2 -> AnalyticsScreen(
                    viewModel = viewModel
                )
                3 -> ReportsScreen(
                    viewModel = viewModel
                )
                4 -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // Add Transaction Bottom Sheet
    if (showAddTxSheet) {
        AddTransactionSheet(
            onDismiss = { showAddTxSheet = false },
            onSave = { title, amount, type, category, dateMillis, paymentMethod, note ->
                viewModel.addTransaction(title, amount, type, category, dateMillis, paymentMethod, note)
            }
        )
    }

    // Add Personal Debt Bottom Sheet
    if (showAddDebtSheet) {
        AddDebtSheet(
            onDismiss = { showAddDebtSheet = false },
            onSave = { personName, phoneNumber, amount, type, dueDateMillis, description ->
                viewModel.addPersonalDebt(personName, phoneNumber, amount, type, dueDateMillis, description)
            }
        )
    }
}

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun CupertinoBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val navItems = listOf(
        NavItem("দৈনিক হিসাব", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
        NavItem("দেনা-পাওনা", Icons.Filled.People, Icons.Outlined.People),
        NavItem("গ্রাফ", Icons.Filled.BarChart, Icons.Outlined.BarChart),
        NavItem("রিপোর্ট", Icons.Filled.PictureAsPdf, Icons.Outlined.PictureAsPdf),
        NavItem("সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                val activeColor = MaterialTheme.colorScheme.primary
                val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = if (isSelected) activeColor else inactiveColor,
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.title,
                        fontSize = 10.5.sp,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) activeColor else inactiveColor
                    )
                }
            }
        }
    }
}
