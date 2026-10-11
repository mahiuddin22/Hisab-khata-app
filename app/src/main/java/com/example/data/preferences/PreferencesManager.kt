package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hisab_khata_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_IS_LOCK_ENABLED = booleanPreferencesKey("is_lock_enabled")
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "DARK", "LIGHT"
        val KEY_MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val KEY_CURRENCY = stringPreferencesKey("currency")

        fun hashPin(pin: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(pin.toByteArray())
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }

    val isLockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOCK_ENABLED] ?: false
    }

    val pinHash: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_HASH] ?: ""
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_BIOMETRIC_ENABLED] ?: true
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "DARK"
    }

    val monthlyBudget: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_MONTHLY_BUDGET] ?: 25000.0
    }

    val currency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY] ?: "৳"
    }

    suspend fun setLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_HASH] = hashPin(pin)
            prefs[KEY_IS_LOCK_ENABLED] = true
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_HASH] = ""
            prefs[KEY_IS_LOCK_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setMonthlyBudget(budget: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MONTHLY_BUDGET] = budget
        }
    }
}
