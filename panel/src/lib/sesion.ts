"use client";

import { useEffect, useState } from "react";
import {
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  type User,
} from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";
import { auth, db } from "./firebase";

/**
 * Estados posibles de quien abre el panel.
 *
 * `sin-acceso` es un caso real y frecuente: alguien con cuenta de PagoYa
 * (un comerciante, por ejemplo) que entra a la URL del panel. Tiene sesión
 * válida, pero no está en `operadores/`. No es un error: es la puerta cerrada
 * funcionando.
 */
export type EstadoSesion = "cargando" | "fuera" | "sin-acceso" | "operador";

export type Sesion = {
  estado: EstadoSesion;
  usuario: User | null;
  nombreOperador: string;
};

export function useSesion(): Sesion {
  const [estado, setEstado] = useState<EstadoSesion>("cargando");
  const [usuario, setUsuario] = useState<User | null>(null);
  const [nombreOperador, setNombreOperador] = useState("");

  useEffect(() => {
    return onAuthStateChanged(auth, async (u) => {
      setUsuario(u);
      if (!u) {
        setNombreOperador("");
        setEstado("fuera");
        return;
      }
      try {
        // Las reglas permiten leer el documento propio de operadores. Si no
        // existe, la lectura devuelve vacío (no error) y no hay acceso.
        const snap = await getDoc(doc(db, "operadores", u.uid));
        if (snap.exists()) {
          setNombreOperador((snap.data().nombre as string) ?? "");
          setEstado("operador");
        } else {
          setEstado("sin-acceso");
        }
      } catch {
        setEstado("sin-acceso");
      }
    });
  }, []);

  return { estado, usuario, nombreOperador };
}

export async function entrarConGoogle(): Promise<void> {
  await signInWithPopup(auth, new GoogleAuthProvider());
}

export async function entrarConCorreo(correo: string, clave: string): Promise<void> {
  await signInWithEmailAndPassword(auth, correo.trim(), clave);
}

export async function salir(): Promise<void> {
  await signOut(auth);
}
