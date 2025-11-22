package cl.duoc.levelupgamer.ui.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import cl.duoc.levelupgamer.data.repository.AuthRepository
import data.local.UserPreferences
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

    private val repo = AuthRepository()
    private val userPrefs = UserPreferences(application)

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

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

    fun submit() {
        val err = validar()
        if (err != null) {
            _ui.update { it.copy(error = err) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, message = null) }

            val result = repo.login(_ui.value.email, _ui.value.password)

            result.fold(
                onSuccess = { usuario ->
                    // ⭐ Guardar en SharedPreferences
                    userPrefs.saveUser(
                        email = usuario.email,
                        nombre = usuario.nombre,
                        idUsuario = usuario.idUsuario ?: -1
                    )

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
}
