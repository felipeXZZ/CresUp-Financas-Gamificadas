package com.cresup.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cresup.app.domain.model.Transaction
import com.cresup.app.domain.model.TransactionCategory
import com.cresup.app.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    title = title,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = TransactionCategory.valueOf(category),
    date = date,
    note = note
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    title = title,
    amount = amount,
    type = type.name,
    category = category.name,
    date = date,
    note = note
)
