package ui.recover


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.repository.auth.AuthRepository

data class RecoverUiState(
    val email: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val sent: Boolean = false,
    val message: String? = null
)

class RecuperarViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RecoverUiState())
    val ui: StateFlow<RecoverUiState> = _ui

    fun onEmailChange(v: String) = _ui.update { it.copy(email = v, error = null, message = null) }

    private fun validar(): String? {
        val s = _ui.value
        if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) return "Email inválido"
        return null
    }

    fun sendReset() {
        val err = validar()
        if (err != null) {
            _ui.update { it.copy(error = err) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, message = null) }

            val ok = repo.sendPasswordReset(_ui.value.email)
            _ui.update {
                if (ok)
                    it.copy(loading = false, sent = true, message = "Correo de recuperación enviado.")
                else
                    it.copy(loading = false, error = "No se pudo enviar el correo")
            }
        }
    }

    fun messageConsumed() { _ui.update { it.copy(message = null) } }
}