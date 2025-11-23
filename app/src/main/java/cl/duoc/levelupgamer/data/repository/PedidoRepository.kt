package data.repository

import cl.duoc.levelupgamer.data.remote.RetrofitClient
import data.model.CrearPedidoRequest
import data.model.PedidoDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PedidoRepository {

    private val apiService = RetrofitClient.pedidosService

    suspend fun crearPedido(pedido: CrearPedidoRequest): Result<PedidoDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.crearPedido(pedido)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Error al crear pedido"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun obtenerPedidosUsuario(idUsuario: Long): Result<List<PedidoDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerPedidosUsuario(idUsuario)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Error al obtener pedidos"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun obtenerPedido(idPedido: Long): Result<PedidoDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerPedido(idPedido)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Pedido no encontrado"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun cancelarPedido(idPedido: Long): Result<PedidoDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelarPedido(idPedido)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Error al cancelar pedido"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}