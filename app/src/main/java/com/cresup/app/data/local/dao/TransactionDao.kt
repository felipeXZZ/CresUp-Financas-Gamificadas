package com.cresup.app.data.local.dao

import androidx.room.*
import com.cresup.app.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%Y', date/1000, 'unixepoch') = :year
        AND strftime('%m', date/1000, 'unixepoch') = :month
        ORDER BY date DESC
    """)
    fun getByMonth(year: String, month: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    suspend fun totalIncome(): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    suspend fun totalExpenses(): Double?
}
