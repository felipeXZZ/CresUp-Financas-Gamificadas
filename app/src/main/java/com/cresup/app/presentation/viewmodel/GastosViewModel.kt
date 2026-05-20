package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.Transaction
import com.cresup.app.domain.model.TransactionCategory
import com.cresup.app.domain.model.TransactionType
import com.cresup.app.domain.repository.TransactionRepository
import com.cresup.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GastosState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val selectedType: TransactionType? = null,
    val selectedCategory: TransactionCategory? = null,
    val searchQuery: String = "",
    val isAddingTransaction: Boolean = false,
    val editingTransaction: Transaction? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class GastosViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GastosState())
    val state: StateFlow<GastosState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { list ->
                _state.update {
                    it.copy(
                        transactions = list,
                        filteredTransactions = applyFilters(list, it.selectedType, it.selectedCategory, it.searchQuery)
                    )
                }
            }
        }
    }

    fun addTransaction(
        title: String,
        amountText: String,
        type: TransactionType,
        category: TransactionCategory,
        note: String
    ) {
        val amount = amountText.replace(",", ".").toDoubleOrNull()
        if (title.isBlank()) {
            _state.update { it.copy(errorMessage = "Informe um título para a transação") }
            return
        }
        if (amount == null || amount <= 0) {
            _state.update { it.copy(errorMessage = "Informe um valor válido") }
            return
        }
        viewModelScope.launch {
            try {
                transactionRepository.insert(
                    Transaction(title = title, amount = amount, type = type, category = category, note = note)
                )
                userRepository.addXP(if (type == TransactionType.EXPENSE) 10 else 20)
                _state.update { it.copy(isAddingTransaction = false, successMessage = "Transação adicionada! +${if (type == TransactionType.EXPENSE) 10 else 20} XP") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao salvar transação") }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.delete(transaction)
                _state.update { it.copy(successMessage = "Transação removida") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao remover transação") }
            }
        }
    }

    fun setTypeFilter(type: TransactionType?) {
        _state.update { s ->
            s.copy(
                selectedType = type,
                filteredTransactions = applyFilters(s.transactions, type, s.selectedCategory, s.searchQuery)
            )
        }
    }

    fun setSearch(query: String) {
        _state.update { s ->
            s.copy(
                searchQuery = query,
                filteredTransactions = applyFilters(s.transactions, s.selectedType, s.selectedCategory, query)
            )
        }
    }

    fun showAddDialog() = _state.update { it.copy(isAddingTransaction = true) }
    fun hideAddDialog() = _state.update { it.copy(isAddingTransaction = false) }
    fun clearMessages() = _state.update { it.copy(errorMessage = null, successMessage = null) }

    private fun applyFilters(
        list: List<Transaction>,
        type: TransactionType?,
        category: TransactionCategory?,
        query: String
    ) = list
        .filter { type == null || it.type == type }
        .filter { category == null || it.category == category }
        .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
}
