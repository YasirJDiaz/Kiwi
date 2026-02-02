// Importar funciones de Firebase (CDN)
// import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";
// import { getFirestore, collection, onSnapshot } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js";

// Configuración de Firebase (Extraída de google-services.json)
const firebaseConfig = {
    apiKey: "AIzaSyBs7hUxpXrWQMWBsJQkHhbDzEkGXxhQoF0",
    authDomain: "kiwi-2025.firebaseapp.com",
    projectId: "kiwi-2025",
    storageBucket: "kiwi-2025.firebasestorage.app",
    messagingSenderId: "785572669533",
    appId: "1:785572669533:web:43ae91cc179932d072c5b4" // App ID aproximado para web, usando projeect number
};

// Inicializar Firebase (Compat Syntax)
// Inicializar Firebase (Compat Syntax)
firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();
const auth = firebase.auth();

// Autenticación Anónima (Opcional - no bloquea si falla)
auth.signInAnonymously()
    .then(() => {
        console.log("Autenticado anónimamente para Firestore");
    })
    .catch((error) => {
        console.warn("Auth anónima no disponible (puede ser normal):", error.code);
        // Continuar sin auth - no bloquear la app
    });

// ========================================
// FIREBASE CLOUD MESSAGING (Push Notifications)
// ========================================

let messaging = null;
let currentFCMToken = null;

// Inicializar FCM solo si el navegador lo soporta
if (firebase.messaging.isSupported()) {
    messaging = firebase.messaging();

    // NOTA: El service worker (firebase-messaging-sw.js) maneja TODAS las notificaciones
    // No usamos onMessage aquí para evitar notificaciones duplicadas

    // messaging.onMessage((payload) => {
    //     console.log('[FCM] Mensaje recibido en primer plano:', payload);
    //     // Service worker ya muestra la notificación
    // });
} else {
    console.warn('[FCM] Este navegador no soporta Firebase Messaging');
}

// Función para solicitar permisos y obtener token FCM
async function requestNotificationPermission() {
    if (!messaging) {
        console.warn('[FCM] Messaging no disponible');
        return null;
    }

    try {
        const permission = await Notification.requestPermission();

        if (permission === 'granted') {
            console.log('[FCM] Permiso de notificación concedido');

            // Obtener token FCM con VAPID key
            const token = await messaging.getToken({
                vapidKey: 'BLCKeXldoX7vnBRIojWA6ltqb0ljIx2RJ0OhKtzlpxGgwEsJ_Qab74MsdgZpo4L5Va6WIqB-76VBMZv4vkxS1cQ'
            });
            console.log('[FCM] Token obtenido:', token);
            currentFCMToken = token;

            return token;
        } else {
            console.log('[FCM] Permiso de notificación denegado');
            return null;
        }
    } catch (error) {
        console.error('[FCM] Error al solicitar permisos:', error);
        return null;
    }
}

