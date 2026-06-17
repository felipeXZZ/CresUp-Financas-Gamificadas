package com.cresup.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.FriendRequest
import com.cresup.app.domain.model.PublicProfile
import com.cresup.app.domain.repository.SocialRepository
import com.cresup.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(SocialState())
    val state: StateFlow<SocialState> = _state.asStateFlow()

    private var socialJobs = mutableListOf<Job>()
    private var currentUid: String? = null
    private var hasSynced = false

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val newUid = firebaseAuth.currentUser?.uid
        if (newUid != currentUid) {
            currentUid = newUid
            hasSynced = false
            restartListeners()
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    private fun restartListeners() {
        socialJobs.forEach { it.cancel() }
        socialJobs.clear()
        _state.value = SocialState()

        if (currentUid == null) return

        socialJobs += viewModelScope.launch {
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
        socialJobs += viewModelScope.launch {
            socialRepository.getFriends().collect { friends ->
                _state.update { it.copy(friends = friends) }
            }
        }
        socialJobs += viewModelScope.launch {
            socialRepository.getIncomingRequests().collect { requests ->
                _state.update { it.copy(incomingRequests = requests) }
            }
        }
        socialJobs += viewModelScope.launch {
            socialRepository.getSentRequests().collect { requests ->
                _state.update { it.copy(sentRequests = requests) }
            }
        }
        socialJobs += viewModelScope.launch {
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
                val msg = when (e.message) {
                    "already_friends" -> "Vocês já são amigos!"
                    "already_sent" -> "Pedido já enviado!"
                    "has_incoming" -> "Essa pessoa já te enviou um pedido! Aceite na aba Amigos."
                    else -> "Erro ao enviar pedido"
                }
                _state.update { it.copy(searchResult = null, searchCode = "", errorMessage = msg) }
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
