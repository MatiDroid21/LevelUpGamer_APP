package ui.principal.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.Producto

@Composable
fun UiProductosCard(
    producto: Producto,
    qty: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .background(
                // Degradado gamer para fondo de tarjeta
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1A233A), Color(0xFF242851))
                )
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xEE222C44)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(producto.imagenRes),
                    contentDescription = producto.titulo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                    contentScale = ContentScale.Crop
                )
                if (qty > 0) {
                    Surface(
                        color = Color(0xFF42F5E3),
                        contentColor = Color.Black,
                        shape = MaterialTheme.shapes.small,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "x$qty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Text(
                text = producto.titulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF42F5E3),
                    fontWeight = FontWeight.ExtraBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!producto.descripcion.isNullOrBlank()) {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFB0B9D3)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = producto.precio,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF1BA1FF),
                        fontWeight = FontWeight.Bold
                    )
                )

                if (qty == 0) {
                    Button(
                        onClick = onIncrement,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1BA1FF),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.heightIn(min = 36.dp)
                    ) {
                        Text(
                            "Agregar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.animateContentSize()
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            enabled = qty > 1,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFF357A))
                        ) {
                            Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Quitar 1")
                        }
                        Text(
                            text = "$qty",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF42F5E3),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = onIncrement,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF1BA1FF))
                        ) {
                            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Agregar 1")
                        }
                    }
                }
            }
        }
    }
}
