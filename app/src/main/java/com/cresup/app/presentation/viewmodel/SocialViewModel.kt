package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.FriendRequest
import com.cresup.app.domain.model.PublicProfile
import com.cresup.app.domain.repository.SocialRepository
import com.cresup.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialState(
    val currentUser: PublicProfile = PublicProfile(),
    val friends: List<PublicProfile> = emptyList(),
    val incomingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val feed: List<FeedItem> = emptyList(),
    val searchCode: String = "",
    val searchResult: PublicProfile? = null,
    val isSearching: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SocialState())
    val state: StateFlow<SocialState> = _state.asStateFlow()

    private var hasSynced = false

    init {
        viewModelScope.launch {
            userRepository.getUser().collect { user ->
                val profile = PublicProfile(
                    uid = "",
                    name = user.name,
                    userCode = user.userCode,
                    level = user.level,
                    levelName = user.levelName,
                    xp = user.xp
                )
                _state.update { it.copy(currentUser = profile) }
                if (!hasSynced) {
                    hasSynced = true
                    try { socialRepository.syncPublicProfile(profile) } catch (_: Exception) {}
                }
            }
        }
        viewModelScope.launch {
            socialRepository.getFriends().collect { friends ->
                _state.update { it.copy(friends = friends) }
            }
        }
        viewModelScope.launch {
            socialRepository.getIncomingRequests().collect { requests ->
                _state.update { it.copy(incomingRequests = requests) }
            }
        }
        viewModelScope.launch {
            socialRepository.getSentRequests().collect { requests ->
                _state.update { it.copy(sentRequests = requests) }
            }
        }
        viewModelScope.launch {
            socialRepository.getFeed().collect { feed ->
                _state.update { it.copy(feed = feed) }
            }
        }
    }

    fun onSearchCodeChange(code: String) {
        _state.update { it.copy(searchCode = code, searchResult = null, errorMessage = null) }
    }

    fun searchFriend() {
        val code = _state.value.searchCode.trim()
        if (code.isBlank()) return
        _state.update { it.copy(isSearching = true, searchResult = null, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = socialRepository.searchByCode(code)
                _state.update {
                    it.copy(
                        searchResult = result,
                        isSearching = false,
                        errorMessage = if (result == null) "Código não encontrado" else null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSearching = false, errorMessage = "Erro ao buscar") }
            }
        }
    }

    fun sendFriendRequest(toProfile: PublicProfile) {
        viewModelScope.launch {
            try {
                socialRepository.sendFriendRequest(toProfile, _state.value.currentUser)
                _state.update {
                    it.copy(
                        searchResult = null,
                        searchCode = "",
                        successMessage = "Pedido enviado para ${toProfile.name}!"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao enviar pedido") }
            }
        }
    }

    fun acceptRequest(request: FriendRequest) {
        viewModelScope.launch {
            try {
                socialRepository.acceptRequest(request)
                _state.update { it.copy(successMessage = "${request.fromName} adicionado(a)!") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao aceitar pedido") }
            }
        }
    }

    fun rejectRequest(request: FriendRequest) {
        viewModelScope.launch {
            try {
                socialRepository.rejectRequest(request.id)
                _state.update { it.copy(successMessage = "Pedido recusado") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao recusar") }
            }
        }
    }

    fun removeFriend(friendUid: String) {
        viewModelScope.launch {
            try {
                socialRepository.removeFriend(friendUid)
                _state.update { it.copy(successMessage = "Amigo removido") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Erro ao remover") }
            }
        }
    }

    fun clearMessages() = _state.update { it.copy(successMessage = null, errorMessage = null) }
    fun clearSearch() = _state.update { it.copy(searchCode = "", searchResult = null, errorMessage = null) }
}
