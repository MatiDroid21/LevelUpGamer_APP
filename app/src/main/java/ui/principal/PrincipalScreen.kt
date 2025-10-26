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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.levelupgamer.repository.auth.FirebaseAuthDataSource
import data.media.MediaRepository
import ui.principal.components.UiProductosCard
import ui.principal.checkout.CheckoutScreen
import ui.profile.ProfileScreen
import ui.profile.ProfileViewModel
import ui.vmfactory.ProfileVMFactory

// --- Bottom items ---
sealed class BottomItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home : BottomItem("home", "Inicio", Icons.Outlined.Home)
    data object Favs : BottomItem("favs", "Favoritos", Icons.Outlined.FavoriteBorder)
    data object Cart : BottomItem("cart", "Carrito", Icons.Outlined.ShoppingCart)
    data object Agenda : BottomItem("agenda", "Agenda", Icons.Outlined.PlayArrow)
    data object More : BottomItem("more", "Más", Icons.Outlined.Menu)
}

private val bottomItems = listOf(
    BottomItem.Home, BottomItem.Favs, BottomItem.Cart, BottomItem.Agenda, BottomItem.More
)

@Composable
private fun BottomBar(
    navController: NavHostController,
    onHomeTap: () -> Unit,
    cartBadge: Int
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar {
        bottomItems.forEach { item ->
            val isCart = item is BottomItem.Cart
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == BottomItem.Home.route) {
                        onHomeTap()
                        navController.navigate(BottomItem.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = false }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (isCart && cartBadge > 0) {
                        BadgedBox(badge = { Badge { Text("$cartBadge") } }) {
                            Icon(item.icon, contentDescription = item.title)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.title)
                    }
                },
                label = { Text(item.title) }
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
    // Sincroniza el email al crear la pantalla
    LaunchedEffect(Unit) { vm.refreshUserEmail() }
    // Sincroniza al volver a esta vista
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshUserEmail()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val categoriaSel by vm.categoriaSel.collectAsState()
    val productos by vm.productosFiltrados.collectAsState()
    val totalItems by vm.totalItems.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val tabsNav = rememberNavController()

    // Logout reactivo
    LaunchedEffect(state.loggedOut) { if (state.loggedOut) onLogout() }

    // Snackbar para errores
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) { state.error?.let { snackbarHostState.showSnackbar(it) } }

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
        bottomBar = { BottomBar(tabsNav, onHomeTap = { vm.refreshHome() }, cartBadge = totalItems) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        NavHost(
            navController = tabsNav,
            startDestination = BottomItem.Home.route,
            modifier = Modifier.padding(inner)
        ) {
            // HOME
            composable(route = BottomItem.Home.route) {
                LaunchedEffect(Unit) { if (productos.isEmpty()) vm.cargarProductos() }

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
                                    .aspectRatio(0.66f)
                                    .animateContentSize()
                            ) {
                                // OBSERVA qty COMO STATE: recomposición inmediata
                                val qty by vm.cantidadDeFlow(producto.id).collectAsState(initial = 0)

                                UiProductosCard(
                                    producto = producto,
                                    qty = qty,
                                    onIncrement = { vm.agregarAlCarrito(producto, +1) },
                                    onDecrement = { vm.quitarDelCarrito(producto) }
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
                val items by vm.carrito.collectAsState()
                val total by vm.totalCLP.collectAsState()

                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Carrito", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))

                    if (items.isEmpty()) {
                        Text("No hay productos en el carrito")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(items, key = { it.producto.id }) { item ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.producto.titulo, style = MaterialTheme.typography.titleMedium)
                                        Text(item.producto.precio, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { vm.quitarDelCarrito(item.producto) },
                                            enabled = item.cantidad > 1
                                        ) { Icon(Icons.Outlined.Close, contentDescription = "Menos") }

                                        Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)

                                        IconButton(onClick = { vm.agregarAlCarrito(item.producto, +1) }) {
                                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Más")
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        val subtotal = (item.producto.precio.filter(Char::isDigit).toIntOrNull() ?: 0) * item.cantidad
                                        Text(subtotal.formateaCLP(), style = MaterialTheme.typography.titleMedium)

                                        IconButton(onClick = { vm.setCantidad(item.producto, 0) }) {
                                            Icon(Icons.Outlined.Close, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Total: ${total.formateaCLP()}", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { tabsNav.navigate("checkout") },
                                enabled = items.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Continuar compra") }

                            FilledTonalButton(
                                onClick = { vm.limpiarCarrito() },
                                modifier = Modifier.weight(1f)
                            ) { Text("Vaciar") }
                        }
                    }
                }
            }

            // AGENDA
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

            // CHECKOUT
            composable("checkout") {
                CheckoutScreen(
                    vm = vm,
                    onFinished = {
                        vm.limpiarCarrito()
                        tabsNav.navigate(BottomItem.Home.route) {
                            popUpTo(BottomItem.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onClose = { tabsNav.popBackStack() }
                )
            }
        }
    }
}
