package ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duoc.levelupgamer.R

@Composable
fun AnimatedLogo(
    modifier: Modifier = Modifier,
    logoRes: Int = R.drawable.logo,
    contentScale: ContentScale = ContentScale.Fit
) {
    // Entrada: pop + fade-in
    var appeared by remember { mutableStateOf(false) }
    val enterScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "enterScale"
    )
    val enterAlpha by animateFloatAsState(
        targetValue = if (appeared) 3f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "enterAlpha"
    )

    // Animaciones infinitas: pulso + balanceo
    val infinite = rememberInfiniteTransition(label = "logoInfinite")
    val pulse by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val tilt by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    // Feedback al toque (press)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "pressScale"
    )

    LaunchedEffect(Unit) { appeared = true }

    Image(
        painter = painterResource(id = logoRes),
        contentDescription = "Logo App",
        contentScale = contentScale,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .graphicsLayer {
                // escala total = entrada * pulso infinito * feedback de presión
                val finalScale = enterScale * pulse * pressScale
                scaleX = finalScale
                scaleY = finalScale
                rotationZ = tilt
                alpha = enterAlpha
            }
            // Para que el press funcione aunque no tenga onClick (solo feedback táctil visual)
            .then(Modifier) // placeholder para extender si luego lo haces clickeable
        ,
    )
}
