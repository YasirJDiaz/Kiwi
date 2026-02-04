// Kiwii PWA Service Worker
// Estrategia: Network-First con fallback a Cache
// Versión dinámica para forzar actualizaciones automáticas

const CACHE_NAME_PREFIX = 'kiwi-cache-v';
const CACHE_VERSION = 'date-' + Date.now(); // Dynamic version
// v5 - Fix Auto Reload
const CACHE_NAME = `${CACHE_NAME_PREFIX}${CACHE_VERSION}`;

// Archivos críticos a cachear
const CRITICAL_ASSETS = [
    '/',
    '/index.html',
    '/css/styles.css',
    '/css/styles-admin.css',
    '/js/app_v2.js',
    '/js/pdfGenerator.js',
    '/assets/img/logo_kiwi.png',
    '/assets/img/icon-192x192.png',
    '/assets/img/icon-512x512.png',
    '/manifest.json'
];

// === INSTALL: Cachear archivos críticos ===
self.addEventListener('install', (event) => {
    console.log('[SW] 📦 Instalando Service Worker versión:', CACHE_VERSION);

    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('[SW] ✅ Cache abierto:', CACHE_NAME);
                return cache.addAll(CRITICAL_ASSETS);
            })
            .then(() => {
                console.log('[SW] ✅ Archivos críticos cacheados');
                // IMPORTANTE: No llamamos a skipWaiting() aquí para que el usuario decida cuándo actualizar
                // El nuevo SW se quedará en estado "waiting" hasta que se le ordene.
            })
            .catch(err => {
                console.error('[SW] ❌ Error al cachear archivos:', err);
            })
    );
});

// === ACTIVATE: Limpiar cachés antiguos ===
self.addEventListener('activate', (event) => {
    console.log('[SW] 🔄 Activando Service Worker:', CACHE_VERSION);

    event.waitUntil(
        caches.keys()
            .then(cacheNames => {
                return Promise.all(
                    cacheNames.map(cacheName => {
                        if (cacheName !== CACHE_NAME) {
                            console.log('[SW] 🗑️ Eliminando caché antiguo:', cacheName);
                            return caches.delete(cacheName);
                        }
                    })
                );
            })
            .then(() => {
                console.log('[SW] ✅ Cachés antiguos limpiados');
                // Tomar control de todas las páginas inmediatamente
                return self.clients.claim();
            })
    );
});

// === FETCH: Estrategia Network-First ===
self.addEventListener('fetch', (event) => {
    // Solo interceptar requests GET
    if (event.request.method !== 'GET') {
        return;
    }

    // Ignorar requests a Firebase, Google APIs, etc.
    const url = new URL(event.request.url);
    if (
        url.origin.includes('firebasestorage') ||
        url.origin.includes('googleapis') ||
        url.origin.includes('gstatic') ||
        url.origin.includes('cloudflare')
    ) {
        return;
    }

    event.respondWith(
        // Estrategia: Network First
        fetch(event.request)
            .then(response => {
                // Si la respuesta es válida, cachearla
                if (response && response.status === 200) {
                    const responseToCache = response.clone();

                    caches.open(CACHE_NAME)
                        .then(cache => {
                            cache.put(event.request, responseToCache);
                        });
                }

                return response;
            })
            .catch(() => {
                // Si falla la red, intentar con caché
                return caches.match(event.request)
                    .then(cachedResponse => {
                        if (cachedResponse) {
                            console.log('[SW] 📦 Sirviendo desde caché:', event.request.url);
                            return cachedResponse;
                        }

                        // Si no está en caché y es navegación, devolver index.html cacheado
                        if (event.request.mode === 'navigate') {
                            return caches.match('/');
                        }

                        // Si no hay nada, error
                        return new Response('Sin conexión y recurso no cacheado', {
                            status: 503,
                            statusText: 'Service Unavailable'
                        });
                    });
            })
    );
});

// === MENSAJE: Comunicación con la app ===
self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});

console.log('[SW] 🚀 Service Worker cargado:', CACHE_VERSION);
