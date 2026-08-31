/**
 * Service worker del panel PagoYa. Hecho a mano (sin Workbox) para no meter
 * dependencias y ser compatible con el export estático de Next.
 *
 * Estrategia:
 *  · Navegaciones → network-first: si hay señal, muestra lo último y guarda el
 *    shell; sin señal, sirve el shell cacheado (el reseller en el mercado no se
 *    queda con pantalla en blanco).
 *  · Assets estáticos (_next, íconos, fuentes) → stale-while-revalidate.
 *  · Firebase / Google APIs (auth, Firestore) → NUNCA se tocan: son datos en
 *    vivo y con permisos; se dejan pasar directo a la red.
 */
const VERSION = "pagoya-panel-v1";
const SHELL = [
  "/",
  "/manifest.webmanifest",
  "/icon-192.png",
  "/icon-512.png",
  "/apple-touch-icon.png",
];

self.addEventListener("install", (evento) => {
  evento.waitUntil(
    caches
      .open(VERSION)
      .then((c) => c.addAll(SHELL))
      .then(() => self.skipWaiting()),
  );
});

self.addEventListener("activate", (evento) => {
  evento.waitUntil(
    caches
      .keys()
      .then((claves) =>
        Promise.all(claves.filter((k) => k !== VERSION).map((k) => caches.delete(k))),
      )
      .then(() => self.clients.claim()),
  );
});

self.addEventListener("fetch", (evento) => {
  const req = evento.request;
  if (req.method !== "GET") return;

  const url = new URL(req.url);
  // Solo mismo origen. Firebase/Firestore/Google van directo a la red.
  if (url.origin !== self.location.origin) return;

  if (req.mode === "navigate") {
    evento.respondWith(
      fetch(req)
        .then((res) => {
          const copia = res.clone();
          caches.open(VERSION).then((c) => c.put("/", copia));
          return res;
        })
        .catch(() =>
          caches
            .match("/", { ignoreSearch: true })
            .then((r) => r || caches.match(req)),
        ),
    );
    return;
  }

  const esEstatico =
    url.pathname.startsWith("/_next/") ||
    /\.(png|svg|ico|webmanifest|woff2?|css|js)$/.test(url.pathname);
  if (esEstatico) {
    evento.respondWith(
      caches.match(req).then((cacheado) => {
        const red = fetch(req)
          .then((res) => {
            const copia = res.clone();
            caches.open(VERSION).then((c) => c.put(req, copia));
            return res;
          })
          .catch(() => cacheado);
        return cacheado || red;
      }),
    );
  }
});
