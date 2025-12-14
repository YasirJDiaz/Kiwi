package com.example.kiwi
//ID FIREBASE: kiwi-2025


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kiwi.ui.theme.KiwiTheme
import kotlinx.coroutines.delay
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiwi.viewmodel.SharedViewModel
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.material.icons.filled.Send
import com.google.firebase.FirebaseApp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.firestore.Query
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.example.kiwi.viewmodel.EstadoPedido


class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("new_products")
            .addOnCompleteListener { task ->
                var msg = "Suscrito a notificaciones de nuevos productos"
                if (!task.isSuccessful) {
                    msg = "Error al suscribirse a notificaciones"
                }
                Log.d("FCM", msg)
            }
        setContent {

            // Modificación de KiwiTheme para forzar el modo claro
            KiwiTheme(darkTheme = false) {

                val navController = rememberNavController()
                val sharedViewModel: SharedViewModel = viewModel()
                val rotarCarrito = remember { mutableStateOf(false) }

                val anguloCarrito by animateFloatAsState(
                    targetValue = if (rotarCarrito.value) 360f else 0f,
                    animationSpec = tween(durationMillis = 600),
                    finishedListener = { rotarCarrito.value = false }
                )


                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { SplashScreen { navController.navigate("login") } }
                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            onCompradorClick = { navController.navigate("buyer") }
                        )
                    }

                    composable("buyer") {
                        BuyerScreen(navController, sharedViewModel, anguloCarrito)
                    }

                    composable("carrito") {
                        CarritoScreen(navController, sharedViewModel)
                    }

                    composable("solicitudes") {
                        SolicitudesScreen(navController, sharedViewModel)
                    }

                    composable("detalleProducto/{productoId}") { backStackEntry ->
                        val productoId = backStackEntry.arguments?.getString("productoId") ?: ""
                        DetalleProductoScreen(productoId, navController, sharedViewModel, rotarCarrito, anguloCarrito)
                    }

                    composable("vendedoraMenu") {
                        VendedoraMenuScreen(navController, sharedViewModel)
                    }

                    composable("agregarProducto") {
                        AgregarProductoScreen(navController, sharedViewModel)
                    }

                    composable("vendedoraSolicitudes") {
                        SolicitudesVendedoraScreen(navController, sharedViewModel)
                    }

                    composable("FacturaScreen") {
                        FacturaScreen(navController, sharedViewModel)
                    }

                    composable("editarProducto/{productoId}") { backStackEntry ->
                        val productoId = backStackEntry.arguments?.getString("productoId") ?: ""
                        EditarProductoScreen(productoId, navController, sharedViewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun SplashScreen(onFinish: () -> Unit)
{
    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo_kiwi),
                contentDescription = "Logo de Kiwi",
                modifier = Modifier.size(180.dp)
            )
        }
    }
}


@Composable
fun LoginScreen(navController: NavHostController, onCompradorClick: () -> Unit)
{

    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var backPressedTime by remember { mutableStateOf(0L) }
    val activity = (LocalContext.current as? ComponentActivity)

    // Animación de rotación para el logo
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotationAngle.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000), // Gira cada 3 segundos
                repeatMode = RepeatMode.Restart
            )
        )
    }

    BackHandler(enabled = true) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {

            activity?.finish()
        } else {

            backPressedTime = currentTime

            Toast.makeText(context, "Vuelve a presionar para salir de la aplicación", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_kiwi),
                contentDescription = "Logo Kiwi",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { rotationZ = rotationAngle.value } // Aplica la rotación animada
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "VENDEDORES",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { if (it.matches(Regex("[a-zA-Z0-9]*"))) contrasena = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                )
            )


            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    // Validamos que los campos no estén vacíos
                    if (usuario.isNotBlank() && contrasena.isNotBlank()) {
                        // Usamos Firebase para iniciar sesión
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(usuario, contrasena)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Si las credenciales son correctas, navega al menú
                                    navController.navigate("vendedoraMenu")
                                } else {
                                    // Si hay un error mostramos un mensaje
                                    Toast.makeText(context, "Error de autenticación. Verifica tus credenciales.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Mensaje si los campos están vacíos
                        Toast.makeText(context, "Por favor, ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("INGRESAR")
            }


            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "COMPRADORES",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onCompradorClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("INGRESAR")
            }
        }
    }
}


data class ProductoDetalle(
    val idFirestore: String? = null,
    val id: Int = 0,
    val nombre: String = "",
    val referencia: String = "",
    val categoria: String = "",
    val tallas: List<String> = emptyList(),
    val precio: Double = 0.0,
    var stock: Int = 0,
    var cantidadReservada: Int = 0,
    val imagenUrl: String? = null // URL de la imagen en Firebase Storage
)
{
    // Constructor sin argumentos requerido en el Firestore
    constructor() : this(
        null, 0, "", "", "", emptyList(), 0.0, 0, 0, null
    )
}