// Función para guardar token FCM en Firestore (para vendedores)
async function saveFCMTokenToFirestore(token, userId) {
    if (!token || !userId) return;

    try {
        // Generar un ID único y ESTABLE para este dispositivo
        // Basado en características que no cambian: userAgent + resolución de pantalla
        const fingerprint = navigator.userAgent + screen.width + screen.height + screen.colorDepth;

        // Crear un hash simple (no necesitamos crypto perfecto, solo consistencia)
        let hash = 0;
        for (let i = 0; i < fingerprint.length; i++) {
            const char = fingerprint.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        const deviceId = 'device_' + Math.abs(hash).toString(36);

        await db.collection('fcmTokens').doc(userId).collection('devices').doc(deviceId).set({
            token: token,
            platform: 'web',
            deviceType: /mobile|android|iphone|ipad/i.test(navigator.userAgent) ? 'mobile' : 'desktop',
            userAgent: navigator.userAgent,
            lastUpdated: firebase.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

        console.log('[FCM] Token guardado en Firestore (deviceId:', deviceId, ')');
    } catch (error) {
        console.error('[FCM] Error al guardar token:', error);
    }
}

// Estado de Productos (Ya no es mock estático, se llenará con DB)
let productos = [];

// Esperar a que el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', () => {

    // Referencias a elementos del DOM
    const pantallaCarga = document.getElementById('pantalla-carga');
    const pantallaLogin = document.getElementById('pantalla-login');
    const pantallaCatalogo = document.getElementById('pantalla-catalogo');
    const formVendedor = document.getElementById('form-login-vendedor');
    const btnComprador = document.getElementById('btn-comprador');
    const gridProductos = document.getElementById('grid-productos');
    const chipsFiltro = document.querySelectorAll('.chip');

    // TIEMPO DE CARGA
    setTimeout(() => {
        pantallaCarga.style.opacity = '0';
        setTimeout(() => {
            pantallaCarga.classList.add('oculto');
            pantallaCarga.style.display = 'none'; // FIX: Eliminar del flujo para evitar bloqueo de clicks
            pantallaLogin.classList.remove('oculto');
            pantallaLogin.style.display = 'flex'; // Asegura flex

            // Asegurar que nav-bar esté oculto en Login
            const navBar = document.getElementById('barra-navegacion');
            if (navBar) {
                navBar.classList.add('oculto');
                navBar.style.display = 'none';
            }

            // Asegurar que controles-admin esté oculto en Login
            const controlesAdmin = document.getElementById('controles-admin');
            if (controlesAdmin) {
                controlesAdmin.classList.add('oculto');
                controlesAdmin.style.display = 'none';
            }
        }, 500);
    }, 2500);

    // ESCUCHAR COLECCIÓN 'PRODUCTOS' EN TIEMPO REAL (Compat Syntax)
    // Se elimina orderBy del servidor para traer TODOS los productos (inclusive sin timestamp)
    // y se ordena en cliente.
    db.collection("productos").onSnapshot((snapshot) => {
        productos = [];
        snapshot.forEach((doc) => {
            const data = doc.data();
            productos.push({
                id: doc.id, // Firestore ID
                nombre: data.nombre || "Producto sin nombre",
                referencia: data.referencia || "---",
                categoria: data.categoria || "Otros",
                precio: Number(data.precio) || 0,
                // Stock Real = Stock Físico - Reservado
                stock: (Number(data.stock) || 0) - (Number(data.cantidadReservada) || 0),
                imagen: data.imagenUrl || null, // Mapear imagenUrl a imagen
                tallas: data.tallas || [], // Array de tallas
                timestamp: data.timestamp || null // Guardar timestamp para ordenar
            });
        });

        // Ordenar en Cliente: Descendente (Más nuevos primero)
        // Si no tiene timestamp, se asume 0 (muy antiguo)
        productos.sort((a, b) => {
            const tA = a.timestamp ? a.timestamp.seconds : 0;
            const tB = b.timestamp ? b.timestamp.seconds : 0;
            return tB - tA; // Descendente
        });

        // Al recibir cambios, actualizar vista si estamos en catalogo
        aplicarFiltrosCombinados();
        console.log("Productos actualizados desde Firestore:", productos.length);

        // --- ACTUALIZACIÓN REAL-TIME DETALLE PRODUCTO ---
        // Si hay un producto abierto en detalle, actualizar su información en vivo
        if (typeof window.productoActualDetalle !== 'undefined' && window.productoActualDetalle) {
            const pantallaDetalle = document.getElementById('pantalla-detalle');
            // Verificar si la pantalla está visible
            if (pantallaDetalle && !pantallaDetalle.classList.contains('oculto')) {
                const prodActualizado = productos.find(p => p.id === window.productoActualDetalle.id);
                if (prodActualizado) {
                    // Mantener el valor del input si es posible, o dejar que se resetee para seguridad
                    // Por ahora recargamos todo para asegurar estado correcto (ej. si pasa a agotado)
                    console.log("Actualizando detalle en tiempo real:", prodActualizado.nombre);
                    mostrarDetalleProducto(prodActualizado);
                }
            }
        }
    });

    // ESTADO DE SESIÓN
    let esAdmin = false;

    // LÓGICA DE LOGIN (VENDEDOR)
    if (formVendedor) {
        formVendedor.addEventListener('submit', (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            // UI Loading
            const btnSubmit = formVendedor.querySelector('button');
            const originalText = btnSubmit.textContent;
            btnSubmit.textContent = "Verificando...";
            btnSubmit.disabled = true;

            auth.signInWithEmailAndPassword(email, password)
                .then((userCredential) => {
                    // Login Exitoso
                    console.log("Vendedor autenticado:", userCredential.user.email);
                    esAdmin = true;

                    // Configurar UI Admin
                    toggleModoAdmin(true, userCredential.user.uid);

                    // Navegar al Catálogo
                    if (/android/i.test(navigator.userAgent)) {
                        history.pushState({ screen: 'catalogo' }, "Catálogo", "#catalogo");
                    }
                    cambiarPantalla(pantallaLogin, pantallaCatalogo);

                    // Resetear formulario
                    formVendedor.reset();
                })
                .catch((error) => {
                    console.error("Error login:", error);
                    mostrarDialogo("Verifica tus credenciales", 'alerta', null, "Error de autenticación");
                })
                .finally(() => {
                    btnSubmit.textContent = originalText;
                    btnSubmit.disabled = false;
                });
        });
    }

    // Función para activar/desactivar modo Admin
    function toggleModoAdmin(activo, vendedorUid = null) {
        const navBar = document.getElementById('barra-navegacion');
        const controlesAdmin = document.getElementById('controles-admin');

        if (activo) {
            // Modo Vendedor: Ocultar Nav Inferior, Mostrar FABs
            if (navBar) navBar.classList.add('oculto');
            if (navBar) navBar.style.display = 'none'; // Forzar oculto

            if (controlesAdmin) {
                controlesAdmin.classList.remove('oculto');
                controlesAdmin.style.display = 'block';
            }
            // Iniciar listener de solicitudes y badge
            if (typeof cargarSolicitudesVendedora === 'function') {
                cargarSolicitudesVendedora();
            }

            // FCM: Solicitar permisos de notificación y registrar token
            if (messaging && vendedorUid) {
                requestNotificationPermission().then(token => {
                    if (token) {
                        saveFCMTokenToFirestore(token, vendedorUid);
                    }
                });
            }

            // Forzar re-renderizadopara mostrar botones de eliminar
            aplicarFiltrosCombinados();
        } else {
            // Modo Comprador
            if (navBar) navBar.classList.remove('oculto');
            if (navBar) navBar.style.display = 'flex';

            if (controlesAdmin) {
                controlesAdmin.classList.add('oculto');
                controlesAdmin.style.display = 'none';
            }
            // Detener listener admin si existe
            if (typeof unsubscribeSolicitudesAdmin === 'function' && unsubscribeSolicitudesAdmin) {
                unsubscribeSolicitudesAdmin();
                unsubscribeSolicitudesAdmin = null;
            }
        }
    }

    // BOTÓN COMPRADOR (Modo Normal)
    if (btnComprador) {
        btnComprador.addEventListener('click', () => {
            esAdmin = false;
            toggleModoAdmin(false);
            if (/android/i.test(navigator.userAgent)) {
                history.pushState({ screen: 'catalogo' }, "Catálogo", "#catalogo");
            }
            cambiarPantalla(pantallaLogin, pantallaCatalogo);
        });
    }

    // ESTADO DE FILTROS
    let categoriaActual = 'todos';
    let terminoBusqueda = '';

    // Referencia al input de búsqueda y botón limpiar
    const inputBusqueda = document.getElementById('input-busqueda');
    const btnLimpiarBusqueda = document.getElementById('btn-limpiar-busqueda');

    // LÓGICA DE FILTROS (CHIPS)
    chipsFiltro.forEach(chip => {
        chip.addEventListener('click', () => {
            const yaEstabaActivo = chip.classList.contains('activo');

            // Remover clase activo de todos
            chipsFiltro.forEach(c => c.classList.remove('activo'));

            if (yaEstabaActivo) {
                // Toggled off -> Mostrar todos
                categoriaActual = 'todos';
            } else {
                // Activated -> Set category
                chip.classList.add('activo');
                categoriaActual = chip.dataset.categoria;
            }
            aplicarFiltrosCombinados();
        });
    });

    // LÓGICA DE BÚSQUEDA
    if (inputBusqueda) {
        inputBusqueda.addEventListener('input', (e) => {
            terminoBusqueda = e.target.value.toLowerCase().trim();
            toggleBtnLimpiar(terminoBusqueda);
            aplicarFiltrosCombinados();
        });
    }

    if (btnLimpiarBusqueda) {
        btnLimpiarBusqueda.addEventListener('click', () => {
            inputBusqueda.value = '';
            terminoBusqueda = '';
            toggleBtnLimpiar('');
            aplicarFiltrosCombinados();
            inputBusqueda.focus(); // Mantener foco opcional
        });
    }

    function toggleBtnLimpiar(texto) {
        if (btnLimpiarBusqueda) {
            if (texto.length > 0) {
                btnLimpiarBusqueda.classList.remove('oculto');
                btnLimpiarBusqueda.style.display = 'flex'; // O block, segun CSS
            } else {
                btnLimpiarBusqueda.classList.add('oculto');
                btnLimpiarBusqueda.style.display = 'none';
            }
        }
    }

    // FUNCIÓN: Aplicar Filtros Combinados
    function aplicarFiltrosCombinados() {
        // Usar array dinámico 'productos' en lugar de mock
        let resultados = productos;

        // 1. Filtrar por Categoría
        if (categoriaActual !== 'todos') {
            resultados = resultados.filter(p => p.categoria === categoriaActual);
        }

        // 2. Filtrar por Búsqueda (Nombre o Referencia)
        if (terminoBusqueda) {
            resultados = resultados.filter(p =>
                p.nombre.toLowerCase().includes(terminoBusqueda) ||
                p.referencia.toLowerCase().includes(terminoBusqueda)
            );
        }

        renderizarProductos(resultados);
    }

    // FUNCIÓN: Renderizar Productos en el Grid
    function renderizarProductos(listaProductos) {
        gridProductos.innerHTML = ''; // Limpiar grid

        if (listaProductos.length === 0) {
            gridProductos.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: #888; margin-top: 2rem;">No hay productos disponibles.</p>';
            return;
        }

        listaProductos.forEach(producto => {
            // Crear el elemento Card
            const card = document.createElement('div');
            card.className = 'producto-card';

            // EVENTO CLICK EN TARJETA
            if (esAdmin) {
                // Modo Vendedor: Editar
                card.onclick = () => {
                    history.pushState({ screen: 'editar' }, "Editar Producto", "#editar");
                    mostrarPantallaEditar(producto);
                };
            } else {
                // Modo Comprador: Ver Detalles
                card.onclick = () => mostrarDetalleProducto(producto);
            }

            // Lógica de Stock (Badge)
            let badgeHTML = '';
            if (producto.stock === 0) {
                badgeHTML = '<div class="badge-stock badge-agotado">AGOTADO</div>';
            } else if (producto.stock <= 3) {
                badgeHTML = '<div class="badge-stock badge-ultimas">ÚLTIMAS</div>';
            }

            // Imagen (Usando logo mock)
            const imagenHTML = producto.imagen
                ? `<img src="${producto.imagen}" alt="${producto.nombre}" class="imagen-producto">`
                : `<img src="assets/img/logo_kiwi.png" alt="Placeholder" class="imagen-placeholder">`;

            // Botón Eliminar (Solo Admin)
            let btnEliminarHTML = '';
            if (esAdmin) {
                btnEliminarHTML = `
                <button class="btn-eliminar-producto" title="Eliminar Producto">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                    </svg>
                </button>`;
            }

            // HTML Interno de la Card
            card.innerHTML = `
                <div class="contenedor-imagen">
                    ${btnEliminarHTML}
                    ${badgeHTML}
                    ${imagenHTML}
                </div>
                <div class="info-producto">
                    <h3 class="nombre-producto">${producto.nombre}</h3>
                    <span class="referencia-producto">Ref: ${producto.referencia}</span>
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <p class="precio-producto">$${producto.precio.toFixed(2)}</p>
                        ${producto.stock > 0 ? `<span class="stock-docenas" style="color: #4CAF50; font-weight: bold; font-size: 14px;">${producto.stock} dcns</span>` : ''}
                    </div>
                </div>
            `;

            // Listener para Botón Eliminar (Evitar propagación al click de la card)
            if (esAdmin) {
                // Truco: Al ser innerHTML, el botón ya es un elemento hijo.
                // Necesitamos esperar a que se renderice o buscarlo en 'card' (que es un div en memoria aun)
                // 'card' aún no está en DOM, pero podemos buscar en él.

                // Nota: se busca DESPUÉS de asignar innerHTML
                setTimeout(() => {
                    const btnEliminar = card.querySelector('.btn-eliminar-producto');
                    if (btnEliminar) {
                        btnEliminar.onclick = (e) => {
                            e.stopPropagation(); // No abrir editar
                            console.log("Click en eliminar");
                            mostrarDialogo(`¿Seguro que deseas eliminar el producto "${producto.nombre}"?`, 'confirmacion', () => {
                                // Borrar de Firestore
                                db.collection("productos").doc(producto.id).delete()
                                    .then(() => {
                                        mostrarDialogo("Producto eliminado exitosamente.", 'alerta');
                                        // El listener onSnapshot actualizará la UI automáticamente
                                    })
                                    .catch((error) => {
                                        console.error("Error eliminando documento: ", error);
                                        mostrarDialogo("Error al eliminar.", 'alerta');
                                    });
                            });
                        };
                    }
                }, 0);
            }

            gridProductos.appendChild(card);
        });
    }

    // LÓGICA DE NAVEGACIÓN (TABS ANIMADOS)
    const navItems = document.querySelectorAll('.nav-item');
    // ESTADO CARRITO
    let carrito = [];
    const pantallaCarrito = document.getElementById('pantalla-carrito');
    const listaCarrito = document.getElementById('lista-carrito');
    const carritoVacio = document.getElementById('carrito-vacio');
    const footerCarrito = document.getElementById('footer-carrito');
    const totalCarritoElem = document.getElementById('total-carrito');
    const btnRealizarPedido = document.getElementById('btn-realizar-pedido');
    // const btnIrCatalogo = document.getElementById('btn-ir-catalogo'); // Eliminado
    const btnAtrasCarrito = document.getElementById('btn-atras-carrito'); // Nuevo botón
    const btnAtrasSolicitudes = document.getElementById('btn-atras-solicitudes'); // Nuevo botón solicitudes
    const pantallaSolicitudes = document.getElementById('pantalla-solicitudes');

    const indicador = document.querySelector('.indicador-activo');
    const navBar = document.querySelector('.barra-navegacion');

    // Lógica Botón Atrás en Carrito
    if (btnAtrasCarrito) {
        btnAtrasCarrito.addEventListener('click', () => {
            // Visual update
            if (navItems.length > 0) {
                navItems.forEach(n => n.classList.remove('activo'));
                navItems[0].classList.add('activo');
                moverIndicador(navItems[0]);
            }
            // Transition
            cambiarPantalla(pantallaCarrito, pantallaCatalogo, 'grid');
        });
    }

    // Lógica Botón Atrás en Solicitudes
    if (btnAtrasSolicitudes) {
        btnAtrasSolicitudes.addEventListener('click', (e) => {
            e.preventDefault();
            // Visual update
            if (navItems.length > 0) {
                navItems.forEach(n => n.classList.remove('activo'));
                navItems[0].classList.add('activo');
                moverIndicador(navItems[0]);
            }
            // Transition
            cambiarPantalla(pantallaSolicitudes, pantallaCatalogo, 'grid');
        });
    }

    function moverIndicador(item) {
        // Calcular posición: (Posición del item + Mitad de su ancho) - (Mitad del ancho del indicador)
        // El indicador mide 50px de ancho (definido en CSS)
        const itemCenter = item.offsetLeft + (item.offsetWidth / 2);
        const indicadorOffset = itemCenter - 25; // 25 es la mitad de 50px

        indicador.style.transform = `translateX(${indicadorOffset}px)`;
    }

    // Inicializar indicador en la pestaña activa (Solo si ya es visible, si no, se maneja en la transición)
    // Eliminamos el setTimeout global aquí porque al inicio está oculto y daría 0

    // Lógica de navegación movida al final del archivo para soportar persistencia
    // navItems logic replaced...

    // Evento ir al catálogo desde carrito vacío (Eliminado por solicitud de usuario)
    // La navegación inferior cumple esta función.

    // FUNCIÓN: Renderizar Carrito
    function renderizarCarrito() {
        listaCarrito.innerHTML = '';
        let total = 0;

        // Siempre mostrar footer (incluso si vacío, para ver Total: $0.00)
        footerCarrito.classList.remove('oculto');
        footerCarrito.style.display = 'flex';

        if (carrito.length === 0) {
            carritoVacio.classList.remove('oculto');
            carritoVacio.style.display = 'flex'; // Centrado

            listaCarrito.classList.add('oculto');
            listaCarrito.style.display = 'none'; // Desaparecer del DOM layout

            // Resetear Total
            totalCarritoElem.textContent = "$0.00";
            // Deshabilitar botón
            btnRealizarPedido.disabled = true;
            return;
        }

        carritoVacio.classList.add('oculto');
        carritoVacio.style.display = 'none';

        listaCarrito.classList.remove('oculto');
        listaCarrito.style.display = 'flex'; // Flex column


        // Habilitar botón si hay items
        btnRealizarPedido.disabled = false;

        carrito.forEach((item, index) => {
            total += item.precio * item.cantidad;

            const div = document.createElement('div');
            div.className = 'carrito-item';

            // Imagen o placeholder
            const imgSrc = item.imagen || 'assets/img/logo_kiwi.png';

            div.innerHTML = `
                <img src="${imgSrc}" class="carrito-img">
                <div class="carrito-info">
                    <div class="carrito-nombre">${item.nombre}</div>
                    <div class="carrito-variante" style="font-size: 12px; color: #000; margin-bottom: 2px;">Ref: ${item.referencia}</div>
                    <div class="carrito-variante">Talla: ${item.talla}</div>
                    <div class="carrito-precio">$${item.precio.toFixed(2)} x ${item.cantidad}</div>
                </div>
                <div class="carrito-controles">
                    <button class="btn-eliminar" data-index="${index}">
                        <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                        </svg>
                    </button>
                    <span class="cantidad-badge">${item.cantidad} doc.</span>
                </div>
            `;
            listaCarrito.appendChild(div);
        });

        // Eventos Eliminar con Animación
        document.querySelectorAll('.btn-eliminar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const button = e.currentTarget;
                const index = button.dataset.index;

                // Evitar doble clic
                if (button.classList.contains('cargando')) return;
                button.classList.add('cargando');

                // Mostrar spinner
                button.innerHTML = '<div class="spinner-eliminar"></div>';

                // Esperar y eliminar
                setTimeout(() => {
                    carrito.splice(index, 1);
                    renderizarCarrito();
                }, 600);
            });
        });

        totalCarritoElem.textContent = `$${total.toFixed(2)}`;
    }

    // Recalcular posición al cambiar tamaño de ventana (Responsividad)
    window.addEventListener('resize', () => {
        const currentActive = document.querySelector('.nav-item.activo');
        if (currentActive) {
            moverIndicador(currentActive);
        }
    });

    // LÓGICA DE COMPRADOR -> IR AL CATÁLOGO
    if (btnComprador) {
        btnComprador.addEventListener('click', () => {
            // Animación de salida del Login
            pantallaLogin.style.opacity = '0';

            setTimeout(() => {
                pantallaLogin.classList.add('oculto');
                pantallaLogin.style.display = 'none';

                // Mostrar Catálogo
                pantallaCatalogo.classList.remove('oculto');
                pantallaCatalogo.style.display = 'flex'; // Flex para layout
                pantallaCatalogo.style.opacity = '0';

                // Mostrar Barra de Navegación (Global)
                if (navBar) {
                    navBar.classList.remove('oculto');
                    // navBar.style.display = 'flex'; // Asumido por CSS .barra-navegacion
                }

                // Renderizar productos al entrar
                aplicarFiltrosCombinados(); // Usar la función de filtros que usa el array real

                // Inicializar posición del indicador ahora que ES VISIBLE
                const activeItem = document.querySelector('.nav-item.activo');
                if (activeItem) {
                    // Pequeño delay para asegurar renderizado del DOM
                    setTimeout(() => moverIndicador(activeItem), 50);
                }

                // Animación de entrada del Catálogo
                setTimeout(() => {
                    pantallaCatalogo.style.opacity = '1';
                }, 100);

            }, 500);
        });
    }

    // =========================================
    // LÓGICA DE DETALLE DE PRODUCTO
    // =========================================
    const pantallaDetalle = document.getElementById('pantalla-detalle');
    const btnAtras = document.getElementById('btn-atras');
    const btnAgregarCarrito = document.getElementById('btn-agregar-carrito');

    // Elementos a rellenar
    const detalleImagen = document.getElementById('detalle-imagen');
    const detalleNombre = document.getElementById('detalle-nombre');
    const detallePrecio = document.getElementById('detalle-precio');
    const detalleStock = document.getElementById('detalle-stock'); // Nuevo
    const detalleReferencia = document.getElementById('detalle-referencia');
    // const detalleTexto = document.getElementById('detalle-texto'); // Si tuviéramos descripción real

    // Elementos Lightbox
    const lightbox = document.getElementById('lightbox-imagen');
    const imgFullScreen = document.getElementById('img-full-screen');
    const btnCerrarLightbox = document.getElementById('btn-cerrar-lightbox');

    function mostrarDetalleProducto(producto) {
        // Resetear selectores y llenarlos dinámicamente
        const selectorTalla = document.getElementById('selector-talla');
        selectorTalla.innerHTML = '<option value="" selected disabled>Seleccionar talla</option>';
        document.getElementById('input-cantidad').value = ""; // Iniciar vacío para ver placeholder

        if (producto.tallas && producto.tallas.length > 0) {
            producto.tallas.forEach(talla => {
                const option = document.createElement('option');
                option.value = talla;
                option.textContent = talla;
                selectorTalla.appendChild(option);
            });
        } else {
            // Caso opcional
            const option = document.createElement('option');
            option.value = "Única";
            option.textContent = "Talla Única";
            selectorTalla.appendChild(option);
        }

        // Guardar referencia global para validaciones
        window.productoActualDetalle = producto;

        // Rellenar datos
        detalleNombre.textContent = producto.nombre;
        detallePrecio.innerHTML = `$${producto.precio.toFixed(2)}<span class="sufijo-precio"> por docena</span>`;
        detalleReferencia.textContent = `Ref: ${producto.referencia}`;
        detalleStock.textContent = `Docenas disponibles: ${producto.stock}`; // Stock

        // Color de stock segun disponibilidad (Opcional)
        // Y lógica de deshabilitar inputs si agotado
        const inputCantidad = document.getElementById('input-cantidad');
        // const selectorTalla = document.getElementById('selector-talla'); // ELIMINADO: Ya declarado arriba

        // Establecer máximo en input (asegurar que es atributo HTML válido)
        if (producto.stock > 0) {
            inputCantidad.setAttribute('max', producto.stock.toString());
        } else {
            inputCantidad.removeAttribute('max'); // Opcional, pero si es 0 se bloquea abajo
        }

        console.log(`Stock del producto: ${producto.stock}, Max input: ${inputCantidad.max}`);

        inputCantidad.value = ""; // Asegurar vacío al abrir

        if (producto.stock === 0) {
            detalleStock.style.color = '#D32F2F'; // Rojo agotado
            detalleStock.textContent = "Agotado";

            // Deshabilitar
            inputCantidad.disabled = true;
            selectorTalla.disabled = true;
            btnAgregarCarrito.disabled = true;
            btnAgregarCarrito.textContent = "AGOTADO";
            btnAgregarCarrito.style.backgroundColor = "#ccc";
            btnAgregarCarrito.style.cursor = "not-allowed";
        } else {
            detalleStock.style.color = 'var(--color-primario)'; // Negro disponible, antes color-secundario

            // Habilitar
            inputCantidad.disabled = false;
            selectorTalla.disabled = false;
            btnAgregarCarrito.disabled = false;
            btnAgregarCarrito.textContent = "AGOTADO" ? "AGREGAR AL CARRITO" : "AGREGAR AL CARRITO"; // Restaurar texto si estaba agotado
            // Restaurar estilos del botón (el CSS se encarga del hover, solo quitamos inline si pusimos)
            btnAgregarCarrito.style.backgroundColor = "";
            btnAgregarCarrito.style.cursor = "";

            // Asegurar texto correcto siempre
            btnAgregarCarrito.textContent = "AGREGAR AL CARRITO";
        }

        if (producto.imagen) {
            detalleImagen.src = producto.imagen;
            detalleImagen.classList.remove('imagen-placeholder');

            // Habilitar Zoom
            detalleImagen.onclick = () => {
                imgFullScreen.src = producto.imagen;
                lightbox.classList.remove('oculto');
                lightbox.style.display = 'flex';
                lightbox.style.opacity = '0';
                setTimeout(() => lightbox.style.opacity = '1', 10);
            };

        } else {
            detalleImagen.src = 'assets/img/logo_kiwi.png';
            detalleImagen.classList.add('imagen-placeholder');
            detalleImagen.onclick = null; // No zoom si es placeholder
        }

        // Mostrar pantalla
        history.pushState({ screen: 'detalle' }, "Detalle Producto", "#detalle");
        pantallaDetalle.classList.remove('oculto');
        pantallaDetalle.style.display = 'flex';
        // Animacion simple de entrada
        pantallaDetalle.style.opacity = '0';
        pantallaDetalle.style.transform = 'translateY(20px)';

        setTimeout(() => {
            pantallaDetalle.style.opacity = '1';
            pantallaDetalle.style.transform = 'translateY(0)';
            pantallaDetalle.style.transition = 'all 0.3s ease-out';

            // Limpiar transform después de la animación para evitar problemas con position: fixed
            setTimeout(() => {
                pantallaDetalle.style.transform = 'none';
            }, 300);
        }, 10);
    }

    // Cerrar Lightbox
    if (btnCerrarLightbox) {
        btnCerrarLightbox.addEventListener('click', () => {
            lightbox.style.opacity = '0';
            setTimeout(() => {
                lightbox.classList.add('oculto');
                lightbox.style.display = 'none';
            }, 300);
        });
    }

    // Cerrar Lightbox al tocar fuera
    if (lightbox) {
        lightbox.addEventListener('click', (e) => {
            if (e.target === lightbox) {
                btnCerrarLightbox.click();
            }
        });
    }

    function ocultarDetalleProducto() {
        pantallaDetalle.style.opacity = '0';
        pantallaDetalle.style.transform = 'translateY(20px)';

        setTimeout(() => {
            pantallaDetalle.classList.add('oculto');
            pantallaDetalle.style.display = 'none';
        }, 300);
    }

    if (btnAtras) {
        btnAtras.addEventListener('click', ocultarDetalleProducto);
    }

    // Validación Input Cantidad (Solo Números Enteros)
    const inputCantidad = document.getElementById('input-cantidad');
    if (inputCantidad) {
        inputCantidad.addEventListener('input', (e) => {
            // Reemplazar todo lo que no sea dígito con vacío
            e.target.value = e.target.value.replace(/\D/g, '');
        });
    }

    if (btnAgregarCarrito) {
        btnAgregarCarrito.addEventListener('click', () => {
            const rawCantidad = document.getElementById('input-cantidad').value;
            const cantidad = parseInt(rawCantidad);
            const talla = document.getElementById('selector-talla').value;
            // FIX: Usar la versión más reciente del producto desde el array global 'productos'
            // para evitar validar con datos viejos si la ventana lleva rato abierta.
            const productoEnMemoria = productos.find(p => p.id === window.productoActualDetalle.id);
            const producto = productoEnMemoria || window.productoActualDetalle;

            if (talla === "") {
                mostrarDialogo("Por favor, selecciona una talla para continuar.", 'alerta');
                return;
            }

            // Validar que se haya ingresado cantidad
            if (!rawCantidad || isNaN(cantidad)) {
                mostrarDialogo("Por favor, ingresa una cantidad válida.", 'alerta');
                return;
            }

            if (producto && cantidad > producto.stock) {
                mostrarDialogo(`No hay suficiente stock. Solo quedan ${producto.stock} docenas disponibles.`, 'alerta');
                return;
            }

            if (cantidad < 1) {
                mostrarDialogo("La cantidad mínima es 1.", 'alerta');
                return;
            }

            // Animación de éxito
            const textoOriginal = btnAgregarCarrito.textContent;

            btnAgregarCarrito.classList.add('exito');
            btnAgregarCarrito.innerHTML = '✓ ¡Agregado!'; // Check y texto

            // LIMPIEZA INMEDIATA DE CAMPOS
            document.getElementById('input-cantidad').value = "";
            document.getElementById('selector-talla').value = ""; // Volver al placeholder

            // Lógica REAL de carrito

            // Buscar si ya existe este producto con esta talla
            const indexExistente = carrito.findIndex(item => item.id === producto.id && item.talla === talla);

            if (indexExistente >= 0) {
                // Actualizar cantidad
                const nuevaCantidad = carrito[indexExistente].cantidad + cantidad;
                // Validar stock total vs nueva cantidad (Opcional, pero recomendable)
                if (nuevaCantidad > producto.stock) {
                    mostrarDialogo(`No puedes agregar más. El stock máximo es ${producto.stock}. Ya tienes ${carrito[indexExistente].cantidad} en el carrito.`, 'alerta');
                    return; // Stop
                }
                carrito[indexExistente].cantidad = nuevaCantidad;
            } else {
                // Nuevo item
                carrito.push({
                    id: producto.id,
                    nombre: producto.nombre,
                    referencia: producto.referencia || 'N/A', // Guardar referencia
                    precio: producto.precio,
                    imagen: producto.imagen,
                    tallas: producto.tallas,
                    talla: talla,
                    cantidad: cantidad,
                    stock: producto.stock
                });
            }

            console.log("Carrito actualizado:", carrito);

            setTimeout(() => {
                btnAgregarCarrito.classList.remove('exito');
                btnAgregarCarrito.textContent = textoOriginal;
                ocultarDetalleProducto(); // Cerrar automáticamente al agregar con éxito
            }, 1000); // Reducido tiempo para agilidad
        });
    }

    // Actualizar renderizado para usar la función de detalle
    // Sobreescribimos la función anterior para asegurar que tiene el onclick correcto
    // Nota: Como ya definí 'renderizarProductos' arriba en el mismo scope, necesito modificar esa definición
    // --- LÓGICA FACTURA / CHECKOUT CON NAVEGACIÓN PERSISTENTE ---
    const pantallaFactura = document.getElementById('pantalla-factura');
    const btnAtrasFactura = document.getElementById('btn-atras-factura');
    const btnEnviarPedido = document.getElementById('btn-enviar-pedido');
    const formFactura = document.getElementById('formulario-factura');

    // Estado de Navegación Persistente
    let checkoutPendiente = false;

    // --- FUNCIÓN DE TRANSICIÓN SUAVE (Global) ---
    // --- FUNCIÓN DE TRANSICIÓN SUAVE (Mejorada) ---
    function cambiarPantalla(pantallaSalida, pantallaEntrada, displayMode = 'flex') {
        if (!pantallaSalida || !pantallaEntrada) return;
        if (pantallaSalida === pantallaEntrada) return;

        // Cerrar teclado/foco si existe
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }

        // 1. Ocultar inmediatamente Salida (Fade)
        pantallaSalida.style.opacity = '0';

        // 2. Timeout para cambio de display
        setTimeout(() => {
            // "Reset" completo de displays para evitar que ocupen espacio fantasma
            [pantallaCatalogo, pantallaCarrito, pantallaFactura, pantallaSolicitudes, pantallaDetalle, pantallaLogin, pantallaAgregar, pantallaSolicitudesVendedora, pantallaEditar].forEach(p => {
                if (p) {
                    p.style.display = 'none';
                    p.classList.add('oculto'); // Asegurar clase
                }
            });

            // Preparar Entrada
            pantallaEntrada.classList.remove('oculto');
            pantallaEntrada.style.display = displayMode;
            pantallaEntrada.style.opacity = '0'; // Empezar invisible

            // Scroll Top forzado (Ayuda a resetear viewport en iOS)
            window.scrollTo(0, 0);

            // 3. Fade In
            requestAnimationFrame(() => {
                pantallaEntrada.style.opacity = '1';
            });

        }, 300);
    }

    // Lógica de Navegación (Tabs) - Reemplaza a la anterior
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            const tabId = item.dataset.tab;

            // Determinar pantalla actual visible para salir de ella (Fail-safe)
            let pantallaActual = null;
            if (!pantallaCatalogo.classList.contains('oculto')) pantallaActual = pantallaCatalogo;
            else if (!pantallaCarrito.classList.contains('oculto')) pantallaActual = pantallaCarrito;
            else if (!pantallaFactura.classList.contains('oculto')) pantallaActual = pantallaFactura;
            else if (pantallaSolicitudes && !pantallaSolicitudes.classList.contains('oculto')) pantallaActual = pantallaSolicitudes;

            // Debug
            console.log(`Click Tab: ${tabId}`, {
                actual: pantallaActual ? pantallaActual.id : 'null',
                solicitudesDefined: !!pantallaSolicitudes
            });

            // 1. Manejo Especial: Clic en Carrito con Checkout Pendiente
            if (tabId === 'carrito' && checkoutPendiente) {
                // Visualmente activar tab carrito
                navItems.forEach(n => n.classList.remove('activo'));
                item.classList.add('activo');
                moverIndicador(item);

                // Transición a Factura independientemente de donde venga
                if (pantallaActual !== pantallaFactura) {
                    cambiarPantalla(pantallaActual, pantallaFactura, 'flex');
                }
                return;
            }

            // Evitar re-animar si ya estoy en la misma tab visualmente
            if (item.classList.contains('activo')) return;

            // 2. Flujo Normal
            // Animación indicador
            moverIndicador(item);
            navItems.forEach(n => n.classList.remove('activo'));
            item.classList.add('activo');

            // Determinar pantalla destino
            let pantallaDestino = null;
            let displayDestino = 'flex';

            if (tabId === 'inicio') {
                pantallaDestino = pantallaCatalogo;
                displayDestino = 'grid'; // Grid para catálogo
                aplicarFiltrosCombinados();
            } else if (tabId === 'carrito') {
                pantallaDestino = pantallaCarrito;
                displayDestino = 'flex'; // Flex column (corregido de block)
                renderizarCarrito();
            } else if (tabId === 'solicitudes') {
                pantallaDestino = pantallaSolicitudes;
                displayDestino = 'flex';
                cargarSolicitudes(); // Cargar datos antes de mostrar
            }

            console.log(`Navegando a: ${pantallaDestino ? pantallaDestino.id : 'null'}`, {
                cambioNecesario: pantallaDestino && pantallaActual && pantallaActual !== pantallaDestino
            });

            // Ejecutar transición solo si hay cambio real
            if (pantallaDestino && pantallaActual && pantallaActual !== pantallaDestino) {
                cambiarPantalla(pantallaActual, pantallaDestino, displayDestino);
            } else if (pantallaDestino && !pantallaActual) {
                // Caso raro: No hay pantalla actual (ej. inicio corrupto), forzamos entrada
                pantallaDestino.classList.remove('oculto');
                pantallaDestino.style.display = displayDestino;
                pantallaDestino.style.opacity = '1';
            }
        });
    });

    // Navegación: Carrito -> Factura (Inicio Checkout)
    btnRealizarPedido.addEventListener('click', () => {
        checkoutPendiente = true; // ACTIVAR PERSISTENCIA

        // Push State para soportar Back Button
        history.pushState({ screen: 'factura' }, "Factura", "#factura");

        cambiarPantalla(pantallaCarrito, pantallaFactura, 'flex'); // Navegación Suave

        // NO ocultamos Nav Bar
        if (navBar) navBar.classList.remove('oculto');
    });

    // Navegación: Factura -> Carrito (Cancelar/Volver)
    // Navegación: Factura -> Carrito (Cancelar/Volver)
    btnAtrasFactura.addEventListener('click', () => {
        checkoutPendiente = false; // DESACTIVAR PERSISTENCIA
        cambiarPantalla(pantallaFactura, pantallaCarrito, 'flex'); // Navegación Suave
    });

    // --- VALIDACIONES FACTURA (Estilo Android) ---
    function mostrarError(input, mensaje) {
        const grupo = input.closest('.grupo-input');
        const span = grupo.querySelector('.mensaje-error');
        grupo.classList.add('error');
        span.textContent = mensaje;
        span.style.display = 'block';
    }

    function limpiarError(input) {
        const grupo = input.closest('.grupo-input');
        const span = grupo.querySelector('.mensaje-error');
        grupo.classList.remove('error');
        span.style.display = 'none';
        span.textContent = '';
    }

    // Validadores Individuales
    function validarNombre() {
        const input = document.getElementById('factura-nombre');
        const valor = input.value.trim();
        // Regex: Solo letras y espacios (incluye tildes y ñ)
        const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;

        if (valor.length === 0) {
            mostrarError(input, "El nombre es obligatorio");
            return false;
        } else if (valor.length < 3) {
            mostrarError(input, "El nombre es muy corto");
            return false;
        } else if (!regexNombre.test(valor)) {
            mostrarError(input, "El nombre solo debe contener letras");
            return false;
        }
        limpiarError(input);
        return true;
    }

    function validarCelular() {
        const input = document.getElementById('factura-celular');
        const valor = input.value.trim();
        // Regex: 8 a 12 dígitos
        const regexCelular = /^\d{8,12}$/;

        if (valor.length === 0) {
            mostrarError(input, "El celular es obligatorio");
            return false;
        } else if (!regexCelular.test(valor)) {
            mostrarError(input, "Debe tener entre 8 y 12 dígitos");
            return false;
        }
        limpiarError(input);
        return true;
    }

    function validarEmail() {
        const input = document.getElementById('factura-email');
        const valor = input.value.trim();
        // Regex: Dominios específicos (gmail, outlook, hotmail)
        const regexEmail = /^[^\s@]+@(gmail\.com|outlook\.com|hotmail\.com)$/;

        if (valor.length === 0) {
            mostrarError(input, "El correo es obligatorio");
            return false;
        } else if (!regexEmail.test(valor)) {
            mostrarError(input, "Solo se permiten correos Gmail, Outlook o Hotmail");
            return false;
        }
        limpiarError(input);
        return true;
    }

    // Listeners para validación en tiempo real (Blur e Input)
    const inputNombre = document.getElementById('factura-nombre');
    const inputCelular = document.getElementById('factura-celular');
    const inputEmail = document.getElementById('factura-email');

    // Nombre
    inputNombre.addEventListener('blur', validarNombre);
    inputNombre.addEventListener('input', (e) => {
        // Opcional: Impedir escribir números en tiempo real
        e.target.value = e.target.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '');
        limpiarError(inputNombre);
    });

    // Celular (Solo Números)
    inputCelular.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '').slice(0, 12); // Solo números, max 12
        limpiarError(inputCelular);
    });
    inputCelular.addEventListener('blur', validarCelular);

    // Email
    inputEmail.addEventListener('blur', validarEmail);
    inputEmail.addEventListener('input', () => limpiarError(inputEmail));


    // --- LÓGICA DE SOLICITUDES (Modo "Ver Todo" / Admin) ---
    let unsubscribeSolicitudes = null;

    function cargarSolicitudes() {
        const listaContainer = document.getElementById('lista-solicitudes');
        const vacioContainer = document.querySelector('.contenido-solicitudes .mensaje-vacio');

        if (listaContainer) {
            listaContainer.style.display = 'grid'; // Grid o block
            listaContainer.style.gap = '15px';
            // Mensaje de carga inicial
            if (listaContainer.innerHTML.trim() === '') {
                listaContainer.innerHTML = '<div style="text-align:center; padding: 20px;">Cargando pedidos...</div>';
            }
        }

        // Evitar múltiples suscripciones limpiando la anterior si existe
        if (unsubscribeSolicitudes) {
            unsubscribeSolicitudes();
        }

        // Consultar TODAS las solicitudes en tiempo real (Alineado con reglas DB)
        unsubscribeSolicitudes = db.collection('solicitudes')
            .orderBy('timestampCreacion', 'desc')
            .limit(50)
            .onSnapshot((querySnapshot) => {
                const listaContainer = document.getElementById('lista-solicitudes');
                const vacioContainer = document.querySelector('.contenido-solicitudes .mensaje-vacio');

                if (!listaContainer) return;

                listaContainer.innerHTML = '';

                if (querySnapshot.empty) {
                    listaContainer.style.display = 'none';
                    if (vacioContainer) vacioContainer.style.display = 'flex';
                    return;
                }

                // Hay datos
                if (vacioContainer) vacioContainer.style.display = 'none';
                listaContainer.style.display = 'grid';

                // Cache para PDF
                window.pedidosCache = {};

                querySnapshot.forEach((doc) => {
                    const pedido = doc.data();
                    const id = doc.id;
                    // Guardar en cache para acceso rápido
                    window.pedidosCache[id] = { id, ...pedido };

                    // Lectura de campos compatible (Android vs Web Legacy)
                    // Android: comprador, estado, productos, timestampCreacion/fecha

                    let fechaDisplay = pedido.fecha || "---"; // Usa string fecha por defecto
                    // Si queremos ser más precisos con timestamp si existe
                    if (pedido.timestampCreacion && pedido.timestampCreacion.seconds) {
                        fechaDisplay = new Date(pedido.timestampCreacion.toMillis()).toLocaleDateString('es-ES', {
                            day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
                        });
                    }

                    // Mapeo de Estados (Android)
                    // Pendiente, Aceptada, Devuelta
                    let estadoColor = '#ef6c00'; // Naranja (Pendiente)
                    let estadoBg = '#fff3e0';
                    const estado = pedido.estado || pedido.estatus || 'Pendiente'; // Fallback legacy

                    if (estado === 'Aceptada' || estado === 'completado') {
                        estadoColor = '#2e7d32'; // Verde
                        estadoBg = '#e8f5e9';
                    } else if (estado === 'Devuelta' || estado === 'rechazado') {
                        estadoColor = '#c62828'; // Rojo
                        estadoBg = '#ffebee';
                    }

                    // Generar HTML de productos
                    let productosHtml = '';
                    const listaProductos = pedido.productos || pedido.items || [];
                    listaProductos.forEach(item => {
                        // Safe access a propiedades del producto (puede venir anidado en 'producto' o directo según versión)
                        const datosProd = item.producto || item;
                        const nombre = datosProd.nombre || "Producto";
                        const ref = datosProd.referencia || "---";
                        const precioDoc = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(datosProd.precio || 0);
                        const cant = item.cantidad || 0;

                        productosHtml += `
                                <div class="pedido-item-row">
                                    <div class="pedido-item-nombre">${nombre}</div>
                                    <div class="pedido-item-detalle">
                                        <span>Ref: ${ref}</span>
                                        <span>${cant} Docenas x ${precioDoc}</span>
                                    </div>
                                </div>
                            `;
                    });

                    const nombreCliente = pedido.comprador || (pedido.cliente ? pedido.cliente.nombre : "Cliente Web");
                    const cantidadItems = listaProductos.length;
                    const totalFormato = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(pedido.total || 0);

                    const card = document.createElement('div');
                    card.className = 'pedido-card';
                    // Inline style removed, using .pedido-card class

                    // Botones de Acción según Estado
                    let botonAccionHtml = '';
                    if (estado === 'Aceptada' || estado === 'completado') {
                        botonAccionHtml = `<button class="btn-accion-solicitud btn-descargar" data-id="${id}">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
                            </svg>
                            Comprobante
                        </button>`;
                    } else if (estado === 'Devuelta' || estado === 'rechazado') {
                        // Escapar comillas en motivo para evitar romper HTML
                        const motivoSafe = (pedido.motivoRechazo || "Sin motivo").replace(/"/g, '&quot;');
                        botonAccionHtml = `<button class="btn-accion-solicitud btn-motivo" data-motivo="${motivoSafe}">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="8" x2="12" y2="12"></line>
                                <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
                            Ver Motivo
                        </button>`;
                    }


                    // Layout: ID + (Botón + Estado) | Lista Productos | Fecha + Total
                    card.innerHTML = `
                            <div class="pedido-header">
                                <span class="pedido-id">Orden #${id.slice(0, 6).toUpperCase()}</span>
                                <div class="pedido-header-actions">
                                    ${botonAccionHtml}
                                    <span class="etiqueta-estado" style="background: ${estadoBg}; color: ${estadoColor};">${estado}</span>
                                </div>
                            </div>
                            
                            <div class="pedido-productos">
                                ${productosHtml}
                            </div>

                            <div class="pedido-footer">
                                 <div class="pedido-fecha">${fechaDisplay}</div>
                                <span class="pedido-total">${totalFormato}</span>
                            </div>
                        `;
                    listaContainer.appendChild(card);
                });
            }, (error) => {
                console.error("Error escuchando pedidos:", error);
                const listaContainer = document.getElementById('lista-solicitudes');
                // Mostrar error detallado para diagnostico
                if (listaContainer) listaContainer.innerHTML = `
                    <div style="color: red; text-align: center; padding: 20px;">
                        <strong>Error de Conexión:</strong><br>
                        ${error.code}<br>
                        <small>${error.message}</small><br><br>
                        Verifica las "Reglas" en Firebase Console.
                    </div>`;
            });

        // Listener Delegado para Botones de Acción (Solo una vez)
        // const listaContainer ya existe en el scope de la función
        if (listaContainer && !listaContainer.dataset.listenerAdjunto) {
            listaContainer.addEventListener('click', (e) => {
                const btnDescargar = e.target.closest('.btn-descargar');
                const btnMotivo = e.target.closest('.btn-motivo');

                if (btnDescargar) {
                    e.stopPropagation();
                    const idPedido = btnDescargar.dataset.id;
                    const pedidoData = window.pedidosCache[idPedido];

                    if (pedidoData) {
                        // Llamar a función del nuevo script pdfGenerator.js
                        if (typeof generarComprobantePDF === 'function') {
                            generarComprobantePDF(pedidoData);
                        } else {
                            alert("Error: Generador de PDF no cargado.");
                        }
                    } else {
                        alert("Error: Datos del pedido no encontrados.");
                    }
                } else if (btnMotivo) {
                    e.stopPropagation();
                    const motivoRaw = btnMotivo.dataset.motivo;
                    const motivoTexto = (motivoRaw && motivoRaw !== "Sin motivo") ? motivoRaw : "No se especificó un motivo detallado.";
                    // Simulamos "Negrita" con mayúsculas y espaciado en el alert nativo
                    alert(`MOTIVO DE DEVOLUCIÓN\n\n${motivoTexto}`);
                }
            });
            listaContainer.dataset.listenerAdjunto = "true";
        }
    }

    // Función Helper para Cerrar Factura y Limpiar
    function cerrarFactura() {
        checkoutPendiente = false;
        formFactura.reset();

        // Limpiar errores visuales
        const inputs = formFactura.querySelectorAll('input');
        inputs.forEach(input => limpiarError(input));

        // Navegar a catálogo (o inicio)
        cambiarPantalla(pantallaFactura, pantallaCatalogo, 'grid');

        // Reset Nav Tab a Inicio
        const tabInicio = document.querySelector('.nav-item[data-tab="inicio"]');
        if (tabInicio) {
            navItems.forEach(n => n.classList.remove('activo'));
            tabInicio.classList.add('activo');
            if (typeof moverIndicador === 'function') moverIndicador(tabInicio);
        }
    }

    // Acción: Enviar Pedido (Actualizado con Firestore)
    btnEnviarPedido.addEventListener('click', (e) => {
        e.preventDefault(); // FIX: Evitar recarga de página en iPhone/Android

        const nombreValido = validarNombre();
        const celularValido = validarCelular();
        const emailValido = validarEmail();

        if (!nombreValido || !celularValido || !emailValido) return;

        // UI Loading
        btnEnviarPedido.disabled = true;
        btnEnviarPedido.textContent = "Enviando...";

        // Formato fechas compatible con Android
        const now = new Date();
        const fechaString = now.getFullYear() + "-" +
            String(now.getMonth() + 1).padStart(2, '0') + "-" +
            String(now.getDate()).padStart(2, '0');
        const horaString = String(now.getHours()).padStart(2, '0') + ":" +
            String(now.getMinutes()).padStart(2, '0') + ":" +
            String(now.getSeconds()).padStart(2, '0');

        // Estructura EXACTA de Android (SharedViewModel.kt)
        // Transformar items del carrito (planos) a ProductoEnPedido (anidados)
        const productosAndroid = carrito.map(item => {
            return {
                cantidad: item.cantidad,
                producto: {
                    idFirestore: item.id || "",
                    nombre: item.nombre || "Producto",
                    referencia: item.referencia || "",
                    precio: Number(item.precio) || 0,
                    stock: Number(item.stock) || 0,
                    // Android usa 'imagenUrl', Web usaba 'imagen'
                    // Aseguramos compatibilidad
                    imagenUrl: item.imagen || "",
                    categoria: item.categoria || "Otros",
                    tallas: item.tallas || [],
                    // Campos adicionales para evitar nulls en Android
                    cantidadReservada: 0,
                    timestamp: firebase.firestore.Timestamp.now()
                }
            };
        });


        const pedido = {
            comprador: inputNombre.value.trim(), // Antes cliente.nombre
            celular: inputCelular.value.trim(),  // Antes cliente.celular
            // email: inputEmail.value.trim(),   // Android no guarda email en Solicitud

            productos: productosAndroid, // Usamos la estructura transformada
            total: carrito.reduce((sum, item) => sum + (item.precio * item.cantidad), 0),

            fecha: fechaString, // String YYYY-MM-DD
            hora: horaString,   // String HH:mm:ss
            timestampCreacion: firebase.firestore.Timestamp.now(), // Antes fecha

            estado: 'Pendiente', // Capitalizado (Android usa: Pendiente, Aceptada, Devuelta)
            motivoRechazo: "",
            plataforma: 'web',

            // FIX: Agregar info del dispositivo para que Cloud Functions envíen notificaciones
            userAgent: navigator.userAgent || 'unknown',
            deviceType: /mobile|android|iphone|ipad/i.test(navigator.userAgent) ? 'mobile' : 'desktop'
        };

        // Transacción de Escritura (Atomicidad: Pedido + Inventario)
        // Transacción de Escritura (Atomicidad: Pedido + Inventario)
        // REPLICA EXACTA DE ANDROID (SharedViewModel.kt): Leer -> Validar -> Escribir
        db.runTransaction(async (transaction) => {
            // 1. LEER: Obtener snapshots actualizados de todos los productos en el carrito
            const referencias = [];

            // Preparar referencias
            productosAndroid.forEach(item => {
                if (item.producto.idFirestore) {
                    const ref = db.collection('productos').doc(item.producto.idFirestore);
                    referencias.push({ ref: ref, cantidadSolicitada: item.cantidad, nombre: item.producto.nombre });
                }
            });

            // Ejecutar todas las lecturas en paralelo (dentro de la transacción)
            const snapshots = await Promise.all(referencias.map(r => transaction.get(r.ref)));

            // 2. VALIDAR: Comprobar stock disponible (Stock - Reservado)
            snapshots.forEach((snap, index) => {
                if (!snap.exists) {
                    throw new Error(`El producto "${referencias[index].nombre}" ya no existe.`);
                }

                const prodData = snap.data();
                const stockFisico = Number(prodData.stock) || 0;
                const reservado = Number(prodData.cantidadReservada) || 0;
                const disponibleReal = stockFisico - reservado;
                const solicitado = referencias[index].cantidadSolicitada;

                if (solicitado > disponibleReal) {
                    throw new Error(`Stock insuficiente para "${referencias[index].nombre}".\nSolicitaste: ${solicitado}\nDisponibles: ${disponibleReal}`);
                }
            });

            // 3. ESCRIBIR: Si todo está bien, proceder con los updates y el set
            const nuevaSolicitudRef = db.collection('solicitudes').doc();

            // Actualizar cantidadReservada en cada producto
            referencias.forEach(r => {
                transaction.update(r.ref, {
                    cantidadReservada: firebase.firestore.FieldValue.increment(r.cantidadSolicitada)
                });
            });

            // Crear el pedido con ID asignado
            const pedidoConId = { ...pedido, idFirestore: nuevaSolicitudRef.id };
            transaction.set(nuevaSolicitudRef, pedidoConId);

            return nuevaSolicitudRef.id;

        }).then((idGenerado) => {
            console.log("Pedido creado con éxito ID:", idGenerado);

            // Mensaje de éxito
            mostrarDialogo("Tu orden ha sido registrada. Se ha reservado tu mercancía y una vendedora lo revisará.", 'alerta', null, "✅ ¡Pedido Enviado con Éxito!");

            // --- LÓGICA DE CARRITO (Estado Global) ---
            // FIX: No usar 'let' para no ocultar la variable global 'carrito'.
            // Sincronizamos con localStorage por seguridad antes de enviar.
            const carritoStorage = JSON.parse(localStorage.getItem('kiwi_carrito')) || [];

            // Si la variable global está vacía pero hay storage, actualizamos global
            if (carrito.length === 0 && carritoStorage.length > 0) {
                carrito.push(...carritoStorage);
            }

            // Función auxiliar para actualizar el badge del carrito
            function actualizarContadorCarrito() {
                const badge = document.querySelector('.badge-carrito');
                if (!badge) return;

                const totalItems = carrito.reduce((sum, item) => sum + item.cantidad, 0);
                badge.textContent = totalItems;

                if (totalItems > 0) {
                    badge.style.display = 'flex';
                    badge.classList.remove('oculto');
                } else {
                    badge.style.display = 'none';
                    badge.classList.add('oculto');
                }
            }

            // Llamada inicial para restaurar estado
            actualizarContadorCarrito();

            // Actualizar Contador al iniciar
            // Nota: renderizarCarrito se define más abajorá la vista

            // Limpiar datos
            localStorage.removeItem('kiwi_carrito');
            carrito.length = 0; // Limpiar array global en lugar de reasignar
            actualizarContadorCarrito();
            renderizarCarrito(); // Limpiará la vista

            // Cerrar factura y volver
            cerrarFactura();
            navItems[0].click(); // Ir a inicio

            // Guardar IDs locales (opcional)
            const historial = JSON.parse(localStorage.getItem('kiwi_pedidos_locales') || '[]');
            historial.push(idGenerado);
            localStorage.setItem('kiwi_pedidos_locales', JSON.stringify(historial));

        }).catch((error) => {
            console.error("Error en transacción:", error);
            mostrarDialogo("Hubo un error al procesar tu pedido. Inténtalo nuevamente.\n\n" + error.message, 'alerta');
        }).finally(() => {
            // FIX: Asegurar que el botón se reactiva SIEMPRE, incluso si hay error
            const btn = document.getElementById('btn-enviar-pedido');
            if (btn) {
                btn.disabled = false;
                btn.textContent = "ENVIAR PEDIDO AHORA";
            }
        });
    });

    // =========================================
    // PANTALLA SOLICITUDES VENDEDORA (Admin)
    // =========================================
    const pantallaSolicitudesVendedora = document.getElementById('pantalla-solicitudes-vendedora');
    const btnAtrasSolicitudesVendedora = document.getElementById('btn-atras-solicitudes-vendedora');
    const btnAdminSolicitudes = document.getElementById('btn-admin-solicitudes');
    const badgeAdminSolicitudes = document.getElementById('badge-admin-solicitudes'); // Si existe badge

    let unsubscribeSolicitudesAdmin = null;

    // Función: Cargar Solicitudes (Admin - Todas)
    function cargarSolicitudesVendedora() {
        const listaContainer = document.getElementById('lista-solicitudes-vendedora');
        const emptyState = document.getElementById('mensaje-vacio-vendedora');

        if (!listaContainer) return;

        // Limpiar previo
        if (unsubscribeSolicitudesAdmin) {
            unsubscribeSolicitudesAdmin();
        }

        console.log("Cargando solicitudes de vendedora...");
        // Escuchar TODAS las solicitudes
        unsubscribeSolicitudesAdmin = db.collection('solicitudes')
            .orderBy('timestampCreacion', 'desc') // Más recientes primero
            .onSnapshot((snapshot) => {
                const totalDocs = snapshot.size;
                console.log(`Solicitudes encontradas (Admin): ${totalDocs}`);

                // Badge Admin Update: Solo mostrar si hay NUEVAS (Pendientes)
                let pendientes = 0;
                snapshot.forEach(doc => {
                    if (doc.data().estado === 'Pendiente') pendientes++;
                });

                if (badgeAdminSolicitudes) {
                    const elPantalla = document.getElementById('pantalla-solicitudes-vendedora');
                    const estaVisible = elPantalla && !elPantalla.classList.contains('oculto') && elPantalla.style.display !== 'none';

                    // Mostrar solo si hay pendientes Y NO estoy viendo la pantalla
                    if (pendientes > 0 && !estaVisible) {
                        badgeAdminSolicitudes.textContent = '';
                        badgeAdminSolicitudes.classList.remove('oculto');
                    } else {
                        // Si no hay pendientes o estoy viendo la pantalla, ocultar
                        badgeAdminSolicitudes.classList.add('oculto');
                    }
                }

                if (totalDocs === 0) {
                    listaContainer.style.display = 'none';
                    if (emptyState) emptyState.style.display = 'block';
                    listaContainer.innerHTML = '';
                    return;
                }

                if (emptyState) emptyState.style.display = 'none';
                listaContainer.style.display = 'block';
                listaContainer.innerHTML = '';

                snapshot.forEach((doc) => {
                    const pedido = doc.data();
                    const id = doc.id;

                    // Formato Fecha
                    let fechaDisplay = pedido.fecha || "---";
                    if (pedido.timestampCreacion && pedido.timestampCreacion.seconds) {
                        fechaDisplay = new Date(pedido.timestampCreacion.toMillis()).toLocaleDateString('es-ES', {
                            day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
                        });
                    }

                    // Estado Visual
                    let estadoColor = '#ef6c00'; // Naranja
                    let estadoBg = '#fff3e0';
                    const estado = pedido.estado || 'Pendiente';

                    if (estado === 'Aceptada' || estado === 'completado') {
                        estadoColor = '#2e7d32'; // Verde
                        estadoBg = '#e8f5e9';
                    } else if (estado === 'Devuelta' || estado === 'rechazado') {
                        estadoColor = '#c62828'; // Rojo
                        estadoBg = '#ffebee';
                    }

                    // Productos HTML y Total Docenas
                    let productosHtml = '';
                    let totalDocenas = 0;
                    const listaProductos = pedido.productos || [];

                    listaProductos.forEach(item => {
                        const datosProd = item.producto || item;
                        const nombre = datosProd.nombre || "Producto";
                        const ref = datosProd.referencia || "---";
                        const precioDoc = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(datosProd.precio || 0);
                        const cant = Number(item.cantidad) || 0;
                        totalDocenas += cant;

                        productosHtml += `
                            <div style="margin-bottom: 8px; padding-bottom: 8px; border-bottom: 1px dashed #eee;">
                                <div style="font-weight: 600; font-size: 14px; color: #333;">${nombre}</div>
                                <div style="font-size: 12px; color: #666; display: flex; justify-content: space-between; margin-top: 2px;">
                                    <span>Ref: ${ref}</span>
                                    <span>${cant} Dcns x ${precioDoc}</span>
                                </div>
                            </div>
                        `;
                    });

                    const nombreCliente = pedido.comprador || "Cliente";
                    const celularCliente = pedido.celular || "---";
                    const totalFormato = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(pedido.total || 0);

                    const card = document.createElement('div');
                    card.className = 'pedido-card-admin';
                    // Estilo base idéntico a comprador + cursor pointer para indicar click
                    card.style.cssText = `
                        background: white; border-radius: 12px; padding: 15px; margin-bottom: 15px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.05); display: flex; flex-direction: column; gap: 0px;
                        cursor: pointer; transition: all 0.2s ease; overflow: hidden;
                        width: 100%; box-sizing: border-box;
                    `;

                    // Lógica de expansión al hacer click (Con animación CSS)
                    card.onclick = (e) => {
                        // Evitar colapso si se clickea en botones de acción futuros
                        if (e.target.closest('.acciones-admin')) return;

                        // Togglear clase en la tarjeta principal
                        card.classList.toggle('expandida');
                    };

                    card.innerHTML = `
                        <!-- HEADER (Siempre visible) -->
                        <div style="position: relative; padding-right: 30px;"> <!-- Padding derecho para evitar solapamiento con estado -->
                            
                            <!-- Estado (Top Right Absolute) -->
                            <div style="position: absolute; top: -5px; right: -5px; z-index: 2;">
                                <span style="font-size: 11px; background: ${estadoBg}; color: ${estadoColor}; padding: 3px 8px; border-radius: 12px; font-weight: 600;">${estado}</span>
                            </div>

                            <div style="display: flex; flex-direction: column; gap: 2px;">
                                <span style="font-weight: 700; color: #333; font-size: 15px; margin-bottom: 6px;">Orden #${id.slice(0, 6).toUpperCase()}</span>
                                <div style="font-weight: 600; font-size: 14px; color: #444;">${nombreCliente}</div>
                                <div style="font-size: 13px; color: #666;">${celularCliente}</div>
                                <div style="margin-top: 4px; font-weight: 700; color: var(--color-primario); font-size: 13px;">
                                    Solicitadas: ${totalDocenas} docenas
                                </div>
                            </div>

                            <!-- Icono Chevron (Bottom Right Absolute) -->
                            <div style="position: absolute; bottom: 0px; right: 0px; display: flex; align-items: center; justify-content: center; width: 24px; height: 24px;">
                                <svg class="icono-expandir" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" style="transition: transform 0.3s ease;">
                                    <path d="M6 9l6 6 6-6"/>
                                </svg>
                            </div>
                        </div>
                        
                        <!-- BODY (Controlado por CSS) -->
                        <div class="card-body-detalles">
                            <!-- Lista Productos -->
                            <div style="background: #fff; padding: 10px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 10px;">
                                ${productosHtml}
                            </div>

                            <!-- Footer Data -->
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 5px; padding: 0 5px;">
                                 <div style="font-size: 12px; color: #888;">${fechaDisplay}</div>
                                <div style="text-align: right;">
                                    <span style="display: block; font-size: 11px; color: #666;">Total Pedido:</span>
                                    <span style="font-weight: 700; color: var(--color-primario); font-size: 18px;">${totalFormato}</span>
                                </div>
                            </div>
                            
                            <!-- ÁREA DE ACCIONES ADMIN -->
                            <div class="acciones-admin" style="display: flex; flex-direction: column; gap: 10px; margin-top: 15px;">
                                <!-- Se llena dinámicamente ABAJO para tener referencias a los elementos -->
                            </div>
                        </div>
                    `;
                    listaContainer.appendChild(card);

                    // --- LÓGICA DE BOTONES DINÁMICOS ---
                    const containerAcciones = card.querySelector('.acciones-admin');

                    // 1. ESTADO PENDIENTE
                    if (estado === 'Pendiente') {
                        containerAcciones.innerHTML = `
                            <div class="vendor-actions-row">
                                <button class="btn-vendor-action accept btn-accion-aceptar">
                                    ACEPTAR
                                </button>
                                <button class="btn-vendor-action reject btn-accion-devolver">
                                    DEVOLVER
                                </button>
                            </div>
                        `;

                        // Listener ACEPTAR
                        card.querySelector('.btn-accion-aceptar').onclick = (e) => {
                            e.stopPropagation(); // No colapsar card
                            mostrarDialogo('¿Confirmar aceptación del pedido?', 'confirmacion', () => {
                                db.runTransaction(async (transaction) => {
                                    const pedidoRef = db.collection('solicitudes').doc(id);
                                    const pedidoDoc = await transaction.get(pedidoRef);

                                    if (!pedidoDoc.exists) throw new Error("El pedido no existe.");
                                    const dataPedido = pedidoDoc.data();

                                    if (dataPedido.estado !== 'Pendiente') {
                                        throw new Error("El pedido ya no está en estado Pendiente.");
                                    }

                                    // 1. Actualizar Estado del Pedido
                                    transaction.update(pedidoRef, {
                                        estado: 'Aceptada'
                                    });

                                    // 2. Actualizar Stock e Inventario (Iterar productos)
                                    // Al aceptar: Restamos Stock Real (se llevó) y Restamos Reserva (ya no está reservado, está vendido)
                                    const items = dataPedido.productos || [];
                                    items.forEach(item => {
                                        const prodId = item.producto?.idFirestore || item.producto?.id;
                                        const cantidad = item.cantidad;

                                        if (prodId && cantidad > 0) {
                                            const prodRef = db.collection('productos').doc(prodId);
                                            transaction.update(prodRef, {
                                                stock: firebase.firestore.FieldValue.increment(-cantidad),
                                                cantidadReservada: firebase.firestore.FieldValue.increment(-cantidad)
                                            });
                                        }
                                    });
                                }).then(() => {
                                    mostrarDialogo('Pedido aceptado y stock actualizado.', 'alerta');
                                }).catch(err => mostrarDialogo('Error: ' + err.message, 'alerta'));
                            });
                        };

                        // Listener DEVOLVER
                        card.querySelector('.btn-accion-devolver').onclick = (e) => {
                            e.stopPropagation();
                            // Mostrar input para motivo (simple prompt por ahora)
                            // Para hacerlo más elegante en el futuro se podría usar un div en el DOM, 
                            // pero el req pide "aparece un cuadro" tipo prompt o input. 
                            // Usaremos un prompt nativo por simplicidad y robustez inmediata, 
                            // o inyectamos un mini form si el usuario lo prefiere visual.
                            // Dado "aparece un cuadro", inyectaremos un mini formulario en la card

                            containerAcciones.innerHTML = `
                                <div class="rejection-form">
                                    <label class="rejection-label">Motivo de devolución:</label>
                                    <input type="text" class="rejection-input input-motivo" placeholder="Ej: Falta de stock...">
                                    <div class="rejection-actions">
                                        <button class="btn-rejection-cancel btn-cancelar-dev">Cancelar</button>
                                        <button class="btn-rejection-confirm btn-confirmar-dev">Confirmar</button>
                                    </div>
                                </div>
                            `;

                            // Lógica Mini Form Devolución
                            const btnConfirmar = containerAcciones.querySelector('.btn-confirmar-dev');
                            const btnCancelar = containerAcciones.querySelector('.btn-cancelar-dev');
                            const inputMotivo = containerAcciones.querySelector('.input-motivo');
                            inputMotivo.focus();

                            btnCancelar.addEventListener('click', (ev) => {
                                ev.stopPropagation();
                                cargarSolicitudesVendedora(); // Recargar para volver al estado inicial botones
                            });

                            btnConfirmar.addEventListener('click', (ev) => {
                                ev.stopPropagation();
                                const motivo = inputMotivo.value.trim();
                                // if (!motivo) return mostrarDialogo("Escribe un motivo.", 'alerta');

                                db.runTransaction(async (transaction) => {
                                    const pedidoRef = db.collection('solicitudes').doc(id);
                                    const pedidoDoc = await transaction.get(pedidoRef);

                                    if (!pedidoDoc.exists) throw new Error("El pedido no existe.");
                                    const dataPedido = pedidoDoc.data();

                                    if (dataPedido.estado !== 'Pendiente') {
                                        throw new Error("El pedido ya no está en estado Pendiente.");
                                    }

                                    // 1. Actualizar Estado del Pedido
                                    transaction.update(pedidoRef, {
                                        estado: 'Devuelta',
                                        motivoRechazo: motivo
                                    });

                                    // 2. Devolver Stock Reservado (Iterar productos)
                                    const items = dataPedido.productos || [];
                                    items.forEach(item => {
                                        const prodId = item.producto?.idFirestore || item.producto?.id;
                                        const cantidad = item.cantidad;

                                        if (prodId && cantidad > 0) {
                                            const prodRef = db.collection('productos').doc(prodId);
                                            // Decrementar Atomicamente
                                            transaction.update(prodRef, {
                                                cantidadReservada: firebase.firestore.FieldValue.increment(-cantidad)
                                            });
                                        }
                                    });
                                }).then(() => {
                                    mostrarDialogo('Pedido devuelto y stock liberado.', 'alerta');
                                }).catch(err => mostrarDialogo('Error: ' + err.message, 'alerta'));
                            });

                            // Prevent click propagation on input
                            containerAcciones.querySelector('.form-devolucion').onclick = (ev) => ev.stopPropagation();
                        };
                    }

                    // 2. ESTADO ACEPTADA
                    else if (estado === 'Aceptada') {
                        containerAcciones.innerHTML = `
                            <button class="btn-descargar-pdf" style="width: 100%; background: #000; color: white; border: none; padding: 12px; border-radius: 8px; font-weight: bold; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg> 
                                DESCARGAR COMPROBANTE
                            </button>
                        `;

                        card.querySelector('.btn-descargar-pdf').onclick = (e) => {
                            e.stopPropagation();
                            // Llamar función de generar PDF real
                            if (typeof generarComprobantePDF === 'function') {
                                generarComprobantePDF(pedido).then(() => {
                                    console.log("PDF Generado");
                                }).catch(err => {
                                    console.error("Error PDF", err);
                                    mostrarDialogo("Error al generar PDF: " + err.message, 'alerta');
                                });
                            } else {
                                mostrarDialogo("Error: Librería PDF no cargada.", 'alerta');
                            }
                        };
                    }

                    // 3. ESTADO DEVUELTA
                    else if (estado === 'Devuelta') {
                        const motivo = pedido.motivoRechazo || "Sin motivo especificado";
                        containerAcciones.innerHTML = `
                             <div style="background: #ffebee; color: #c62828; padding: 10px; border-radius: 8px; font-size: 13px; border: 1px solid #ef9a9a;">
                                <strong>Motivo:</strong> ${motivo}
                            </div>
                        `;
                    }

                });

            }, (error) => {
                console.error("Error al cargar solicitudes vendedora:", error);
                if (listaContainer) listaContainer.innerHTML = `<p style="color:red; text-align:center;">Error al cargar datos.</p>`;
            });
    }

    // BOTÓN FLOTANTE: IR A SOLICITUDES VENDEDORA
    if (btnAdminSolicitudes) {
        btnAdminSolicitudes.addEventListener('click', () => {
            console.log("Abriendo Solicitudes Vendedora");

            // Mostrar con transición
            cambiarPantalla(pantallaCatalogo, pantallaSolicitudesVendedora, 'flex');

            // Cargar datos
            cargarSolicitudesVendedora();

            // Ocultar botones flotantes admin
            const controlesAdmin = document.getElementById('controles-admin');
            if (controlesAdmin) {
                controlesAdmin.classList.add('oculto');
                controlesAdmin.style.display = 'none';
            }
        });
    }


    // BOTÓN ATRÁS: VOLVER AL CATÁLOGO
    if (btnAtrasSolicitudesVendedora) {
        btnAtrasSolicitudesVendedora.addEventListener('click', () => {
            // Volver con transición suave
            cambiarPantalla(pantallaSolicitudesVendedora, pantallaCatalogo, 'grid');

            // Restaurar botones flotantes admin
            const controlesAdmin = document.getElementById('controles-admin');
            if (controlesAdmin) {
                controlesAdmin.classList.remove('oculto');
                controlesAdmin.style.display = 'block';
            }
        });
    }


    // =========================================
    // PANTALLA DE EDICIÓN DE PRODUCTO (Vendedor)
    // =========================================
    const pantallaEditar = document.getElementById('pantalla-editar-producto');
    const btnAtrasEditar = document.getElementById('btn-atras-editar');
    const formEditar = document.getElementById('form-editar-producto');
    const listaTallasChips = document.getElementById('lista-tallas-chips');
    const inputNuevaTalla = document.getElementById('input-nueva-talla');
    const btnAddTalla = document.getElementById('btn-add-talla');
    const previewImagen = document.getElementById('edit-preview-imagen');
    const placeholderImg = document.getElementById('edit-placeholder-img');
    const inputImagenFile = document.getElementById('edit-input-imagen');

    let productoEditandoId = null;
    let tallasEditando = []; // Array local de tallas
    let nuevaImagenFile = null; // Para guardar el archivo si se cambia

    if (btnAtrasEditar) {
        btnAtrasEditar.addEventListener('click', () => {
            // Volver al catálogo con transición suave
            cambiarPantalla(pantallaEditar, pantallaCatalogo, 'grid');

            // Restaurar botones flotantes admin
            const controlesAdmin = document.getElementById('controles-admin');
            if (controlesAdmin) {
                controlesAdmin.classList.remove('oculto');
                controlesAdmin.style.display = 'block';
            }
        });
    }

    // LISTENER IMAGEN PREVIEW
    if (inputImagenFile) {
        inputImagenFile.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                nuevaImagenFile = file;
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewImagen.src = e.target.result;
                    previewImagen.style.display = 'block';
                    placeholderImg.style.display = 'none';
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // LISTENER AGREGAR TALLA
    if (btnAddTalla) {
        btnAddTalla.addEventListener('click', () => {
            const val = inputNuevaTalla.value.trim().toUpperCase(); // Normalizar
            if (val && !tallasEditando.includes(val)) {
                tallasEditando.push(val);
                renderizarChipsTallas();
                inputNuevaTalla.value = '';
            } else if (tallasEditando.includes(val)) {
                mostrarDialogo("Esa talla ya está agregada.", 'alerta');
            }
        });
    }

    // RENDERIZAR CHIPS
    function renderizarChipsTallas() {
        listaTallasChips.innerHTML = '';
        tallasEditando.forEach((talla, index) => {
            const chip = document.createElement('div');
            chip.className = 'chip-edit';
            chip.innerHTML = `
                ${talla}
                <span class="chip-delete" onclick="eliminarTalla(${index})">×</span>
            `;
            listaTallasChips.appendChild(chip);
        });
    }

    // Función global para poder llamarla desde el onclick del HTML generado
    window.eliminarTalla = function (index) {
        tallasEditando.splice(index, 1);
        renderizarChipsTallas();
    };

    // FUNCIÓN: MOSTRAR PANTALLA EDITAR
    window.mostrarPantallaEditar = function (producto) {
        console.log("Abriendo editar para:", producto.nombre);
        productoEditandoId = producto.id;
        tallasEditando = [...(producto.tallas || [])]; // Copia de array
        nuevaImagenFile = null; // Reset

        // Llenar inputs
        document.getElementById('edit-nombre').value = producto.nombre;
        document.getElementById('edit-referencia').value = producto.referencia;
        document.getElementById('edit-categoria').value = producto.categoria;
        document.getElementById('edit-docenas').value = producto.stock; // Stock Real
        document.getElementById('edit-precio').value = producto.precio.toFixed(2);

        // Preview Imagen
        if (producto.imagen) {
            previewImagen.src = producto.imagen;
            previewImagen.style.display = 'block';
            placeholderImg.style.display = 'none';
        } else {
            previewImagen.style.display = 'none';
            placeholderImg.style.display = 'block';
        }

        renderizarChipsTallas();

        // Mostrar pantalla con Transición Suave
        cambiarPantalla(pantallaCatalogo, pantallaEditar, 'flex');

        // Ocultar botones flotantes admin
        const controlesAdmin = document.getElementById('controles-admin');
        if (controlesAdmin) {
            controlesAdmin.classList.add('oculto');
            controlesAdmin.style.display = 'none';
        }
    };

    // AJUSTAR STOCK (+/-)
    window.ajustarStock = function (delta) {
        const input = document.getElementById('edit-docenas');
        let val = parseInt(input.value) || 0;
        val = Math.max(0, val + delta); // No negativos
        input.value = val;
    };

    // VALIDACIÓN DE INPUTS (Solo positivos, sin signos)
    const inputEditDocenas = document.getElementById('edit-docenas');
    const inputEditPrecio = document.getElementById('edit-precio');

    [inputEditDocenas, inputEditPrecio].forEach(input => {
        if (input) {
            // Prevenir signos en keydown
            input.addEventListener('keydown', (e) => {
                if (['-', '+', 'e', 'E'].includes(e.key)) {
                    e.preventDefault();
                }
            });
            // Sanitizar en input (paste, etc)
            input.addEventListener('input', (e) => {
                let val = e.target.value;
                // Si es precio permite punto, si es docenas no
                if (e.target.id === 'edit-precio') {
                    if (val < 0) e.target.value = Math.abs(val);
                } else {
                    e.target.value = val.replace(/[^0-9]/g, '');
                }
            });
        }
    });

    // GUARDAR CAMBIOS (Submit)
    if (formEditar) {
        formEditar.addEventListener('submit', (e) => {
            e.preventDefault();

            const nuevoNombre = document.getElementById('edit-nombre').value.trim();
            const nuevaRef = document.getElementById('edit-referencia').value.trim();
            const nuevaCat = document.getElementById('edit-categoria').value;
            const nuevoStock = parseInt(document.getElementById('edit-docenas').value);
            const nuevoPrecio = parseFloat(document.getElementById('edit-precio').value);

            if (!nuevoNombre || !nuevaRef || !nuevaCat || isNaN(nuevoStock) || isNaN(nuevoPrecio)) {
                mostrarDialogo("Por favor completa todos los campos correctamente.", 'alerta');
                return;
            }

            if (tallasEditando.length === 0) {
                mostrarDialogo("Debes agregar al menos una talla.", 'alerta');
                return;
            }

            // Feedback visual
            const btnGuardar = formEditar.querySelector('.boton-primario');
            const txtOriginal = btnGuardar.textContent;
            btnGuardar.textContent = "Guardando...";
            btnGuardar.disabled = true;

            const updateData = {
                nombre: nuevoNombre,
                referencia: nuevaRef,
                categoria: nuevaCat,
                stock: nuevoStock,
                precio: nuevoPrecio,
                tallas: tallasEditando
            };

            const refProducto = db.collection("productos").doc(productoEditandoId);

            const promesaActualizacion = new Promise((resolve, reject) => {
                if (nuevaImagenFile) {
                    // 1. Si hay imagen nueva, subirla primero
                    const storageRef = firebase.storage().ref();
                    const nombreArchivo = `productos/${Date.now()}_${nuevaImagenFile.name}`;
                    const imagenRef = storageRef.child(nombreArchivo);

                    imagenRef.put(nuevaImagenFile)
                        .then(snapshot => snapshot.ref.getDownloadURL())
                        .then(url => {
                            updateData.imagenUrl = url; // Campo Firestore
                            // Actualizar documento con imagen
                            return refProducto.update(updateData);
                        })
                        .then(() => resolve())
                        .catch(error => reject(error));
                } else {
                    // 2. Solo actualizar datos (sin cambiar imagen)
                    refProducto.update(updateData)
                        .then(() => resolve())
                        .catch(error => reject(error));
                }
            });

            promesaActualizacion
                .then(() => {
                    mostrarDialogo("Producto actualizado correctamente.", 'alerta');
                    // Volver con transición suave
                    cambiarPantalla(pantallaEditar, pantallaCatalogo, 'grid');

                    // Restaurar botones flotantes admin
                    const controlesAdmin = document.getElementById('controles-admin');
                    if (controlesAdmin) {
                        controlesAdmin.classList.remove('oculto');
                        controlesAdmin.style.display = 'block';
                    }
                })
                .catch((error) => {
                    console.error("Error al actualizar:", error);
                    mostrarDialogo("Error al guardar cambios: " + error.message, 'alerta');
                })
                .finally(() => {
                    btnGuardar.textContent = txtOriginal;
                    btnGuardar.disabled = false;
                });
        });
    }

    // =========================================
    // PANTALLA DE AGREGAR PRODUCTO (Vendedor)
    // =========================================
    const pantallaAgregar = document.getElementById('pantalla-agregar-producto');
    const btnAtrasAgregar = document.getElementById('btn-atras-agregar');
    const formAgregar = document.getElementById('form-agregar-producto');
    const listaTallasChipsAdd = document.getElementById('add-lista-tallas-chips');
    const inputNuevaTallaAdd = document.getElementById('add-input-nueva-talla');
    const btnAddTallaAdd = document.getElementById('btn-add-talla-agregar');
    const previewImagenAdd = document.getElementById('add-preview-imagen');
    const placeholderImgAdd = document.getElementById('add-placeholder-img');
    const inputImagenFileAdd = document.getElementById('add-input-imagen');

    let tallasAgregar = []; // Array local de tallas para nuevo producto
    let nuevaImagenFileAdd = null; // Archivo obligatorio

    // BOTÓN FAB: ABRIR AGREGAR
    const btnAdminAgregar = document.getElementById('btn-admin-agregar');
    if (btnAdminAgregar) {
        btnAdminAgregar.addEventListener('click', () => {
            mostrarPantallaAgregar();
        });
    }

    if (btnAtrasAgregar) {
        btnAtrasAgregar.addEventListener('click', () => {
            // Volver con transición suave
            cambiarPantalla(pantallaAgregar, pantallaCatalogo, 'grid');

            // Restaurar FABs Admin
            const controlesAdmin = document.getElementById('controles-admin');
            if (controlesAdmin) {
                controlesAdmin.classList.remove('oculto');
                controlesAdmin.style.display = 'block';
            }
        });
    }

    // LISTENER IMAGEN PREVIEW (Agregar)
    if (inputImagenFileAdd) {
        inputImagenFileAdd.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                nuevaImagenFileAdd = file;
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewImagenAdd.src = e.target.result;
                    previewImagenAdd.style.display = 'block';
                    placeholderImgAdd.style.display = 'none';
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // LISTENER AGREGAR TALLA (Agregar)
    if (btnAddTallaAdd) {
        btnAddTallaAdd.addEventListener('click', () => {
            const val = inputNuevaTallaAdd.value.trim().toUpperCase();
            if (val && !tallasAgregar.includes(val)) {
                tallasAgregar.push(val);
                renderizarChipsTallasAdd();
                inputNuevaTallaAdd.value = '';
            } else if (tallasAgregar.includes(val)) {
                alert("Esa talla ya está agregada.");
            }
        });
    }

    // RENDERIZAR CHIPS (Agregar)
    function renderizarChipsTallasAdd() {
        listaTallasChipsAdd.innerHTML = '';
        tallasAgregar.forEach((talla, index) => {
            const chip = document.createElement('div');
            chip.className = 'chip-edit';
            chip.innerHTML = `
                ${talla}
                <span class="chip-delete" onclick="eliminarTallaAdd(${index})">×</span>
            `;
            listaTallasChipsAdd.appendChild(chip);
        });
    }

    window.eliminarTallaAdd = function (index) {
        tallasAgregar.splice(index, 1);
        renderizarChipsTallasAdd();
    };

    // MOSTRAR PANTALLA AGREGAR (Reset)
    function mostrarPantallaAgregar() {
        tallasAgregar = [];
        nuevaImagenFileAdd = null;

        // Limpiar inputs
        formAgregar.reset();

        // Reset preview imagen
        previewImagenAdd.src = "";
        previewImagenAdd.style.display = 'none';
        placeholderImgAdd.style.display = 'block';

        renderizarChipsTallasAdd();

        // Mostrar con transición
        cambiarPantalla(pantallaCatalogo, pantallaAgregar, 'flex');

        // History Support
        history.pushState({ screen: 'agregar' }, "Agregar Producto", "#agregar");

        // Ocultar FABs Admin
        const controlesAdmin = document.getElementById('controles-admin');
        if (controlesAdmin) {
            controlesAdmin.classList.add('oculto');
            controlesAdmin.style.display = 'none';
        }
    }





    // GUARDAR NUEVO PRODUCTO
    if (formAgregar) {
        formAgregar.addEventListener('submit', (e) => {
            e.preventDefault();

            const nombre = document.getElementById('add-nombre').value.trim();
            const referencia = document.getElementById('add-referencia').value.trim();
            const categoria = document.getElementById('add-categoria').value;
            const stock = parseInt(document.getElementById('add-docenas').value);
            const precio = parseFloat(document.getElementById('add-precio').value);

            if (!nombre || !referencia || !categoria || isNaN(stock) || isNaN(precio)) {
                alert("Por favor completa todos los campos.");
                return;
            }

            if (tallasAgregar.length === 0) {
                alert("Debes agregar al menos una talla.");
                return;
            }

            const btnGuardar = formAgregar.querySelector('.boton-primario');
            const txtOriginal = btnGuardar.textContent;
            btnGuardar.textContent = "Guardando...";
            btnGuardar.disabled = true;

            const nuevoProducto = {
                nombre: nombre,
                referencia: referencia,
                categoria: categoria,
                stock: stock,
                precio: precio,
                tallas: tallasAgregar,
                cantidadReservada: 0,
                timestamp: firebase.firestore.FieldValue.serverTimestamp() // Orden cronológico
            };

            const coleccionProductos = db.collection("productos");

            const promesaGuardado = new Promise((resolve, reject) => {
                if (nuevaImagenFileAdd) {
                    // Subir imagen
                    const storageRef = firebase.storage().ref();
                    const nombreArchivo = `productos/${Date.now()}_${nuevaImagenFileAdd.name}`;
                    const imagenRef = storageRef.child(nombreArchivo);

                    imagenRef.put(nuevaImagenFileAdd)
                        .then(snapshot => snapshot.ref.getDownloadURL())
                        .then(url => {
                            nuevoProducto.imagenUrl = url;
                            return coleccionProductos.add(nuevoProducto);
                        })
                        .then(() => resolve())
                        .catch(err => reject(err));
                } else {
                    // Sin imagen
                    nuevoProducto.imagenUrl = null;
                    coleccionProductos.add(nuevoProducto)
                        .then(() => resolve())
                        .catch(err => reject(err));
                }
            });

            promesaGuardado
                .then(() => {
                    alert("¡Producto agregado exitosamente!");
                    mostrarPantallaAgregar(); // Resetear form para limpieza interna (aunque salgamos)

                    // Volver al catálogo con transición
                    cambiarPantalla(pantallaAgregar, pantallaCatalogo, 'grid');

                    // Restaurar FABs Admin
                    const controlesAdmin = document.getElementById('controles-admin');
                    if (controlesAdmin) {
                        controlesAdmin.classList.remove('oculto');
                        controlesAdmin.style.display = 'block';
                    }
                })
                .catch((error) => {
                    console.error("Error al guardar:", error);
                    alert("Error al agregar producto: " + error.message);
                })
                .finally(() => {
                    btnGuardar.textContent = txtOriginal;
                    btnGuardar.disabled = false;
                });
        });
    }

    // VALIDACIÓN INPUTS ADD
    const inputAddDocenas = document.getElementById('add-docenas');
    const inputAddPrecio = document.getElementById('add-precio');
    const inputAddNombre = document.getElementById('add-nombre');

    // Validación Nombre: Solo letras y espacios
    if (inputAddNombre) {
        inputAddNombre.addEventListener('input', (e) => {
            // Reemplaza todo lo que NO sea letra (a-z con acentos) o espacio
            e.target.value = e.target.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '');
        });
    }

    // Validación Precio y Stock: Solo positivos, sin signos
    [inputAddDocenas, inputAddPrecio].forEach(input => {
        if (input) {
            input.addEventListener('keydown', (e) => {
                if (['-', '+', 'e', 'E'].includes(e.key)) e.preventDefault();
            });
            input.addEventListener('input', (e) => {
                let val = e.target.value;
                // Eliminar cualquier caracter no numérico (excepto punto para precio)
                if (e.target.id === 'add-precio') {
                    // Solo permite números y un punto decimal
                    val = val.replace(/[^0-9.]/g, '');
                    // Evitar múltiples puntos
                    const parts = val.split('.');
                    if (parts.length > 2) val = parts[0] + '.' + parts.slice(1).join('');
                    e.target.value = val;
                } else {
                    // Solo números enteros
                    e.target.value = val.replace(/[^0-9]/g, '');
                }
            });
        }
    });

    // --- LÓGICA DE LOGOUT ---
    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            // Limpiar historial local al salir? (Opcional, mejor no para persistencia)
            // localStorage.removeItem('kiwi_mis_pedidos'); 

            mostrarDialogo("¿Deseas cerrar sesión?", 'confirmacion', () => {
                // 0. Desactivar flag de seguridad Admin
                esAdmin = false;
                toggleModoAdmin(false);

                // 0.1 Ocultar controles explícitamente (redundancia seguridad)
                const controlesAdmin = document.getElementById('controles-admin');
                if (controlesAdmin) {
                    controlesAdmin.classList.add('oculto');
                    controlesAdmin.style.display = 'none';
                }

                // 1. Resetear datos
                if (typeof inputBusqueda !== 'undefined') inputBusqueda.value = '';
                if (typeof filtrosContainer !== 'undefined') {
                    filtrosContainer.querySelectorAll('.chip').forEach(c => c.classList.remove('activo'));
                }

                // 2. Transición Login
                const pLogin = document.getElementById('pantalla-login');
                const pCatalogo = document.getElementById('pantalla-catalogo');
                const pCarrito = document.getElementById('pantalla-carrito');
                const pFactura = document.getElementById('pantalla-factura');
                const pSolicitudes = document.getElementById('pantalla-solicitudes');
                const navBar = document.getElementById('barra-navegacion'); // Referencia explícita

                // Ocultar todo
                [pCatalogo, pCarrito, pFactura, pSolicitudes].forEach(p => {
                    if (p) { p.classList.add('oculto'); p.style.display = 'none'; }
                });

                // Ocultar Barra de Navegación explícitamente
                if (navBar) {
                    navBar.classList.add('oculto');
                    navBar.style.display = 'none';
                }

                if (pLogin) {
                    pLogin.classList.remove('oculto');
                    pLogin.style.display = 'flex';
                    pLogin.style.opacity = '1';
                }
            });
        });
    }


}); // Cierre DOMContentLoaded original (si existe)

