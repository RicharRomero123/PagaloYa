/** @type {import('next').NextConfig} */
const nextConfig = {
  // Exportación 100% estática: el resultado queda en out/ y se sirve
  // desde cualquier hosting estático (Firebase Hosting incluido).
  output: 'export',
  trailingSlash: true,
  images: { unoptimized: true },
};

export default nextConfig;
