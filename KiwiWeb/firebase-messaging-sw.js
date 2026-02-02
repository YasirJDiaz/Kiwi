// Firebase Cloud Messaging Service Worker
// Este archivo es REQUERIDO por Firebase Messaging

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

// Manejar mensajes en segundo plano
messaging.onBackgroundMessage((payload) => {
    console.log('[FCM SW] Mensaje recibido:', payload);

    const notificationTitle = payload.notification?.title || 'Nueva Solicitud';
    const notificationOptions = {
        body: payload.notification?.body || 'Tienes un nuevo pedido pendiente',
        icon: '/assets/img/icon-192x192.png',
        badge: '/assets/img/icon-192x192.png',
        tag: 'kiwi-notification',
        requireInteraction: true,
        data: payload.data
    };

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
