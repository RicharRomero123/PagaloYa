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
        <main className="flex min-h-[100dvh] flex-col items-center justify-center gap-4">
          <span className="h-9 w-9 animate-spin rounded-full border-[3px] border-borde border-t-naranja" />
          <p className="text-sm font-semibold text-texto-medio">Cargando…</p>
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
    <main className="flex min-h-[100dvh] items-center justify-center px-4">
      <div className="tarjeta max-w-md p-6 shadow-media">
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
