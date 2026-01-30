package com.example.kiwi
//ID FIREBASE: kiwi-2025


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.kiwi.ui.theme.KiwiTheme
import com.example.kiwi.viewmodel.EstadoPedido
import com.example.kiwi.viewmodel.SharedViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("new_products")
        FirebaseMessaging.getInstance().subscribeToTopic("pedidos_vendedora")

        setContent {
            KiwiTheme(darkTheme = false) {
                val navController = rememberNavController()
                val sharedViewModel: SharedViewModel = viewModel()
                val rotarCarrito = remember { mutableStateOf(false) }

                val anguloCarrito by animateFloatAsState(
                    targetValue = if (rotarCarrito.value) 360f else 0f,
                    animationSpec = tween(durationMillis = 600),
                    finishedListener = { rotarCarrito.value = false },
                    label = "rotacion"
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val rutaActual = navBackStackEntry?.destination?.route
                val pantallasConBarra = listOf("catalogo", "carrito", "solicitudes", "FacturaScreen")
                val mostrarBarra = rutaActual in pantallasConBarra || rutaActual?.startsWith("detalleProducto") == true
                val usarFondoBlanco = false

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "carga") {
                        composable("carga") { CargaScreen { navController.navigate("login") } }

                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                onCompradorClick = { navController.navigate("catalogo") }
                            )
                        }

                        composable("catalogo") {
                            ClienteScreen(navController, sharedViewModel)
                        }

                        composable("carrito") {
                            CarritoScreen(navController, sharedViewModel)
                        }

                        composable("solicitudes") {
                            SolicitudesScreen(navController, sharedViewModel)
                        }

                        composable("detalleProducto/{productoId}") { backStackEntry ->
                            val productoId = backStackEntry.arguments?.getString("productoId") ?: ""
                            DetalleProductoScreen(
                                productoId,
                                navController,
                                sharedViewModel,
                                rotarCarrito,
                                anguloCarrito
                            )
                        }

                        composable("FacturaScreen") {
                            FacturaScreen(navController, sharedViewModel)
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
                        composable("editarProducto/{productoId}") { backStackEntry ->
                            val productoId = backStackEntry.arguments?.getString("productoId") ?: ""
                            EditarProductoScreen(productoId, navController, sharedViewModel)
                        }
                    }

                    AnimatedVisibility(
                        visible = mostrarBarra,
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(durationMillis = 400, delayMillis = 100)
                        ) + fadeIn(animationSpec = tween(durationMillis = 400)),
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Box(contentAlignment = Alignment.BottomCenter) {

                            if (usarFondoBlanco) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .align(Alignment.BottomCenter),
                                    color = Color.White,
                                    shadowElevation = 15.dp
                                ) {}
                            }

                            Navegacion(
                                selectedTab = if (rutaActual == "FacturaScreen") "carrito" else (rutaActual ?: "catalogo"),
                                onCatalogoClick = {
                                    if (rutaActual != "catalogo") {
                                        navController.navigate("catalogo") {
                                            popUpTo("catalogo") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onCarritoClick = {
                                    if (rutaActual != "carrito") {
                                        navController.navigate("carrito") {
                                            popUpTo("catalogo") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onSolicitudesClick = {
                                    if (rutaActual != "solicitudes") {
                                        navController.navigate("solicitudes") {
                                            popUpTo("catalogo") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                tieneNotificacionSolicitudesComprador = sharedViewModel.tieneActualizacionSolicitudesComprador,
                                rotacionCarrito = anguloCarrito
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CargaScreen(onFinish: () -> Unit)
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
    val mensaje = LocalContext.current
    val tecladoControl = LocalSoftwareKeyboardController.current
    var salir by remember { mutableStateOf(0L) }
    val ejecutar = (LocalContext.current as? ComponentActivity)
    val rotacion = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotacion.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    BackHandler(enabled = true) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - salir < 2000) {

            ejecutar?.finish()
        } else {

            salir = currentTime

            Toast.makeText(
                mensaje,
                "Vuelve a presionar para salir de la aplicación",
                Toast.LENGTH_SHORT
            ).show()
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
                    .graphicsLayer { rotationZ = rotacion.value }
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
                    onDone = { tecladoControl?.hide() }
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
                    onDone = { tecladoControl?.hide() }
                )
            )


            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (usuario.isNotBlank() && contrasena.isNotBlank()) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(usuario, contrasena)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("vendedoraMenu")
                                } else {
                                    Toast.makeText(
                                        mensaje,
                                        "Error de autenticación. Verifica tus credenciales.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            mensaje,
                            "Por favor, ingresa correo y contraseña",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("INGRESAR", color = Color.White)
            }


            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "COMPRADORES",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onCompradorClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
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
    val imagenUrl: String? = null,
    val timestamp: com.google.firebase.Timestamp? = null
) {
    constructor() : this(
        null, 0, "", "", "", emptyList(), 0.0, 0, 0, null, null
    )
}

data class ProductoEnPedido(

    val producto: ProductoDetalle? = null,
    val cantidad: Int = 0
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClienteScreen(
    navegacion: NavHostController,
    viewModel: SharedViewModel
) {
    val categorias = listOf(
        "Suéter", "Blusa", "Jeans", "Enterizo", "Conjunto", "Short",
        "Falda", "Body", "Traje", "Licra", "Traje de baño", "Mallas"
    )
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    val productos = viewModel.productos.sortedByDescending { it.timestamp }
    val productosFiltrados = productos.filter {
        (categoriaSeleccionada == null || it.categoria == categoriaSeleccionada) &&
                (busqueda.isBlank() || it.referencia.contains(busqueda, ignoreCase = true))
    }
    val tecladoControl = LocalSoftwareKeyboardController.current
    val estadoPosicion = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val mostrarBotonSubir by remember {
        derivedStateOf { estadoPosicion.firstVisibleItemIndex > 2 }
    }

    val volver by navegacion.currentBackStackEntryAsState()
    val rutaActual = volver?.destination?.route

    LaunchedEffect(rutaActual) {
        if (rutaActual == "solicitudes") {
            viewModel.tieneActualizacionSolicitudesComprador = false
        }
    }

    BackHandler {
        navegacion.navigate("login") {
            popUpTo("catalogo") { inclusive = true }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF6F6F6)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
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
                                    IconButton(onClick = {
                                        busqueda = ""
                                    }) { Icon(Icons.Default.Close, contentDescription = "Limpiar") }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { tecladoControl?.hide() })
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
                                colors = ButtonDefaults.buttonColors(containerColor = if (seleccionado) Color.Green else Color.Black),
                                modifier = Modifier.height(36.dp)
                            ) { Text(text = categoria, color = Color.White) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (productosFiltrados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay mercancía disponible", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        state = estadoPosicion,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(productosFiltrados, key = { it.referencia }) { producto ->
                            ProductoCard(
                                producto = producto,
                                onClick = { navegacion.navigate("detalleProducto/${producto.referencia}") },
                                esVendedor = false
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = mostrarBotonSubir,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 100.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { estadoPosicion.animateScrollToItem(0) } },
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    shape = CircleShape
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_top),
                        contentDescription = "Subir"
                    )
                }
            }
        }
    }
}


@Composable
fun Navegacion(
    selectedTab: String,
    onCatalogoClick: () -> Unit,
    onCarritoClick: () -> Unit,
    onSolicitudesClick: () -> Unit,
    tieneNotificacionSolicitudesComprador: Boolean = false,
    rotacionCarrito: Float = 0f
) {
    val selectedIndex = remember(selectedTab) {
        when (selectedTab) {
            "catalogo" -> 0
            "carrito" -> 1
            "solicitudes" -> 2
            else -> 0
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        val maxWidth = maxWidth
        val tabWidth = maxWidth / 3
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "indicatorAnimation"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(50),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(50),
            color = Color.Black,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .offset(x = indicatorOffset)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(50))
                            .background(Color.Green)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onCatalogoClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Catálogo",
                            modifier = Modifier.size(28.dp),
                            tint = if (selectedIndex == 0) Color.Black else Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onCarritoClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Carrito",
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer { rotationZ = rotacionCarrito },
                            tint = if (selectedIndex == 1) Color.Black else Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSolicitudesClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Solicitudes",
                                modifier = Modifier.size(28.dp),
                                tint = if (selectedIndex == 2) Color.Black else Color.White
                            )
                            if (tieneNotificacionSolicitudesComprador) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .border(1.dp, Color.Black, CircleShape)
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
fun CarritoScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val productosEnCarrito = viewModel.carrito.reversed()
    val total = productosEnCarrito.sumOf { it.producto?.precio?.times(it.cantidad) ?: 0.0 }
    var itemAEliminarEnCarrito by remember { mutableStateOf<com.example.kiwi.ProductoEnPedido?>(null) }
    val mensaje = LocalContext.current

    BackHandler {
        navController.navigate("catalogo") {
            popUpTo("catalogo") { inclusive = false }
            launchSingleTop = true
        }
    }

    if (itemAEliminarEnCarrito != null) {
        AlertDialog(
            onDismissRequest = { itemAEliminarEnCarrito = null },
            title = { Text("Confirmar eliminación", fontWeight = FontWeight.Bold) },
            text = {
                val (productoDetalle, cantidad) = itemAEliminarEnCarrito!!
                Text("¿Seguro desea eliminar ${cantidad} docenas de ${productoDetalle?.nombre} (${productoDetalle?.referencia}) del carrito?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.carrito.remove(itemAEliminarEnCarrito!!)
                    itemAEliminarEnCarrito = null
                    Toast.makeText(mensaje, "Producto eliminado del carrito", Toast.LENGTH_SHORT)
                        .show()
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

    Scaffold(containerColor = Color(0xFFF6F6F6)) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 100.dp)
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

                if (productosEnCarrito.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "El carrito está vacío",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(productosEnCarrito) { productoEnPedido ->
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
                                    if (productoDetalle?.imagenUrl != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(productoDetalle.imagenUrl)
                                                    .crossfade(true)
                                                    .build()
                                            ),
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
                                        Text(productoDetalle?.nombre ?: "Producto Desconocido")
                                        Text("Ref: ${productoDetalle?.referencia ?: "N/A"}")
                                        Text("Cantidad: $cantidad")
                                        Text(
                                            "Precio x docena: \$${
                                                String.format(
                                                    "%.2f",
                                                    productoDetalle?.precio ?: 0.0
                                                )
                                            }"
                                        )
                                    }
                                    IconButton(onClick = {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(16.dp)
            ){
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Divider()
                    Text(
                        "Total: \$${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
                            containerColor = if (carritoVacio) Color.Gray else Color.Black
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
fun SolicitudesScreen(navController: NavHostController, viewModel: SharedViewModel) {
    val solicitudes = viewModel.solicitudes
    var estadoSeleccionado by remember { mutableStateOf<String?>(null) }
    val ordenEstandar by viewModel.solicitudesSortDirection.collectAsState()
    val solicitudesFiltradas = solicitudes
        .filter { estadoSeleccionado == null || it.estado == estadoSeleccionado }
    var mostrarDialogoMotivo by remember { mutableStateOf(false) }
    var motivoAMostrar by remember { mutableStateOf("") }

    BackHandler {
        navController.navigate("catalogo") {
            popUpTo("solicitudes") { inclusive = true }
            popUpTo("catalogo") { inclusive = false }
            launchSingleTop = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.tieneActualizacionSolicitudesComprador = false
    }

    if (mostrarDialogoMotivo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoMotivo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Motivo de devolución", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = if (motivoAMostrar.isNotBlank()) motivoAMostrar else "No se especificó un motivo detallado.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoMotivo = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    Scaffold(containerColor = Color(0xFFF6F6F6)) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.logo_kiwi),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val estados = listOf("Aceptada", "Pendiente", "Devuelta")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(estados) { estado ->
                            val activo = estado == estadoSeleccionado
                            val colorFondo = if (activo) {
                                when (estado) {
                                    "Aceptada" -> Color(0xFF2E7D32)
                                    "Pendiente" -> Color(0xFFF9A825)
                                    "Devuelta" -> Color(0xFFD32F2F)
                                    else -> Color.Black
                                }
                            } else {
                                Color.Black
                            }

                            Button(
                                onClick = { estadoSeleccionado = if (activo) null else estado },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorFondo,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(36.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                            ) {
                                Text(text = estado, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ordenar por fecha:", modifier = Modifier.weight(1f))
                        Ordenamiento(
                            ordenAscendente = ordenEstandar == Query.Direction.ASCENDING,
                            onOrdenChange = { nuevoOrden ->
                                val direccion =
                                    if (nuevoOrden) Query.Direction.ASCENDING else Query.Direction.DESCENDING
                                viewModel.setSolicitudesSortDirection(direccion)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (solicitudesFiltradas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay solicitudes registradas", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(solicitudesFiltradas) { solicitud ->
                            val context = LocalContext.current
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
                                            val nombre = productoDetalle?.nombre ?: "Desconocido"
                                            val ref = productoDetalle?.referencia ?: "N/A"
                                            Text(
                                                "Prenda: $nombre",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "Referencia: $ref",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "Docenas: $cantidadPedida",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "Precio x Docena: \$%.2f".format(
                                                    productoDetalle?.precio ?: 0.0
                                                ), style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Text(
                                            "Total: \$${String.format("%.2f", solicitud.total)}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Fecha: ${solicitud.fecha} ${solicitud.hora}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.padding(start = 12.dp)
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
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (solicitud.estado == "Aceptada") {
                                            IconButton(onClick = {
                                                com.example.kiwi.utils.PdfGenerator(
                                                    context
                                                ).generarFactura(solicitud)
                                            }) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_descarga_pdf),
                                                    contentDescription = "Descargar Comprobante",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Text("Comprobante", fontSize = 9.sp, color = Color.Gray)

                                        } else if (solicitud.estado == "Devuelta") {
                                            IconButton(onClick = {
                                                motivoAMostrar = solicitud.motivoRechazo
                                                mostrarDialogoMotivo = true
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Ver Motivo",
                                                    tint = Color(0xFFD32F2F),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                            Text("Ver motivo", fontSize = 9.sp, color = Color.Gray)
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


@Composable
fun Ordenamiento(
    ordenAscendente: Boolean,
    onOrdenChange: (Boolean) -> Unit
) {
    var expandida by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expandida = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text(if (ordenAscendente) "Ascendente" else "Descendente")
        }
        DropdownMenu(expanded = expandida, onDismissRequest = { expandida = false }) {
            DropdownMenuItem(
                text = { Text("Ascendente") },
                onClick = {
                    expandida = false
                    if (!ordenAscendente) onOrdenChange(true)
                }
            )
            DropdownMenuItem(
                text = { Text("Descendente") },
                onClick = {
                    expandida = false
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
    navegacion: NavHostController,
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
    val mensaje = LocalContext.current
    var botonExitoso by remember { mutableStateOf(false) }

    BackHandler {
        navegacion.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_kiwi),
                contentDescription = "Logo Kiwi",
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(0.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = producto.referencia,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (producto.imagenUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(producto.imagenUrl)
                            .crossfade(true)
                            .build()
                    ),
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
                Text(
                    "Seleccione una talla",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
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
                    Text(
                        "\$${String.format("%.2f", producto.precio)}",
                        style = MaterialTheme.typography.titleMedium
                    )
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {

            val colorFondo by animateColorAsState(
                targetValue = if (botonExitoso) Color.Green else Color.Black,
                label = "color",
                animationSpec = tween(durationMillis = 500)
            )

            ExtendedFloatingActionButton(
                onClick = {
                    val cantidadFinal = cantidadTexto.toIntOrNull() ?: 0
                    val stockRealmenteDisponible = producto.stock - producto.cantidadReservada
                    val sinTalla = tipoTallaSeleccionada == null
                    val cantidadInvalida = cantidadTexto.isBlank() || cantidadFinal <= 0

                    when {
                        sinTalla && cantidadInvalida -> Toast.makeText(
                            mensaje,
                            "Seleccione una talla y agregue una cantidad",
                            Toast.LENGTH_SHORT
                        ).show()

                        sinTalla -> Toast.makeText(
                            mensaje,
                            "Seleccione una talla",
                            Toast.LENGTH_SHORT
                        ).show()

                        cantidadInvalida -> Toast.makeText(
                            mensaje,
                            "Agregue la cantidad que desea",
                            Toast.LENGTH_SHORT
                        ).show()

                        cantidadFinal > stockRealmenteDisponible -> Toast.makeText(
                            mensaje,
                            "No hay suficientes docenas disponibles (${stockRealmenteDisponible} restantes).",
                            Toast.LENGTH_LONG
                        ).show()

                        else -> {
                            if ((producto.stock - producto.cantidadReservada) > 0) {
                                viewModel.agregarAlCarrito(producto, cantidadFinal)
                                cantidadTexto = ""
                                tipoTallaSeleccionada = null
                                botonExitoso = true

                                composableScope.launch {
                                    rotarCarrito.value = true
                                    delay(2000)
                                    botonExitoso = false
                                }
                            }
                        }
                    }
                },
                containerColor = colorFondo,
                contentColor = Color.White
            ) {
                AnimatedContent(targetState = botonExitoso, label = "icono") { esExito ->
                    if (esExito) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("¡Agregado!", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar al carrito")
                        }
                    }
                }
            }
        }
    }

    if (imagenAmpliada) {
        Dialog(
            onDismissRequest = { imagenAmpliada = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            var escalaImg by remember { mutableStateOf(1f) }
            var achicarImg by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            escalaImg = (escalaImg * zoom).coerceIn(1f, 4f)
                            if (escalaImg > 1f) {
                                val volverAchicar = achicarImg + pan
                                achicarImg = volverAchicar
                            } else {
                                achicarImg = Offset.Zero
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (escalaImg > 1f) {
                                    escalaImg = 1f
                                    achicarImg = Offset.Zero
                                } else {
                                    escalaImg = 2f
                                }
                            },
                        )
                    }
            ) {
                IconButton(
                    onClick = { imagenAmpliada = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .zIndex(2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }

                val painter = producto.imagenUrl?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(id = R.drawable.logo_kiwi)

                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = escalaImg,
                            scaleY = escalaImg,
                            translationX = achicarImg.x,
                            translationY = achicarImg.y
                        )
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
            title = { Text("Mercancía agregada", fontWeight = FontWeight.Bold) },
            text = { Text("Su mercancía ha sido agregada al carrito") },
            confirmButton = {}
        )

        LaunchedEffect(Unit) {
            delay(2000)
            mostrarDialogoConfirmacion = false
        }
    }
}


@Composable
fun FacturaScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val nombre = remember { mutableStateOf("") }
    val celular = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val tecladoControl = LocalSoftwareKeyboardController.current
    val mensaje = LocalContext.current
    var mensajeError by remember { mutableStateOf<String?>(null) }


    when (val estado = viewModel.estadoPedido) {
        is EstadoPedido.Success -> {
            AlertDialog(
                onDismissRequest = { },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                title = { Text("Pedido enviado", fontWeight = FontWeight.Bold) },
                text = { Text("Su pedido ha sido enviado con éxito y será revisado por la vendedora.") },
                confirmButton = { }
            )
            LaunchedEffect(Unit) {
                delay(2000)
                viewModel.resetEstadoPedido()
                navController.navigate("catalogo") {
                    popUpTo("FacturaScreen") { inclusive = true }
                }
            }
        }

        is EstadoPedido.Conflict -> {
            val conflicto = estado.info
            AlertDialog(
                onDismissRequest = { viewModel.resetEstadoPedido() },
                title = { Text("Inventario insuficiente", fontWeight = FontWeight.Bold) },
                text = {
                    val listaProductos =
                        conflicto.productosAgotados.joinToString(separator = "\n") { item ->
                            "${item.producto?.nombre ?: "Sin nombre"} - ${item.producto?.referencia ?: "N/A"}"
                        }
                    Text("Productos agotados:\n\n$listaProductos")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetEstadoPedido()
                            navController.navigate("carrito") {
                                popUpTo("carrito") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Editar pedido")
                    }
                }
            )
        }

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

        is EstadoPedido.Failure -> {
            LaunchedEffect(estado) {
                mensajeError = estado.error
            }
        }
        else -> {}
    }

    if (mensajeError != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetEstadoPedido()
                mensajeError = null
            },
            title = { Text("Error", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_kiwi),
                contentDescription = "Logo Kiwi",
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Datos para factura no fiscal", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Coloque sus datos para generar su comprobante de mercancía",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre.value,
                onValueChange = {
                    if (it.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*"))) nombre.value = it
                },
                label = { Text("Nombre y Apellido") },
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
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { tecladoControl?.hide() })
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    val nombreLleno = nombre.value.isNotBlank()
                    val celularLleno = celular.value.isNotBlank()
                    val emailLleno = email.value.isNotBlank()

                    val nombreFormatoValido =
                        nombre.value.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+\$"))
                    val celularFormatoValido = celular.value.matches(Regex("^\\+?\\d{8,15}\$"))
                    val emailFormatoValido =
                        email.value.endsWith("@gmail.com") || email.value.endsWith("@outlook.com") || email.value.endsWith(
                            "@hotmail.com"
                        ) || email.value.endsWith("@icloud.com") || email.value.endsWith("@yahoo.com")

                    when {
                        !nombreLleno -> Toast.makeText(mensaje, "Introduzca su nombre", Toast.LENGTH_SHORT).show()
                        !celularLleno -> Toast.makeText(mensaje, "Introduzca su celular", Toast.LENGTH_SHORT).show()
                        !emailLleno -> Toast.makeText(mensaje, "Introduzca su email", Toast.LENGTH_SHORT).show()
                        !nombreFormatoValido -> Toast.makeText(mensaje, "El nombre solo debe contener letras", Toast.LENGTH_SHORT).show()
                        !celularFormatoValido -> Toast.makeText(mensaje, "El celular debe tener entre 8 y 15 dígitos", Toast.LENGTH_SHORT).show()
                        !emailFormatoValido -> Toast.makeText(mensaje, "El email debe ser uno válido", Toast.LENGTH_SHORT).show()
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendedoraMenuScreen(navController: NavHostController, viewModel: SharedViewModel)
{
    val categorias = listOf(
        "Suéter", "Blusa", "Jeans", "Enterizo", "Conjunto", "Short",
        "Falda", "Body", "Traje", "Licra", "Traje de baño", "Mallas"
    )
    var productoAEliminar by remember { mutableStateOf<ProductoDetalle?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    val productos = viewModel.productos.sortedByDescending { it.timestamp }
    val productosFiltrados = productos.filter {
        (categoriaSeleccionada == null || it.categoria == categoriaSeleccionada) &&
                (busqueda.isBlank() || it.referencia.contains(busqueda, ignoreCase = true))
    }
    val estadoPosicion = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val mostrarBotonSubir by remember {
        derivedStateOf { estadoPosicion.firstVisibleItemIndex > 2 }
    }
    val tecladoControl = LocalSoftwareKeyboardController.current
    val mensaje = LocalContext.current

    BackHandler {
        navController.navigate("login") {
            popUpTo("vendedoraMenu") { inclusive = true }
        }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Confirmar eliminación", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro desea eliminar el producto '${productoAEliminar!!.nombre}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProductoDeFirestore(
                        producto = productoAEliminar!!,
                        onSuccess = {
                            productoAEliminar = null
                            Toast.makeText(
                                mensaje,
                                "Producto eliminado exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { e ->
                            Log.e("VendedoraMenu", "Error al eliminar producto", e)
                            productoAEliminar = null
                            Toast.makeText(
                                mensaje,
                                "No se pudo eliminar el producto.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }) { Text("Sí") }
            },
            dismissButton = { TextButton(onClick = { productoAEliminar = null }) { Text("No") } }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF6F6F6),
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box {
                    FloatingActionButton(
                        onClick = {
                            viewModel.tieneSolicitudesNuevas = false
                            navController.navigate("vendedoraSolicitudes")
                        },
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ) { Icon(Icons.Default.List, contentDescription = "Solicitudes") }

                    if (viewModel.tieneSolicitudesNuevas) {
                        var saltoPorPx by remember { mutableStateOf(0.dp) }
                        val animacionSalto by animateDpAsState(
                            targetValue = saltoPorPx,
                            label = "AnimacionGlobo",
                            animationSpec = infiniteRepeatable(
                                animation = tween(300),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        LaunchedEffect(viewModel.tieneSolicitudesNuevas) { saltoPorPx = 6.dp }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp - animacionSalto)
                                .background(Color.Green, shape = CircleShape)
                        )
                    }
                }
                FloatingActionButton(onClick = { navController.navigate("agregarProducto") },
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    Text("   + AGREGAR MERCANCIA   ")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
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
                                    IconButton(onClick = {
                                        busqueda = ""
                                    }) { Icon(Icons.Default.Close, contentDescription = "Limpiar") }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { tecladoControl?.hide() }),
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
                                colors = ButtonDefaults.buttonColors(containerColor = if (seleccionado) Color.Green else Color.Black),
                                modifier = Modifier.height(36.dp)
                            ) { Text(text = categoria, color = Color.White) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                LazyColumn(
                    state = estadoPosicion,
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
                            ProductoCard(
                                producto = producto,
                                onClick = { navController.navigate("editarProducto/${producto.referencia}") },
                                esVendedor = true,
                                onDeleteClick = { productoAEliminar = producto }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(140.dp)) }
                    }
                }
            }
            AnimatedVisibility(
                visible = mostrarBotonSubir,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 20.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { estadoPosicion.animateScrollToItem(0) } },
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_top),
                        contentDescription = "Subir"
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AgregarProductoScreen(navController: NavHostController, viewModel: SharedViewModel) {
    val nombre = remember { mutableStateOf("") }
    val referencia = remember { mutableStateOf("") }
    val categoria = remember { mutableStateOf("") }
    val categoriasDisponibles = listOf(
        "Suéter",
        "Blusa",
        "Jeans",
        "Enterizo",
        "Conjunto",
        "Short",
        "Falda",
        "Body",
        "Traje",
        "Licra",
        "Traje de baño",
        "Mallas"
    )
    val docena = remember { mutableStateOf("") }
    val precio = remember { mutableStateOf("") }
    val tallasAgregadas = remember { mutableStateListOf<String>() }
    var nuevaTallaInput by remember { mutableStateOf("") }
    val mensaje = LocalContext.current
    var imagenUri: Uri? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    val tecladoControl = LocalSoftwareKeyboardController.current
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var estaCargando by remember { mutableStateOf(false) }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(containerColor = Color(0xFFF6F6F6),
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
                        onDone = { tecladoControl?.hide() }
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
                        onDone = { tecladoControl?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val interaccion = remember { MutableInteractionSource() }
                val esPresionado by interaccion.collectIsPressedAsState()
                var expansionCategoria by remember { mutableStateOf(false) }

                LaunchedEffect(esPresionado) {
                    if (esPresionado) expansionCategoria = true
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
                    interactionSource = interaccion,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expansionCategoria,
                    onDismissRequest = { expansionCategoria = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoriasDisponibles.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                categoria.value = opcion
                                expansionCategoria = false
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
                            if (nuevaTallaInput.isNotBlank() && !tallasAgregadas.contains(
                                    nuevaTallaInput
                                )
                            ) {
                                tallasAgregadas.add(nuevaTallaInput.trim())
                                nuevaTallaInput = ""
                            }
                            tecladoControl?.hide()
                        })
                    )
                    Button(
                        onClick = {
                            if (nuevaTallaInput.isNotBlank() && !tallasAgregadas.contains(
                                    nuevaTallaInput
                                )
                            ) {
                                tallasAgregadas.add(nuevaTallaInput.trim())
                                nuevaTallaInput = ""
                            } else if (nuevaTallaInput.isNotBlank() && tallasAgregadas.contains(
                                    nuevaTallaInput
                                )
                            ) {
                                Toast.makeText(
                                    mensaje,
                                    "Esa talla ya ha sido agregada.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = nuevaTallaInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.White
                        )
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
                    ) {
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
                            onDone = { tecladoControl?.hide() }
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
                                tecladoControl?.hide()
                                val parsedPrice = precio.value.toDoubleOrNull()
                                if (parsedPrice != null) {
                                    precio.value = String.format(
                                        java.util.Locale.getDefault(),
                                        "%.2f",
                                        parsedPrice
                                    )
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

            }
            Button(
                onClick = {
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
                        Toast.makeText(mensaje, "Completa todos los campos", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        estaCargando = true
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
                                estaCargando = false
                                mostrarDialogoConfirmacion = true
                            },
                            onFailure = { e ->
                                estaCargando = false
                                Log.e("AgregarProducto", "Error al guardar producto: ${e.message}")
                                Toast.makeText(
                                    mensaje,
                                    "Error al guardar producto: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                enabled = !estaCargando
            ) {
                if (estaCargando) {
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
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = {
                Text("Mercancía agregada", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("¡Mercancía agregada exitosamente!")
            },
            confirmButton = {}
        )

        LaunchedEffect(Unit) {
            delay(2000)
            mostrarDialogoConfirmacion = false
            navController.navigate("vendedoraMenu") {
                popUpTo("agregarProducto") {
                    inclusive = true
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SolicitudesVendedoraScreen(navController: NavHostController, viewModel: SharedViewModel) {
    val solicitudes: SnapshotStateList<SharedViewModel.Solicitud> = viewModel.solicitudes
    var estadoSeleccionado by remember { mutableStateOf<String?>(null) }
    val ordenSolicitudes by viewModel.solicitudesSortDirection.collectAsState()
    val tarjetasExpandibles = remember { mutableStateMapOf<String, Boolean>() }
    val botonesVisibles = remember { mutableStateMapOf<String, Boolean>() }
    val solicitudesFiltradas = solicitudes
        .filter { estadoSeleccionado == null || it.estado == estadoSeleccionado }
    val mensaje = LocalContext.current
    var solicitudARechazar by remember { mutableStateOf<SharedViewModel.Solicitud?>(null) }
    var motivoRechazo by remember { mutableStateOf("") }

    BackHandler {
        navController.popBackStack()
    }

    LaunchedEffect(Unit) {
        viewModel.tieneSolicitudesNuevas = false
    }

    if (solicitudARechazar != null) {
        AlertDialog(
            onDismissRequest = {
                solicitudARechazar = null
                motivoRechazo = ""
            },
            title = { Text("Devolver Solicitud", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Indique el motivo del rechazo:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it },
                        label = { Text("Escribe el motivo aquí") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 5,
                        singleLine = false,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rechazarSolicitud(solicitudARechazar!!, motivoRechazo)
                        botonesVisibles[solicitudARechazar!!.idFirestore!!] = false
                        viewModel.tieneSolicitudesNuevas = false

                        solicitudARechazar = null
                        motivoRechazo = ""
                        val toast =
                            Toast.makeText(mensaje, "Solicitud devuelta", Toast.LENGTH_SHORT)
                        toast.setGravity(
                            android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL,
                            0,
                            100
                        )
                        toast.show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Devolver Pedido")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    solicitudARechazar = null
                    motivoRechazo = ""
                }) {
                    Text("Cancelar")
                }
            },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
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

                val estados = listOf("Aceptada", "Pendiente", "Devuelta")

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(estados) { estado ->
                        val activo = estado == estadoSeleccionado
                        val colorFondo = if (activo) {
                            when (estado) {
                                "Aceptada" -> Color(0xFF2E7D32)
                                "Pendiente" -> Color(0xFFF9A825)
                                "Devuelta" -> Color(0xFFD32F2F)
                                else -> Color.Black
                            }
                        } else {
                            Color.Black
                        }

                        Button(
                            onClick = { estadoSeleccionado = if (activo) null else estado },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorFondo,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Text(text = estado, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ordenar por fecha:", modifier = Modifier.weight(1f))
                    Ordenamiento(
                        ordenAscendente = ordenSolicitudes == Query.Direction.ASCENDING,
                        onOrdenChange = { nuevoOrden ->
                            val direction =
                                if (nuevoOrden) Query.Direction.ASCENDING else Query.Direction.DESCENDING
                            viewModel.setSolicitudesSortDirection(direction)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

            }

            if (solicitudesFiltradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes registradas", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(solicitudesFiltradas, key = { it.idFirestore!! }) { solicitud ->
                        val idActual = solicitud.idFirestore!!

                        LaunchedEffect(idActual, solicitud.estado) {
                            if (!tarjetasExpandibles.containsKey(idActual)) tarjetasExpandibles[idActual] =
                                false
                            botonesVisibles[idActual] = solicitud.estado == "Pendiente"
                        }

                        val expandida = tarjetasExpandibles[idActual] ?: false
                        val mostrarBotones = botonesVisibles[idActual] ?: false

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tarjetasExpandibles[idActual] = !expandida },
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
                                            val nombre = productoDetalle?.nombre
                                            val ref = productoDetalle?.referencia
                                            if (!nombre.isNullOrBlank() && !ref.isNullOrBlank()) {
                                                Text(
                                                    "⬤ $nombre |$ref| - ($cantidad)",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            } else {
                                                Text(
                                                    "* Producto inválido",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Fecha: ${solicitud.fecha} ${solicitud.hora}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Total: \$${String.format("%.2f", solicitud.total)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (solicitud.estado == "Devuelta" && solicitud.motivoRechazo.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Motivo: ${solicitud.motivoRechazo}",
                                                color = Color.Red,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }

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
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    if (solicitud.estado == "Aceptada") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        IconButton(onClick = {
                                            com.example.kiwi.utils.PdfGenerator(
                                                mensaje
                                            ).generarFactura(solicitud)
                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_descarga_pdf),
                                                contentDescription = "Descargar",
                                                tint = Color.Black,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Text("Comprobante", fontSize = 9.sp, color = Color.Gray)
                                    }

                                    if (expandida && mostrarBotones) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(onClick = {
                                                viewModel.aceptarSolicitud(solicitud)
                                                botonesVisibles[idActual] = false
                                                viewModel.tieneSolicitudesNuevas = false
                                            }) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Aceptar",
                                                    tint = Color(0xFF2E7D32)
                                                )
                                            }
                                            IconButton(onClick = {
                                                solicitudARechazar = solicitud
                                            }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Devolver",
                                                    tint = Color(0xFFD32F2F)
                                                )
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
fun EditarProductoScreen(
    productoReferencia: String,
    navegacion: NavHostController,
    viewModel: SharedViewModel
) {
    val productoOriginal = viewModel.productos.find { it.referencia == productoReferencia }

    if (productoOriginal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Producto no encontrado para editar", color = Color.Red)
        }
        LaunchedEffect(Unit) {
            delay(1500)
            navegacion.popBackStack()
        }
        return
    }
    val nombre = remember { mutableStateOf(productoOriginal.nombre) }
    val referencia =
        remember { mutableStateOf(productoOriginal.referencia) }
    val categoria = remember { mutableStateOf(productoOriginal.categoria) }
    val categoriasDisponibles = listOf(
        "Suéter",
        "Blusa",
        "Jeans",
        "Enterizo",
        "Conjunto",
        "Short",
        "Falda",
        "Body",
        "Traje",
        "Licra",
        "Traje de baño",
        "Mallas"
    )
    val docena = remember { mutableStateOf(productoOriginal.stock.toString()) }
    val precio = remember {
        mutableStateOf(
            String.format(
                Locale.getDefault(),
                "%.2f",
                productoOriginal.precio
            )
        )
    }

    var newImagenUri: Uri? by remember { mutableStateOf(null) }
    var imageActualUrl: String? by remember { mutableStateOf(productoOriginal.imagenUrl) }
    val tallasAgregadas =
        remember { mutableStateListOf<String>().apply { addAll(productoOriginal.tallas) } }
    var nuevaTalla by remember { mutableStateOf("") }
    val mensaje = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            newImagenUri = uri
            imageActualUrl = null
        }
    }
    val tecladoControl = LocalSoftwareKeyboardController.current
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    BackHandler {
        navegacion.popBackStack()
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
                        navegacion.navigate("vendedoraMenu") {
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(12.dp))

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
                        onDone = { tecladoControl?.hide() }
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
                        onDone = { tecladoControl?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val interaccion = remember { MutableInteractionSource() }
                val esPresionado by interaccion.collectIsPressedAsState()
                var categoriaExpandida by remember { mutableStateOf(false) }

                LaunchedEffect(esPresionado) {
                    if (esPresionado) categoriaExpandida = true
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
                    interactionSource = interaccion,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = categoriaExpandida,
                    onDismissRequest = { categoriaExpandida = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoriasDisponibles.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                categoria.value = opcion
                                categoriaExpandida = false
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
                    if (newImagenUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(newImagenUri),
                            contentDescription = "Imagen seleccionada",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 250.dp)
                        )
                    } else if (imageActualUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageActualUrl),
                            contentDescription = "Imagen actual",
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

                Text("Conjuntos de tallas:")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevaTalla,
                        onValueChange = { nuevaTalla = it },
                        label = { Text("Añadir nueva talla (Ej: S, M, L)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (nuevaTalla.isNotBlank() && !tallasAgregadas.contains(
                                    nuevaTalla.trim()
                                )
                            ) {
                                tallasAgregadas.add(nuevaTalla.trim())
                                nuevaTalla = ""
                            } else if (tallasAgregadas.contains(nuevaTalla.trim())) {
                                Toast.makeText(
                                    mensaje,
                                    "Esa talla ya ha sido agregada.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            tecladoControl?.hide()
                        })
                    )
                    Button(
                        onClick = {
                            if (nuevaTalla.isNotBlank() && !tallasAgregadas.contains(
                                    nuevaTalla.trim()
                                )
                            ) {
                                tallasAgregadas.add(nuevaTalla.trim())
                                nuevaTalla = ""
                            } else if (tallasAgregadas.contains(nuevaTalla.trim())) {
                                Toast.makeText(
                                    mensaje,
                                    "Esa talla ya ha sido agregada.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = nuevaTalla.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                onDone = { tecladoControl?.hide() }
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
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ){
                                Text("+")
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
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("-", color = Color.White)
                            }
                        }
                    }
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
                            onDone = {
                                tecladoControl?.hide()
                                val parsedPrice = precio.value.toDoubleOrNull()
                                if (parsedPrice != null) {
                                    precio.value =
                                        String.format(Locale.getDefault(), "%.2f", parsedPrice)
                                } else if (precio.value.isNotEmpty()) {
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
            Button(
                onClick = {

                    val newNombre = nombre.value.trim()
                    val newReferencia = referencia.value.trim()
                    val newCategoria = categoria.value.trim()
                    val newDocena = docena.value.toIntOrNull()
                    val newPrecio = precio.value.toDoubleOrNull()

                    if (
                        newNombre.isBlank() || newReferencia.isBlank() || newCategoria.isBlank() ||
                        tallasAgregadas.isEmpty() || newDocena == null || newDocena < 0 || newPrecio == null || newPrecio <= 0
                    ) {
                        Toast.makeText(
                            mensaje,
                            "Completa todos los campos correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val productoConNuevosDatos = productoOriginal.copy(
                            nombre = newNombre,
                            referencia = newReferencia,
                            categoria = newCategoria,
                            tallas = tallasAgregadas.toList(),
                            precio = newPrecio,
                            stock = newDocena
                        )
                        viewModel.actualizarProductoYGestionarImagen(
                            productoOriginal = productoOriginal,
                            productoActualizado = productoConNuevosDatos,
                            nuevaImagenUri = newImagenUri,
                            onSuccess = {
                                mostrarDialogoConfirmacion = true
                            },
                            onFailure = { e ->
                                Log.e(
                                    "EditarProducto",
                                    "Error al actualizar producto: ${e.message}"
                                )
                                Toast.makeText(
                                    mensaje,
                                    "Error al guardar cambios: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("GUARDAR CAMBIOS", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = {
                Text("Producto Actualizado", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("¡El producto ha sido actualizado exitosamente!")
            },
            confirmButton = { }
        )

        LaunchedEffect(Unit) {
            delay(2000)
            mostrarDialogoConfirmacion = false
            navegacion.popBackStack()

        }
    }
}


@Preview(showBackground = true)
@Composable
fun SplashPreview(){
    KiwiTheme {
        CargaScreen(onFinish = { })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoCard(
    producto: ProductoDetalle,
    onClick: () -> Unit,
    esVendedor: Boolean = false,
    onDeleteClick: (() -> Unit)? = null
) {
    val stockVisible = producto.stock - producto.cantidadReservada

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            ) {

                if (producto.imagenUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(producto.imagenUrl)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_kiwi),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            alpha = 0.3f
                        )
                    }
                }

                if (stockVisible <= 0) {
                    Surface(
                        color = Color(0xFFD32F2F),
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "AGOTADO",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (stockVisible <= 3) {
                    Surface(
                        color = Color(0xFFF9A825),
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "ÚLTIMAS",
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (esVendedor && onDeleteClick != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clickable { onDeleteClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Ref: ${producto.referencia}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", producto.precio)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )

                    if (stockVisible > 0) {
                        Text(
                            text = "$stockVisible dcns",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
            }
        }
    }
}