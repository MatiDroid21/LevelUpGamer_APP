package ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.data.local.UserPreferences
import cl.duoc.levelupgamer.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val repository = AuthRepository(userPrefs)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Verificar si hay sesión activa al iniciar
        checkExistingSession()
    }

    /**
     * Verifica si hay una sesión activa
     */
    private fun checkExistingSession() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val hasLocalSession = userPrefs.isLoggedIn()

        if (firebaseUser != null && hasLocalSession) {
            Log.d("AuthViewModel", "👤 Sesión activa detectada")

            viewModelScope.launch {
                val email = firebaseUser.email
                if (email != null) {
                    val result = repository.obtenerPorEmail(email)
                    result.fold(
                        onSuccess = { usuario ->
                            Log.d("AuthViewModel", "✅ Usuario recuperado")
                            _authState.value = AuthState.Authenticated(usuario)
                        },
                        onFailure = {
                            Log.w("AuthViewModel", "⚠️ Error al recuperar usuario")
                            logout() // Limpiar sesión corrupta
                        }
                    )
                }
            }
        } else {
            Log.d("AuthViewModel", "🔒 No hay sesión activa")
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Login
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "🔐 Intentando login: $email")

            val result = repository.login(email, password)

            result.fold(
                onSuccess = { usuario ->
                    Log.d("AuthViewModel", "✅ Login exitoso: ${usuario.nombre}")
                    _authState.value = AuthState.Authenticated(usuario)
                },
                onFailure = { error ->
                    Log.e("AuthViewModel", "❌ Error login: ${error.message}")
                    _authState.value = AuthState.Error(
                        error.message ?: "Error al iniciar sesión"
                    )
                }
            )
        }
    }

    /**
     * Logout
     */
    fun logout() {
        Log.d("AuthViewModel", "🚪 Cerrando sesión")
        repository.logout()
        userPrefs.clearUser()
        _authState.value = AuthState.Idle
    }

    /**
     * Resetear estado (útil para limpiar errores)
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Verificar si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }

    /**
     * Obtener usuario actual
     */
    fun getCurrentUser() = when (val state = _authState.value) {
        is AuthState.Authenticated -> state.usuario
        else -> null
    }
}
