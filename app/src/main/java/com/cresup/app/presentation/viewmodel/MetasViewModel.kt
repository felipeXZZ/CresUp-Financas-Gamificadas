package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.Goal
import com.cresup.app.domain.repository.GoalRepository
import com.cresup.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetasState(
    val goals: List<Goal> = emptyList(),
    val isAddingGoal: Boolean = false,
    val contributingToGoal: Goal? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class MetasViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MetasState())
    val state: StateFlow<MetasState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            goalRepository.getAllGoals().collect { goals ->
                _state.update { it.copy(goals = goals) }
            }
        }
    }

    fun addGoal(title: String, emoji: String, targetText: String) {
        if (title.isBlank()) {
            _state.update { it.copy(errorMessage = "Informe um título para a meta") }
            return
        }
        val target = (targetText.toLongOrNull() ?: 0L) / 100.0
        if (target <= 0) {
            _state.update { it.copy(errorMessage = "Informe um valor alvo válido") }
            return
        }
        viewModelScope.launch {
            try {
                goalRepository.insert(Goal(title = title, emoji = emoji, targetAmount = target))
                userRepository.addXP(30)
                _state.update { it.copy(isAddingGoal = false, successMessage = "Meta criada! +30 XP") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao criar meta") }
            }
        }
    }

    fun contribute(goalId: Long, amountText: String) {
        val amount = (amountText.toLongOrNull() ?: 0L) / 100.0
        if (amount <= 0) {
            _state.update { it.copy(errorMessage = "Informe um valor válido") }
            return
        }
        viewModelScope.launch {
            try {
                goalRepository.addContribution(goalId, amount)
                userRepository.addXP(25)
                _state.update { it.copy(contributingToGoal = null, successMessage = "Contribuição adicionada! +25 XP") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao adicionar contribuição") }
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.delete(goal)
            _state.update { it.copy(successMessage = "Meta removida") }
        }
    }

    fun showAddDialog() = _state.update { it.copy(isAddingGoal = true) }
    fun hideAddDialog() = _state.update { it.copy(isAddingGoal = false) }
    fun showContributeDialog(goal: Goal) = _state.update { it.copy(contributingToGoal = goal) }
    fun hideContributeDialog() = _state.update { it.copy(contributingToGoal = null) }
    fun clearMessages() = _state.update { it.copy(errorMessage = null, successMessage = null) }
}
