package ui.recover

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarPasswordScreen(
    onBack: () -> Unit,
    onSent: () -> Unit,
    vm: RecuperarViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()

    // Navegación reactiva: si el correo fue enviado
    LaunchedEffect(state.sent) {
        if (state.sent) onSent()
    }

    // Snackbar de mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.messageConsumed()
        }
    }

    Scaffold(
        containerColor = Color(0xFF181840),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "RECUPERA TU ACCESO",
                        color = Color(0xFF42F5E3)
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Atrás", color = Color(0xFF42F5E3))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF222C44))
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF181840), Color(0xFF242851))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::onEmailChange,
                    label = { Text("Correo electrónico", color = Color(0xFF42F5E3)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1BA1FF),
                        unfocusedBorderColor = Color(0xFF42F5E3),
                        cursorColor = Color(0xFF42F5E3)
                    )
                )

                if (state.error != null) {
                    Text(state.error!!, color = Color(0xFFFF357A))
                }

                Button(
                    onClick = vm::sendReset,
                    enabled = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1BA1FF),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (state.loading) "Enviando..." else "Enviar correo de recuperación",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }

            if (state.loading) {
                CircularProgressIndicator(
                    color = Color(0xFF42F5E3),
                    modifier = Modifier.align(Alignment.Center).size(48.dp)
                )
            }
        }
    }
}
