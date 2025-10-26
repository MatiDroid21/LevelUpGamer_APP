// app/src/main/java/ui/principal/checkout/CheckoutViewModel.kt
package ui.principal.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.CarritoItem
import ui.principal.PrincipalViewModel

enum class CheckoutStep { Resumen, Envio, Pago, Confirmar, Exito }

enum class ShippingMethod(val label: String, val costo: Int) {
    Retiro("Retiro en tienda", 0),
    Normal("Envío normal (3-5 días)", 3990),
    Express("Envío express (24-48h)", 6990)
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
    val subtotal: Int = 0,
    val shipping: Int = ShippingMethod.Normal.costo,
    val tax: Int = 0,
    val total: Int = 0,
    val shippingMethod: ShippingMethod = ShippingMethod.Normal,
    val paymentMethod: PaymentMethod = PaymentMethod.ContraEntrega,
    val address: ShippingAddress = ShippingAddress(),
    val placing: Boolean = false,
    val orderId: String? = null,
    val error: String? = null
)

class CheckoutViewModel(
    private val principal: PrincipalViewModel
) : ViewModel() {

    private val _ui = MutableStateFlow(CheckoutUiState())
    val ui: StateFlow<CheckoutUiState> = _ui.asStateFlow()

    init {
        syncFromCart()
        // Si quieres recalcular si cambian cantidades durante el checkout, descomenta:
        /*
        viewModelScope.launch {
            principal.carrito.collect { syncFromCart() }
        }
        */
    }

    private fun syncFromCart() {
        val items = principal.carrito.value
        val subtotal = items.sumOf { (it.producto.precio.filter(Char::isDigit).toIntOrNull() ?: 0) * it.cantidad }
        val shipping = _ui.value.shippingMethod.costo
        val tax = 0
        val total = subtotal + shipping + tax
        _ui.update { it.copy(items = items, subtotal = subtotal, shipping = shipping, tax = tax, total = total) }
    }

    // Navegación
    fun goTo(step: CheckoutStep) { _ui.update { it.copy(step = step, error = null) } }

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

    // Envío
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

    // Pago
    fun setPaymentMethod(m: PaymentMethod) {
        _ui.update { it.copy(paymentMethod = m, error = null) }
    }

    // Confirmación
    fun placeOrder() {
        val s = _ui.value
        if (s.items.isEmpty()) { _ui.update { it.copy(error = "El carrito está vacío") }; return }
        if (!validarEnvio()) return

        viewModelScope.launch {
            _ui.update { it.copy(placing = true, error = null) }
            // Simula procesamiento
            delay(800)
            val generatedId = "ORD-" + System.currentTimeMillis().toString().takeLast(6)
            _ui.update { it.copy(placing = false, orderId = generatedId, step = CheckoutStep.Exito) }
        }
    }
}
