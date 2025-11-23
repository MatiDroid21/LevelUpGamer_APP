package ui.principal.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.levelupgamer.data.local.UserPreferences
import cl.duoc.levelupgamer.data.remote.RetrofitClient
import data.model.CrearPedidoRequest
import data.model.PedidoDetalleDTO
import data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ui.principal.CarritoItem
import ui.principal.PrincipalViewModel

enum class CheckoutStep { Resumen, Envio, Pago, Confirmar, Exito }

enum class ShippingMethod(val label: String, val costo: Double) {
    Retiro("Retiro en tienda", 0.0),
    Normal("Envío normal (3-5 días)", 3990.0),
    Express("Envío express (24-48h)", 6990.0)
}

enum class PaymentMethod(val label: String) {
    ContraEntrega("Paga al recibir"),
    Transferencia("Transferencia bancaria")
}

data class ShippingAddress(
    val nombre: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val comuna: String = "",
    val region: String = ""
)

data class CheckoutUiState(
    val step: CheckoutStep = CheckoutStep.Resumen,
    val items: List<CarritoItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shipping: Double = ShippingMethod.Normal.costo,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val shippingMethod: ShippingMethod = ShippingMethod.Normal,
    val paymentMethod: PaymentMethod = PaymentMethod.ContraEntrega,
    val address: ShippingAddress = ShippingAddress(),
    val placing: Boolean = false,
    val orderId: String? = null,
    val error: String? = null
)

