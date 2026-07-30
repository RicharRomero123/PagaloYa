/**
 * Export estático a propósito: el panel se aloja en Firebase Hosting con el
 * plan Spark (gratis). Nada de SSR ni API routes — eso despliega a Cloud Run y
 * obliga a pasar al plan Blaze. El navegador habla directo con Firestore y las
 * reglas de seguridad son las que protegen (ver backend/firestore.rules).
 *
 * @type {import('next').NextConfig}
 */
const nextConfig = {
  output: "export",
  images: { unoptimized: true },
  trailingSlash: true,
};

export default nextConfig;
