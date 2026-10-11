package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PersonalDebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalDebtDao {
    @Query("SELECT * FROM personal_debts ORDER BY createdDateMillis DESC")
    fun getAllDebts(): Flow<List<PersonalDebtEntity>>

    @Query("SELECT * FROM personal_debts WHERE type = :type ORDER BY createdDateMillis DESC")
    fun getDebtsByType(type: String): Flow<List<PersonalDebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: PersonalDebtEntity): Long

    @Update
    suspend fun updateDebt(debt: PersonalDebtEntity)

    @Delete
    suspend fun deleteDebt(debt: PersonalDebtEntity)

    @Query("UPDATE personal_debts SET settledAmount = :settledAmount, isSettled = :isSettled WHERE id = :id")
    suspend fun updateSettlement(id: Long, settledAmount: Double, isSettled: Boolean)

    @Query("DELETE FROM personal_debts WHERE id = :id")
    suspend fun deleteDebtById(id: Long)
}
