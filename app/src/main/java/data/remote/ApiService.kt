package cl.duoc.levelupgamer.data.remote

import data.model.ApiResponse
import data.model.CategoriaDTO
import data.model.LoginRequest
import data.model.ProductoDTO
import data.model.UsuarioDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========================================
    // apartado de usuarios y sus endpoints
    // ========================================

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

    // ========================================
    // apartado de productos
    // ========================================

    /**
     * GET /api/productos
     * Obtiene todos los productos
     */
    @GET("api/productos")
    suspend fun listarProductos(): Response<ApiResponse<List<ProductoDTO>>>

    /**
     * GET /api/productos/{id}
     * Obtiene un producto por su ID
     */
    @GET("api/productos/{id}")
    suspend fun obtenerProducto(
        @Path("id") id: Long
    ): Response<ApiResponse<ProductoDTO>>

    /**
     * GET /api/productos/categoria/{idCategoria}
     * Obtiene productos filtrados por categoría
     */
    @GET("api/productos/categoria/{idCategoria}")
    suspend fun obtenerPorCategoria(
        @Path("idCategoria") idCategoria: Long
    ): Response<ApiResponse<List<ProductoDTO>>>

    /**
     * GET /api/productos/disponibles
     * Obtiene solo productos que tienen stock > 0
     */
    @GET("api/productos/disponibles")
    suspend fun obtenerProductosDisponibles(): Response<ApiResponse<List<ProductoDTO>>>

    // ========================================
    // CATEGORÍAS
    // ========================================

    /**
     * GET /api/categorias
     * Obtiene todas las categorías disponibles
     */
    @GET("api/categorias")
    suspend fun listarCategorias(): Response<ApiResponse<List<CategoriaDTO>>>
}
