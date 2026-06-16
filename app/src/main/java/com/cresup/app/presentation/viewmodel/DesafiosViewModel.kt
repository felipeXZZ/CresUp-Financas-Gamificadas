package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.Challenge
import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.FeedItemType
import com.cresup.app.domain.repository.ChallengeRepository
import com.cresup.app.domain.repository.SocialRepository
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
    private val userRepository: UserRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DesafiosState())
    val state: StateFlow<DesafiosState> = _state.asStateFlow()

    private val processingIds = mutableSetOf<Long>()

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
            _state.update { it.copy(successMessage = "Desafio ativado! Bora lá!") }
        }
    }

    fun progressChallenge(challenge: Challenge) {
        if (!challenge.isActive || challenge.isCompleted) return
        if (!processingIds.add(challenge.id)) return  // já em processamento, bloqueia double-tap
        viewModelScope.launch {
            try {
                val completed = challengeRepository.incrementProgress(challenge.id)
                if (completed) {
                    userRepository.addXP(challenge.xpReward)
                    val userName = userRepository.getUser().first().name
                    runCatching {
                        socialRepository.postToMyFeed(
                            FeedItem(
                                type = FeedItemType.CHALLENGE,
                                actorName = userName,
                                message = "concluiu o desafio \"${challenge.title}\"! +${challenge.xpReward} XP",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    _state.update { it.copy(successMessage = "Desafio concluído! +${challenge.xpReward} XP") }
                }
            } finally {
                processingIds.remove(challenge.id)
            }
        }
    }

    fun clearMessages() = _state.update { it.copy(successMessage = null, errorMessage = null) }
}
