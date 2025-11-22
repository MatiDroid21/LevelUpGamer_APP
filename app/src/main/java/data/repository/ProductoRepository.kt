package data.repository

import cl.duoc.levelupgamer.data.remote.RetrofitClient
import data.model.CategoriaDTO
import data.model.ProductoDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductoRepository {

    private val apiService = RetrofitClient.productosService

    suspend fun listarProductos(): Result<List<ProductoDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.listarProductos()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Error al obtener productos"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    
    suspend fun obtenerProducto(id: Long): Result<ProductoDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerProducto(id)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Producto no encontrado"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /*Obtener segun cattegoria, que hasta el momento será una función
    * a implementar a futuro.*/
    suspend fun obtenerPorCategoria(idCategoria: Long): Result<List<ProductoDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerPorCategoria(idCategoria)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Error al obtener productos"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /*El siguiente bloque es para traer hacia la app todos los productos
    * que sigan con stock en  la bd*/
    suspend fun obtenerProductosDisponibles(): Result<List<ProductoDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerProductosDisponibles()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Error al obtener productos disponibles"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /*bloque para listar categorias, que hasta el momento será una función
    * a implementar a futuro.*/
    suspend fun listarCategorias(): Result<List<CategoriaDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.listarCategorias()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(Exception("Error al obtener categorías"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}