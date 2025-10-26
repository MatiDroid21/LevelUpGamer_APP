package ui.principal

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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

    NavigationBar(
        containerColor = Color(0xFF181840),
        contentColor = Color(0xFF42F5E3)
    ) {
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
                            Icon(item.icon, contentDescription = item.title, tint = Color(0xFF42F5E3))
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.title, tint = if (currentRoute == item.route) Color(0xFFFF357A) else Color(0xFF42F5E3))
                    }
                },
                label = {
                    Text(item.title, color = Color(0xFF42F5E3), fontWeight = FontWeight.Bold)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF357A),
                    selectedTextColor = Color(0xFFFF357A),
                    indicatorColor = Color(0x8042F5E3),
                    unselectedIconColor = Color(0xFF42F5E3),
                    unselectedTextColor = Color(0xFF42F5E3)
                )
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
    LaunchedEffect(Unit) { vm.refreshUserEmail() }
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

    LaunchedEffect(state.loggedOut) { if (state.loggedOut) onLogout() }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) { state.error?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        containerColor = Color(0xFF101020),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GAMER CENTER",
                        color = Color(0xFF42F5E3),
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Menú", tint = Color(0xFF42F5E3))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil", color = Color(0xFF42F5E3)) },
                            onClick = {
                                expanded = false
                                tabsNav.navigate("profile")
                            },
                            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = Color(0xFF42F5E3)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout", color = Color(0xFFFF357A)) },
                            onClick = {
                                expanded = false
                                vm.logout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF181840),
                    titleContentColor = Color(0xFF42F5E3)
                )
            )
        },
        bottomBar = { BottomBar(tabsNav, onHomeTap = { vm.refreshHome() }, cartBadge = totalItems) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        NavHost(
            navController = tabsNav,
            startDestination = BottomItem.Home.route,
            modifier = Modifier
                .padding(inner)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF181840), Color(0xFF191A2E))
                    )
                )
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
                    val saludo = "¡Bienvenido gamer, ${state.email ?: "usuario"}!"
                    Text(
                        saludo,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF42F5E3),
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        "Disfruta del mundo gamer.",
                        color = Color(0xFF66EEFF)
                    )

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
                                label = {
                                    Text(
                                        cat,
                                        color = if (categoriaSel == cat) Color(0xFF101020) else Color(0xFF42F5E3),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF42F5E3),
                                    containerColor = Color(0xFF1A233A)
                                )
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
                                val qty by vm.cantidadDeFlow(producto.id).collectAsState(initial = 0)

                                UiProductosCard(
                                    producto = producto,
                                    qty = qty,
                                    onIncrement = { vm.agregarAlCarrito(producto, +1) },
                                    onDecrement = { vm.quitarDelCarrito(producto) }
                                    // Puedes dar estilo gamer adicional a UiProductosCard en su propio archivo
                                )
                            }
                        }
                    }
                }
            }

            // El resto (Favoritos, Carrito, Agenda, Más, Perfil, Checkout) pueden mantener estilos
            // oscuros/neón de los ejemplos previos:
            composable(BottomItem.Favs.route) {
                Box(Modifier
                    .fillMaxSize()
                    .background(Color(0xFF181840)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Favoritos", color = Color(0xFF42F5E3), fontWeight = FontWeight.Bold)
                }
            }

            composable(BottomItem.Cart.route) {
                val items by vm.carrito.collectAsState()
                val total by vm.totalCLP.collectAsState()

                Column(Modifier
                    .fillMaxSize()
                    .background(Color(0xFF181840))
                    .padding(16.dp)) {
                    Text("Carrito", color = Color(0xFF42F5E3), style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))

                    if (items.isEmpty()) {
                        Text("No hay productos en el carrito", color = Color(0xFF66EEFF))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(items, key = { it.producto.id }) { item ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.producto.titulo, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(item.producto.precio, color = Color(0xFF42F5E3))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { vm.quitarDelCarrito(item.producto) },
                                            enabled = item.cantidad > 1
                                        ) { Icon(Icons.Outlined.Close, contentDescription = "Menos", tint = Color(0xFFFF357A)) }

                                        Text("${item.cantidad}", color = Color.White, fontWeight = FontWeight.Bold)

                                        IconButton(onClick = { vm.agregarAlCarrito(item.producto, +1) }) {
                                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Más", tint = Color(0xFF42F5E3))
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        val subtotal = (item.producto.precio.filter(Char::isDigit).toIntOrNull() ?: 0) * item.cantidad
                                        Text(subtotal.formateaCLP(), color = Color(0xFFFF357A), fontWeight = FontWeight.Bold)

                                        IconButton(onClick = { vm.setCantidad(item.producto, 0) }) {
                                            Icon(Icons.Outlined.Close, contentDescription = "Eliminar", tint = Color(0xFFFF357A))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Total: ${total.formateaCLP()}", color = Color(0xFF42F5E3), style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { tabsNav.navigate("checkout") },
                                enabled = items.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1BA1FF),
                                    contentColor = Color.White
                                )
                            ) { Text("Continuar compra", fontWeight = FontWeight.Bold) }

                            FilledTonalButton(
                                onClick = { vm.limpiarCarrito() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFFF357A),
                                    contentColor = Color.White
                                )
                            ) { Text("Vaciar", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            // Agenda
            composable(BottomItem.Agenda.route) {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Box(Modifier
                        .fillMaxSize()
                        .background(Color(0xFF181840)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Debes iniciar sesión para ver tus recordatorios.", color = Color(0xFFFF357A))
                    }
                } else {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val factory = remember(key1 = uid) { ui.vmfactory.RecordatorioVMFactory(context, uid) }
                    val rvm: ui.recordatorio.RecordatorioViewModel =
                        androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                    ui.recordatorio.RecordatorioScreen(rvm)
                }
            }

            // Más
            composable(BottomItem.More.route) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF181840))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Text("Más opciones", color = Color(0xFF42F5E3), fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { vm.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF357A),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.loading) "Cerrando..." else "Cerrar sesión", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Perfil
            composable("profile") {
                val authDs = remember { FirebaseAuthDataSource() }
                val mediaRepo = remember { MediaRepository() }
                val factory = remember { ProfileVMFactory(authDs, mediaRepo) }
                val pvm: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(pvm)
            }

            // Checkout
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
