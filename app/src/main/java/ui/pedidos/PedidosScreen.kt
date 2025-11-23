package ui.pedidos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import data.model.PedidoDTO

import ui.principal.formateaCLP

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(
    vm: PedidosViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()
    val pedidos by vm.pedidos.collectAsState()

    LaunchedEffect(Unit) {
        vm.cargarPedidos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Pedidos") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            state.error ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.cargarPedidos() }) {
                            Text("Reintentar")
                        }
                    }
                }
                pedidos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No tienes pedidos aún",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pedidos, key = { it.idPedido ?: 0 }) { pedido ->
                            PedidoCard(
                                pedido = pedido,
                                onCancelar = { vm.cancelarPedido(pedido.idPedido ?: 0) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: PedidoDTO,
    onCancelar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pedido #${pedido.idPedido}",
                    style = MaterialTheme.typography.titleMedium
                )
                EstadoChip(pedido.estado)
            }

            // Información básica
            Text(
                "Fecha: ${pedido.fechaPedido ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                "Total: ${pedido.total.formateaCLP()}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // ⭐ AJUSTADO: Solo dirección (porque el backend tiene todo en un campo)
            Text(
                "Dirección: ${pedido.direccion}",
                style = MaterialTheme.typography.bodySmall
            )

            if (pedido.fechaEstimada != null) {
                Text(
                    "Entrega estimada: ${pedido.fechaEstimada}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider()

            // Items del pedido
            Text(
                "Items:",
                style = MaterialTheme.typography.labelMedium
            )

            // ⭐ AJUSTADO: detalles en lugar de items
            pedido.detalles.forEach { detalle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Producto #${detalle.idProducto} x${detalle.cantidad}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    val subtotal = detalle.precioUnitario * detalle.cantidad
                    Text(
                        subtotal.formateaCLP(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Botón de cancelar
            if (pedido.estado == "PENDIENTE") {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar pedido")
                }
            }
        }
    }
}

@Composable
fun EstadoChip(estado: String) {
    val (color, containerColor) = when (estado) {
        "PENDIENTE" -> MaterialTheme.colorScheme.onSecondaryContainer to MaterialTheme.colorScheme.secondaryContainer
        "CONFIRMADO" -> MaterialTheme.colorScheme.onPrimaryContainer to MaterialTheme.colorScheme.primaryContainer
        "ENVIADO" -> MaterialTheme.colorScheme.onTertiaryContainer to MaterialTheme.colorScheme.tertiaryContainer
        "ENTREGADO" -> MaterialTheme.colorScheme.onPrimary to MaterialTheme.colorScheme.primary
        "CANCELADO" -> MaterialTheme.colorScheme.onErrorContainer to MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            estado,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
