// ============================================
// FIREBASE CLOUD MESSAGING SERVICE WORKER
// ============================================
// IMPORTANTE: Este Service Worker es EXCLUSIVO para Firebase Cloud Messaging (notificaciones push)
// El caché de la aplicación (CSS/JS/HTML) es manejado por service-worker.js
// Ambos SWs conviven sin conflictos ya que tienen responsabilidades separadas
// ============================================

importScripts('https://www.gstatic.com/firebasejs/9.6.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.6.1/firebase-messaging-compat.js');

// Configuración Firebase
const firebaseConfig = {
    apiKey: "AIzaSyBs7hUxpXrWQMWBsJQkHhbDzEkGXxhQoF0",
    authDomain: "kiwi-2025.firebaseapp.com",
    projectId: "kiwi-2025",
    storageBucket: "kiwi-2025.firebasestorage.app",
    messagingSenderId: "785572669533",
    appId: "1:785572669533:web:43ae91cc179932d072c5b4"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// Cache para evitar notificaciones duplicadas
const recentNotifications = new Map();

// Manejar mensajes en segundo plano
messaging.onBackgroundMessage((payload) => {
    console.log('[FCM SW] Mensaje recibido:', payload);

    const notificationTitle = payload.notification?.title || 'Nueva Solicitud';
    const notificationBody = payload.notification?.body || 'Tienes un nuevo pedido pendiente';

    // Crear ID único para esta notificación
    const notificationId = `${notificationTitle}-${notificationBody}`;

    // Verificar si ya mostramos esta notificación recientemente (últimos 5 segundos)
    const now = Date.now();
    const lastShown = recentNotifications.get(notificationId);

    if (lastShown && (now - lastShown) < 5000) {
        console.log('[FCM SW] ⚠️ Notificación duplicada ignorada:', notificationId);
        return; // Ignorar duplicado
    }

    // Guardar timestamp de esta notificación
    recentNotifications.set(notificationId, now);

    // Limpiar cache antiguo (mantener solo últimos 10)
    if (recentNotifications.size > 10) {
        const firstKey = recentNotifications.keys().next().value;
        recentNotifications.delete(firstKey);
    }

    const notificationOptions = {
        body: notificationBody,
        icon: '/assets/img/icon-192x192.png',
        badge: '/assets/img/icon-192x192.png',
        tag: 'kiwi-notification',
        requireInteraction: true,
        data: payload.data
    };

    console.log('[FCM SW] ✅ Mostrando notificación:', notificationId);
    return self.registration.showNotification(notificationTitle, notificationOptions);
});

// Click en notificación
self.addEventListener('notificationclick', (event) => {
    console.log('[FCM SW] Notificación clickeada');
    event.notification.close();

    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
            for (const client of clientList) {
                if (client.url.includes(self.location.origin) && 'focus' in client) {
                    return client.focus();
                }
            }
            if (clients.openWindow) {
                return clients.openWindow('/');
            }
        })
    );
});
