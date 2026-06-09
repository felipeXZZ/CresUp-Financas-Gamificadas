package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.TransactionType
import com.cresup.app.domain.repository.TransactionRepository
import com.cresup.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

enum class InsightType { POSITIVE, NEUTRAL, WARNING }

data class CategorySpend(
    val label: String,
    val amount: Double,
    val fraction: Float,
    val colorKey: String
)

data class FinancialInsight(
    val message: String,
    val type: InsightType
)

data class AnalyticsState(
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val savingsRate: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val insights: List<FinancialInsight> = emptyList(),
    val topCategory: String = "",
    val prevMonthExpenses: Double = 0.0,
    val trendPercent: Double = 0.0,
    val totalTransactions: Int = 0,
    val streakDays: Int = 0
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            combine(
                userRepository.getUser(),
                transactionRepository.getAllTransactions()
            ) { user, transactions ->
                val cal = Calendar.getInstance()
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)

                val monthlyTx = transactions.filter { tx ->
                    val c = Calendar.getInstance().apply { timeInMillis = tx.date }
                    c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) + 1 == month
                }

                val prevMonth = if (month == 1) 12 else month - 1
                val prevYear = if (month == 1) year - 1 else year
                val prevTx = transactions.filter { tx ->
                    val c = Calendar.getInstance().apply { timeInMillis = tx.date }
                    c.get(Calendar.YEAR) == prevYear && c.get(Calendar.MONTH) + 1 == prevMonth
                }

                val income = monthlyTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expenses = monthlyTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val prevExpenses = prevTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                val savingsRate = if (income > 0) ((income - expenses) / income * 100) else 0.0
                val dailyAvg = expenses / dayOfMonth
                val trendPct = if (prevExpenses > 0) ((expenses - prevExpenses) / prevExpenses * 100) else 0.0

                val rawBreakdown = monthlyTx
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }
                    .entries
                    .sortedByDescending { it.value }
                    .take(6)

                val totalExpenses = rawBreakdown.sumOf { it.value }.takeIf { it > 0 } ?: 1.0
                val breakdown = rawBreakdown.map { (cat, amount) ->
                    CategorySpend(
                        label = cat.label,
                        amount = amount,
                        fraction = (amount / totalExpenses).toFloat(),
                        colorKey = cat.name
                    )
                }

                val topCategory = breakdown.firstOrNull()?.label ?: ""

                val insights = buildInsights(
                    income = income,
                    expenses = expenses,
                    savingsRate = savingsRate,
                    trendPct = trendPct,
                    topCategory = topCategory,
                    topFraction = breakdown.firstOrNull()?.fraction ?: 0f,
                    streakDays = user.streakDays,
                    level = user.level
                )

                AnalyticsState(
                    monthlyIncome = income,
                    monthlyExpenses = expenses,
                    savingsRate = savingsRate,
                    dailyAverage = dailyAvg,
                    categoryBreakdown = breakdown,
                    insights = insights,
                    topCategory = topCategory,
                    prevMonthExpenses = prevExpenses,
                    trendPercent = trendPct,
                    totalTransactions = transactions.size,
                    streakDays = user.streakDays
                )
            }.collect { _state.value = it }
        }
    }

    private fun buildInsights(
        income: Double,
        expenses: Double,
        savingsRate: Double,
        trendPct: Double,
        topCategory: String,
        topFraction: Float,
        streakDays: Int,
        level: Int
    ): List<FinancialInsight> {
        val list = mutableListOf<FinancialInsight>()

        when {
            expenses > income && income > 0 ->
                list += FinancialInsight("Você gastou mais do que ganhou este mês. Revise seus hábitos.", InsightType.WARNING)
            savingsRate >= 30 ->
                list += FinancialInsight("Parabéns! Você está economizando ${savingsRate.toInt()}% da sua renda — excelente disciplina.", InsightType.POSITIVE)
            savingsRate in 10.0..29.9 ->
                list += FinancialInsight("Você está economizando ${savingsRate.toInt()}% da renda. Tente chegar a 30%!", InsightType.NEUTRAL)
            income > 0 ->
                list += FinancialInsight("Sua taxa de economia está baixa. Tente reduzir gastos desnecessários.", InsightType.WARNING)
        }

        if (topCategory.isNotEmpty() && topFraction > 0.35f) {
            list += FinancialInsight(
                "$topCategory representa ${(topFraction * 100).toInt()}% dos seus gastos. Considere reduzir.",
                InsightType.WARNING
            )
        }

        when {
            trendPct > 20 ->
                list += FinancialInsight("Seus gastos aumentaram ${trendPct.toInt()}% em relação ao mês anterior.", InsightType.WARNING)
            trendPct < -10 ->
                list += FinancialInsight("Ótimo! Seus gastos caíram ${abs(trendPct.toInt())}% em relação ao mês passado.", InsightType.POSITIVE)
        }

        if (streakDays >= 7)
            list += FinancialInsight("$streakDays dias de streak! Continue registrando seus gastos diariamente.", InsightType.POSITIVE)

        if (level >= 3)
            list += FinancialInsight("Você já é ${if (level == 3) "Especialista Financeiro" else "Elite Financeira"}. Mantenha o ritmo!", InsightType.POSITIVE)

        if (list.isEmpty())
            list += FinancialInsight("Continue registrando suas transações para receber insights personalizados.", InsightType.NEUTRAL)

        return list
    }
}
