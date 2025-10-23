package ui.recordatorio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import model.Recordatorio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatorioScreen(vm: RecordatorioViewModel) {
    val state by vm.ui.collectAsState()
    val focus = LocalFocusManager.current

    LaunchedEffect(state.error) {
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Recordatorios", style = MaterialTheme.typography.headlineSmall)
        Text("Usuario: ${state.uid}")

        // Formulario
        OutlinedTextField(
            value = state.mensaje,
            onValueChange = vm::onMensajeChange,
            label = { Text("Mensaje") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    vm.guardar()
                    focus.clearFocus()
                },
                enabled = !state.loading
            ) {
                Text(if (state.editingId == null) "Guardar" else "Actualizar")
            }
            OutlinedButton(onClick = { vm.onNuevo(); focus.clearFocus() }, enabled = !state.loading) {
                Text("Nuevo")
            }
        }

        if (state.error != null) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
        }

        Divider()

        // Listado
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay recordatorios")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(item.message, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Creado: ${item.createdAt}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onEdit(item) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Editar")
                }
                OutlinedButton(onClick = { onDelete(item) }, colors = ButtonDefaults.outlinedButtonColors()) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}
