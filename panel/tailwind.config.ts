import type { Config } from "tailwindcss";

/** Paleta de BRAND.md. Nada de morado (Yape) ni turquesa (Plin). */
const config: Config = {
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        naranja: "#FF6B1A",
        "naranja-hondo": "#D95100",
        "naranja-suave": "#FFE9DA",
        azul: "#1A2B4A",
        "azul-claro": "#2C4570",
        crema: "#FFF6EF",
        humo: "#F3EBE3",
        borde: "#E6DACD",
        "texto-medio": "#5C6B85",
        "texto-tenue": "#93A0B5",
        "verde-ok": "#14803C",
        "verde-suave": "#E4F6EA",
        "rojo-alerta": "#C62828",
        "rojo-suave": "#FDEAEA",
        "ambar-aviso": "#9A6100",
        "ambar-suave": "#FFF2DC",
      },
    },
  },
  plugins: [],
};

export default config;