// --- PARCHE DELEGACIÓN GLOBAL: BOTÓN ATRÁS SOLICITUDES ---
// Usamos delegación al document por si el botón se creó dinámicamente o la referencia falló
document.addEventListener('click', (e) => {
    // Buscar si el clic fue en el botón o en su icono SVG interno
    const btn = e.target.closest('#btn-atras-solicitudes');

    if (btn) {
        e.preventDefault();
        console.log("Clic detectado en Botón Atrás Solicitudes (Delegado - Suave)");

        // 1. Resetear Navegación Visual (Barra Inferior)
        const navItems = document.querySelectorAll('.nav-item');
        if (navItems.length > 0) {
            // Remover activo de todos
            navItems.forEach(n => n.classList.remove('activo'));

            // Asignar activo al primero (Inicio)
            const itemInicio = navItems[0];
            itemInicio.classList.add('activo');

            // Forzar movimiento del indicador (Cálculo directo)
            const indicador = document.querySelector('.indicador-activo');
            if (indicador) {
                // Asegurar que el navegador ha renderizado layout
                requestAnimationFrame(() => {
                    const itemCenter = itemInicio.offsetLeft + (itemInicio.offsetWidth / 2);
                    const indicadorOffset = itemCenter - 25; // 25 = mitad de 50px width
                    indicador.style.transform = `translateX(${indicadorOffset}px)`;
                });
            }
        }

        // 2. Transición Suave (Reutilizando cambiarPantalla)
        const pCatalogo = document.getElementById('pantalla-catalogo');
        const pSolicitudes = document.getElementById('pantalla-solicitudes');

        if (pSolicitudes && pCatalogo && typeof cambiarPantalla === 'function') {
            cambiarPantalla(pSolicitudes, pCatalogo, 'grid');
        } else {
            // Fallback manual si cambiarPantalla no existe o falla
            if (pSolicitudes) {
                pSolicitudes.style.opacity = '0';
                setTimeout(() => {
                    pSolicitudes.classList.add('oculto');
                    pSolicitudes.style.display = 'none';

                    if (pCatalogo) {
                        pCatalogo.classList.remove('oculto');
                        pCatalogo.style.display = 'grid';
                        setTimeout(() => { pCatalogo.style.opacity = '1'; }, 50);
                    }
                }, 300);
            }
        }
    }
});

