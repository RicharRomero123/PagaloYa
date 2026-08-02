'use client';

import { useState } from 'react';
import { IconoWsp } from '../../components/Iconos';
import { wsp } from '../../lib/enlaces';

// Correo de soporte (mismo de toda la landing).
const CORREO = 'pimentel@inklop.com';

/**
 * Formulario de consultas para sitio 100% estático (sin backend). No envía
 * nada a un servidor: arma el mensaje con lo que escribe el casero y lo abre
 * en WhatsApp o en su app de correo, ya redactado. Así el usuario solo pega
 * "enviar" en el canal que prefiera.
 */
export default function FormularioConsulta() {
  const [nombre, setNombre] = useState('');
  const [negocio, setNegocio] = useState('');
  const [mensaje, setMensaje] = useState('');

  const texto =
    `Hola PagoYa, tengo una consulta.\n` +
    (nombre ? `Nombre: ${nombre}\n` : '') +
    (negocio ? `Negocio: ${negocio}\n` : '') +
    (mensaje ? `Consulta: ${mensaje}` : '');

  const enviarWsp = (e) => {
    e.preventDefault();
    window.open(wsp(texto), '_blank', 'noopener');
  };

  const enviarCorreo = () => {
    const asunto = encodeURIComponent('Consulta desde pagoya.pe');
    window.location.href = `mailto:${CORREO}?subject=${asunto}&body=${encodeURIComponent(texto)}`;
  };

  return (
    <form className="form-consulta" onSubmit={enviarWsp}>
      <label>
        Tu nombre
        <input
          type="text"
          name="nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          placeholder="Cómo te llamas"
          autoComplete="name"
        />
      </label>

      <label>
        Tu negocio <span className="opcional">(opcional)</span>
        <input
          type="text"
          name="negocio"
          value={negocio}
          onChange={(e) => setNegocio(e.target.value)}
          placeholder="Bodega, puesto de mercado, restaurante…"
        />
      </label>

      <label>
        Tu consulta
        <textarea
          name="mensaje"
          value={mensaje}
          onChange={(e) => setMensaje(e.target.value)}
          rows={4}
          placeholder="Cuéntanos en qué te ayudamos"
          required
        />
      </label>

      <div className="form-botones">
        <button type="submit" className="btn btn-wsp">
          <IconoWsp />
          Enviar por WhatsApp
        </button>
        <button type="button" className="btn btn-claro" onClick={enviarCorreo}>
          Enviar por correo
        </button>
      </div>

      <p className="form-nota">
        Este formulario no guarda nada en internet: solo abre tu WhatsApp o tu
        correo con el mensaje ya escrito para que lo mandes. Así de directo.
      </p>
    </form>
  );
}
