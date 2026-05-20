package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.Challenge
import com.cresup.app.domain.repository.ChallengeRepository
import com.cresup.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DesafiosState(
    val challenges: List<Challenge> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DesafiosViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DesafiosState())
    val state: StateFlow<DesafiosState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            challengeRepository.seedDefaultChallenges()
            challengeRepository.getAllChallenges().collect { list ->
                _state.update { it.copy(challenges = list) }
            }
        }
    }

    fun activateChallenge(challenge: Challenge) {
        if (challenge.isActive || challenge.isCompleted) return
        viewModelScope.launch {
            challengeRepository.activateChallenge(challenge.id)
            _state.update { it.copy(successMessage = "Desafio ativado! Bora lá! 💪") }
        }
    }

    fun progressChallenge(challenge: Challenge) {
        if (!challenge.isActive || challenge.isCompleted) return
        viewModelScope.launch {
            challengeRepository.incrementProgress(challenge.id)
            val updated = _state.value.challenges.find { it.id == challenge.id }
            if (updated?.progressCurrent == updated?.durationDays?.minus(1)) {
                userRepository.addXP(challenge.xpReward)
                _state.update { it.copy(successMessage = "Desafio concluído! +${challenge.xpReward} XP 🏆") }
            }
        }
    }

    fun clearMessages() = _state.update { it.copy(successMessage = null, errorMessage = null) }
}
