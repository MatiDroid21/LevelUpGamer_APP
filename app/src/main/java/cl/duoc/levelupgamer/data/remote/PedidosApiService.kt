package data.remote
import data.model.ApiResponse
import data.model.CrearPedidoRequest
import data.model.PedidoDTO
import retrofit2.Response
import retrofit2.http.*

interface PedidosApiService {

    /**
     * Crear un nuevo pedido
     */
    @POST("api/pedidos")
    suspend fun crearPedido(
        @Body pedido: CrearPedidoRequest
    ): Response<ApiResponse<PedidoDTO>>

    /**
     * Obtener todos los pedidos de un usuario
     */
    @GET("api/pedidos/usuario/{idUsuario}")
    suspend fun obtenerPedidosUsuario(
        @Path("idUsuario") idUsuario: Long
    ): Response<ApiResponse<List<PedidoDTO>>>

    /**
     * Obtener un pedido por ID
     */
    @GET("api/pedidos/{id}")
    suspend fun obtenerPedido(
        @Path("id") idPedido: Long
    ): Response<ApiResponse<PedidoDTO>>

    /**
     * Cancelar un pedido
     */
    @PUT("api/pedidos/{id}/cancelar")
    suspend fun cancelarPedido(
        @Path("id") idPedido: Long
    ): Response<ApiResponse<PedidoDTO>>

    /**
     * Actualizar estado del pedido (admin)
     */
    @PUT("api/pedidos/{id}/estado")
    suspend fun actualizarEstado(
        @Path("id") idPedido: Long,
        @Query("estado") nuevoEstado: String
    ): Response<ApiResponse<PedidoDTO>>
}