// --- UTILIDAD: DIÁLOGO PERSONALIZADO (Reemplazo alert/confirm) ---
window.mostrarDialogo = function (mensaje, tipo = 'alerta', onConfirm = null, tituloCustom = null) {
    const overlay = document.getElementById('custom-dialog-overlay');
    const titulo = document.getElementById('dialog-titulo');
    const texto = document.getElementById('dialog-mensaje');
    const btnAceptar = document.getElementById('btn-dialog-aceptar');
    const btnCancelar = document.getElementById('btn-dialog-cancelar');

    if (!overlay) {
        // Fallback si no existe el HTML
        if (tipo === 'confirmacion') {
            if (confirm(mensaje) && onConfirm) onConfirm();
        } else {
            alert(mensaje);
            if (onConfirm) onConfirm();
        }
        return;
    }

    // Resetear listeners previos (clonar clean)
    const newBtnAceptar = btnAceptar.cloneNode(true);
    const newBtnCancelar = btnCancelar.cloneNode(true);
    btnAceptar.parentNode.replaceChild(newBtnAceptar, btnAceptar);
    btnCancelar.parentNode.replaceChild(newBtnCancelar, btnCancelar);

    // Configurar Texto
    if (tituloCustom) {
        titulo.textContent = tituloCustom;
    } else {
        titulo.textContent = (tipo === 'confirmacion') ? '¿Estás seguro?' : 'Mensaje';
    }
    texto.textContent = mensaje;

    const cerrarDialogo = () => {
        overlay.classList.add('oculto');
        setTimeout(() => overlay.style.display = 'none', 300);
    };

    // Configurar Botones
    if (tipo === 'alerta') {
        newBtnCancelar.style.display = 'none';
        newBtnAceptar.textContent = "Aceptar";
        newBtnAceptar.onclick = () => {
            cerrarDialogo();
            if (onConfirm) onConfirm();
        };
    } else {
        newBtnCancelar.style.display = 'inline-block';
        newBtnAceptar.textContent = "Confirmar";
        newBtnAceptar.onclick = () => {
            cerrarDialogo();
            if (onConfirm) onConfirm();
        };
        newBtnCancelar.onclick = () => {
            cerrarDialogo();
        };
    }

    // Mostrar
    overlay.style.display = 'flex';
    overlay.classList.remove('oculto');
    // Forzar reflow para animación
    void overlay.offsetWidth;
    overlay.style.opacity = '1';
};

