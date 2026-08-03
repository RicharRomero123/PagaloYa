import { doc, getDoc, serverTimestamp, setDoc, type DocumentData } from "firebase/firestore";
import { db } from "./firebase";

/**
 * Enlaces del producto que la app Android lee en caliente desde
 * `config/enlaces`. Cambiarlos aquí NO exige rebuildear el APK.
 *
 * Reglas (backend/firestore.rules): el doc config/enlaces lo escribe solo
 * esOperador() y lo lee cualquier autenticado. Por eso la app puede leerlo
 * pero solo el operador lo edita desde este panel.
 */
export type Enlaces = {
  /** WhatsApp de ventas en formato internacional SIN "+", ej "51987654321". */
  whatsappVentas: string;
  /** URL o handle (@usuario). */
  instagram: string;
  /** URL o handle (@usuario). */
  tiktok: string;
  /** URL o nombre de página. */
  facebook: string;
};

/**
 * Campos editables del formulario, en orden de aparición. Sumar un enlace
 * nuevo (ej. web, correoSoporte) es agregar una entrada aquí y su clave en
 * el tipo Enlaces — el formulario y el guardado se adaptan solos.
 */
export type CampoEnlace = {
  clave: keyof Enlaces;
  etiqueta: string;
  placeholder: string;
  ayuda: string;
  /** Valida el valor ya recortado; devuelve null si está OK o un mensaje. */
  validar?: (valor: string) => string | null;
};

export const CAMPOS_ENLACES: CampoEnlace[] = [
  {
    clave: "whatsappVentas",
    etiqueta: "WhatsApp de ventas",
    placeholder: "51987654321",
    ayuda: "Solo dígitos, formato internacional sin +. Ej: 51987654321",
    validar: (valor) => {
      if (!valor) return null; // vacío es válido: aún no configurado
      if (!/^\d+$/.test(valor))
        return "Solo dígitos, sin + ni espacios. Ej: 51987654321";
      if (valor.length < 8)
        return "Muy corto para un número con código de país.";
      return null;
    },
  },
  {
    clave: "instagram",
    etiqueta: "Instagram",
    placeholder: "@pagoya.pe o https://instagram.com/pagoya.pe",
    ayuda: "Puede ser un @handle o la URL completa.",
  },
  {
    clave: "tiktok",
    etiqueta: "TikTok",
    placeholder: "@pagoya.pe o https://tiktok.com/@pagoya.pe",
    ayuda: "Puede ser un @handle o la URL completa.",
  },
  {
    clave: "facebook",
    etiqueta: "Facebook",
    placeholder: "PagoYa o https://facebook.com/pagoya.pe",
    ayuda: "Puede ser el nombre de la página o la URL completa.",
  },
];

export const ENLACES_VACIOS: Enlaces = {
  whatsappVentas: "",
  instagram: "",
  tiktok: "",
  facebook: "",
};

/** Lee config/enlaces. Si no existe, devuelve todos los campos vacíos. */
export async function leerEnlaces(): Promise<Enlaces> {
  const snap = await getDoc(doc(db, "config", "enlaces"));
  if (!snap.exists()) return { ...ENLACES_VACIOS };
  return aEnlaces(snap.data());
}

function aEnlaces(datos: DocumentData): Enlaces {
  return {
    whatsappVentas: (datos.whatsappVentas as string) ?? "",
    instagram: (datos.instagram as string) ?? "",
    tiktok: (datos.tiktok as string) ?? "",
    facebook: (datos.facebook as string) ?? "",
  };
}

/**
 * Guarda config/enlaces con merge, así los campos que no toques (o que sume
 * otro agente más adelante) no se pierden. Recorta espacios en todos.
 */
export async function guardarEnlaces(enlaces: Enlaces): Promise<void> {
  const limpio: Record<string, unknown> = {};
  for (const clave of Object.keys(enlaces) as (keyof Enlaces)[]) {
    limpio[clave] = enlaces[clave].trim();
  }
  await setDoc(
    doc(db, "config", "enlaces"),
    { ...limpio, actualizadoEn: serverTimestamp() },
    { merge: true },
  );
}
