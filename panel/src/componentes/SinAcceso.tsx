"use client";

import { useEffect, useState } from "react";
import { salir } from "@/lib/sesion";
import {
  ReglasNoListas,
  miSolicitud,
  postular,
  type Solicitud,
} from "@/lib/solicitudes";
import type { User } from "firebase/auth";

/**
 * No es un error: es la puerta cerrada funcionando. Pasa cuando un comerciante
 * (o cualquiera con cuenta de PagoYa) abre la URL del panel. Se le dice claro
 * y se le ofrece la salida, sin filtrar nada del negocio.
 *
 * Si de verdad es del equipo, aquí puede postular: deja su solicitud y espera
 * que un dueño lo apruebe. El bloque del UID queda como respaldo para el alta
 * manual de siempre.
 */
export function SinAcceso({ usuario }: { usuario: User | null }) {
  const [copiado, setCopiado] = useState(false);
  const [solicitud, setSolicitud] = useState<Solicitud | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    if (!usuario) {
      setCargando(false);
      return;
    }
    let vivo = true;
    void miSolicitud(usuario.uid)
      .then((s) => {
        if (vivo) setSolicitud(s);
      })
      .catch(() => {
        // Reglas aún no listas o sin permiso de lectura: mostramos el form igual.
        if (vivo) setSolicitud(null);
      })
      .finally(() => {
        if (vivo) setCargando(false);
      });
    return () => {
      vivo = false;
    };
  }, [usuario]);

  return (
    <main className="flex min-h-[100dvh] items-center justify-center px-4 py-10">
      <div className="w-full max-w-sm animate-subir text-center">
        <div className="mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-[1.4rem] bg-humo text-4xl shadow-suave">
          🔒
        </div>
        <h1 className="text-2xl font-black text-azul">Esta cuenta no tiene acceso</h1>
        <p className="mt-2 text-sm text-texto-medio">
          El panel es solo para el equipo de PagoYa. Si eres comerciante, todo lo
          tuyo está en la app: tus pagos, tu caja y tu equipo.
        </p>

        {usuario && !cargando && (
          <BloqueEquipo
            usuario={usuario}
            solicitud={solicitud}
            alPostular={setSolicitud}
          />
        )}

        {usuario && (
          <details className="mt-4 text-left">
            <summary className="cursor-pointer text-xs font-bold text-texto-tenue">
              ¿Prefieren darte el acceso a mano? Muestra tu código
            </summary>
            <div className="tarjeta mt-2 p-4 text-sm">
              <p className="text-texto-tenue">Entraste como</p>
              <p className="font-bold">{usuario.email ?? usuario.uid}</p>

              <p className="mt-4 text-texto-tenue">
                Pásale este código a quien te dio el acceso:
              </p>
              <code className="mt-1 block break-all rounded-lg bg-humo p-2 font-mono text-xs">
                {usuario.uid}
              </code>
              <button
                type="button"
                onClick={() => {
                  void navigator.clipboard.writeText(usuario.uid).then(() => {
                    setCopiado(true);
                    window.setTimeout(() => setCopiado(false), 2000);
                  });
                }}
                className="mt-2 text-xs font-bold text-naranja underline underline-offset-4"
              >
                {copiado ? "¡Copiado!" : "Copiar código"}
              </button>
            </div>
          </details>
        )}

        <button
          type="button"
          onClick={() => void salir()}
          className="btn-primario mt-6 h-12 w-full"
        >
          Salir
        </button>
      </div>
    </main>
  );
}

function BloqueEquipo({
  usuario,
  solicitud,
  alPostular,
}: {
  usuario: User;
  solicitud: Solicitud | null;
  alPostular: (s: Solicitud) => void;
}) {
  const [nombre, setNombre] = useState("");
  const [notaRol, setNotaRol] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Ya postuló: le mostramos en qué anda su solicitud.
  if (solicitud) {
    return <EstadoSolicitudBloque estado={solicitud.estado} />;
  }

  return (
    <div className="tarjeta mt-6 p-4 text-left">
      <h2 className="font-black text-azul">¿Eres del equipo? Postula acá</h2>
      <p className="mt-1 text-sm text-texto-medio">
        Deja tus datos y un dueño te da el visto bueno. Nadie se mete solito.
      </p>

      <div className="mt-4 space-y-3">
        <label className="block">
          <span className="text-xs font-bold uppercase text-texto-tenue">
            Tu nombre
          </span>
          <input
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="María Quispe"
            className="campo mt-1"
          />
        </label>
        <label className="block">
          <span className="text-xs font-bold uppercase text-texto-tenue">
            ¿Qué vas a hacer? (opcional)
          </span>
          <input
            value={notaRol}
            onChange={(e) => setNotaRol(e.target.value)}
            placeholder="Cobranzas de la zona sur"
            className="campo mt-1"
          />
        </label>

        {error && <p className="text-sm text-rojo-alerta">{error}</p>}

        <button
          type="button"
          disabled={enviando}
          onClick={() => {
            setError(null);
            if (!nombre.trim()) {
              setError("Ponle tu nombre para que sepan quién eres.");
              return;
            }
            setEnviando(true);
            void postular({ nombre, notaRol })
              .then(() => {
                alPostular({
                  uid: usuario.uid,
                  email: usuario.email ?? "",
                  nombre: nombre.trim(),
                  notaRol: notaRol.trim(),
                  estado: "pendiente",
                });
              })
              .catch((e: unknown) => {
                if (e instanceof ReglasNoListas) {
                  setError(
                    "Las reglas de acceso aún no están activas. Avísale al dueño o pásale tu código a mano.",
                  );
                } else {
                  setError("No se pudo enviar tu postulación. Prueba de nuevo.");
                }
              })
              .finally(() => setEnviando(false));
          }}
          className="btn-primario h-12 w-full"
        >
          {enviando ? "Enviando…" : "Postular al equipo"}
        </button>
      </div>
    </div>
  );
}

function EstadoSolicitudBloque({ estado }: { estado: Solicitud["estado"] }) {
  const contenido = {
    pendiente: {
      icono: "⏳",
      titulo: "Tu solicitud está pendiente",
      texto:
        "Ya quedó registrada. Espera que un dueño te apruebe — en cuanto lo haga, entras directo.",
      color: "bg-naranja-suave text-naranja-hondo",
    },
    aceptada: {
      icono: "✅",
      titulo: "¡Ya te aprobaron!",
      texto:
        "Si aún ves esta pantalla, cierra sesión y vuelve a entrar para refrescar tu acceso.",
      color: "bg-verde-suave text-verde-ok",
    },
    rechazada: {
      icono: "🚫",
      titulo: "Tu solicitud fue rechazada",
      texto:
        "Si crees que hubo un error, habla con el dueño y vuelve a postular.",
      color: "bg-humo text-texto-medio",
    },
  }[estado];

  return (
    <div className={`mt-6 rounded-2xl p-4 text-left ${contenido.color}`}>
      <p className="text-2xl">{contenido.icono}</p>
      <h2 className="mt-1 font-black">{contenido.titulo}</h2>
      <p className="mt-1 text-sm opacity-90">{contenido.texto}</p>
    </div>
  );
}
