package ui.principal.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ui.principal.PrincipalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    vm: PrincipalViewModel,
    onFinished: () -> Unit,
    onClose: () -> Unit
) {
    val cvm: CheckoutViewModel = viewModel(
        key = "checkout_vm",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CheckoutViewModel(vm) as T
            }
        }
    )

    val state by cvm.ui.collectAsState()

    Scaffold(
        containerColor = Color(0xFF181840),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        tituloPaso(state.step),
                        color = Color(0xFF42F5E3),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.step == CheckoutStep.Resumen) onClose() else cvm.back()
                    }) { Icon(Icons.Outlined.Close, contentDescription = null, tint = Color(0xFFFF357A)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF222C44))
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222C44))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.step != CheckoutStep.Resumen && state.step != CheckoutStep.Exito) {
                    OutlinedButton(
                        onClick = { cvm.back() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF42F5E3))
                    ) { Text("Atrás") }
                }
                when (state.step) {
                    CheckoutStep.Resumen -> {
                        Button(
                            onClick = { cvm.goTo(CheckoutStep.Envio) },
                            modifier = Modifier.weight(1f),
                            enabled = state.items.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BA1FF))
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
                            enabled = envioValido,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BA1FF))
                        ) { Text("Ir a pago") }
                    }
                    CheckoutStep.Pago -> {
                        Button(
                            onClick = { cvm.goTo(CheckoutStep.Confirmar) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB26ED))
                        ) { Text("Revisar") }
                    }
                    CheckoutStep.Confirmar -> {
                        Button(
                            onClick = { cvm.placeOrder() },
                            modifier = Modifier.weight(1f),
                            enabled = !state.placing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42F5E3), contentColor = Color.Black)
                        ) {
                            if (state.placing) {
                                CircularProgressIndicator(
                                    color = Color(0xFF42F5E3),
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Confirmar compra")
                            }
                        }
                    }
                    CheckoutStep.Exito -> {
                        Button(
                            onClick = onFinished,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BA1FF))
                        ) { Text("Finalizar") }
                    }
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF181840), Color(0xFF242851))
                    )
                )
                .imePadding()
        ) {
            LinearProgressIndicator(
                progress = progresoPaso(state.step),
                color = Color(0xFF42F5E3),
                trackColor = Color(0x773C4771),
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
                Text(it, color = Color(0xFFFF357A), modifier = Modifier.padding(16.dp))
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
        items(state.items, key = { it.producto.id }) { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${item.producto.titulo} x${item.cantidad}",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
                val subtotal = (item.producto.precio.filter(Char::isDigit).toIntOrNull() ?: 0) * item.cantidad
                Text(
                    "$subtotal CLP",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF1BA1FF))
                )
            }
        }
        item {
            Divider(color = Color(0x66FFFFFF))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = Color(0xFF42F5E3)); Text("${state.subtotal} CLP", color = Color(0xFFB0B9D3))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Envío", color = Color(0xFF42F5E3)); Text("${state.shipping} CLP", color = Color(0xFFB0B9D3))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Impuestos", color = Color(0xFF42F5E3)); Text("${state.tax} CLP", color = Color(0xFFB0B9D3))
                }
                Divider(color = Color(0x66FFFFFF))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF42F5E3)))
                    Text("${state.total} CLP", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1BA1FF)))
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
                label = { Text("Nombre", color = Color(0xFF42F5E3)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1BA1FF),
                    unfocusedBorderColor = Color(0xFF42F5E3),
                    cursorColor = Color(0xFF42F5E3)
                )
            )
        }
        item {
            OutlinedTextField(
                value = state.address.telefono,
                onValueChange = { v -> onChange { it.copy(telefono = v) } },
                label = { Text("Teléfono", color = Color(0xFF42F5E3)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1BA1FF),
                    unfocusedBorderColor = Color(0xFF42F5E3),
                    cursorColor = Color(0xFF42F5E3)
                )
            )
        }
        item {
            OutlinedTextField(
                value = state.address.direccion,
                onValueChange = { v -> onChange { it.copy(direccion = v) } },
                label = { Text("Dirección", color = Color(0xFF42F5E3)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1BA1FF),
                    unfocusedBorderColor = Color(0xFF42F5E3),
                    cursorColor = Color(0xFF42F5E3)
                )
            )
        }
        item {
            OutlinedTextField(
                value = state.address.comuna,
                onValueChange = { v -> onChange { it.copy(comuna = v) } },
                label = { Text("Comuna", color = Color(0xFF42F5E3)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1BA1FF),
                    unfocusedBorderColor = Color(0xFF42F5E3),
                    cursorColor = Color(0xFF42F5E3)
                )
            )
        }
        item {
            OutlinedTextField(
                value = state.address.region,
                onValueChange = { v -> onChange { it.copy(region = v) } },
                label = { Text("Región", color = Color(0xFF42F5E3)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1BA1FF),
                    unfocusedBorderColor = Color(0xFF42F5E3),
                    cursorColor = Color(0xFF42F5E3)
                )
            )
        }
        item {
            Text("Método de envío", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFDB26ED)))
        }
        items(ShippingMethod.values().toList()) { m ->
            FilterChip(
                selected = state.shippingMethod == m,
                onClick = { onShipping(m) },
                label = {
                    Text(
                        m.label,
                        color = if (state.shippingMethod == m) Color(0xFF42F5E3) else Color.White
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x441BA1FF),
                    containerColor = Color(0x80181940)
                )
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
        Monto: ${state.total} CLP
        Asunto/Referencia: Orden pendiente
    """.trimIndent()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Elige cómo pagar", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFDB26ED))) }
        items(PaymentMethod.values().toList()) { m ->
            FilterChip(
                selected = state.paymentMethod == m,
                onClick = { onPayment(m) },
                label = {
                    Text(
                        m.label,
                        color = if (state.paymentMethod == m) Color(0xFF42F5E3) else Color.White
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x441BA1FF),
                    containerColor = Color(0x80181940)
                )
            )
        }

        if (state.paymentMethod == PaymentMethod.Transferencia) {
            item { Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFF42F5E3)) }
            item { Text("Instrucciones de transferencia", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF42F5E3))) }
            item { Text(datosTransferencia, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB0B9D3))) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { clipboard.setText(AnnotatedString(datosTransferencia)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1BA1FF))
                    ) { Text("Copiar datos") }
                }
            }
        } else {
            item { Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFF42F5E3)) }
            item {
                Text(
                    "Pagarás al recibir tu pedido en la dirección indicada. Aceptamos efectivo y/o transferencia al momento de la entrega.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB0B9D3))
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
        item { Text("Revisa tu pedido", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF42F5E3))) }
        item { Text("Envío: ${state.shippingMethod.label}", color = Color.White) }
        item { Text("Pago: ${state.paymentMethod.label}", color = Color.White) }
        item { Text("Destinatario: ${state.address.nombre}", color = Color.White) }
        item { Text("Dirección: ${state.address.direccion}, ${state.address.comuna}, ${state.address.region}", color = Color.White) }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF1BA1FF)))
                Text("${state.total} CLP", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF42F5E3)))
            }
        }
    }
}

@Composable
private fun PasoExito(state: CheckoutUiState, onFinished: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("¡Compra realizada con éxito!", style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xFF42F5E3)))
            Text("N° orden: ${state.orderId ?: "-"}", color = Color.White)
            Button(
                onClick = onFinished,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BA1FF))
            ) { Text("Volver al inicio") }
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
