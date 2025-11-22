package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.login(email, password)

            result.fold(
                onSuccess = { usuario ->
                    _authState.value = AuthState.Authenticated(usuario)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(
                        error.message ?: "Error al iniciar sesión"
                    )
                }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
