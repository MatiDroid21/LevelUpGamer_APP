package data.remote

import data.model.LoginRequest
import data.model.UsuarioDTO


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UsuariosApiService {

    @POST("api/usuarios/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<UsuarioDTO>

    @Multipart
    @POST("api/usuarios/registro")
    suspend fun registrar(
        @Part("nombre") nombre: RequestBody,
        @Part("rut") rut: RequestBody,
        @Part("email") email: RequestBody,
        @Part("contrasena") contrasena: RequestBody,
        @Part("fechaNacimiento") fechaNacimiento: RequestBody,
        @Part("idRol") idRol: RequestBody,
        @Part("direccion") direccion: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part foto: MultipartBody.Part,
        @Part("codigoReferido") codigoReferido: RequestBody? = null
    ): Response<UsuarioDTO>

    @GET("api/usuarios/{email}")
    suspend fun obtenerPorEmail(
        @Path("email") email: String
    ): Response<UsuarioDTO>

    @GET("api/usuarios")
    suspend fun listarTodos(): Response<List<UsuarioDTO>>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Body usuario: UsuarioDTO
    ): Response<UsuarioDTO>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Path("id") id: Long
    ): Response<Void>
}
