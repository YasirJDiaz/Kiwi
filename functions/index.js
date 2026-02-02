const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const { getMessaging } = require("firebase-admin/messaging");

admin.initializeApp();

// --- NOTIFICACIÓN A CLIENTES (NUEVO PRODUCTO) ---
exports.notificarNuevoProducto = onDocumentCreated("productos/{productoId}", async (event) => {

    const snapshot = event.data;
    if (!snapshot) return;

    // 1. LISTA DE MENSAJES DISPONIBLES
    const frases = [
        "Nueva mercancía disponible en la app. Haz tu pedido ahora para asegurar disponibilidad.",
        "Hemos añadido nuevos estilos a la colección. ¡Entra y surte tu negocio!",
        "Stock fresco recién agregado. ¡Sé el primero en hacer tu pedido!",
        "Nuevas oportunidades de venta para tu negocio. ¡Mira lo que acaba de llegar!",
        "¡Alerta de novedad! El catálogo se ha renovado. No te quedes sin stock.",
        "Nuevos productos disponibles. ¡Descúbrelos ahora!",
        "Nuevos estilos, nuevos colores. Mira lo que acabamos de subir.",
        "No te quedes sin mercancía. Entra ya y aparta las novedades.",
        "¡Tenemos novedades esperándote en la app!",
        "¿Ya viste lo nuevo? Entra a ver las novedades!",
        "Nueva mercancía disponible ahora mismo, en KIWI MODA",
        "Echa un vistazo a lo nuevo: Entra y explora la mercancía reciente."
    ];

    // 2. SELECCIÓN ALEATORIA
    const mensajeAleatorio = frases[Math.floor(Math.random() * frases.length)];

    // 3. CONFIGURAR NOTIFICACIÓN
    const payload = {
        notification: {
            title: "✨ ¡Nueva Colección Disponible! 🥝",
            body: mensajeAleatorio,
        }
    };

    try {
        await getMessaging().send({
            topic: "new_products",
            notification: payload.notification
        });
        console.log("Aviso de producto enviado: ", mensajeAleatorio);
    } catch (error) {
        console.error("Error enviando aviso producto:", error);
    }
});

// --- NOTIFICACIÓN DE NUEVA SOLICITUD (Android app + PWA web) ---
exports.notificarNuevaSolicitud = onDocumentCreated("solicitudes/{solicitudId}", async (event) => {

    const snapshot = event.data;

    if (!snapshot) {
        console.log("No hay datos asociados al evento.");
        return;
    }

    const solicitud = snapshot.data();
    const comprador = solicitud.comprador || "Cliente";
    const total = solicitud.total || 0;

    // Mensaje para vendedoras
    const notificacion = {
        title: "🔔 ¡Nuevo Pedido Recibido! 💰",
        body: `${comprador} ha realizado un pedido por $${Number(total).toFixed(2)}`,
    };

    // ========================================
    // 1. ENVIAR A TOPIC (Android App)
    // ========================================
    try {
        await getMessaging().send({
            topic: "pedidos_vendedora",
            notification: notificacion
        });
        console.log("✅ Notificación enviada a topic pedidos_vendedora (Android App)");
    } catch (error) {
        console.error("❌ Error enviando a topic:", error);
    }

    // ========================================
    // 2. ENVIAR A TOKENS WEB (PWA)
    // ========================================
    try {
        const vendorsSnapshot = await admin.firestore().collection('fcmTokens').get();

        if (vendorsSnapshot.empty) {
            console.log("⚠️ No hay vendedores registrados en fcmTokens");
            return;
        }

        // Recopilar tokens de TODOS los dispositivos de TODOS los vendedores
        const allTokens = [];

        for (const vendorDoc of vendorsSnapshot.docs) {
            const devicesSnapshot = await vendorDoc.ref.collection('devices').get();

            devicesSnapshot.docs.forEach(deviceDoc => {
                const token = deviceDoc.data().token;
                if (token && token.length > 0) {
                    allTokens.push(token);
                }
            });
        }

        if (allTokens.length === 0) {
            console.log("⚠️ No hay tokens web válidos");
            return;
        }

        // Enviar a todos los tokens web
        const message = {
            notification: notificacion,
            data: {
                solicitudId: event.params.solicitudId,
                tipo: 'nueva_solicitud',
                click_action: 'https://kiwi-2025.web.app'
            },
            tokens: allTokens
        };

        const response = await getMessaging().sendEachForMulticast(message);

        console.log(`✅ Notificaciones web enviadas: ${response.successCount}/${allTokens.length}`);

        // Log de tokens fallidos (para debug)
        if (response.failureCount > 0) {
            console.log(`⚠️ Tokens fallidos: ${response.failureCount}`);
            response.responses.forEach((resp, idx) => {
                if (!resp.success) {
                    console.log(`  - Token fallido [${idx}]: ${resp.error?.message || 'Unknown error'}`);
                }
            });
        }

    } catch (error) {
        console.error("❌ Error enviando notificaciones web:", error);
    }
});