data class ProductoEnPedido(

    val producto: ProductoDetalle? = null,
    val cantidad: Int = 0
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BuyerScreen(navController: NavHostController, viewModel: SharedViewModel, rotacionCarrito: Float)
{
    val categorias = listOf(
        "Suéter", "Blusa", "Jeans", "Enterizo", "Conjunto", "Short",
        "Falda", "Body", "Traje", "Licra", "Traje de baño", "Malla"
    )
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    val productos = viewModel.productos.reversed()
    val productosFiltrados = productos.filter {
        (categoriaSeleccionada == null || it.categoria == categoriaSeleccionada) &&
                (busqueda.isBlank() || it.referencia.contains(busqueda, ignoreCase = true))
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Obtiene la ruta actual del back stack de navegación
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    LaunchedEffect(currentRoute) {
        if (currentRoute == "solicitudes") {
            viewModel.tieneActualizacionSolicitudesComprador = false // Resetea la bandera
            Log.d("BuyerScreen", "Cliente ha entrado a la pantalla de solicitudes. Notificación para el comprador resetada.")
        }
    }

    BackHandler {
        navController.navigate("login") {
            popUpTo("buyer") { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = currentRoute ?: "catalogo", // Pasa la ruta actual para resaltar la pestaña seleccionada
                onCatalogoClick = {
                    navController.navigate("buyer") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onCarritoClick = { navController.navigate("carrito") },
                onSolicitudesClick = {
                    navController.navigate("solicitudes")
                },
                tieneNotificacionSolicitudesComprador = viewModel.tieneActualizacionSolicitudesComprador,
                rotacionCarrito = rotacionCarrito
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Logo, Buscador y Categorías
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_kiwi),
                        contentDescription = "Logo Kiwi",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(end = 12.dp)
                    )

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = {
                            if (it.matches(Regex("[a-zA-Z0-9#\\-_.]*"))) busqueda = it
                        },
                        label = { Text("Nº. Referencia", fontSize = 12.sp) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        trailingIcon = {
                            if (busqueda.isNotEmpty()) {
                                IconButton(onClick = { busqueda = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categorias) { categoria ->
                        val seleccionado = categoria == categoriaSeleccionada
                        Button(
                            onClick = {
                                categoriaSeleccionada = if (seleccionado) null else categoria
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (seleccionado) Color.Black else Color.Gray
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(text = categoria, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Feed de productos
            if (productosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay mercancía disponible", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Dos columnas fijas
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaciado entre columnas
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Espaciado entre filas
                ) {
                    items(productosFiltrados, key = { it.referencia }) { producto ->
                        Card(
                            onClick = {
                                navController.navigate("detalleProducto/${producto.referencia}")
                            },
                            modifier = Modifier
                                .fillMaxWidth() // Ocupa el ancho en la celda
                                .height(200.dp), // Altura fija para que la información no se corte
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column( // Contenido de la tarjeta (imagen arriba, texto abajo)
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally // Centra el contenido horizontalmente
                            ) {
                                if (producto.imagenUrl != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(producto.imagenUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop, // Recorta para llenar el espacio
                                        modifier = Modifier
                                            .size(100.dp) // Tamaño fijo para la imagen dentro de la tarjeta
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_kiwi),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp) // Tamaño fijo para la imagen dentro de la tarjeta
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp)) // Espacio entre imagen y texto

                                // Información del producto
                                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                                Text(producto.referencia, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                Text("Precio: \$${String.format("%.2f", producto.precio)}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                val stockVisible = producto.stock - producto.cantidadReservada
                                Text(
                                    if (stockVisible > 0) "Docenas: $stockVisible" else "Agotado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (stockVisible > 0) Color.Unspecified else Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    // Espacio al final, que abarca las dos columnas
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onCatalogoClick: () -> Unit,
    onCarritoClick: () -> Unit,
    onSolicitudesClick: () -> Unit,
    tieneNotificacionSolicitudesComprador: Boolean = false,
    rotacionCarrito: Float = 0f
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == "catalogo",
            onClick = onCatalogoClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Catálogo") },
            label = { Text("Catálogo") }
        )

        NavigationBarItem(
            selected = selectedTab == "carrito",
            onClick = onCarritoClick,
            icon = {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Carrito",
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotacionCarrito
                    }
                )
            },
            label = { Text("Carrito") }
        )

        NavigationBarItem(
            selected = selectedTab == "solicitudes",
            onClick = onSolicitudesClick,
            icon = {
                Box {
                    Icon(Icons.Default.List, contentDescription = "Solicitudes")
                    if (tieneNotificacionSolicitudesComprador) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .offset(x = 12.dp, y = (-2).dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                    }
                }
            },
            label = { Text("Solicitudes") }
        )
    }
}


@Composable
fun CarritoScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val snackbarHostState = remember { SnackbarHostState() }
    val productosEnCarrito = viewModel.carrito.reversed()
    val total = productosEnCarrito.sumOf { it.producto?.precio?.times(it.cantidad) ?: 0.0 }
    var itemAEliminarEnCarrito by remember { mutableStateOf<com.example.kiwi.ProductoEnPedido?>(null) }

    BackHandler {
        navController.navigate("buyer") {
            popUpTo("carrito") { inclusive = true }
        }
    }

    // Diálogo de confirmación de eliminación
    if (itemAEliminarEnCarrito != null) {
        AlertDialog(
            onDismissRequest = { itemAEliminarEnCarrito = null },
            title = { Text("Confirmar eliminación") },
            text = {
                val (productoDetalle, cantidad) = itemAEliminarEnCarrito!!
                Text("¿Seguro desea eliminar ${cantidad} docenas de ${productoDetalle?.nombre} (${productoDetalle?.referencia}) del carrito?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.carrito.remove(itemAEliminarEnCarrito!!)
                    itemAEliminarEnCarrito = null
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar("Producto eliminado del carrito")
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemAEliminarEnCarrito = null }) {
                    Text("No")
                }
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "carrito",
                onCatalogoClick = { navController.navigate("buyer") },
                onCarritoClick = { },
                onSolicitudesClick = { navController.navigate("solicitudes") },
                tieneNotificacionSolicitudesComprador = viewModel.tieneActualizacionSolicitudesComprador
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 120.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_kiwi),
                    contentDescription = "Logo Kiwi",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Usamos 'productosEnCarrito' para verificar si está vacío
                if (productosEnCarrito.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("El carrito está vacío", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Iterar sobre 'productosEnCarrito'
                        items(productosEnCarrito) { productoEnPedido ->
                            // productoEnPedido es de tipo ProductoEnPedido
                            // Destrúirlo para acceder a ProductoDetalle y cantidad
                            val (productoDetalle, cantidad) = productoEnPedido

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    // Usa 'productoDetalle?.imagenUrl'
                                    if (productoDetalle?.imagenUrl != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(productoDetalle.imagenUrl),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.logo_kiwi),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .padding(end = 2.dp)
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Safe calls para las propiedades de productoDetalle
                                        Text(productoDetalle?.nombre ?: "Producto Desconocido")
                                        Text("Ref: ${productoDetalle?.referencia ?: "N/A"}")
                                        Text("Cantidad: $cantidad")
                                        Text("Precio x docena: \$${String.format("%.2f", productoDetalle?.precio ?: 0.0)}")
                                    }
                                    IconButton(onClick = {
                                        //Pasar el objeto ProductoEnPedido al estado
                                        itemAEliminarEnCarrito = productoEnPedido
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fila fija con total + botón
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Divider()
                    Text(
                        "Total: \$${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Usa 'productosEnCarrito' para verificar si está vacío
                    val carritoVacio = productosEnCarrito.isEmpty()

                    Button(
                        onClick = {
                            if (!carritoVacio) {
                                navController.navigate("FacturaScreen")
                            }
                        },
                        enabled = !carritoVacio,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (carritoVacio) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("PEDIR", color = if (carritoVacio) Color.LightGray else Color.White)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val solicitudes = viewModel.solicitudes
    var estadoSeleccionado by remember { mutableStateOf<String?>(null) }
    val currentSortDirection by viewModel.solicitudesSortDirection.collectAsState()
    val solicitudesFiltradas = solicitudes
        .filter { estadoSeleccionado == null || it.estado == estadoSeleccionado }


    BackHandler {
        navController.navigate("buyer") {
            popUpTo("solicitudes") { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.tieneActualizacionSolicitudesComprador = false
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "solicitudes",
                onCatalogoClick = { navController.navigate("buyer") },
                onCarritoClick = { navController.navigate("carrito") },
                onSolicitudesClick = { },
                tieneNotificacionSolicitudesComprador = viewModel.tieneActualizacionSolicitudesComprador
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Logo
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo_kiwi),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filtros de estado (Aceptada, Pendiente, Rechazada)
                val estados = listOf("Aceptada", "Pendiente", "Devuelta")
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
                {
                    items(estados) { estado ->
                        val activo = estado == estadoSeleccionado
                        FilterChip(
                            selected = activo,
                            onClick = {
                                estadoSeleccionado = if (activo) null else estado
                            },
                            label = { Text(estado) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de orden por fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ordenar por fecha:", modifier = Modifier.weight(1f))
                    // Conecta el DropdownMenuBox con el ViewModel
                    DropdownMenuBox(
                        ordenAscendente = currentSortDirection == Query.Direction.ASCENDING,
                        onOrdenChange = { nuevoOrden ->
                            val direction = if (nuevoOrden) Query.Direction.ASCENDING else Query.Direction.DESCENDING
                            viewModel.setSolicitudesSortDirection(direction)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }


            // Lista de solicitudes
            if (solicitudesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay solicitudes registradas", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(solicitudesFiltradas) { solicitud ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    solicitud.productos.forEach { (productoDetalle, cantidadPedida) ->

                                        val referencia = productoDetalle?.referencia
                                        val nombreProducto = productoDetalle?.nombre
                                        val precioUnitario = productoDetalle?.precio

                                        Text("Prenda: ${nombreProducto ?: "Desconocido"}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Referencia: ${referencia ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Docenas: $cantidadPedida", style = MaterialTheme.typography.bodyMedium)
                                        Text("Precio x Docena: \$%.2f".format(precioUnitario ?: 0.0), style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Text("Total: \$${String.format("%.2f", solicitud.total)}", fontWeight = FontWeight.Bold)
                                    Text("Fecha: ${solicitud.fecha} ${solicitud.hora}", style = MaterialTheme.typography.bodyMedium)
                                }

                                // Estado a la derecha
                                Text(
                                    text = solicitud.estado,
                                    color = when (solicitud.estado) {
                                        "Aceptada" -> Color(0xFF2E7D32)
                                        "Pendiente" -> Color(0xFFF9A825)
                                        "Devuelta" -> Color(0xFFD32F2F)
                                        else -> Color.Gray
                                    },
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DropdownMenuBox(
    ordenAscendente: Boolean,
    onOrdenChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(if (ordenAscendente) "Ascendente" else "Descendente")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Ascendente") },
                onClick = {
                    expanded = false
                    if (!ordenAscendente) onOrdenChange(true)
                }
            )
            DropdownMenuItem(
                text = { Text("Descendente") },
                onClick = {
                    expanded = false
                    if (ordenAscendente) onOrdenChange(false)
                }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalleProductoScreen(
    productoId: String,
    navController: NavHostController,
    viewModel: SharedViewModel,
    rotarCarrito: MutableState<Boolean>,
    rotacionCarrito: Float
) {
    val producto = viewModel.productos.find { it.referencia == productoId }
    if (producto == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Producto no encontrado", color = Color.Red)
        }
        return
    }

    var tipoTallaSeleccionada by remember { mutableStateOf<String?>(null) }
    var cantidadTexto by remember { mutableStateOf("") }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var imagenAmpliada by remember { mutableStateOf(false) }
    val composableScope = rememberCoroutineScope()
    val context = LocalContext.current

    BackHandler {
        navController.navigate("buyer") {
            popUpTo("detalleProducto/${productoId}") { inclusive = true }
        }
    }

    // Box como contenedor principal para poder apilar elementos
    Box(modifier = Modifier.fillMaxSize()) {

        // Contenido principal de la pantalla que se puede desplazar
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Padding inferior para que el contenido no quede oculto detrás de los elementos flotantes
                .padding(bottom = 150.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_kiwi),
                contentDescription = "Logo Kiwi",
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(producto.nombre, style = MaterialTheme.typography.titleLarge)
            Text(producto.referencia, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            if (producto.imagenUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(producto.imagenUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { imagenAmpliada = true },
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo_kiwi),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { imagenAmpliada = true },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Tallas disponibles:")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (producto.tallas.isEmpty()) {
                    Text("No hay tallas configuradas para este producto.")
                } else {
                    producto.tallas.forEach { tallaConjunto ->
                        Button(
                            onClick = { tipoTallaSeleccionada = tallaConjunto },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tipoTallaSeleccionada == tallaConjunto) Color.Black else Color.Gray
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(tallaConjunto, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tipoTallaSeleccionada == null) {
                Text("Seleccione una talla", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 48.dp)
                ) {
                    Text("Docenas disponibles", style = MaterialTheme.typography.labelLarge)
                    val stockVisible = producto.stock - producto.cantidadReservada
                    Text(
                        if (stockVisible > 0) "$stockVisible" else "Agotado",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (stockVisible > 0) Color.Unspecified else Color.Red
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Precio x Docena", style = MaterialTheme.typography.labelLarge)
                    Text("\$${String.format("%.2f", producto.precio)}", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cantidadTexto,
                onValueChange = {
                    if (it.isEmpty() || it.toIntOrNull() in 1..producto.stock) {
                        cantidadTexto = it
                    }
                },
                label = { Text("Cantidad") },
                placeholder = { Text("") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(100.dp),
                singleLine = true
            )
        }

        // Botón flotante anclado en la parte inferior y central
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp) // Sube el botón para que quede encima de la barra de navegación
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    val cantidadFinal = cantidadTexto.toIntOrNull() ?: 0
                    val stockRealmenteDisponible = producto.stock - producto.cantidadReservada
                    val sinTalla = tipoTallaSeleccionada == null
                    val cantidadInvalida = cantidadTexto.isBlank() || cantidadFinal <= 0

                    when {
                        sinTalla && cantidadInvalida -> Toast.makeText(context, "Seleccione una talla y agregue una cantidad", Toast.LENGTH_SHORT).show()
                        sinTalla -> Toast.makeText(context, "Seleccione una talla", Toast.LENGTH_SHORT).show()
                        cantidadInvalida -> Toast.makeText(context, "Agregue la cantidad que desea", Toast.LENGTH_SHORT).show()
                        cantidadFinal > stockRealmenteDisponible -> Toast.makeText(context, "No hay suficientes docenas disponibles (${stockRealmenteDisponible} restantes).", Toast.LENGTH_LONG).show()
                        else -> {
                            if ((producto.stock - producto.cantidadReservada) > 0) {
                                viewModel.agregarAlCarrito(producto, cantidadFinal)
                                cantidadTexto = ""
                                tipoTallaSeleccionada = null
                                mostrarDialogoConfirmacion = true

                                composableScope.launch {
                                    delay(2500)
                                    rotarCarrito.value = true
                                }
                            }
                        }
                    }
                },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Agregar al Carrito",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Agregar al carrito")
            }
        }

        // Barra de navegación inferior
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavigationBar(
                selectedTab = "catalogo",
                onCatalogoClick = { navController.navigate("buyer") },
                onCarritoClick = { navController.navigate("carrito") },
                onSolicitudesClick = { navController.navigate("solicitudes") },
                tieneNotificacionSolicitudesComprador = viewModel.tieneActualizacionSolicitudesComprador,
                rotacionCarrito = rotacionCarrito
            )
        }
    }

    // Diálogos que se muestran sobre toda la pantalla
    if (imagenAmpliada) {
        Dialog(
            onDismissRequest = { imagenAmpliada = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val painter = producto.imagenUrl?.let { rememberAsyncImagePainter(it) } ?: painterResource(id = R.drawable.logo_kiwi)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .clickable { imagenAmpliada = false },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = { Text("Mercancía agregada") },
            text = { Text("Tu mercancía ha sido agregada al carrito") },
            confirmButton = {}
        )

        LaunchedEffect(Unit) {
            delay(2000)
            mostrarDialogoConfirmacion = false
        }
    }
}


@Composable
fun FacturaScreen(navController: NavHostController, viewModel: SharedViewModel) {
    val nombre = remember { mutableStateOf("") }
    val celular = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val estadoActualPedido = viewModel.estadoPedido
    var mensajeError by remember { mutableStateOf<String?>(null) }


    when (val estado = viewModel.estadoPedido) {
        is EstadoPedido.Success -> {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Pedido enviado") },
                text = { Text("Su pedido ha sido enviado con éxito y será revisado por la vendedora.") },
                confirmButton = { }
            )
            LaunchedEffect(Unit) {
                delay(2000)
                viewModel.resetEstadoPedido()
                navController.navigate("buyer") {
                    popUpTo("FacturaScreen") { inclusive = true }
                }
            }
        }
        is EstadoPedido.Conflict -> {
            val conflicto = estado.info
            AlertDialog(
                onDismissRequest = { viewModel.resetEstadoPedido() },
                title = { Text("Inventario insuficiente") },
                text = {
                    // CAMBIO 1: Formato "Nombre - Referencia"
                    // Usamos un salto de linea (\n) para que se vea ordenado si son varios
                    val listaProductos = conflicto.productosAgotados.joinToString(separator = "\n") { item ->
                        "${item.producto?.nombre ?: "Sin nombre"} - ${item.producto?.referencia ?: "N/A"}"
                    }
                    Text("Productos agotados:\n\n$listaProductos")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // CAMBIO 2: Navegar al carrito para editar
                            viewModel.resetEstadoPedido() // Limpia el error
                            navController.navigate("carrito") {
                                // Opcional: Esto ayuda a que no se duplique la pantalla del carrito
                                popUpTo("carrito") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Editar pedido")
                    }
                }
            )
        }
        // CASO 1: Mostrar indicador de carga
        is EstadoPedido.Loading -> {
            Dialog(onDismissRequest = {}) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        // CASO 2: Manejar el error
        is EstadoPedido.Failure -> {
            // Guardamos el error para mostrarlo en un diálogo
            LaunchedEffect(estado) {
                mensajeError = estado.error
            }
        }
        else -> { }
    }

    // Diálogo para mostrar errores si ocurren
    if (mensajeError != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetEstadoPedido()
                mensajeError = null
            },
            title = { Text("Error") },
            text = { Text(mensajeError ?: "Error desconocido") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetEstadoPedido()
                    mensajeError = null
                }) {
                    Text("Cerrar")
                }
            }
        )
    }



    Box(modifier = Modifier.fillMaxSize()) {

        // Barra de navegación inferior
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavigationBar(
                selectedTab = "factura",
                onCatalogoClick = { navController.navigate("buyer") },
                onCarritoClick = { navController.navigate("carrito") },
                onSolicitudesClick = { navController.navigate("solicitudes") }
            )
        }

        // Contenido principal de la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 150.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_kiwi),
                contentDescription = "Logo Kiwi",
                modifier = Modifier.size(80.dp).padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Datos para factura no fiscal", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Coloque sus datos para enviar a su correo la factura de compra", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre.value,
                onValueChange = {
                    if (it.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*"))) nombre.value = it
                },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = celular.value,
                onValueChange = {
                    if (it.matches(Regex("^\\+?\\d{0,15}$"))) celular.value = it
                },
                label = { Text("Celular") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
            )
        }

        // Botón flotante "Enviar"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    val nombreLleno = nombre.value.isNotBlank()
                    val celularLleno = celular.value.isNotBlank()
                    val emailLleno = email.value.isNotBlank()

                    val nombreFormatoValido = nombre.value.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+\$"))
                    val celularFormatoValido = celular.value.matches(Regex("^\\+?\\d{8,15}\$"))
                    val emailFormatoValido = email.value.endsWith("@gmail.com") || email.value.endsWith("@outlook.com")

                    when {
                        !nombreLleno -> Toast.makeText(context, "Introduzca su nombre", Toast.LENGTH_SHORT).show()
                        !celularLleno -> Toast.makeText(context, "Introduzca su celular", Toast.LENGTH_SHORT).show()
                        !emailLleno -> Toast.makeText(context, "Introduzca su email", Toast.LENGTH_SHORT).show()
                        !nombreFormatoValido -> Toast.makeText(context, "El nombre solo debe contener letras", Toast.LENGTH_SHORT).show()
                        !celularFormatoValido -> Toast.makeText(context, "El celular debe tener entre 8 y 15 dígitos", Toast.LENGTH_SHORT).show()
                        !emailFormatoValido -> Toast.makeText(context, "El email debe ser @gmail.com o @outlook.com", Toast.LENGTH_SHORT).show()
                        else -> {
                            viewModel.realizarPedido(
                                nombre = nombre.value,
                                celular = celular.value
                            )
                        }
                    }
                },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Enviar")
            }
        }
    }
}


@Composable
fun VendedoraMenuScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val categorias = listOf(
        "Suéter", "Blusa", "Jeans", "Enterizo", "Conjunto", "Short",
        "Falda", "Body", "Traje", "Licra", "Traje de baño", "Mallas"
    )

    var productoAEliminar by remember { mutableStateOf<ProductoDetalle?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    val productos = viewModel.productos.reversed()
    val productosFiltrados = productos.filter {
        (categoriaSeleccionada == null || it.categoria == categoriaSeleccionada) &&
                (busqueda.isBlank() || it.referencia.contains(busqueda, ignoreCase = true))
    }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    BackHandler {
        navController.navigate("login") {
            popUpTo("vendedoraMenu") { inclusive = true }
        }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Seguro desea eliminar el producto '${productoAEliminar!!.nombre}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProductoDeFirestore(
                        producto = productoAEliminar!!,
                        onSuccess = {
                            productoAEliminar = null
                            Toast.makeText(context, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { e ->
                            // Para mi: Imprime el error detallado en el Logcat para que sepas qué pasó.
                            Log.e("VendedoraMenu", "Error al eliminar producto", e)

                            // Para el usuario: Muestra un mensaje amigable y genérico.
                            productoAEliminar = null
                            Toast.makeText(context, "No se pudo eliminar el producto. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                        }
                    )
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Botón de solicitudes con globo animado
                Box {
                    FloatingActionButton(
                        onClick = {
                            viewModel.tieneSolicitudesNuevas = false
                            navController.navigate("vendedoraSolicitudes")
                        }
                    ) {
                        Icon(Icons.Default.List, contentDescription = "Solicitudes")
                    }

                    if (viewModel.tieneSolicitudesNuevas) {
                        var saltoPorPx by remember { mutableStateOf(0.dp) }
                        val animacionSalto by animateDpAsState(
                            targetValue = saltoPorPx,
                            label = "AnimacionGlobo"
                        )

                        LaunchedEffect(viewModel.tieneSolicitudesNuevas) {
                            while (viewModel.tieneSolicitudesNuevas) {
                                saltoPorPx = 6.dp
                                delay(300)
                                saltoPorPx = 0.dp
                                delay(300)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp - animacionSalto)
                                .background(Color.Red, shape = CircleShape)
                        )
                    }
                }

                // Botón de agregar mercancía
                FloatingActionButton(
                    onClick = { navController.navigate("agregarProducto") },
                ) {
                    Text("   + AGREGAR MERCANCIA   ", color = Color.DarkGray)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // LOGO + BUSCADOR + CATEGORÍAS
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_kiwi),
                        contentDescription = "Logo Kiwi",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(end = 12.dp)
                    )

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = {
                            if (it.matches(Regex("[a-zA-Z0-9#\\-_.]*"))) busqueda = it
                        },
                        label = { Text("Nº. Referencia", fontSize = 12.sp) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        trailingIcon = {
                            if (busqueda.isNotEmpty()) {
                                IconButton(onClick = { busqueda = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categorias) { categoria ->
                        val seleccionado = categoria == categoriaSeleccionada
                        Button(
                            onClick = {
                                categoriaSeleccionada = if (seleccionado) null else categoria
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (seleccionado) Color.Black else Color.Gray
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(text = categoria, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // FEED DE PRODUCTOS
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (productosFiltrados.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay mercancía disponible", color = Color.Gray)
                        }
                    }
                } else {
                    items(productosFiltrados) { producto ->
                        Card(
                            // Al hacer clic en la tarjeta, navegar a la pantalla de edición
                            onClick = {
                                navController.navigate("editarProducto/${producto.referencia}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                val imagenPainter = producto.imagenUrl?.let {
                                    rememberAsyncImagePainter(it)
                                }

                                if (imagenPainter != null) {
                                    Image(
                                        painter = imagenPainter,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(end = 16.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_kiwi),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(end = 16.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                                    Text(producto.referencia, style = MaterialTheme.typography.bodySmall)
                                    Text("Precio: \$${String.format("%.2f", producto.precio)}", style = MaterialTheme.typography.bodySmall)
                                    val stockVisible = producto.stock - producto.cantidadReservada
                                    Text(
                                        if (stockVisible > 0) "Docenas: $stockVisible" else "Agotado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (stockVisible > 0) Color.Unspecified else Color.Red
                                    )
                                }

                                IconButton(onClick = {
                                    productoAEliminar = producto
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar producto",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AgregarProductoScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val nombre = remember { mutableStateOf("") }
    val referencia = remember { mutableStateOf("") }
    val categoria = remember { mutableStateOf("") }
    val categoriasDisponibles = listOf("Suéter", "Blusa", "Jeans", "Enterizo", "Conjunto", "Short", "Falda", "Body", "Traje", "Licra", "Traje de baño", "Mallas")
    val docena = remember { mutableStateOf("") }
    val precio = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val tallasAgregadas = remember { mutableStateListOf<String>() }
    var nuevaTallaInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    var imagenUri: Uri? by remember { mutableStateOf(null) } // La URI local seleccionada
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    BackHandler {
        navController.navigate("vendedoraMenu") {
            popUpTo("agregarProducto") { inclusive = true }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Agregar Producto")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_kiwi),
                                contentDescription = "Logo Kiwi",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("vendedoraMenu") {
                            popUpTo("agregarProducto") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = nombre.value,
                    onValueChange = {
                        if (it.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*"))) nombre.value = it
                    },
                    label = { Text("Nombre de prenda") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = referencia.value,
                    onValueChange = {
                        if (it.matches(Regex("[a-zA-Z0-9#\\-_.]*"))) referencia.value = it
                    },
                    label = { Text("Referencia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                var expandedCategoriaDropdown by remember { mutableStateOf(false) }

                LaunchedEffect(isPressed) {
                    if (isPressed) expandedCategoriaDropdown = true
                }

                OutlinedTextField(
                    value = categoria.value,
                    onValueChange = {},
                    label = { Text("Categoría") },
                    readOnly = true,
                    enabled = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expandedCategoriaDropdown,
                    onDismissRequest = { expandedCategoriaDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoriasDisponibles.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                categoria.value = opcion
                                expandedCategoriaDropdown = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .wrapContentSize()
                        .align(Alignment.CenterHorizontally)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imagenUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imagenUri),
                            contentDescription = "Imagen seleccionada",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 250.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Toca para agregar imagen", color = Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Agregar conjuntos de tallas:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevaTallaInput,
                        onValueChange = { nuevaTallaInput = it },
                        label = { Text("Ej: S, M, L, XL") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (nuevaTallaInput.isNotBlank() && !tallasAgregadas.contains(nuevaTallaInput)) {
                                tallasAgregadas.add(nuevaTallaInput.trim())
                                nuevaTallaInput = ""
                            }
                            keyboardController?.hide()
                        })
                    )
                    Button(
                        onClick = {
                            if (nuevaTallaInput.isNotBlank() && !tallasAgregadas.contains(nuevaTallaInput)) {
                                tallasAgregadas.add(nuevaTallaInput.trim())
                                nuevaTallaInput = ""
                            } else if (nuevaTallaInput.isNotBlank() && tallasAgregadas.contains(nuevaTallaInput)) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Esa talla ya ha sido agregada.")
                                }
                            }
                        },
                        enabled = nuevaTallaInput.isNotBlank()
                    ) {
                        Text("+")
                    }
                }

                if (tallasAgregadas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Conjuntos de tallas:")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        tallasAgregadas.forEach { tallaConjunto ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(tallaConjunto) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { tallasAgregadas.remove(tallaConjunto) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar talla",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay conjuntos de tallas agregados. Agregue uno.", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = docena.value,
                        onValueChange = { if (it.matches(Regex("\\d*"))) docena.value = it },
                        label = { Text("Docenas") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = precio.value,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) { // Regex mejorado
                                precio.value = it
                            }
                        },
                        label = { Text("Precio x Docena") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                val parsedPrice = precio.value.toDoubleOrNull()
                                if (parsedPrice != null) {
                                    precio.value = String.format(java.util.Locale.getDefault(), "%.2f", parsedPrice)
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón AGREGAR
            Button(
                onClick = {
                    // La lógica de validación se mantiene igual
                    val newNombre = nombre.value.trim()
                    val newReferencia = referencia.value.trim()
                    val newCategoria = categoria.value.trim()
                    val newDocena = docena.value.toIntOrNull()
                    val newPrecio = precio.value.toDoubleOrNull()

                    if (
                        newNombre.isBlank() || newReferencia.isBlank() || newCategoria.isBlank() ||
                        tallasAgregadas.isEmpty() || newDocena == null || newDocena < 0 ||
                        newPrecio == null || newPrecio <= 0
                    ) {
                        Toast.makeText(context, "Completa todos los campos...", Toast.LENGTH_LONG).show()
                    } else {
                        isLoading = true
                        val nuevoProducto = ProductoDetalle(
                            idFirestore = null,
                            nombre = newNombre,
                            referencia = newReferencia,
                            categoria = newCategoria,
                            tallas = tallasAgregadas.toList(),
                            precio = newPrecio,
                            stock = newDocena,
                            imagenUrl = null
                        )

                        viewModel.guardarNuevoProductoEnFirestore(
                            producto = nuevoProducto,
                            imagenUri = imagenUri,
                            onSuccess = {
                                isLoading = false
                                mostrarDialogoConfirmacion = true
                            },
                            onFailure = { e ->
                                isLoading = false
                                Log.e("AgregarProducto", "Error al guardar producto: ${e.message}")
                                Toast.makeText(context, "Error al guardar producto: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("AGREGAR", color = Color.White)
                }
            }
        }
    }
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("Mercancía agregada")
            },
            text = {
                Text("¡Mercancía agregada exitosamente!")
            },
            confirmButton = {} // Sin botón, solo cierra automático
        )

        LaunchedEffect(Unit) {
            delay(2000)
            mostrarDialogoConfirmacion = false
            // Opcional: Navegar a otra pantalla después de agregar, como la lista de productos
            navController.navigate("vendedoraMenu") { // Navegar al menú de vendedora
                popUpTo("agregarProducto") { inclusive = true } // Eliminar esta pantalla del back stack
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesVendedoraScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val solicitudes: SnapshotStateList<SharedViewModel.Solicitud> = viewModel.solicitudes
    var estadoSeleccionado by remember { mutableStateOf<String?>(null) }
    val currentSortDirection by viewModel.solicitudesSortDirection.collectAsState()
    val tarjetasExpandibles = remember { mutableStateMapOf<String, Boolean>() }
    val botonesVisibles = remember { mutableStateMapOf<String, Boolean>() }
    val solicitudesFiltradas = solicitudes
        .filter { estadoSeleccionado == null || it.estado == estadoSeleccionado }

    BackHandler {
        navController.navigate("vendedoraMenu") {
            popUpTo("vendedoraSolicitudes") { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.tieneSolicitudesNuevas = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Solicitudes")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_kiwi),
                                contentDescription = "Logo Kiwi",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("vendedoraMenu") {
                            popUpTo("vendedoraSolicitudes") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Filtros de estado
                val estados = listOf("Aceptada", "Pendiente", "Devuelta")
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
                {
                    items(estados) { estado ->
                        val activo = estado == estadoSeleccionado
                        FilterChip(
                            selected = activo,
                            onClick = {
                                estadoSeleccionado = if (activo) null else estado
                            },
                            label = { Text(estado) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de orden
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ordenar por fecha:", modifier = Modifier.weight(1f))
                    // Connect DropdownMenuBox to the ViewModel
                    DropdownMenuBox(
                        ordenAscendente = currentSortDirection == Query.Direction.ASCENDING,
                        onOrdenChange = { nuevoOrden ->
                            val direction = if (nuevoOrden) Query.Direction.ASCENDING else Query.Direction.DESCENDING
                            viewModel.setSolicitudesSortDirection(direction)
                        }
                    )

                }
                Spacer(modifier = Modifier.height(12.dp))
            }


            // Lista de solicitudes
            if (solicitudesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay solicitudes registradas", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // <--- IMPORTANT CHANGE HERE: Use idFirestore as key
                    items(solicitudesFiltradas, key = { it.idFirestore!! }) { solicitud ->
                        // We use '!!' here because if the request comes from Firestore, its idFirestore should NOT be null.
                        // If, for any reason, a Solicitud without idFirestore could reach here,
                        // you should use: key = { it.idFirestore ?: it.id.toString() }
                        // But ideally, all displayed requests should already have an idFirestore.

                        // <--- IMPORTANT CHANGE HERE: Use idFirestore for maps
                        val currentIdForKey = solicitud.idFirestore!!

                        LaunchedEffect(currentIdForKey, solicitud.estado) {
                            if (!tarjetasExpandibles.containsKey(currentIdForKey)) {
                                tarjetasExpandibles[currentIdForKey] = false
                            }
                            if (!botonesVisibles.containsKey(currentIdForKey)) {
                                botonesVisibles[currentIdForKey] = solicitud.estado == "Pendiente"
                            } else {
                                // If the request status changes externally, update visibility
                                botonesVisibles[currentIdForKey] = solicitud.estado == "Pendiente"
                            }
                        }

                        val expandida = tarjetasExpandibles[currentIdForKey] ?: false
                        val showButtons = botonesVisibles[currentIdForKey] ?: false

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tarjetasExpandibles[currentIdForKey] = !expandida
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Cliente: ${solicitud.comprador.ifBlank { "Desconocido" }}")
                                    Text("Celular: ${solicitud.celular.ifBlank { "No disponible" }}")
                                    Text("Docenas solicitadas: ${solicitud.productos.sumOf { it.cantidad }}")

                                    if (expandida) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        solicitud.productos.forEach { (productoDetalle, cantidad) ->
                                            val referencia = productoDetalle?.referencia
                                            val nombreProducto = productoDetalle?.nombre

                                            if (nombreProducto?.isNotBlank() == true && referencia?.isNotBlank() == true){
                                                Text("⬤ $nombreProducto |$referencia| - ($cantidad)", style = MaterialTheme.typography.bodyMedium) // Ensure MaterialTheme.typography
                                            } else {
                                                Text("* Producto inválido o incompleto", style = MaterialTheme.typography.bodyMedium) // Ensure MaterialTheme.typography
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Fecha: ${solicitud.fecha} ${solicitud.hora}", style = MaterialTheme.typography.bodyMedium) // Ensure MaterialTheme.typography
                                        Text("Total: \$${String.format("%.2f", solicitud.total)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) // Ensure MaterialTheme.typography
                                    }
                                }

                                // Right column: status + buttons (only if expanded and pending)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = solicitud.estado,
                                        color = when (solicitud.estado) {
                                            "Aceptada" -> Color(0xFF2E7D32)
                                            "Pendiente" -> Color(0xFFF9A825)
                                            "Devuelta" -> Color(0xFFD32F2F)
                                            else -> Color.Gray
                                        },
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )

                                    if (expandida && showButtons) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(onClick = {
                                                viewModel.aceptarSolicitud(solicitud)
                                                botonesVisibles[currentIdForKey] = false // Hide instantly
                                                viewModel.tieneSolicitudesNuevas = false
                                            }) {
                                                Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color(0xFF2E7D32))
                                            }
                                            IconButton(onClick = {
                                                viewModel.rechazarSolicitud(solicitud)
                                                botonesVisibles[currentIdForKey] = false // Hide instantly
                                                viewModel.tieneSolicitudesNuevas = false
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Devolver", tint = Color(0xFFD32F2F))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditarProductoScreen(productoReferencia: String, navController: NavHostController, viewModel: SharedViewModel)
{
    // Buscar el producto original
    val productoOriginal = viewModel.productos.find { it.referencia == productoReferencia }

    // Si no se encuentra el producto, mostrar un mensaje de error y regresar
    if (productoOriginal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Producto no encontrado para editar", color = Color.Red)
        }
        LaunchedEffect(Unit) {
            delay(1500)
            navController.popBackStack()
        }
        return
    }

    // Estados para los campos editables, inicializados con los valores del producto original
    val nombre = remember { mutableStateOf(productoOriginal.nombre) }
    val referencia = remember { mutableStateOf(productoOriginal.referencia) } // Considerar si la referencia debería ser editable (clave única)
    val categoria = remember { mutableStateOf(productoOriginal.categoria) }
    val categoriasDisponibles = listOf("Suéter", "Blusa", "Jeans", "Enterizo", "Conjunto", "Short", "Falda", "Body", "Traje", "Licra", "Traje de baño", "Mallas")
    val docena = remember { mutableStateOf(productoOriginal.stock.toString()) }
    val precio = remember { mutableStateOf(String.format(Locale.getDefault(), "%.2f", productoOriginal.precio)) }

    var newImagenUri: Uri? by remember { mutableStateOf(null) }
    var currentImageUrl: String? by remember { mutableStateOf(productoOriginal.imagenUrl) }

    val tallasAgregadas = remember { mutableStateListOf<String>().apply { addAll(productoOriginal.tallas) } }
    var nuevaTallaInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        newImagenUri = uri
        // Cuando se selecciona una nueva imagen, limpiamos la URL actual para que se muestre la nueva URI local
        currentImageUrl = null
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    BackHandler {
        navController.navigate("vendedoraMenu") {
            popUpTo("editarProducto/${productoReferencia}") { inclusive = true }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Editar Producto")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_kiwi),
                                contentDescription = "Logo Kiwi",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("vendedoraMenu") {
                            popUpTo("editarProducto/${productoReferencia}") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scrollable form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Nombre
                OutlinedTextField(
                    value = nombre.value,
                    onValueChange = {
                        if (it.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*"))) nombre.value = it
                    },
                    label = { Text("Nombre de prenda") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Referencia (Normalmente NO editable para evitar romper la clave única, pero lo mantengo si es tu diseño)
                OutlinedTextField(
                    value = referencia.value,
                    onValueChange = {
                        if (it.matches(Regex("[a-zA-Z0-9#\\-_.]*"))) referencia.value = it
                    },
                    label = { Text("Referencia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categoría estilizada (dropdown)
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                var expandedCategoriaDropdown by remember { mutableStateOf(false) }

                LaunchedEffect(isPressed) {
                    if (isPressed) expandedCategoriaDropdown = true
                }

                OutlinedTextField(
                    value = categoria.value,
                    onValueChange = {},
                    label = { Text("Categoría") },
                    readOnly = true,
                    enabled = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expandedCategoriaDropdown,
                    onDismissRequest = { expandedCategoriaDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoriasDisponibles.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                categoria.value = opcion
                                expandedCategoriaDropdown = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bloque de visualización de imagen
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .wrapContentSize()
                        .align(Alignment.CenterHorizontally)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (newImagenUri != null) { // Si hay una nueva URI local seleccionada
                        Image(
                            painter = rememberAsyncImagePainter(newImagenUri),
                            contentDescription = "Imagen seleccionada",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 250.dp)
                        )
                    } else if (currentImageUrl != null) { // Si no, pero hay una URL existente
                        Image(
                            painter = rememberAsyncImagePainter(currentImageUrl),
                            contentDescription = "Imagen actual",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 250.dp)
                        )
                    } else { // Si no hay ninguna imagen
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Toca para agregar imagen", color = Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sección para agregar/editar tallas dinámicamente
                Text("Conjuntos de tallas:")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tallasAgregadas.forEach { tallaConjunto ->
                        InputChip(
                            selected = false,
                            onClick = { /* No hay acción de selección aquí */ },
                            label = { Text(tallaConjunto) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { tallasAgregadas.remove(tallaConjunto) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar talla",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Campo para añadir nuevas tallas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevaTallaInput,
                        onValueChange = { nuevaTallaInput = it },
                        label = { Text("Añadir nueva talla (Ej: S, M, L)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (nuevaTallaInput.isNotBlank() && !tallasAgregadas.contains(nuevaTallaInput.trim())) {
                                tallasAgregadas.add(nuevaTallaInput.trim())
                                nuevaTallaInput = ""
                            } else if (tallasAgregadas.contains(nuevaTallaInput.trim())) {
                                Toast.makeText(context, "Esa talla ya ha sido agregada.", Toast.LENGTH_SHORT).show()
                            }
                            keyboardController?.hide()
                        })
                    )
                    Button(
                        onClick = {
                            if (nuevaTallaInput.isNotBlank() && !tallasAgregadas.contains(nuevaTallaInput.trim())) {
                                tallasAgregadas.add(nuevaTallaInput.trim())
                                nuevaTallaInput = ""
                            } else if (tallasAgregadas.contains(nuevaTallaInput.trim())) {
                                Toast.makeText(context, "Esa talla ya ha sido agregada.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = nuevaTallaInput.isNotBlank()
                    ) {
                        Text("+")
                    }
                }


                Spacer(modifier = Modifier.height(12.dp))

                // Docenas (con botones +/-) y Precio x Docena
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = docena.value,
                            onValueChange = { newValue ->
                                if (newValue.matches(Regex("\\d*"))) {
                                    docena.value = newValue
                                }
                            },
                            label = { Text("Docenas") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Button(
                                onClick = {
                                    val currentDocenas = docena.value.toIntOrNull() ?: 0
                                    docena.value = (currentDocenas + 1).toString()
                                },
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+", color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    val currentDocenas = docena.value.toIntOrNull() ?: 0
                                    if (currentDocenas > 0) {
                                        docena.value = (currentDocenas - 1).toString()
                                    }
                                },
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("-", color = Color.White)
                            }
                        }
                    }

                    // Campo de "Precio x Docena"
                    OutlinedTextField(
                        value = precio.value,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                precio.value = newValue
                            }
                        },
                        label = { Text("Precio x Docena") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide()
                                val parsedPrice = precio.value.toDoubleOrNull()
                                if (parsedPrice != null) {
                                    // Si es un número válido, formatéalo a dos decimales
                                    precio.value = String.format(Locale.getDefault(), "%.2f", parsedPrice)
                                } else if (precio.value.isNotEmpty()) { }
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // Botón GUARDAR CAMBIOS
            Button(
                onClick = {
                    val newNombre = nombre.value.trim()
                    val newReferencia = referencia.value.trim()
                    val newCategoria = categoria.value.trim()
                    val newDocena = docena.value.toIntOrNull()
                    val newPrecio = precio.value.toDoubleOrNull()

                    // Validación
                    if (
                        newNombre.isBlank() || newReferencia.isBlank() || newCategoria.isBlank() ||
                        tallasAgregadas.isEmpty() || newDocena == null || newDocena < 0 || newPrecio == null || newPrecio <= 0
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Completa todos los campos correctamente")
                        }
                    } else {
                        // Creamos un objeto con todos los datos actualizados de los campos.
                        val productoConNuevosDatos = productoOriginal.copy(
                            nombre = newNombre,
                            referencia = newReferencia,
                            categoria = newCategoria,
                            tallas = tallasAgregadas.toList(),
                            precio = newPrecio,
                            stock = newDocena
                        )

                        // Llamamos a la nueva función centralizada en el ViewModel.
                        viewModel.actualizarProductoYGestionarImagen(
                            productoOriginal = productoOriginal,
                            productoActualizado = productoConNuevosDatos,
                            nuevaImagenUri = newImagenUri, // Pasamos la URI de la nueva imagen (puede ser null).
                            onSuccess = {
                                mostrarDialogoConfirmacion = true
                            },
                            onFailure = { e ->
                                Log.e("EditarProducto", "Error al actualizar producto: ${e.message}")
                                Toast.makeText(context, "Error al guardar cambios: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GUARDAR CAMBIOS", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo de confirmación de guardado
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("Producto Actualizado")
            },
            text = {
                Text("¡El producto ha sido actualizado exitosamente!")
            },
            confirmButton = {} // Sin botón, solo cierra automático
        )

        LaunchedEffect(Unit) {
            delay(2000)
            mostrarDialogoConfirmacion = false
            navController.navigate("vendedoraMenu") {
                popUpTo("editarProducto/${productoReferencia}") { inclusive = true }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    KiwiTheme {
        SplashScreen(onFinish = {})
    }
}