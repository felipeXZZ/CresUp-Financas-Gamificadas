package com.cresup.app.data.repository

import com.cresup.app.data.local.dao.TransactionDao
import com.cresup.app.data.local.entities.toDomain
import com.cresup.app.data.local.entities.toEntity
import com.cresup.app.domain.model.Transaction
import com.cresup.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>> =
        dao.getByMonth(
            year = year.toString(),
            month = month.toString().padStart(2, '0')
        ).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(transaction: Transaction) {
        dao.insert(transaction.toEntity())
    }

    override suspend fun update(transaction: Transaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun delete(transaction: Transaction) {
        dao.delete(transaction.toEntity())
    }

    override suspend fun getById(id: Long): Transaction? =
        dao.getById(id)?.toDomain()
}
