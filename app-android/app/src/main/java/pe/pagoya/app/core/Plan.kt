package pe.pagoya.app.core

/**
 * Planes de PagoYa. El tope es por DISPOSITIVOS vinculados al comercio (cada
 * teléfono que escucha cuenta, el del dueño incluido).
 *
 * Regla de negocio: gratis desde el día 1 con un solo teléfono; el cobro llega
 * cuando el negocio crece y necesita sumar gente. El cobro es MANUAL, fuera de
 * la app (por WhatsApp) — aquí los precios son solo informativos y NUNCA hay un
 * botón de "pagar" (política de Play para bienes digitales). El operador activa
 * el plan a mano desde su panel.
 */
enum class Plan(
    val id: String,
    val etiqueta: String,
    val maxDispositivos: Int,
    /** Precio en soles al mes. 0 = gratis. Solo informativo. */
    val precioMensual: Double,
    val resumen: String,
) {
    GRATIS(
        id = "gratis",
        etiqueta = "Gratis",
        maxDispositivos = 1,
        precioMensual = 0.0,
        resumen = "Un teléfono con voz. Para empezar sin pagar nada.",
    ),
    CASERITO(
        id = "caserito",
        etiqueta = "Caserito",
        maxDispositivos = 3,
        precioMensual = 9.90,
        resumen = "Hasta 3 teléfonos. Tú y tu gente escuchan cada venta.",
    ),
    PATRON(
        id = "patron",
        etiqueta = "Patrón",
        maxDispositivos = 10,
        precioMensual = 24.90,
        resumen = "Hasta 10 teléfonos. Para negocios con varios puntos.",
    );

    companion object {
        /** Del id guardado en Firestore al plan. Desconocido/ausente → gratis. */
        fun desdeId(id: String?): Plan =
            entries.firstOrNull { it.id == id } ?: GRATIS
    }
}