class CheckoutViewModel(
    private val principal: PrincipalViewModel,
    private val userPrefs: UserPreferences
) : ViewModel() {

    private val _ui = MutableStateFlow(CheckoutUiState())
    val ui: StateFlow<CheckoutUiState> = _ui.asStateFlow()

    private val pedidoRepo = PedidoRepository()
    private val usuariosApi = RetrofitClient.usuariosService

    init {
        syncFromCart()
    }

    private fun syncFromCart() {
        val items = principal.carrito.value
        val subtotal = items.sumOf { it.producto.precio * it.cantidad }
        val shipping = _ui.value.shippingMethod.costo
        val tax = 0.0
        val total = subtotal + shipping + tax
        _ui.update { it.copy(items = items, subtotal = subtotal, shipping = shipping, tax = tax, total = total) }
    }

    fun goTo(step: CheckoutStep) {
        Log.d("CheckoutVM", "📍 Navegando a: $step")
        _ui.update { it.copy(step = step, error = null) }
    }

    fun next() = goTo(
        when (_ui.value.step) {
            CheckoutStep.Resumen -> CheckoutStep.Envio
            CheckoutStep.Envio -> CheckoutStep.Pago
            CheckoutStep.Pago -> CheckoutStep.Confirmar
            CheckoutStep.Confirmar -> CheckoutStep.Exito
            CheckoutStep.Exito -> CheckoutStep.Exito
        }
    )

    fun back() = goTo(
        when (_ui.value.step) {
            CheckoutStep.Resumen -> CheckoutStep.Resumen
            CheckoutStep.Envio -> CheckoutStep.Resumen
            CheckoutStep.Pago -> CheckoutStep.Envio
            CheckoutStep.Confirmar -> CheckoutStep.Pago
            CheckoutStep.Exito -> CheckoutStep.Confirmar
        }
    )

    fun setShippingMethod(m: ShippingMethod) {
        _ui.update {
            val total = it.subtotal + m.costo + it.tax
            it.copy(shippingMethod = m, shipping = m.costo, total = total, error = null)
        }
    }

    fun updateAddress(block: (ShippingAddress) -> ShippingAddress) {
        _ui.update { it.copy(address = block(it.address), error = null) }
    }

    fun validarEnvio(): Boolean {
        val a = _ui.value.address
        val ok = a.nombre.isNotBlank() &&
                a.telefono.count { it.isDigit() } >= 8 &&
                a.direccion.isNotBlank() &&
                a.comuna.isNotBlank() &&
                a.region.isNotBlank()
        if (!ok) _ui.update { it.copy(error = "Completa los datos de envío") }
        return ok
    }

    fun setPaymentMethod(m: PaymentMethod) {
        _ui.update { it.copy(paymentMethod = m, error = null) }
    }

    // ⭐ OBTENER ID DEL USUARIO (desde SharedPreferences o API)
    private suspend fun obtenerIdUsuario(): Long? {
        // Intento 1: Desde SharedPreferences
        val idUsuario = userPrefs.getIdUsuario()
        if (idUsuario != -1L) {
            Log.d("CheckoutVM", "✅ ID desde SharedPreferences: $idUsuario")
            return idUsuario
        }

        // Intento 2: Desde API usando el email
        val email = userPrefs.getEmail()
        if (email != null) {
            Log.d("CheckoutVM", "🔍 ID no encontrado, buscando por email: $email")
            try {
                val response = usuariosApi.obtenerPorEmail(email)
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!
                    Log.d("CheckoutVM", "✅ Usuario encontrado: ${usuario.idUsuario}")

                    // Guardar ID en SharedPreferences para próximas veces
                    if (usuario.idUsuario != null) {
                        userPrefs.saveIdUsuario(usuario.idUsuario)
                    }

                    return usuario.idUsuario
                } else {
                    Log.e("CheckoutVM", "❌ Usuario no encontrado en API")
                }
            } catch (e: Exception) {
                Log.e("CheckoutVM", "❌ Error al buscar usuario: ${e.message}")
            }
        }

        Log.e("CheckoutVM", "❌ No se pudo obtener ID del usuario")
        return null
    }

    fun placeOrder() {
        Log.d("CheckoutVM", "🔵 placeOrder() INICIADO")

        val s = _ui.value

        if (s.items.isEmpty()) {
            Log.e("CheckoutVM", "❌ Carrito vacío")
            _ui.update { it.copy(error = "El carrito está vacío") }
            return
        }

        if (!validarEnvio()) {
            Log.e("CheckoutVM", "❌ Validación falló")
            return
        }

        Log.d("CheckoutVM", "✅ Validaciones OK, procesando...")

        viewModelScope.launch {
            _ui.update { it.copy(placing = true, error = null) }

            try {
                // ⭐ OBTENER ID DEL USUARIO (funciona con Firebase y API)
                val idUsuario = obtenerIdUsuario()

                if (idUsuario == null) {
                    Log.e("CheckoutVM", "❌ No se pudo obtener ID del usuario")
                    _ui.update {
                        it.copy(
                            placing = false,
                            error = "Error: No se pudo identificar al usuario. Intenta cerrar sesión y volver a entrar."
                        )
                    }
                    return@launch
                }

                Log.d("CheckoutVM", "👤 ID Usuario: $idUsuario")

                // Crear detalles
                val detalles = s.items.map { item ->
                    PedidoDetalleDTO(
                        idProducto = item.producto.idProducto ?: 0,
                        cantidad = item.cantidad,
                        precioUnitario = item.producto.precio
                    )
                }

                // Dirección completa
                val direccionCompleta = buildString {
                    append(s.address.direccion)
                    append(", ${s.address.comuna}")
                    append(", ${s.address.region}")
                    append(" | ")
                    append("Destinatario: ${s.address.nombre}")
                    append(" | Tel: ${s.address.telefono}")
                    append(" | Envío: ${s.shippingMethod.label}")
                    append(" | Pago: ${s.paymentMethod.label}")
                }

                // Request
                val pedidoRequest = CrearPedidoRequest(
                    idUsuario = idUsuario,
                    total = s.total,
                    direccion = direccionCompleta,
                    detalles = detalles
                )

                Log.d("CheckoutVM", "📤 Request: $pedidoRequest")

                // Enviar a la API
                val result = pedidoRepo.crearPedido(pedidoRequest)

                result.fold(
                    onSuccess = { pedido ->
                        Log.d("CheckoutVM", "✅ Pedido creado: ${pedido.idPedido}")
                        _ui.update {
                            it.copy(
                                placing = false,
                                orderId = pedido.idPedido?.toString() ?: "N/A",
                                step = CheckoutStep.Exito
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("CheckoutVM", "❌ Error: ${error.message}", error)
                        _ui.update {
                            it.copy(
                                placing = false,
                                error = error.message ?: "Error al crear pedido"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Log.e("CheckoutVM", "❌ Excepción: ${e.message}", e)
                e.printStackTrace()
                _ui.update {
                    it.copy(
                        placing = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
}
