package cl.duoc.levelupgamer.ui.login

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.data.local.UserPreferences
import cl.duoc.levelupgamer.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import data.model.UsuarioDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
    val user: UsuarioDTO? = null,
    val message: String? = null
)

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val repo = AuthRepository(userPrefs)

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    init {
        // ⭐ SOLO verificar, NO sincronizar automáticamente
        checkIfShouldAutoLogin()
    }

    /**
     * Verifica si debe hacer auto-login (solo si está en otra pantalla)
     * NO loguea automáticamente en la pantalla de login
     */
    private fun checkIfShouldAutoLogin() {
        // Solo verifica, no hace nada
        // El auto-login lo maneja un SplashScreen o MainActivity
        Log.d("LoginViewModel", "🔍 Verificando sesión...")
    }

    fun onEmailChange(v: String) = _ui.update {
        it.copy(email = v, error = null, message = null)
    }

    fun onPasswordChange(v: String) = _ui.update {
        it.copy(password = v, error = null, message = null)
    }

    private fun validar(): String? {
        val s = _ui.value
        if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            return "Email inválido"
        }
        if (s.password.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres"
        }
        return null
    }

    /**
     * Login híbrido (Firebase + API)
     */
    fun submit() {
        val err = validar()
        if (err != null) {
            _ui.update { it.copy(error = err) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, message = null) }

            Log.d("LoginViewModel", "🔐 Intentando login con: ${_ui.value.email}")

            val result = repo.login(_ui.value.email, _ui.value.password)

            result.fold(
                onSuccess = { usuario ->
                    Log.d("LoginViewModel", "✅ Login exitoso: ${usuario.nombre}")

                    if (usuario.idUsuario != null) {
                        userPrefs.saveUser(
                            email = usuario.email,
                            nombre = usuario.nombre,
                            idUsuario = usuario.idUsuario
                        )
                    }

                    _ui.update {
                        it.copy(
                            loading = false,
                            loggedIn = true,
                            user = usuario,
                            message = "Bienvenido ${usuario.nombre}"
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("LoginViewModel", "❌ Error login: ${error.message}")
                    _ui.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "Error al iniciar sesión"
                        )
                    }
                }
            )
        }
    }

    fun messageConsumed() {
        _ui.update { it.copy(message = null) }
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        Log.d("LoginViewModel", "🚪 Cerrando sesión")
        repo.logout()
        userPrefs.clearUser()
        _ui.update { LoginUiState() }
    }
}