// --- TRADUCCIÓN DE VALIDACIONES NATIVAS ---
function configurarMensajesEspanol() {
    const inputsRequeridos = document.querySelectorAll('input[required], select[required], textarea[required]');

    inputsRequeridos.forEach(input => {
        // Evento 'invalid': Cuando el navegador detecta error al enviar
        input.addEventListener('invalid', function () {
            if (this.validity.valueMissing) {
                this.setCustomValidity('Por favor, completa este campo.');
            } else if (this.validity.typeMismatch) {
                if (this.type === 'email') {
                    this.setCustomValidity('Introduce una dirección de correo válida.');
                } else if (this.type === 'url') {
                    this.setCustomValidity('Introduce una URL válida.');
                } else {
                    this.setCustomValidity('Formato inválido.');
                }
            } else if (this.validity.patternMismatch) {
                this.setCustomValidity('El formato no coincide con lo solicitado.');
            } else {
                this.setCustomValidity(''); // Reset si es otro error
            }
        });

        // Evento 'input': Limpiar error al escribir
        input.addEventListener('input', function () {
            this.setCustomValidity('');
        });
    });
}

// Ejecutar al inicio por si hay formularios visibles
// Y al cargar el DOM completo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', configurarMensajesEspanol);
} else {
    configurarMensajesEspanol();
}

