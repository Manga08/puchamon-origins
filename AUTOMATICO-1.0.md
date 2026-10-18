# Puchamon: Origins 1.0 — arranque automático

La entrega `Puchamon-Origins-1.0-Automatico.zip` contiene el wrapper de actualización, no `TestLauncher`. Se importa en Freesm y se inicia con Jugar. No hay un BAT que ejecutar. Hay que autorizar el comando personalizado del paquete si Freesm lo solicita; el paquete no desactiva las protecciones del launcher.

El canal es `https://manga08.github.io/puchamon-origins/channels/stable.json`. Se abre una ventana de comprobación y solo después de verificar la versión se inicia Java para Minecraft. La RAM máxima es 4096 MiB con 8/12 GB físicos y 6144 MiB con 16 GB o más. Los límites no certifican que un PC de 8 GB pueda ejecutar cómodamente este pack.

Las antiguas entregas autónomas `Guia-V2` / `Comodidad-r5` no consultaban internet. No pueden recibir una activación automáticamente: se debe activar una vez la instancia existente o importar la nueva. No sustituir una instancia jugada con un ZIP completo ni borrar mapas para actualizar.

## Publicación 1.0-auto.1

- Mismo software de juego que Comodidad r5: 736 archivos administrados, 187 JAR. No añade otros mods ni cambia Minecraft/Fabric.
- 178 archivos tienen origen oficial directo. Nueve recursos intactos se extraen del MRPACK oficial de Cobbleverse 1.7.42, verificado por hash. No se vuelve a alojar ese archivo.
- Nueve JAR propios y la guía/configuración/traducción del pack se sirven desde Pages. Los créditos de traducción están dentro del recurso; sus textos conservan las licencias y atribuciones originales, no se relicencian como MIT.
- Se aplican una vez los ajustes expresamente solicitados: tecla U, configuración de Pokebelt/Revival y solo los campos de jugadores en los dos perfiles de radar conocidos. No se reemplazan las opciones completas ni las demás categorías del radar.
- Se conserva la compatibilidad `puchamon-0.1.0-rc7.1-amigos` del servidor Comodidad r5, porque el juego no cambia. Esta activación del actualizador no obliga por sí sola a abandonar ese cliente anterior. En una actualización de contenido posterior se coordina un identificador nuevo con el servidor durante mantenimiento; no es un antitrampas.

## Pruebas

Pasaron instalación completa y actualización desde la copia autónoma usando los archivos reales de HTTPS, unsup real y el bootstrap de Windows PowerShell 5.1. Se verificaron los 736 hashes. Se preservaron siete archivos personales de ensayo, incluidas opciones gráficas/audio, mapas, waypoints, skin, partida e IPN. El segundo arranque comprobó de nuevo el canal y no reimpuso los ajustes ya aplicados.

También pasaron extracción exacta de nueve recursos del MRPACK oficial, rechazo de destinos protegidos y mezcla selectiva del radar. La regresión de unsup cubre hash incorrecto, descarga truncada, servicio caído, reintento, retirada de mods conocidos y conservación de mods ajenos. Estas pruebas de actualización lanzan una JVM diagnóstica: no equivalen a una sesión humana dentro de Minecraft ni a pruebas en otro PC físico.

## Actualizaciones posteriores

Crear otra release inmutable con los hashes del software aprobado; usar las URLs oficiales para terceros. Mantener los IDs de las misiones y dejar mundos, cuentas, Pokémon, progreso, mapas, waypoints y ajustes personales fuera de la publicación. Cambios de configuración requieren destinos y claves concretos, una petición explícita y un ID de migración nuevo. No copiar toda la carpeta `config` ni una instancia jugada al repositorio.

Probar instalación, actualización, conservación de datos y compatibilidad del servidor. Subir primero la release y verificar sus hashes en HTTPS; publicar el puntero estable al final. Si cambia contenido requerido, actualizar el marcador del servidor en mantenimiento coordinado. No editar releases ya publicadas. No publicar una versión nueva de Minecraft/Fabric sin migrar la instancia del launcher.

Los mensajes anteriores del README que describen el canal como pendiente corresponden a las candidatas históricas; este documento describe la activación actual.
