package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.PersonalDebtEntity
import com.example.data.model.TransactionEntity
import com.example.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val transactionDao = db.transactionDao()
    private val personalDebtDao = db.personalDebtDao()
    private val prefs = PreferencesManager(application)

    // Current selected month & year for analytics & monthly review
    val selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0-based: 0 = Jan, 9 = Oct

    // Transactions list
    val allTransactions: StateFlow<List<TransactionEntity>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Personal debts list (ব্যক্তিগত দেনা-পাওনা)
    val allDebts: StateFlow<List<PersonalDebtEntity>> = personalDebtDao.getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Preferences
    val isLockEnabled = prefs.isLockEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isBiometricEnabled = prefs.isBiometricEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val themeMode = prefs.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")
    val monthlyBudget = prefs.monthlyBudget.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30000.0)
    val currency = prefs.currency.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "৳")

    // Lock Screen state (True if locked, False once unlocked)
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    init {
        viewModelScope.launch {
            val locked = prefs.isLockEnabled.first()
            _isAppLocked.value = locked

            // Pre-seed realistic data on very first launch if DB is empty
            val currentTx = transactionDao.getRecentTransactions(1).first()
            if (currentTx.isEmpty()) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        // Seed some sample transactions spread across the month
        fun getTimestamp(day: Int): Long {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, currentYear)
            c.set(Calendar.MONTH, currentMonth)
            c.set(Calendar.DAY_OF_MONTH, day)
            c.set(Calendar.HOUR_OF_DAY, 14)
            c.set(Calendar.MINUTE, 30)
            return c.timeInMillis
        }

        val sampleTxs = listOf(
            TransactionEntity(
                title = "মাসিক বেতন ও ইনকাম",
                amount = 45000.0,
                type = "INCOME",
                category = "বেতন ও আয়",
                dateMillis = getTimestamp(1),
                paymentMethod = "ব্যাংক / কার্ড",
                note = "অক্টোবর মাসের বেতন"
            ),
            TransactionEntity(
                title = "সাপ্তাহিক কাঁচাবাজার ও মাছ",
                amount = 3250.0,
                type = "EXPENSE",
                category = "খাবার ও বাজার",
                dateMillis = getTimestamp(2),
                paymentMethod = "নগদ (Cash)",
                note = "কারওয়ান বাজার থেকে কেনাকাটা"
            ),
            TransactionEntity(
                title = "বিদ্যুৎ ও গ্যাস ইউটিলিটি বিল",
                amount = 2150.0,
                type = "EXPENSE",
                category = "বিল ও ইউটিলিটি",
                dateMillis = getTimestamp(4),
                paymentMethod = "বিকাশ (bKash)",
                note = "ডেসকো ও তিতাস বিল"
            ),
            TransactionEntity(
                title = "ব্রডব্যান্ড ইন্টারনেট রিচার্জ",
                amount = 1000.0,
                type = "EXPENSE",
                category = "বিল ও ইউটিলিটি",
                dateMillis = getTimestamp(5),
                paymentMethod = "বিকাশ (bKash)",
                note = "লিংক৩ মান্থলি বিল"
            ),
            TransactionEntity(
                title = "পরিবারের জন্য ওষুধ ও ফার্মা",
                amount = 850.0,
                type = "EXPENSE",
                category = "চিকিৎসা ও ওষুধ",
                dateMillis = getTimestamp(6),
                paymentMethod = "নগদ (Cash)",
                note = "নিয়মিত প্রেসক্রিপশন"
            ),
            TransactionEntity(
                title = "উবার ও রিকশা যাতায়াত ভাড়া",
                amount = 480.0,
                type = "EXPENSE",
                category = "যাতায়াত ও ভাড়া",
                dateMillis = getTimestamp(7),
                paymentMethod = "নগদ (Nagad)",
                note = "অফিস যাতায়াত"
            ),
            TransactionEntity(
                title = "বন্ধুদের সাথে ডিনার ও কফি",
                amount = 1450.0,
                type = "EXPENSE",
                category = "খাবার ও বাজার",
                dateMillis = getTimestamp(8),
                paymentMethod = "ব্যাংক / কার্ড",
                note = "ধানমন্ডি ক্যাফে"
            ),
            TransactionEntity(
                title = "বই ও অনলাইন স্কিল কোর্স",
                amount = 1200.0,
                type = "EXPENSE",
                category = "শিক্ষা ও বই",
                dateMillis = getTimestamp(9),
                paymentMethod = "বিকাশ (bKash)",
                note = "প্রোগ্রামিং টেক বই"
            ),
            TransactionEntity(
                title = "নতুন শার্ট ও ফরমাল প্যান্ট",
                amount = 2600.0,
                type = "EXPENSE",
                category = "শপিং ও পোশাক",
                dateMillis = getTimestamp(10),
                paymentMethod = "ব্যাংক / কার্ড",
                note = "আড়ং শপিং"
            )
        )

        for (tx in sampleTxs) {
            transactionDao.insertTransaction(tx)
        }

        // Seed sample personal debts (দেনা-পাওনা)
        val sampleDebts = listOf(
            PersonalDebtEntity(
                personName = "তানভীর আহমেদ (বন্ধু)",
                phoneNumber = "01711223344",
                amount = 5000.0,
                type = "RECEIVABLE", // পাবো
                settledAmount = 1500.0,
                dueDateMillis = System.currentTimeMillis() + 86400000L * 15,
                description = "জরুরি প্রয়োজনে ধার নিয়েছিল"
            ),
            PersonalDebtEntity(
                personName = "সাজিদ করিম (অফিস কলিগ)",
                phoneNumber = "01819887766",
                amount = 1200.0,
                type = "PAYABLE", // দেনা
                settledAmount = 0.0,
                dueDateMillis = System.currentTimeMillis() + 86400000L * 5,
                description = "টিম লাঞ্চের বকেয়া অংশ"
            ),
            PersonalDebtEntity(
                personName = "রাকিব হাসান",
                phoneNumber = "01912345678",
                amount = 3000.0,
                type = "RECEIVABLE", // পাবো
                settledAmount = 0.0,
                dueDateMillis = System.currentTimeMillis() + 86400000L * 25,
                description = "ল্যাপটপ এক্সেসরিজ কেনার টাকা"
            )
        )

        for (debt in sampleDebts) {
            personalDebtDao.insertDebt(debt)
        }
    }

    fun setSelectedMonthYear(month: Int, year: Int) {
        selectedMonth.value = month
        selectedYear.value = year
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        dateMillis: Long,
        paymentMethod: String,
        note: String
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category,
                dateMillis = dateMillis,
                paymentMethod = paymentMethod,
                note = note.trim()
            )
            transactionDao.insertTransaction(tx)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionDao.deleteTransactionById(id)
        }
    }

    fun addPersonalDebt(
        personName: String,
        phoneNumber: String,
        amount: Double,
        type: String,
        dueDateMillis: Long?,
        description: String
    ) {
        viewModelScope.launch {
            val debt = PersonalDebtEntity(
                personName = personName.trim(),
                phoneNumber = phoneNumber.trim(),
                amount = amount,
                type = type,
                dueDateMillis = dueDateMillis,
                description = description.trim()
            )
            personalDebtDao.insertDebt(debt)
        }
    }

    fun recordDebtSettlement(debtId: Long, additionalPaid: Double, isFullySettled: Boolean) {
        viewModelScope.launch {
            val debt = allDebts.value.find { it.id == debtId } ?: return@launch
            val newSettled = (debt.settledAmount + additionalPaid).coerceAtMost(debt.amount)
            val settled = isFullySettled || newSettled >= debt.amount
            personalDebtDao.updateSettlement(debtId, newSettled, settled)
        }
    }

    fun deletePersonalDebt(id: Long) {
        viewModelScope.launch {
            personalDebtDao.deleteDebtById(id)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val savedHash = prefs.pinHash.first()
        val enteredHash = PreferencesManager.hashPin(pin)
        return savedHash == enteredHash
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (isLockEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun setupPin(pin: String) {
        viewModelScope.launch {
            prefs.setPin(pin)
            _isAppLocked.value = false
        }
    }

    fun disableLock() {
        viewModelScope.launch {
            prefs.clearPin()
            _isAppLocked.value = false
        }
    }

    fun setBiometricToggle(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometricEnabled(enabled)
        }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
        }
    }

    fun setBudgetLimit(budget: Double) {
        viewModelScope.launch {
            prefs.setMonthlyBudget(budget)
        }
    }
}
