package com.example.kiwi.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.ProductoDetalle
import com.example.kiwi.ProductoEnPedido
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue


class SharedViewModel() : ViewModel() {

    val carrito = mutableStateListOf<ProductoEnPedido>()
    val solicitudes = mutableStateListOf<Solicitud>()
    val productos = mutableStateListOf<ProductoDetalle>()
    var tieneSolicitudesNuevas by mutableStateOf(false)
    var tieneActualizacionSolicitudesComprador by mutableStateOf(false)
    var estadoPedido by mutableStateOf<EstadoPedido>(EstadoPedido.Idle)
        private set

    private val db = FirebaseFirestore.getInstance()
    private val _solicitudesSortDirection = MutableStateFlow(Query.Direction.DESCENDING)
    val solicitudesSortDirection = _solicitudesSortDirection.asStateFlow()
    private var isInitialSolicitudesSnapshotProcessed = false

    init {
        cargarProductosDesdeFirestore()

        viewModelScope.launch {
            _solicitudesSortDirection.collectLatest { direction ->
                isInitialSolicitudesSnapshotProcessed = false
                cargarSolicitudesDesdeFirestore(direction)
            }
        }
    }

    private fun cargarSolicitudesDesdeFirestore(direction: Query.Direction) {
        db.collection("solicitudes")
            .orderBy("timestampCreacion", direction)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed for solicitudes.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    var newRequestDetectedInThisBatch = false

                    if (!isInitialSolicitudesSnapshotProcessed) {
                        isInitialSolicitudesSnapshotProcessed = true
                    } else {
                        for (change in snapshots.documentChanges) {
                            if (change.type == DocumentChange.Type.ADDED) {
                                newRequestDetectedInThisBatch = true
                            }
                        }
                    }

                    if (newRequestDetectedInThisBatch) {
                        tieneSolicitudesNuevas = true
                    }

                    val completeCurrentSolicitudes = mutableListOf<Solicitud>()

                    for (doc in snapshots.documents) {
                        val solicitud = doc.toObject(Solicitud::class.java)
                        if (solicitud != null) {
                            completeCurrentSolicitudes.add(solicitud.copy(idFirestore = doc.id))
                        }
                    }

                    solicitudes.clear()
                    solicitudes.addAll(completeCurrentSolicitudes)
                }
            }
    }


    private fun actualizarEstadoSolicitudEnFirestore(solicitud: Solicitud) {
        solicitud.idFirestore?.let { id ->

            val updates = mutableMapOf<String, Any>()
            updates["estado"] = solicitud.estado

            if (solicitud.estado == "Aceptada" || solicitud.estado == "Devuelta") {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, 5)
                val fechaDeExpiracion = Timestamp(calendar.time)
                updates["fechaExpiracion"] = fechaDeExpiracion
                Log.d("TTL", "Solicitud ${solicitud.idFirestore} marcada para expirar en: $fechaDeExpiracion")
            }
            db.collection("solicitudes").document(id)
                .update(updates)
                .addOnSuccessListener {
                    Log.d("Firestore", "Estado de solicitud actualizado con ID: $id a ${solicitud.estado}")

                    solicitud.productos.forEach { productoEnPedido ->
                        val cantidadPedida = productoEnPedido.cantidad
                        val productoOriginal = productos.find { it.referencia == productoEnPedido.producto?.referencia }

                        productoOriginal?.let { p ->
                            val updatedProduct = when (solicitud.estado) {
                                "Aceptada" -> {
                                    p.copy(
                                        stock = p.stock - cantidadPedida,
                                        cantidadReservada = p.cantidadReservada - cantidadPedida
                                    )
                                }
                                "Devuelta" -> {
                                    p.copy(
                                        cantidadReservada = p.cantidadReservada - cantidadPedida
                                    )
                                }
                                else -> p
                            }
                            actualizarProductoEnFirestore(updatedProduct)
                        }
                    }
                    tieneActualizacionSolicitudesComprador = true
                }
        }
    }


    fun setSolicitudesSortDirection(direction: Query.Direction) {
        _solicitudesSortDirection.value = direction
    }


    private fun cargarProductosDesdeFirestore() {
        db.collection("productos")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Error al cargar productos", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val loadedProducts = snapshot.documents.mapNotNull { doc ->
                        val product = doc.toObject(ProductoDetalle::class.java)
                        product?.copy(idFirestore = doc.id)
                    }
                    productos.clear()
                    productos.addAll(loadedProducts)
                }
            }
    }


    fun actualizarProductoEnFirestore(producto: ProductoDetalle) {
        producto.idFirestore?.let { id ->
            db.collection("productos").document(id)
                .set(producto)
                .addOnSuccessListener { Log.d("Firestore", "Producto actualizado: ${producto.nombre}") }
                .addOnFailureListener { e -> Log.e("Firestore", "Error actualizando producto", e) }
        }
    }


    fun actualizarProductoYGestionarImagen(
        productoOriginal: ProductoDetalle,
        productoActualizado: ProductoDetalle,
        nuevaImagenUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (nuevaImagenUri != null) {
            uploadImageToFirebaseStorage(
                uri = nuevaImagenUri,
                onSuccess = { newImageUrl ->
                    val productoFinal = productoActualizado.copy(imagenUrl = newImageUrl)
                    val oldImageUrl = productoOriginal.imagenUrl
                    if (!oldImageUrl.isNullOrBlank()) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl).delete()
                    }
                    actualizarProductoEnFirestore(productoFinal)
                    onSuccess()
                },
                onFailure = { onFailure(it) }
            )
        } else {
            actualizarProductoEnFirestore(productoActualizado)
            onSuccess()
        }
    }


    fun guardarNuevoProductoEnFirestore(
        producto: ProductoDetalle,
        imagenUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val newDocRef = db.collection("productos").document()
        val productoConDatos = producto.copy(
            idFirestore = newDocRef.id,
            timestamp = Timestamp.now()
        )

        if (imagenUri != null) {
            uploadImageToFirebaseStorage(
                uri = imagenUri,
                onSuccess = { imageUrl ->
                    val productoFinal = productoConDatos.copy(imagenUrl = imageUrl)
                    newDocRef.set(productoFinal)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                },
                onFailure = { onFailure(it) }
            )
        } else {
            newDocRef.set(productoConDatos)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
    }



    fun eliminarProductoDeFirestore(
        producto: ProductoDetalle,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val productoId = producto.idFirestore ?: ""
        if (productoId.isBlank()) {
            onFailure(Exception("El producto no tiene un ID de Firestore para eliminar."))
            return
        }

        if (!producto.imagenUrl.isNullOrBlank()) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(producto.imagenUrl)
            storageRef.delete()
                .addOnSuccessListener {
                    db.collection("productos").document(productoId).delete()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                }
                .addOnFailureListener { onFailure(it) }
        } else {
            db.collection("productos").document(productoId).delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
    }


    fun guardarNuevaSolicitudEnFirestore(solicitud: Solicitud, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val solicitudAGuardar = solicitud.copy(
            timestampCreacion = Timestamp.now()
        )

        db.collection("solicitudes").add(solicitudAGuardar)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    data class Solicitud(
        val idFirestore: String? = null,
        val id: Int = 0,
        val productos: List<ProductoEnPedido> = emptyList(),
        val total: Double = 0.0,
        val fecha: String = "",
        val hora: String = "",
        val timestampCreacion: Timestamp? = null,
        var estado: String = "",
        val comprador: String = "",
        val celular: String = "",
        val fechaExpiracion: Timestamp? = null,
        val motivoRechazo: String = ""
    ) {
        constructor() : this(
            idFirestore = null,
            id = 0,
            productos = emptyList(),
            total = 0.0,
            fecha = "",
            hora = "",
            timestampCreacion = null,
            estado = "",
            comprador = "",
            celular = "",
            fechaExpiracion = null,
            motivoRechazo = ""
        )
    }


    fun agregarAlCarrito(producto: ProductoDetalle, cantidad: Int) {
        val existingItem = carrito.find { it.producto?.referencia == producto.referencia }

        if (existingItem != null) {
            val updatedQuantity = existingItem.cantidad + cantidad
            val index = carrito.indexOf(existingItem)
            carrito[index] = existingItem.copy(cantidad = updatedQuantity)
        } else {
            carrito.add(ProductoEnPedido(producto = producto, cantidad = cantidad))
        }
    }


    fun removerDelCarrito(producto: ProductoDetalle) {
        carrito.removeAll { it.producto?.referencia == producto.referencia }
    }


    fun realizarPedido(nombre: String, celular: String) {
        estadoPedido = EstadoPedido.Loading
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {

                val resultado = db.runTransaction { transaction ->
                    val productosAgotados = mutableListOf<ProductoEnPedido>()
                    val productosDisponibles = mutableListOf<ProductoEnPedido>()

                    for (itemEnCarrito in carrito) {
                        val productoRef = db.collection("productos").document(itemEnCarrito.producto!!.idFirestore!!)
                        val productoSnapshot = transaction.get(productoRef)
                        val productoActual = productoSnapshot.toObject(ProductoDetalle::class.java)!!

                        val stockRealDisponible = productoActual.stock - productoActual.cantidadReservada
                        if (itemEnCarrito.cantidad <= stockRealDisponible) {
                            productosDisponibles.add(itemEnCarrito)
                        } else {
                            productosAgotados.add(itemEnCarrito)
                        }
                    }

                    if (productosAgotados.isNotEmpty()) {
                        return@runTransaction EstadoPedido.Conflict(PedidoConflict(productosAgotados, productosDisponibles))
                    }

                    val productosDelPedido = carrito.toList()
                    val total = productosDelPedido.sumOf { it.producto?.precio?.times(it.cantidad) ?: 0.0 }
                    val nuevaSolicitudRef = db.collection("solicitudes").document()

                    productosDelPedido.forEach { item ->
                        val productoRef = db.collection("productos").document(item.producto!!.idFirestore!!)
                        transaction.update(productoRef, "cantidadReservada", FieldValue.increment(item.cantidad.toLong()))
                    }

                    val solicitudData = Solicitud(
                        productos = productosDelPedido,
                        total = total,
                        fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                        timestampCreacion = Timestamp.now(),
                        estado = "Pendiente",
                        comprador = nombre.ifBlank { "Cliente" },
                        celular = celular.ifBlank { "00000000" }
                    )
                    transaction.set(nuevaSolicitudRef, solicitudData)
                    return@runTransaction EstadoPedido.Success

                }.await()

                estadoPedido = resultado

                if (resultado is EstadoPedido.Success) {
                    carrito.clear()
                }
            } catch (e: Exception) {
                Log.e("Pedido", "Error en la transacción de pedido", e)
                estadoPedido = EstadoPedido.Failure(e.localizedMessage ?: "Error desconocido")
            }
        }
    }


    fun confirmarPedidoConModificaciones(nombre: String, celular: String) {
        estadoPedido = EstadoPedido.Idle
    }


    fun resetEstadoPedido() {
        estadoPedido = EstadoPedido.Idle
    }


    fun aceptarSolicitud(solicitud: Solicitud) {
        val solicitudActualizada = solicitud.copy(estado = "Aceptada")
        actualizarEstadoSolicitudEnFirestore(solicitudActualizada)
    }


    fun rechazarSolicitud(solicitud: Solicitud, motivo: String) {
        val solicitudId = solicitud.idFirestore ?: return
        val ref = db.collection("solicitudes").document(solicitudId)
        val updates = mapOf(
            "estado" to "Devuelta",
            "motivoRechazo" to motivo
        )

        ref.update(updates)
            .addOnSuccessListener {

                val index = solicitudes.indexOfFirst { it.idFirestore == solicitudId }
                if (index != -1) {
                    solicitudes[index] = solicitudes[index].copy(
                        estado = "Devuelta",
                        motivoRechazo = motivo
                    )
                }

                solicitud.productos.forEach { productoEnPedido ->
                    val cantidadPedida = productoEnPedido.cantidad
                    val productoOriginal = productos.find { it.referencia == productoEnPedido.producto?.referencia }

                    productoOriginal?.let { p ->
                        val updatedProduct = p.copy(
                            cantidadReservada = p.cantidadReservada - cantidadPedida
                        )
                        actualizarProductoEnFirestore(updatedProduct)
                    }
                }

                tieneActualizacionSolicitudesComprador = true
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "Error al rechazar solicitud", e)
            }
    }


    fun uploadImageToFirebaseStorage(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("product_images/${uri.lastPathSegment}_${System.currentTimeMillis()}")
        val uploadTask = imagesRef.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                onSuccess(downloadUri.toString())
            } else {
                onFailure(task.exception ?: Exception("Unknown error uploading image"))
            }
        }
    }
}


data class PedidoConflict(
    val productosAgotados: List<ProductoEnPedido>,
    val productosDisponibles: List<ProductoEnPedido>
)


sealed class EstadoPedido {
    object Idle : EstadoPedido()
    object Loading : EstadoPedido()
    object Success : EstadoPedido()
    data class Conflict(val info: PedidoConflict) : EstadoPedido()
    data class Failure(val error: String) : EstadoPedido()
}