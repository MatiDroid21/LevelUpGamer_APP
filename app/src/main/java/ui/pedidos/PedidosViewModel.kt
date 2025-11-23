package ui.pedidos

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.data.local.UserPreferences
import data.model.PedidoDTO
import data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PedidosUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class PedidosViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = PedidoRepository()
    private val userPrefs = UserPreferences(application)

    private val _ui = MutableStateFlow(PedidosUiState())
    val ui: StateFlow<PedidosUiState> = _ui.asStateFlow()

    private val _pedidos = MutableStateFlow<List<PedidoDTO>>(emptyList())
    val pedidos: StateFlow<List<PedidoDTO>> = _pedidos.asStateFlow()

    init {
        cargarPedidos()
    }

    fun cargarPedidos() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }

            val idUsuario = userPrefs.getIdUsuario()
            Log.d("PedidosVM", "👤 ID Usuario: $idUsuario")

            if (idUsuario == -1L) {
                Log.e("PedidosVM", "❌ Usuario no logueado")
                _ui.update {
                    it.copy(
                        loading = false,
                        error = "Debes iniciar sesión para ver tus pedidos"
                    )
                }
                return@launch
            }

            try {
                Log.d("PedidosVM", "📤 Obteniendo pedidos de API (puerto 8083)...")

                val result = repo.obtenerPedidosUsuario(idUsuario)

                result.fold(
                    onSuccess = { lista ->
                        Log.d("PedidosVM", "✅ ${lista.size} pedidos obtenidos")
                        _pedidos.value = lista.sortedByDescending { it.idPedido }
                        _ui.update { it.copy(loading = false) }
                    },
                    onFailure = { error ->
                        Log.e("PedidosVM", "❌ Error: ${error.message}")
                        _ui.update {
                            it.copy(
                                loading = false,
                                error = error.message ?: "Error al cargar pedidos"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Log.e("PedidosVM", "❌ Excepción: ${e.message}", e)
                _ui.update {
                    it.copy(
                        loading = false,
                        error = "Error al cargar pedidos: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelarPedido(idPedido: Long) {
        viewModelScope.launch {
            try {
                Log.d("PedidosVM", "🚫 Cancelando pedido #$idPedido")

                val result = repo.cancelarPedido(idPedido)

                result.fold(
                    onSuccess = {
                        Log.d("PedidosVM", "✅ Pedido cancelado")
                        cargarPedidos() // Recargar lista
                    },
                    onFailure = { error ->
                        Log.e("PedidosVM", "❌ Error al cancelar: ${error.message}")
                        _ui.update { it.copy(error = error.message ?: "Error al cancelar") }
                    }
                )

            } catch (e: Exception) {
                Log.e("PedidosVM", "❌ Excepción: ${e.message}", e)
                _ui.update { it.copy(error = "Error al cancelar: ${e.message}") }
            }
        }
    }
}
