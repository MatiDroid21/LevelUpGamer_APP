package ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.home.components.AnimatedLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onRecoverClick: () -> Unit,
) {
    Scaffold(
        containerColor = Color(0xFF101020),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "LEVEL UP GAMER",
                        color = Color(0xFF42F5E3),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF181840))
            )
        }
    ) { inner ->
        HomeContent(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            onLoginClick = onLoginClick,
            onRegisterClick = onRegisterClick,
            onRecoverClick = onRecoverClick
        )
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onRecoverClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF181840), Color(0xFF1A233A))
                )
            )
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedLogo(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        )
        Text(
            "¡Bienvenido gamer!",
            color = Color(0xFF42F5E3),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 4.dp),
            // Si tienes fuente custom, agrégala aquí
        )
        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1BA1FF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp)
        ) {
            Text("Iniciar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onRegisterClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF42F5E3)
                )
            ) {
                Text("Registrarse", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = onRecoverClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFDB26ED)
                )
            ) {
                Text("Recuperar contraseña", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        onLoginClick = {},
        onRegisterClick = {},
        onRecoverClick = {}
    )
}
