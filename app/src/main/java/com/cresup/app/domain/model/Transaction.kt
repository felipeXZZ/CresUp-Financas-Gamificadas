package com.cresup.app.domain.model

import androidx.compose.ui.graphics.Color
import com.cresup.app.presentation.ui.theme.*

enum class TransactionType { INCOME, EXPENSE }

enum class TransactionCategory(
    val label: String,
    val emoji: String,
    val color: Color
) {
    FOOD("Alimentação", "🍔", ColorFood),
    DELIVERY("Delivery", "🛵", ColorDelivery),
    TRANSPORT("Transporte", "🚗", ColorTransport),
    STUDIES("Estudos", "📚", ColorStudies),
    GYM("Academia", "💪", ColorGym),
    LEISURE("Lazer", "🎮", ColorLeisure),
    STREAMING("Streaming", "🎬", ColorStreaming),
    INVESTMENTS("Investimentos", "📈", ColorInvestments),
    SHOPPING("Compras", "🛍️", ColorShopping),
    SALARY("Salário", "💰", NeonGreen),
    OTHER("Outros", "💳", TextMuted)
}

data class Transaction(
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
