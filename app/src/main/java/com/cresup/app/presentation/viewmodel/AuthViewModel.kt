package com.cresup.app.presentation.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cresup.app.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    private val credentialManager = CredentialManager.create(context)

    val isLoggedIn: Boolean get() = auth.currentUser != null

    // ─── Email/Senha ──────────────────────────────────────────────────────────

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Preencha todos os campos")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = mapFirebaseError(e.message))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Preencha todos os campos")
            return
        }
        if (password != confirmPassword) {
            _state.value = _state.value.copy(errorMessage = "As senhas não coincidem")
            return
        }
        if (password.length < 6) {
            _state.value = _state.value.copy(errorMessage = "A senha deve ter pelo menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = mapFirebaseError(e.message))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    // ─── Google Sign-In ───────────────────────────────────────────────────────

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val serverClientId = context.getString(R.string.default_web_client_id)
                android.util.Log.d("AUTH_DEBUG", "Usando Client ID: $serverClientId")

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = activity, request = request)
                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                onSuccess()
            } catch (e: GetCredentialCancellationException) {
                // usuário cancelou — não mostrar erro
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Erro Google (${e.javaClass.simpleName}): ${e.message}"
                )
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    fun logout() {
        auth.signOut()
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun mapFirebaseError(message: String?): String = when {
        message == null -> "Erro desconhecido"
        "no user record" in message || "user-not-found" in message -> "E-mail não encontrado"
        "password is invalid" in message || "wrong-password" in message -> "Senha incorreta"
        "email address is already" in message || "email-already-in-use" in message -> "E-mail já cadastrado"
        "badly formatted" in message || "invalid-email" in message -> "E-mail inválido"
        "network" in message.lowercase() -> "Sem conexão com a internet"
        else -> "Erro: $message"
    }
}
