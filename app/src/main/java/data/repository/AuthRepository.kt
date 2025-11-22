package cl.duoc.levelupgamer.data.repository


import cl.duoc.levelupgamer.data.remote.RetrofitClient
import data.model.LoginRequest
import data.model.UsuarioDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AuthRepository {

    // ⭐ CORREGIDO: nombre de variable en minúscula
    private val apiService = RetrofitClient.usuariosService

    suspend fun login(email: String, contrasena: String): Result<UsuarioDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val loginRequest = LoginRequest(email, contrasena)
                // ⭐ CORREGIDO: agregar apiService antes del punto
                val response = apiService.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Credenciales inválidas"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun registrar(
        nombre: String,
        rut: String,
        email: String,
        contrasena: String,
        fechaNacimiento: String,
        idRol: Long = 2L, // 2 = Usuario normal
        direccion: String,
        telefono: String,
        fotoFile: File,
        codigoReferido: String? = null
    ): Result<UsuarioDTO> {
        return withContext(Dispatchers.IO) {
            try {
                // Convertir datos a RequestBody
                val nombreBody = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                val rutBody = rut.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
                val contrasenaBody = contrasena.toRequestBody("text/plain".toMediaTypeOrNull())
                val fechaBody = fechaNacimiento.toRequestBody("text/plain".toMediaTypeOrNull())
                val idRolBody = idRol.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val direccionBody = direccion.toRequestBody("text/plain".toMediaTypeOrNull())
                val telefonoBody = telefono.toRequestBody("text/plain".toMediaTypeOrNull())

                val codigoRefBody = codigoReferido?.toRequestBody("text/plain".toMediaTypeOrNull())

                // Preparar la foto
                val requestFile = fotoFile.asRequestBody("image/*".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData(
                    "foto",
                    fotoFile.name,
                    requestFile
                )

                val response = apiService.registrar(
                    nombreBody,
                    rutBody,
                    emailBody,
                    contrasenaBody,
                    fechaBody,
                    idRolBody,
                    direccionBody,
                    telefonoBody,
                    fotoPart,
                    codigoRefBody
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error al registrar"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun obtenerPorEmail(email: String): Result<UsuarioDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerPorEmail(email)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Usuario no encontrado"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
