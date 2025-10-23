package ui.principal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.Producto
import model.productosDemo

data class PrincipalUiState(
    val email: String? = "usuario@demo.com",
    val loading: Boolean = false,
    val error: String? = null,
    val loggedOut: Boolean = false
)

class PrincipalViewModel : ViewModel() {

    // ---------- Estado general ----------
    private val _ui = MutableStateFlow(PrincipalUiState())
    val ui: StateFlow<PrincipalUiState> = _ui.asStateFlow()

    // ---------- Fuente y filtros ----------
    private val fuente: List<Producto> = productosDemo

    val categorias: List<String> = listOf("Todos") + fuente.map { it.categoria }.distinct()

    private val _categoriaSel = MutableStateFlow("Todos")
    val categoriaSel: StateFlow<String> = _categoriaSel.asStateFlow()

    private val _productosFiltrados = MutableStateFlow<List<Producto>>(emptyList())
    val productosFiltrados: StateFlow<List<Producto>> = _productosFiltrados.asStateFlow()

    // ---------- Carrito ----------
    private val _carrito = MutableStateFlow<List<Producto>>(emptyList())
    val carrito: StateFlow<List<Producto>> = _carrito.asStateFlow()

    fun agregarAlCarrito(p: Producto) {
        _carrito.update { it + p }
    }

    fun quitarDelCarrito(p: Producto) {
        _carrito.update { it.filter { it.id != p.id } }
    }

    fun limpiarCarrito() {
        _carrito.value = emptyList()
    }

    // ---------- Acciones ----------
    fun setCategoria(cat: String) {
        _categoriaSel.value = cat
        aplicarFiltro()
    }

    /** Carga/recarga la grilla (desde productosDemo). */
    fun cargarProductos() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                aplicarFiltro()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message ?: "Error al cargar productos")
            } finally {
                _ui.value = _ui.value.copy(loading = false)
            }
        }
    }

    /** Reset al tocar Inicio: categoría base + recarga. */
    fun refreshHome() {
        _categoriaSel.value = "Todos"
        cargarProductos()
    }

    fun logout() {
        _ui.value = _ui.value.copy(loading = true)
        viewModelScope.launch {
            // Tu lógica real de logout iría aquí
            _ui.value = _ui.value.copy(loading = false, loggedOut = true)
        }
    }

    // ---------- Helper ----------
    private fun aplicarFiltro() {
        val cat = _categoriaSel.value
        _productosFiltrados.value = if (cat == "Todos") {
            fuente
        } else {
            fuente.filter { it.categoria == cat }
        }
    }
}