// --- LISTENERS RETORNO COMPRADOR (Tabs) ---
const btnAtrasCarrito = document.getElementById('btn-atras-carrito');
if (btnAtrasCarrito) {
    btnAtrasCarrito.addEventListener('click', () => {
        const tabInicio = document.querySelector('.nav-item[data-tab="inicio"]');
        if (tabInicio) tabInicio.click();
    });
}

const btnAtrasSolicitudes = document.getElementById('btn-atras-solicitudes');
if (btnAtrasSolicitudes) {
    btnAtrasSolicitudes.addEventListener('click', () => {
        const tabInicio = document.querySelector('.nav-item[data-tab="inicio"]');
        if (tabInicio) tabInicio.click();
    });
}

// --- SOPORTE BACK BUTTON: NAVEGACIÓN TABS & FABs ---
document.addEventListener('click', (e) => {
    // 1. Bottom Navigation Tabs (Comprador/General)
    const navItem = e.target.closest('.nav-item');
    if (navItem) {
        // Corrección iOS: No verificar .activo aquí porque el listener original ya lo habrá activado antes.
        // Siempre permitir el pushState si se hace click.

        const tab = navItem.dataset.tab;
        if (tab === 'carrito') {
            console.log("PushState: Carrito");
            history.pushState({ screen: 'carrito' }, "Carrito", "#carrito");
        } else if (tab === 'solicitudes') {
            console.log("PushState: Solicitudes");
            history.pushState({ screen: 'solicitudes' }, "Solicitudes", "#solicitudes");
        }
    }

    // 2. FAB Solicitudes (Vendedor - #btn-admin-solicitudes)
    const btnAdminSolicitudes = e.target.closest('#btn-admin-solicitudes');
    if (btnAdminSolicitudes) {
        console.log("PushState: Solicitudes Admin");
        // Ocultar Badge inmediatamente al entrar
        const badge = document.getElementById('badge-admin-solicitudes');
        if (badge) badge.classList.add('oculto');

        history.pushState({ screen: 'solicitudes' }, "Solicitudes Admin", "#admin-solicitudes");
    }
});

