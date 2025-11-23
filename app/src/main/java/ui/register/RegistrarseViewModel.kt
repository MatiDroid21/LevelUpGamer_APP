package ui.register

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.data.local.UserPreferences
import cl.duoc.levelupgamer.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class RegisterUiState(
    val nombre: String = "",
    val rut: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fechaNacimiento: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val fotoUri: Uri? = null,
    val codigoReferido: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val registered: Boolean = false,
    val message: String? = null
)

class RegisterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val repo = AuthRepository(userPrefs)  // PASAR userPrefs
    private val context = application

    private val _ui = MutableStateFlow(RegisterUiState())
    val ui: StateFlow<RegisterUiState> = _ui

    fun onNombreChange(v: String) = _ui.update { it.copy(nombre = v, error = null) }
    fun onRutChange(v: String) = _ui.update { it.copy(rut = v, error = null) }
    fun onEmailChange(v: String) = _ui.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _ui.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _ui.update { it.copy(confirmPassword = v, error = null) }
    fun onFechaNacimientoChange(v: String) = _ui.update { it.copy(fechaNacimiento = v, error = null) }
    fun onDireccionChange(v: String) = _ui.update { it.copy(direccion = v, error = null) }
    fun onTelefonoChange(v: String) = _ui.update { it.copy(telefono = v, error = null) }
    fun onFotoChange(uri: Uri?) = _ui.update { it.copy(fotoUri = uri, error = null) }
    fun onCodigoReferidoChange(v: String) = _ui.update { it.copy(codigoReferido = v, error = null) }

    private fun validar(): String? {
        val s = _ui.value

        if (s.nombre.isBlank()) return "El nombre es obligatorio"
        if (s.rut.isBlank()) return "El RUT es obligatorio"
        if (s.email.isBlank()) return "El email es obligatorio"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            return "Email inválido"
        }
        if (s.password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        if (s.password != s.confirmPassword) return "Las contraseñas no coinciden"
        if (s.fechaNacimiento.isBlank()) return "La fecha de nacimiento es obligatoria"
        if (s.direccion.isBlank()) return "La dirección es obligatoria"
        if (s.telefono.isBlank()) return "El teléfono es obligatorio"
        if (s.fotoUri == null) return "Debes seleccionar una foto"

        return null
    }

    fun submit() {
        val err = validar()
        if (err != null) {
            _ui.update { it.copy(error = err) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }

            try {
                val s = _ui.value

                // Convertir Uri a File
                val fotoFile = uriToFile(s.fotoUri!!)

                val result = repo.registrar(
                    nombre = s.nombre,
                    rut = s.rut,
                    email = s.email,
                    contrasena = s.password,
                    fechaNacimiento = s.fechaNacimiento,
                    idRol = 1L, //  Rol  = Usuario normal
                    direccion = s.direccion,
                    telefono = s.telefono,
                    fotoFile = fotoFile,
                    codigoReferido = s.codigoReferido.ifBlank { null }
                )

                result.fold(
                    onSuccess = { usuario ->
                        //  Ya no es necesario guardar aquí porque AuthRepository ya lo hace
                        // Pero lo dejamos por seguridad
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
                                registered = true,
                                message = "Registro exitoso. Bienvenido ${usuario.nombre}"
                            )
                        }
                    },
                    onFailure = { error ->
                        _ui.update {
                            it.copy(
                                loading = false,
                                error = error.message ?: "Error al registrar"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _ui.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "Error inesperado"
                    )
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo leer la imagen")

        val file = File(context.cacheDir, "foto_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    fun messageConsumed() {
        _ui.update { it.copy(message = null) }
    }
}
