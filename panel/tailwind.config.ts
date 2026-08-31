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
        "azul-hondo": "#12203C",
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
      fontFamily: {
        sans: [
          "var(--font-nunito)",
          "ui-rounded",
          "Segoe UI",
          "system-ui",
          "sans-serif",
        ],
      },
      borderRadius: {
        card: "1.25rem",
        "2xl": "1rem",
      },
      boxShadow: {
        // Relieve cálido: sombras teñidas de azul-marca, no negro plano.
        suave:
          "0 1px 2px rgba(26,43,74,0.04), 0 6px 20px -6px rgba(26,43,74,0.12)",
        media: "0 10px 34px -10px rgba(26,43,74,0.20)",
        alta: "0 26px 60px -18px rgba(18,32,60,0.32)",
        naranja: "0 10px 26px -8px rgba(255,107,26,0.50)",
        "borde-suave": "inset 0 0 0 1px rgba(230,218,205,0.7)",
      },
      backgroundImage: {
        "malla-crema":
          "radial-gradient(1100px 480px at 12% -8%, rgba(255,107,26,0.10), transparent 60%), radial-gradient(900px 520px at 108% 4%, rgba(44,69,112,0.12), transparent 55%)",
        "azul-relieve":
          "linear-gradient(160deg, #223a63 0%, #1A2B4A 46%, #12203C 100%)",
        "naranja-relieve": "linear-gradient(150deg, #FF8A3D 0%, #FF6B1A 100%)",
      },
      keyframes: {
        aparecer: {
          "0%": { opacity: "0" },
          "100%": { opacity: "1" },
        },
        subir: {
          "0%": { opacity: "0", transform: "translateY(14px) scale(0.985)" },
          "100%": { opacity: "1", transform: "translateY(0) scale(1)" },
        },
        "subir-hoja": {
          "0%": { opacity: "0", transform: "translateY(24px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        deslizar: {
          "0%": { opacity: "0", transform: "translateX(26px)" },
          "100%": { opacity: "1", transform: "translateX(0)" },
        },
        brillo: {
          "100%": { transform: "translateX(100%)" },
        },
      },
      animation: {
        aparecer: "aparecer 0.25s ease-out both",
        subir: "subir 0.28s cubic-bezier(0.22,1,0.36,1) both",
        "subir-hoja": "subir-hoja 0.32s cubic-bezier(0.22,1,0.36,1) both",
        deslizar: "deslizar 0.34s cubic-bezier(0.22,1,0.36,1) both",
      },
    },
  },
  plugins: [],
};

export default config;
