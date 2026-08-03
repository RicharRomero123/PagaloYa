import { getApps, initializeApp, type FirebaseApp } from "firebase/app";
import { getAuth, type Auth } from "firebase/auth";
import { getFirestore, type Firestore } from "firebase/firestore";
import { getFunctions, type Functions } from "firebase/functions";

/**
 * Estos valores viajan dentro del bundle del navegador: no son un secreto.
 * Lo que protege los datos son las reglas de Firestore (backend/firestore.rules).
 */
const configuracion = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
};

export const hayConfiguracion = Boolean(configuracion.projectId);

const app: FirebaseApp = getApps()[0] ?? initializeApp(configuracion);

export const auth: Auth = getAuth(app);
export const db: Firestore = getFirestore(app);
/**
 * Las Cloud Functions callable de PagoYa (p. ej. enviarCampana) se despliegan
 * en us-central1. La región debe calzar con la del backend o la llamada falla
 * con "not-found".
 */
export const functions: Functions = getFunctions(app, "us-central1");
