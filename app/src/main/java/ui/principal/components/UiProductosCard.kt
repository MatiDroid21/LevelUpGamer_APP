package ui.principal.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

import model.Producto

@Composable
fun UiProductosCard(
    producto: Producto,
    onAgregar: (Producto) -> Unit
) {
    var agregado by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            // Imagen y texto...
            Text(producto.titulo)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    agregado = !agregado
                    onAgregar(producto)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (agregado) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (agregado) "Agregado" else "Agregar")
            }
        }
    }
}
