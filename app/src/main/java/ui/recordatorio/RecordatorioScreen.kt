package ui.recordatorio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import model.Recordatorio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatorioScreen(vm: RecordatorioViewModel) {
    val state by vm.ui.collectAsState()
    val focus = LocalFocusManager.current

    LaunchedEffect(state.error) {}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF181840), Color(0xFF242851))
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Recordatorios",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF42F5E3)
        )
        Text(
            "Usuario: ${state.uid}",
            color = Color(0xFFB0B9D3),
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = state.mensaje,
            onValueChange = vm::onMensajeChange,
            label = { Text("Mensaje", color = Color(0xFF42F5E3)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1BA1FF),
                unfocusedBorderColor = Color(0xFF42F5E3),
                cursorColor = Color(0xFF42F5E3)
            )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    vm.guardar()
                    focus.clearFocus()
                },
                enabled = !state.loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BA1FF))
            ) {
                Text(
                    if (state.editingId == null) "Guardar" else "Actualizar",
                    color = Color.White
                )
            }
            OutlinedButton(
                onClick = { vm.onNuevo(); focus.clearFocus() },
                enabled = !state.loading,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF42F5E3))
            ) {
                Text("Nuevo")
            }
        }

        state.error?.let {
            Text(it, color = Color(0xFFFF357A))
        }

        Divider(color = Color(0x661BA1FF))

        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay recordatorios", color = Color(0xFFDB26ED))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    ReminderItem(
                        item = item,
                        onEdit = vm::onEditar,
                        onDelete = vm::eliminar
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderItem(
    item: Recordatorio,
    onEdit: (Recordatorio) -> Unit,
    onDelete: (Recordatorio) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xEE222C44))
    ) {
        Column(
            Modifier.padding(14.dp)
        ) {
            Text(
                item.message,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF42F5E3)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Creado: ${item.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB0B9D3)
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onEdit(item) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDB26ED))
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Editar")
                }
                OutlinedButton(
                    onClick = { onDelete(item) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF357A))
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}
