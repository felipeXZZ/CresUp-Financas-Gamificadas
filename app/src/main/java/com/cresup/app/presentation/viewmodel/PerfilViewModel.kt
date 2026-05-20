package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.Achievement
import com.cresup.app.domain.model.AllAchievements
import com.cresup.app.domain.model.User
import com.cresup.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilState(
    val user: User = User(),
    val achievements: List<Achievement> = AllAchievements,
    val isEditingName: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilState())
    val state: StateFlow<PerfilState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getUser().collect { user ->
                val unlockedAchievements = computeUnlockedAchievements(user)
                _state.update { it.copy(user = user, achievements = unlockedAchievements) }
            }
        }
    }

    private fun computeUnlockedAchievements(user: User): List<Achievement> {
        return AllAchievements.map { achievement ->
            val unlocked = when (achievement.id) {
                1L -> true // sempre desbloqueado após onboarding
                5L -> user.streakDays >= 30
                8L -> user.level >= 4
                10L -> user.xp >= 5000
                else -> false
            }
            achievement.copy(isUnlocked = unlocked)
        }
    }

    fun updateName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val current = _state.value.user
            userRepository.updateUser(current.copy(name = name))
            _state.update { it.copy(isEditingName = false, successMessage = "Nome atualizado!") }
        }
    }

    fun showEditName() = _state.update { it.copy(isEditingName = true) }
    fun hideEditName() = _state.update { it.copy(isEditingName = false) }
    fun clearMessage() = _state.update { it.copy(successMessage = null) }
}
