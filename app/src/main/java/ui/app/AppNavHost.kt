package ui.app

import ui.login.LoginScreen
import ui.principal.PrincipalScreen
import ui.recover.RecuperarPasswordScreen
import ui.register.RegistrarseScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.levelupgamer.data.local.UserPreferences
import cl.duoc.levelupgamer.ui.app.Route
import cl.duoc.levelupgamer.ui.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import ui.home.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val userPrefs = UserPreferences(context)

    // ⭐ Verificar sesión al inicio
    LaunchedEffect(Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val hasLocalSession = userPrefs.isLoggedIn()

        if (firebaseUser != null && hasLocalSession) {
            // Tiene sesión activa → ir directo a Principal
            nav.navigate(Route.Principal.path) {
                popUpTo(Route.HomeRoot.path) { inclusive = true }
            }
        }
    }

    NavHost(navController = nav, startDestination = Route.HomeRoot.path) {

        // Pantalla inicial (Home sin login)
        composable(Route.HomeRoot.path) {
            HomeScreen(
                onLoginClick = { nav.navigate(Route.Login.path) },
                onRegisterClick = { nav.navigate(Route.Register.path) },
                onRecoverClick = { nav.navigate(Route.RecoverPassword.path) }
            )
        }

        // Login
        composable(Route.Login.path) {
            val loginViewModel: LoginViewModel = viewModel()
            val loginState by loginViewModel.ui.collectAsState()

            // ⭐ Navegar automáticamente cuando loggedIn = true
            LaunchedEffect(loginState.loggedIn) {
                if (loginState.loggedIn) {
                    nav.navigate(Route.Principal.path) {
                        popUpTo(Route.HomeRoot.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            LoginScreen(
                onBack = { nav.popBackStack() },
                onLoginSuccess = {
                    // Ya manejado por LaunchedEffect arriba
                },
                vm = loginViewModel
            )
        }

        // Principal (Home con login)
        composable(Route.Principal.path) {
            val loginViewModel: LoginViewModel = viewModel()

            PrincipalScreen(
                onLogout = {
                    // ⭐ Logout completo
                    loginViewModel.logout()

                    // Navegar a Home y limpiar stack
                    nav.navigate(Route.HomeRoot.path) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Registro
        composable(Route.Register.path) {
            RegistrarseScreen(
                onBack = { nav.popBackStack() },
                onRegistered = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(Route.HomeRoot.path) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Recuperar contraseña
        composable(Route.RecoverPassword.path) {
            RecuperarPasswordScreen(
                onBack = { nav.popBackStack() },
                onSent = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(Route.HomeRoot.path) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
