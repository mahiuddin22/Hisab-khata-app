package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_debts")
data class PersonalDebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val phoneNumber: String = "",
    val amount: Double,
    val type: String, // "RECEIVABLE" (পাবো) or "PAYABLE" (দেনা)
    val settledAmount: Double = 0.0,
    val dueDateMillis: Long? = null,
    val createdDateMillis: Long = System.currentTimeMillis(),
    val description: String = "",
    val isSettled: Boolean = false
) {
    val remainingAmount: Double
        get() = (amount - settledAmount).coerceAtLeast(0.0)
}
