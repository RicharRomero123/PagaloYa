# Reglas R8/ProGuard de PagoYa (release con minify ACTIVADO).
#
# El código NO usa Gson ni deserialización por reflexión: Firestore se lee por
# acceso manual a mapas (getString/getBoolean/get as Map), y los `::class.java`
# son referencias a componentes Android (Activity/Service/Receiver), que R8
# conserva solo porque están en el Manifest. Firebase, Compose, coroutines y
# Credential Manager traen sus propias reglas dentro de sus librerías, así que no
# hace falta mantenerlas a mano.
#
# Lo de abajo es un colchón defensivo por si algún modelo llega a viajar a/desde
# Firestore por reflexión en el futuro.

# Modelos de datos que se serializan a/desde la nube.
-keep class pe.pagoya.app.core.Pago { *; }
-keep class pe.pagoya.app.core.Notificacion { *; }

# Atributos útiles para librerías con genéricos y anotaciones (Firebase).
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
