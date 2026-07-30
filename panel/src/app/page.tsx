"use client";

import { ListaComercios } from "@/componentes/ListaComercios";
import { Login } from "@/componentes/Login";
import { SinAcceso } from "@/componentes/SinAcceso";
import { hayConfiguracion } from "@/lib/firebase";
import { useSesion } from "@/lib/sesion";

export default function Pagina() {
  const { estado, usuario, nombreOperador, soyDueno } = useSesion();

  if (!hayConfiguracion) return <FaltaConfiguracion />;

  switch (estado) {
    case "cargando":
      return (
        <main className="flex min-h-screen items-center justify-center">
          <p className="text-sm text-texto-medio">Cargando…</p>
        </main>
      );
    case "fuera":
      return <Login />;
    case "sin-acceso":
      return <SinAcceso usuario={usuario} />;
    case "operador":
      return (
        <ListaComercios
          nombreOperador={nombreOperador}
          operadorUid={usuario?.uid ?? ""}
          soyDueno={soyDueno}
        />
      );
  }
}

function FaltaConfiguracion() {
  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <div className="max-w-md rounded-2xl bg-white p-6">
        <h1 className="text-xl font-black text-azul">Falta configurar Firebase</h1>
        <p className="mt-2 text-sm text-texto-medio">
          Copia <code className="font-mono">.env.local.ejemplo</code> como{" "}
          <code className="font-mono">.env.local</code> y llena los valores de tu
          app web. Están en la consola de Firebase, en Configuración del proyecto
          → Tus apps.
        </p>
      </div>
    </main>
  );
}
