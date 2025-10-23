package ui.principal
import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.levelupgamer.repository.auth.FirebaseAuthDataSource
import data.media.MediaRepository
import ui.principal.components.UiProductosCard
import ui.profile.ProfileScreen
import ui.profile.ProfileViewModel
import ui.vmfactory.ProfileVMFactory

// --- Bottom items ---
sealed class BottomItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: Int? = null
) {
    data object Home : BottomItem("home", "Inicio", Icons.Outlined.Home)
    data object Favs : BottomItem("favs", "Favoritos", Icons.Outlined.FavoriteBorder)
    data object Cart : BottomItem("cart", "Carrito", Icons.Outlined.ShoppingCart, badge = 3)
    data object Agenda : BottomItem("agenda", "Agenda", Icons.Outlined.PlayArrow)
    data object More : BottomItem("more", "Más", Icons.Outlined.Menu)
}

private val bottomItems = listOf(
    BottomItem.Home, BottomItem.Favs, BottomItem.Cart, BottomItem.Agenda, BottomItem.More
)

@Composable
private fun BottomBar(
    navController: NavHostController,
    onHomeTap: () -> Unit
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == BottomItem.Home.route) {
                        // SIEMPRE refrescamos Home y NO restauramos estado
                        onHomeTap()
                        navController.navigate(BottomItem.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = false }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        // Resto de tabs con preservación de estado
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if ((item.badge ?: 0) > 0) {
                        BadgedBox(badge = { Badge { Text("${item.badge}") } }) {
                            Icon(item.icon, contentDescription = item.title)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.title)
                    }
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrincipalScreen(
    onLogout: () -> Unit = {},
    vm: PrincipalViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()
    val categoriaSel by vm.categoriaSel.collectAsState()
    val productos by vm.productosFiltrados.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val tabsNav = rememberNavController()

    // Logout reactivo
    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLogout()
    }

    // Snackbar para errores
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Principal") },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = {
                                expanded = false
                                tabsNav.navigate("profile")
                            },
                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                expanded = false
                                vm.logout()
                            }
                        )
                    }
                }
            )
        },
        bottomBar = { BottomBar(tabsNav, onHomeTap = { vm.refreshHome() }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        NavHost(
            navController = tabsNav,
            startDestination = BottomItem.Home.route,
            modifier = Modifier.padding(inner)
        ) {
            // HOME
            composable(route = BottomItem.Home.route) {
                // Carga inicial en el primer ingreso
                LaunchedEffect(Unit) {
                    if (productos.isEmpty()) vm.cargarProductos()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val saludo = "Hola ${state.email ?: "usuario"}"
                    Text(saludo, style = MaterialTheme.typography.headlineSmall)
                    Text("Bienvenido a tu pantalla principal.")

                    // Filtros por categoría
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(vm.categorias.size) { idx ->
                            val cat = vm.categorias[idx]
                            FilterChip(
                                selected = categoriaSel == cat,
                                onClick = { vm.setCategoria(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }

                    // Grilla de productos
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 180.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                    ) {
                        items(productos, key = { it.id }) { producto ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.6f)
                                    .animateContentSize()
                            ) {
                                UiProductosCard(
                                    producto = producto,
                                    onAgregar = { p ->
                                        if (vm.carrito.value.contains(p)) {
                                            vm.quitarDelCarrito(p)
                                        } else {
                                            vm.agregarAlCarrito(p)
                                        }
                                    }
                                )

                            }
                        }
                    }
                }
            }

            // FAVORITOS
            composable(BottomItem.Favs.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Favoritos")
                }
            }

            // CARRITO
            composable(BottomItem.Cart.route) {
                val carrito by vm.carrito.collectAsState()
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Carrito", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))

                    if (carrito.isEmpty()) {
                        Text("No hay productos en el carrito")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(carrito) { p ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${p.titulo} - ${p.precio}")
                                    Button(onClick = { vm.quitarDelCarrito(p) }) {
                                        Text("Quitar")
                                    }
                                }
                            }
                        }
                        val total = carrito.sumOf { it.precio.replace(".", "").toInt() }
                        Spacer(Modifier.height(12.dp))
                        Text("Total: $total CLP", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.limpiarCarrito() }, Modifier.fillMaxWidth()) {
                            Text("Vaciar carrito")
                        }
                    }
                }
            }


            // RECORDATORIO
            composable(BottomItem.Agenda.route) {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Debes iniciar sesión para ver tus recordatorios.")
                    }
                } else {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val factory = remember(key1 = uid) { ui.vmfactory.RecordatorioVMFactory(context, uid) }
                    val rvm: ui.recordatorio.RecordatorioViewModel =
                        androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                        ui.recordatorio.RecordatorioScreen(rvm)
                }
            }

            // MÁS
            composable(BottomItem.More.route) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Text("Más opciones")
                    Button(onClick = { vm.logout() }) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.loading) "Cerrando..." else "Cerrar sesión")
                    }
                }
            }

            // PERFIL
            composable("profile") {
                val authDs = remember { FirebaseAuthDataSource() }
                val mediaRepo = remember { MediaRepository() }
                val factory = remember { ProfileVMFactory(authDs, mediaRepo) }
                val pvm: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(pvm)
            }
        }
    }
}
