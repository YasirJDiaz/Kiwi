const CACHE_NAME = 'kiwi-cache-v1';
const ASSETS = [
    './',
    './index.html',
    './css/styles.css',
    './css/styles-admin.css',
    './js/app.js',
    './assets/img/logo_kiwi.png',
    './assets/img/icon-192x192.png',
    './assets/img/icon-512x512.png',
    './manifest.json'
];

self.addEventListener('install', (event) => {
    // Pre-cache de archivos estáticos
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            console.log("Caching app shell");
            return cache.addAll(ASSETS);
        })
    );
});

self.addEventListener('fetch', (event) => {
    // Estrategia: Network First (priorizar datos frescos, fallback a cache si falla)
    // Esto es crucial para una tienda donde el stock cambia.
    event.respondWith(
        fetch(event.request)
            .catch(() => {
                return caches.match(event.request);
            })
    );
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keyList) => {
            return Promise.all(keyList.map((key) => {
                if (key !== CACHE_NAME) {
                    console.log("Borrando cache antigua", key);
                    return caches.delete(key);
                }
            }));
        })
    );
});