// --- SOPORTE BACK BUTTON (ANDROID/IOS) ---
window.addEventListener('popstate', (event) => {
    console.log("Evento PopState detectado:", event.state);

    // Definir pantallas overlays y sus botones de cierre
    const overlays = [
        { id: 'pantalla-detalle', btnClose: 'btn-atras' },
        { id: 'pantalla-editar-producto', btnClose: 'btn-atras-editar' },
        { id: 'pantalla-agregar-producto', btnClose: 'btn-atras-agregar' },
        { id: 'pantalla-factura', btnClose: 'btn-atras-factura' },
        { id: 'pantalla-solicitudes', btnClose: 'btn-atras-solicitudes' }, // Comprador
        { id: 'pantalla-solicitudes-vendedora', btnClose: 'btn-atras-solicitudes-vendedora' }, // Vendedor (Admin)
        { id: 'lightbox-imagen', btnClose: 'btn-cerrar-lightbox' },
        { id: 'pantalla-carrito', btnClose: 'btn-atras-carrito' }
    ];

    // Buscar la overlay visible con mayor z-index (o la primera visible)
    for (const overlay of overlays) {
        const el = document.getElementById(overlay.id);
        if (el && !el.classList.contains('oculto') && el.style.display !== 'none') {
            const btn = document.getElementById(overlay.btnClose);
            if (btn) {
                console.log("Cerrando pantalla por Back Button:", overlay.id);
                btn.click(); // Simular click para ejecutar toda la lógica de limpieza asociada
                return; // Solo cerrar una a la vez
            } else {
                // Fallback si no hay botón (raro)
                el.classList.add('oculto');
                setTimeout(() => el.style.display = 'none', 300);
            }
        }
    }

    // 3. Si no hay overlays -> Estamos en nivel base (Catálogo/Login)
    const pCatalogo = document.getElementById('pantalla-catalogo');
    if (pCatalogo && !pCatalogo.classList.contains('oculto') && pCatalogo.style.display !== 'none') {
        const btnLogout = document.getElementById('btn-logout');
        if (btnLogout) {
            // Detección: Solo Android debe mostrar el diálogo al ir atrás. iPhone (iOS) sigue estándar.
            const isAndroid = /android/i.test(navigator.userAgent);

            if (isAndroid) {
                console.log("Back en Catálogo (Android) -> Logout Dialog");

                // Empujar estado de nuevo para que si cancela, el Back siga funcionando
                history.pushState({ screen: 'catalogo' }, "Catálogo", "#catalogo");

                // Trigger Logout Click (abre confirmación)
                btnLogout.click();
            }
        }
    }

    // Si estamos en login, dejamos pasar el evento default (cerrar app o ir atrás historial)
});
