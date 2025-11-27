package cl.duoc.levelupgamer.data.repository

import android.util.Log
import cl.duoc.levelupgamer.data.local.UserPreferences
import cl.duoc.levelupgamer.data.remote.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import data.model.LoginRequest
import data.model.UsuarioDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AuthRepository(private val userPrefs: UserPreferences) {

    private val apiService = RetrofitClient.usuariosService
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * LOGIN HÍBRIDO
     */
    suspend fun login(email: String, contrasena: String): Result<UsuarioDTO> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Login: $email")

                // Autenticar con Firebase
                val firebaseResult = firebaseAuth.signInWithEmailAndPassword(email, contrasena).await()
                val firebaseUser = firebaseResult.user
                    ?: return@withContext Result.failure(Exception("Error de autenticación"))

                Log.d("AuthRepository", "Login Firebase OK")

                // ⭐ BUSCAR usuario en la API
                try {
                    val response = apiService.obtenerPorEmail(email)

                    if (response.isSuccessful && response.body() != null) {
                        val usuario = response.body()!!
                        Log.d("AuthRepository", "Usuario encontrado en API")

                        //  VALIDAR que tenga idUsuario
                        if (usuario.idUsuario == null) {
                            return@withContext Result.failure(
                                Exception("Tu cuenta no está completa. Por favor, completa tu registro desde la aplicación.")
                            )
                        }

                        //  GUARDAR EN SHAREDPREFERENCES
                        userPrefs.saveUser(
                            email = usuario.email,
                            nombre = usuario.nombre,
                            idUsuario = usuario.idUsuario
                        )
                        Log.d("AuthRepository", "Usuario guardado - ID: ${usuario.idUsuario}")

                        Result.success(usuario)
                    } else {
                        //  Usuario NO existe en la BD → Error claro
                        Log.w("AuthRepository", "Usuario no existe en la base de datos")
                        return@withContext Result.failure(
                            Exception("Tu cuenta no está registrada. Por favor, completa el registro en la aplicación primero.")
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepository", " Error API: ${e.message}")
                    return@withContext Result.failure(
                        Exception("Error al verificar tu cuenta. Verifica tu conexión a internet.")
                    )
                }

            } catch (e: Exception) {
                Log.e("AuthRepository", " Firebase falló, intentando API")
                loginSoloAPI(email, contrasena)
            }
        }
    }

    /**
     * LOGIN SOLO CON API (para usuarios de la web)
     */
    private suspend fun loginSoloAPI(email: String, contrasena: String): Result<UsuarioDTO> {
        return try {
            Log.d("AuthRepository", "📤 Login API")
            val loginRequest = LoginRequest(email, contrasena)
            val response = apiService.login(loginRequest)

            if (response.isSuccessful && response.body() != null) {
                val usuario = response.body()!!
                Log.d("AuthRepository", "Login API OK")

                // GUARDAR EN SHAREDPREFERENCES
                if (usuario.idUsuario != null) {
                    userPrefs.saveUser(
                        email = usuario.email,
                        nombre = usuario.nombre,
                        idUsuario = usuario.idUsuario
                    )
                    Log.d("AuthRepository", " Usuario guardado - ID: ${usuario.idUsuario}")
                }

                Result.success(usuario)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", " Error: ${e.message}")
            Result.failure(Exception("Error al iniciar sesión: ${e.message}"))
        }
    }

    /**
     * REGISTRO (crea en Firebase Y en la API)
     */
    suspend fun registrar(
        nombre: String,
        rut: String,
        email: String,
        contrasena: String,
        fechaNacimiento: String,
        idRol: Long = 2L,
        direccion: String,
        telefono: String,
        fotoFile: File,
        codigoReferido: String? = null
    ): Result<UsuarioDTO> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", " Registrando: $email")

                // 1. Crear en Firebase
                val firebaseResult = firebaseAuth.createUserWithEmailAndPassword(email, contrasena).await()
                val firebaseUser = firebaseResult.user
                    ?: return@withContext Result.failure(Exception("Error al crear cuenta en Firebase"))

                Log.d("AuthRepository", " Creado en Firebase")

                // 2. Guardar en API
                try {
                    val nombreBody = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rutBody = rut.toRequestBody("text/plain".toMediaTypeOrNull())
                    val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
                    val contrasenaBody = contrasena.toRequestBody("text/plain".toMediaTypeOrNull())
                    val fechaBody = fechaNacimiento.toRequestBody("text/plain".toMediaTypeOrNull())
                    val idRolBody = idRol.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val direccionBody = direccion.toRequestBody("text/plain".toMediaTypeOrNull())
                    val telefonoBody = telefono.toRequestBody("text/plain".toMediaTypeOrNull())
                    val codigoRefBody = codigoReferido?.toRequestBody("text/plain".toMediaTypeOrNull())

                    val requestFile = fotoFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val fotoPart = MultipartBody.Part.createFormData("foto", fotoFile.name, requestFile)

                    val response = apiService.registrar(
                        nombreBody, rutBody, emailBody, contrasenaBody,
                        fechaBody, idRolBody, direccionBody, telefonoBody,
                        fotoPart, codigoRefBody
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val usuario = response.body()!!
                        Log.d("AuthRepository", " Registrado en API")

                        //  GUARDAR EN SHAREDPREFERENCES
                        if (usuario.idUsuario != null) {
                            userPrefs.saveUser(
                                email = usuario.email,
                                nombre = usuario.nombre,
                                idUsuario = usuario.idUsuario
                            )
                            Log.d("AuthRepository", " Usuario guardado - ID: ${usuario.idUsuario}")
                        }

                        Result.success(usuario)
                    } else {

                        Log.e("AuthRepository", " Error al registrar en API")
                        firebaseUser.delete().await()
                        Result.failure(Exception("Error al registrar en la base de datos. Tu cuenta no fue creada."))
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepository", " Error API: ${e.message}")
                    // Si falla la API, eliminar de Firebase
                    firebaseUser.delete().await()
                    Result.failure(Exception("Error al registrar: ${e.message}. Tu cuenta no fue creada."))
                }

            } catch (e: Exception) {
                Log.e("AuthRepository", " Error: ${e.message}")
                Result.failure(Exception("Error al crear cuenta: ${e.message}"))
            }
        }
    }

    /**
     * Obtener usuario por email
     */
    suspend fun obtenerPorEmail(email: String): Result<UsuarioDTO> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "🔍 Buscando: $email")
                val response = apiService.obtenerPorEmail(email)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("AuthRepository", " Encontrado")
                    Result.success(response.body()!!)
                } else {
                    Log.w("AuthRepository", " No encontrado")
                    Result.failure(Exception("Usuario no encontrado"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", " Error: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Logout
     */
    fun logout() {
        Log.d("AuthRepository", " Logout")
        firebaseAuth.signOut()
        userPrefs.clearUser()
    }

    /**
     * Usuario actual de Firebase
     */
    fun getCurrentUser() = firebaseAuth.currentUser
}
