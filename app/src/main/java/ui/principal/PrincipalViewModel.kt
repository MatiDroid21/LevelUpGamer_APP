package ui.principal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import model.CarritoItem
import model.Producto
import model.productosDemo
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class PrincipalUiState(
    val email: String? = null,             // ← sin hardcode
    val loading: Boolean = false,
    val error: String? = null,
    val loggedOut: Boolean = false
)

class PrincipalViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() } // ← fuente de verdad

    private val _ui = MutableStateFlow(PrincipalUiState())
    val ui: StateFlow<PrincipalUiState> = _ui.asStateFlow()

    private val fuente: List<Producto> = productosDemo
    val categorias: List<String> = listOf("Todos") + fuente.map { it.categoria }.distinct()

    private val _categoriaSel = MutableStateFlow("Todos")
    val categoriaSel: StateFlow<String> = _categoriaSel.asStateFlow()

    private val _productosFiltrados = MutableStateFlow<List<Producto>>(emptyList())
    val productosFiltrados: StateFlow<List<Producto>> = _productosFiltrados.asStateFlow()

    private val _carrito = MutableStateFlow<Map<Int, CarritoItem>>(emptyMap())
    val carrito: StateFlow<List<CarritoItem>> =
        _carrito.map { it.values.sortedBy { ci -> ci.producto.titulo } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalItems: StateFlow<Int> =
        _carrito.map { it.values.sumOf { ci -> ci.cantidad } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalCLP: StateFlow<Int> =
        _carrito.map { it.values.sumOf { ci -> ci.cantidad * ci.producto.precioInt() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        refreshUserEmail() // ← sincroniza al crear el VM
    }

    fun refreshUserEmail() {
        _ui.update { it.copy(email = auth.currentUser?.email) }
    }

    fun cantidadDe(id: Int): Int = _carrito.value[id]?.cantidad ?: 0
    fun cantidadDeFlow(id: Int): Flow<Int> = _carrito.map { it[id]?.cantidad ?: 0 }

    fun agregarAlCarrito(p: Producto, delta: Int = 1) {
        _carrito.update { curr ->
            val item = curr[p.id]
            val nueva = (item?.cantidad ?: 0) + delta
            when {
                nueva <= 0 -> curr - p.id
                else -> curr + (p.id to CarritoItem(p, nueva))
            }
        }
    }
    fun quitarDelCarrito(p: Producto) = agregarAlCarrito(p, -1)
    fun setCantidad(p: Producto, cantidad: Int) = agregarAlCarrito(p, cantidad - cantidadDe(p.id))
    fun limpiarCarrito() { _carrito.value = emptyMap() }

    fun setCategoria(cat: String) { _categoriaSel.value = cat; aplicarFiltro() }

    fun cargarProductos() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try { aplicarFiltro() }
            catch (e: Exception) { _ui.update { it.copy(error = e.message ?: "Error al cargar productos") } }
            finally { _ui.update { it.copy(loading = false) } }
        }
    }

    fun refreshHome() { _categoriaSel.value = "Todos"; cargarProductos() }

    fun logout() {
        _ui.update { it.copy(loading = true) }
        viewModelScope.launch {
            // lógica real de logout
            _ui.update { it.copy(loading = false, loggedOut = true) }
        }
    }

    private fun aplicarFiltro() {
        val cat = _categoriaSel.value
        _productosFiltrados.value = if (cat == "Todos") fuente else fuente.filter { it.categoria == cat }
    }
}

private fun Producto.precioInt(): Int = precio.filter(Char::isDigit).toIntOrNull() ?: 0
fun Int.formateaCLP(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es","CL"))
    nf.currency = Currency.getInstance("CLP"); nf.maximumFractionDigits = 0
    return nf.format(this)
}
