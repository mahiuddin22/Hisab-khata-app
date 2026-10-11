package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PersonalDebtEntity
import com.example.data.preferences.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("হিসাব খাতা", appName)
    }

    @Test
    fun `verify pin hashing`() {
        val hash1 = PreferencesManager.hashPin("1234")
        val hash2 = PreferencesManager.hashPin("1234")
        assertEquals(hash1, hash2)
        assertTrue(hash1.isNotEmpty())
    }

    @Test
    fun `verify personal debt remaining amount calculation`() {
        val debt = PersonalDebtEntity(
            personName = "Test Person",
            amount = 5000.0,
            type = "RECEIVABLE",
            settledAmount = 1500.0
        )
        assertEquals(3500.0, debt.remainingAmount, 0.001)
    }
}
