package ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.levelupgamer.data.local.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    userPrefs: UserPreferences
) {
    LaunchedEffect(Unit) {
        delay(1500) // Esperar 1.5 segundos

        // Verificar sesión
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val hasLocalSession = userPrefs.isLoggedIn()

        if (firebaseUser != null && hasLocalSession) {
            // Tiene sesión activa → ir a Home
            onNavigateToHome()
        } else {
            // No tiene sesión → ir a Login
            onNavigateToLogin()
        }
    }

    // UI del Splash
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Level Up Gamer",
                style = MaterialTheme.typography.headlineLarge
            )
            CircularProgressIndicator()
        }
    }
}
