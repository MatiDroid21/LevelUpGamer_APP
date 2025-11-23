package ui.principal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.data.local.UserPreferences
import data.model.ProductoDTO
import data.repository.ProductoRepository

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class PrincipalUiState(
    val email: String? = null,
    val nombre: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val loggedOut: Boolean = false
)

data class CarritoItem(
    val producto: ProductoDTO,
    val cantidad: Int
)

class PrincipalViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val productoRepo = ProductoRepository()

    private val _ui = MutableStateFlow(PrincipalUiState())
    val ui: StateFlow<PrincipalUiState> = _ui.asStateFlow()

    // ⭐ Productos desde la API
    private val _productos = MutableStateFlow<List<ProductoDTO>>(emptyList())

    // ⭐ Categorías dinámicas desde los productos
    val categorias: StateFlow<List<String>> = _productos.map { productos ->
        val cats = productos.mapNotNull { it.nombreCategoria }.distinct()
        listOf("Todos") + cats
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf("Todos"))

    private val _categoriaSel = MutableStateFlow("Todos")
    val categoriaSel: StateFlow<String> = _categoriaSel.asStateFlow()

    // ⭐ Productos filtrados por categoría
    val productosFiltrados: StateFlow<List<ProductoDTO>> = combine(
        _productos,
        _categoriaSel
    ) { productos, categoria ->
        if (categoria == "Todos") productos
        else productos.filter { it.nombreCategoria == categoria }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _carrito = MutableStateFlow<Map<Long, CarritoItem>>(emptyMap())

    val carrito: StateFlow<List<CarritoItem>> =
        _carrito.map { it.values.sortedBy { ci -> ci.producto.nombre } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalItems: StateFlow<Int> =
        _carrito.map { it.values.sumOf { ci -> ci.cantidad } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalCLP: StateFlow<Double> =
        _carrito.map { it.values.sumOf { ci -> ci.cantidad * ci.producto.precio } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    init {
        refreshUserEmail()
        cargarProductos() // ⭐ Carga automática al inicio
    }

    fun refreshUserEmail() {
        val email = userPrefs.getEmail()
        val nombre = userPrefs.getNombre()
        _ui.update { it.copy(email = email, nombre = nombre) }
    }

    fun cantidadDe(id: Long): Int = _carrito.value[id]?.cantidad ?: 0

    fun cantidadDeFlow(id: Long): Flow<Int> = _carrito.map { it[id]?.cantidad ?: 0 }

    fun agregarAlCarrito(p: ProductoDTO, delta: Int = 1) {
        val id = p.idProducto ?: return

        _carrito.update { curr ->
            val item = curr[id]
            val nuevaCantidad = (item?.cantidad ?: 0) + delta

            when {
                nuevaCantidad <= 0 -> curr - id
                nuevaCantidad > p.stock -> curr // No agregar más del stock disponible
                else -> curr + (id to CarritoItem(p, nuevaCantidad))
            }
        }
    }

    fun quitarDelCarrito(p: ProductoDTO) = agregarAlCarrito(p, -1)

    fun setCantidad(p: ProductoDTO, cantidad: Int) {
        val id = p.idProducto ?: return
        val actual = cantidadDe(id)
        agregarAlCarrito(p, cantidad - actual)
    }

    fun limpiarCarrito() {
        _carrito.value = emptyMap()
    }

    fun setCategoria(cat: String) {
        _categoriaSel.value = cat
    }

    // ⭐ NUEVO: Carga productos desde la API
    fun cargarProductos() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }

            // Cargar solo productos disponibles (con stock > 0)
            val result = productoRepo.obtenerProductosDisponibles()

            result.fold(
                onSuccess = { productos ->
                    _productos.value = productos
                    _ui.update { it.copy(loading = false) }
                },
                onFailure = { error ->
                    _ui.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "Error al cargar productos"
                        )
                    }
                }
            )
        }
    }

    fun refreshHome() {
        _categoriaSel.value = "Todos"
        cargarProductos()
    }

    fun logout() {
        _ui.update { it.copy(loading = true) }
        viewModelScope.launch {
            userPrefs.clearUser()
            _ui.update {
                it.copy(
                    loading = false,
                    loggedOut = true,
                    email = null,
                    nombre = null
                )
            }
        }
    }
}

// ⭐ Extensión para formatear precios
fun Double.formateaCLP(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es","CL"))
    nf.currency = Currency.getInstance("CLP")
    nf.maximumFractionDigits = 0
    return nf.format(this)
}
