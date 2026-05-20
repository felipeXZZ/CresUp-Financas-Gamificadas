package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.Goal
import com.cresup.app.domain.model.Transaction
import com.cresup.app.domain.model.TransactionType
import com.cresup.app.domain.model.User
import com.cresup.app.domain.repository.GoalRepository
import com.cresup.app.domain.repository.QuoteRepository
import com.cresup.app.domain.repository.TransactionRepository
import com.cresup.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardState(
    val user: User = User(),
    val totalBalance: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val savings: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val activeGoal: Goal? = null,
    val quote: String = "",
    val isLoadingQuote: Boolean = false,
    val categoryBreakdown: Map<String, Double> = emptyMap()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch { userRepository.createDefaultUser() }
        loadData()
        loadQuote()
    }

    private fun loadData() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        viewModelScope.launch {
            combine(
                userRepository.getUser(),
                transactionRepository.getAllTransactions(),
                goalRepository.getAllGoals()
            ) { user, transactions, goals ->
                val monthlyTx = transactions.filter {
                    val txCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) + 1 == month
                }
                val income = monthlyTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expenses = monthlyTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val allIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val allExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                val breakdown = monthlyTx
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category.label }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }

                DashboardState(
                    user = user,
                    totalBalance = allIncome - allExpenses,
                    monthlyExpenses = expenses,
                    monthlyIncome = income,
                    savings = income - expenses,
                    recentTransactions = transactions.take(5),
                    activeGoal = goals.firstOrNull { !it.isCompleted },
                    quote = _state.value.quote,
                    isLoadingQuote = _state.value.isLoadingQuote,
                    categoryBreakdown = breakdown
                )
            }.collect { _state.value = it }
        }
    }

    private fun loadQuote() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingQuote = true) }
            val result = quoteRepository.getMotivationalQuote()
            _state.update { it.copy(
                quote = result.getOrDefault("Disciplina é a ponte entre metas e conquistas."),
                isLoadingQuote = false
            )}
        }
    }
}
