package ui.principal.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.levelupgamer.data.local.UserPreferences

import ui.principal.PrincipalViewModel
import ui.principal.formateaCLP

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    vm: PrincipalViewModel,
    onFinished: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val cvm: CheckoutViewModel = viewModel(
        key = "checkout_vm",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val userPrefs = UserPreferences(context)
                return CheckoutViewModel(vm, userPrefs) as T
            }
        }
    )

    val state by cvm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloPaso(state.step)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.step == CheckoutStep.Resumen) onClose() else cvm.back()
                    }) { Icon(Icons.Outlined.Close, contentDescription = null) }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.step != CheckoutStep.Resumen && state.step != CheckoutStep.Exito) {
                    OutlinedButton(onClick = { cvm.back() }, modifier = Modifier.weight(1f)) { Text("Atrás") }
                }
                when (state.step) {
                    CheckoutStep.Resumen -> {
                        Button(
                            onClick = { cvm.goTo(CheckoutStep.Envio) },
                            modifier = Modifier.weight(1f),
                            enabled = state.items.isNotEmpty()
                        ) { Text("Ir a envío") }
                    }
                    CheckoutStep.Envio -> {
                        val envioValido = remember(state.address, state.shippingMethod) {
                            state.address.nombre.isNotBlank() &&
                                    state.address.telefono.count { it.isDigit() } >= 8 &&
                                    state.address.direccion.isNotBlank() &&
                                    state.address.comuna.isNotBlank() &&
                                    state.address.region.isNotBlank()
                        }
                        Button(
                            onClick = { cvm.goTo(CheckoutStep.Pago) },
                            modifier = Modifier.weight(1f),
                            enabled = envioValido
                        ) { Text("Ir a pago") }
                    }
                    CheckoutStep.Pago -> {
                        Button(
                            onClick = { cvm.goTo(CheckoutStep.Confirmar) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Revisar") }
                    }
                    CheckoutStep.Confirmar -> {
                        Button(
                            onClick = { cvm.placeOrder() },
                            modifier = Modifier.weight(1f),
                            enabled = !state.placing
                        ) {
                            if (state.placing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Confirmar compra")
                            }
                        }
                    }
                    CheckoutStep.Exito -> {
                        Button(onClick = onFinished, modifier = Modifier.weight(1f)) { Text("Finalizar") }
                    }
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .imePadding()
                .fillMaxSize()
        ) {
            LinearProgressIndicator(
                progress = { progresoPaso(state.step) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (state.step) {
                CheckoutStep.Resumen -> PasoResumen(state)
                CheckoutStep.Envio -> PasoEnvio(
                    state = state,
                    onChange = cvm::updateAddress,
                    onShipping = cvm::setShippingMethod
                )
                CheckoutStep.Pago -> PasoPago(
                    state = state,
                    onPayment = cvm::setPaymentMethod
                )
                CheckoutStep.Confirmar -> PasoConfirmar(state)
                CheckoutStep.Exito -> PasoExito(state, onFinished)
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun PasoResumen(state: CheckoutUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.items, key = { it.producto.idProducto ?: 0 }) { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${item.producto.nombre} x${item.cantidad}",
                    style = MaterialTheme.typography.bodyLarge
                )
                val subtotal = item.producto.precio * item.cantidad
                Text(subtotal.formateaCLP(), style = MaterialTheme.typography.bodyLarge)
            }
        }
        item {
            HorizontalDivider()
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal")
                    Text(state.subtotal.formateaCLP())
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Envío")
                    Text(state.shipping.formateaCLP())
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Impuestos")
                    Text(state.tax.formateaCLP())
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text(state.total.formateaCLP(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun PasoEnvio(
    state: CheckoutUiState,
    onChange: ((ShippingAddress) -> ShippingAddress) -> Unit,
    onShipping: (ShippingMethod) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.address.nombre,
                onValueChange = { v -> onChange { it.copy(nombre = v) } },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.address.telefono,
                onValueChange = { v -> onChange { it.copy(telefono = v) } },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.address.direccion,
                onValueChange = { v -> onChange { it.copy(direccion = v) } },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.address.comuna,
                onValueChange = { v -> onChange { it.copy(comuna = v) } },
                label = { Text("Comuna") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.address.region,
                onValueChange = { v -> onChange { it.copy(region = v) } },
                label = { Text("Región") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Método de envío", style = MaterialTheme.typography.titleMedium)
        }
        items(ShippingMethod.entries.toList()) { m ->
            FilterChip(
                selected = state.shippingMethod == m,
                onClick = { onShipping(m) },
                label = { Text(m.label) }
            )
        }
    }
}

@Composable
private fun PasoPago(
    state: CheckoutUiState,
    onPayment: (PaymentMethod) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val datosTransferencia = """
        Banco: Banco Ejemplo
        Tipo de cuenta: Cuenta Vista
        N° de cuenta: 12-345-6789
        Titular: LevelUp Gamer SPA
        RUT: 12.345.678-9
        Correo: pagos@levelupgamer.cl
        Monto: ${state.total.formateaCLP()}
        Asunto/Referencia: Orden pendiente
    """.trimIndent()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Elige cómo pagar", style = MaterialTheme.typography.titleMedium) }
        items(PaymentMethod.entries.toList()) { m ->
            FilterChip(
                selected = state.paymentMethod == m,
                onClick = { onPayment(m) },
                label = { Text(m.label) }
            )
        }

        if (state.paymentMethod == PaymentMethod.Transferencia) {
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { Text("Instrucciones de transferencia", style = MaterialTheme.typography.titleMedium) }
            item { Text(datosTransferencia, style = MaterialTheme.typography.bodySmall) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { clipboard.setText(AnnotatedString(datosTransferencia)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copiar datos")
                    }
                }
            }
        } else {
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item {
                Text(
                    "Pagarás al recibir tu pedido en la dirección indicada. Aceptamos efectivo y/o transferencia al momento de la entrega.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PasoConfirmar(state: CheckoutUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Text("Revisa tu pedido", style = MaterialTheme.typography.titleMedium) }
        item { Text("Envío: ${state.shippingMethod.label}") }
        item { Text("Pago: ${state.paymentMethod.label}") }
        item { Text("Destinatario: ${state.address.nombre}") }
        item { Text("Dirección: ${state.address.direccion}, ${state.address.comuna}, ${state.address.region}") }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleLarge)
                Text(state.total.formateaCLP(), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun PasoExito(state: CheckoutUiState, onFinished: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("¡Compra realizada con éxito!", style = MaterialTheme.typography.headlineSmall)
            Text("N° orden: ${state.orderId ?: "-"}")
            Button(onClick = onFinished, modifier = Modifier.padding(top = 8.dp)) {
                Text("Volver al inicio")
            }
        }
    }
}

private fun tituloPaso(step: CheckoutStep): String = when (step) {
    CheckoutStep.Resumen -> "Resumen"
    CheckoutStep.Envio -> "Datos de envío"
    CheckoutStep.Pago -> "Pago"
    CheckoutStep.Confirmar -> "Confirmación"
    CheckoutStep.Exito -> "Completado"
}

private fun progresoPaso(step: CheckoutStep): Float = when (step) {
    CheckoutStep.Resumen -> 0.2f
    CheckoutStep.Envio -> 0.4f
    CheckoutStep.Pago -> 0.6f
    CheckoutStep.Confirmar -> 0.8f
    CheckoutStep.Exito -> 1f
}
