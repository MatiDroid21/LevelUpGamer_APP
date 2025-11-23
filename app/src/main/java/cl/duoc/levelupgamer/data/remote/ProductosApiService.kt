package data.remote
import data.model.ApiResponse
import data.model.CategoriaDTO
import data.model.ProductoDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductosApiService {

    @GET("api/productos")
    suspend fun listarProductos(): Response<ApiResponse<List<ProductoDTO>>>

    @GET("api/productos/{id}")
    suspend fun obtenerProducto(
        @Path("id") id: Long
    ): Response<ApiResponse<ProductoDTO>>

    @GET("api/productos/categoria/{idCategoria}")
    suspend fun obtenerPorCategoria(
        @Path("idCategoria") idCategoria: Long
    ): Response<ApiResponse<List<ProductoDTO>>>

    @GET("api/productos/disponibles")
    suspend fun obtenerProductosDisponibles(): Response<ApiResponse<List<ProductoDTO>>>

    @GET("api/categorias")
    suspend fun listarCategorias(): Response<ApiResponse<List<CategoriaDTO>>>
}
