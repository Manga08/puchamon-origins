# Puchamon: Origins — cierre antes de abrir a amigos

No hay hosting comprado ni dirección pública. No programar tareas en Migajaland ni enviar el ZIP PRUEBA-LOCAL. No publicar copias de servidores/instancias jugadas. El siguiente despliegue sigue siendo una candidata, no una estable aprobada.

## Perfil inicial propuesto del servidor

- Java 21 y `-Duser.timezone=America/Bogota` antes de `-jar`. Comprobar la hora de una línea nueva del juego; el reloj del panel puede continuar en UTC.
- Servidor de amigos: supervivencia, dificultad normal, whitelist activa y administradores explícitos. El ensayo local mantiene acceso de prueba y no se distribuye.
- `online-mode=false` con EasyAuth obligatorio para todos inicialmente, nombres exactos y registro coordinado. Las cuentas oficiales también entran por ese flujo; no importarles UUID Mojang a la whitelist offline. El helper Ver-UUID-Offline no modifica archivos.
- Contraseña mínima actual de EasyAuth: 12 caracteres. `hide-player-coords=false` evita el limbo remoto; movimiento, inventario, combate e interacción bloqueados antes de autenticarse. Nunca ampliar la excepción de paquetes Pokémon para arreglar skins.
- Homes: dos, preparación 2 s, reutilización 10 s. TPA con aceptación. No conceder tpahere, home de otros, edición de misiones, comodines LuckPerms, /give ni teleport de waypoints. Ajustar cambios de estas reglas solo como una decisión separada.
- Empezar con distancia de visión 8 y simulación 6; medir antes de ampliar. RAM del hosting no es solo heap: reservar margen para JVM y servicios. El servidor local de QA usa Xmx4G; no representa el equipo de un futuro proveedor.
- Voz: un puerto UDP realmente asignado, configuración coherente y prueba entre dos redes. Abrir un puerto TCP del juego no prueba la voz. Offline mode no proporciona la misma garantía de identidad/cifrado que una sesión oficial autenticada.
- Elegir semilla y punto de aparición antes de abrir, comprobar suelo libre y regreso tras morir. No se elimina el mundo de ensayo automáticamente. Pregenerar una zona moderada y medir CPU/disco; no cargar permanentemente las bases de entrada.

## Copias y recuperación

Respaldar servidor completo, no solo región del mundo: Pokémon/equipos/PC, inventarios, Lootr, misiones/Teams, economía, EasyAuth, LuckPerms (incluido mods/luckperms), homes, cuadros y configuraciones. Los backups contienen credenciales/datos personales: privados y fuera del feed.

Los dos scripts PowerShell se destinan al laboratorio Windows; no se afirma que se ejecuten tal cual en un hosting Linux. Rechazan un mundo abierto y una restauración sobre carpeta existente. La operación equivalente en el proveedor será su backup offline/SFTP, verificado y restaurado en copia.

Conservar varias generaciones y una copia fuera del proveedor. No hay purga automática ni tarea programada activa. Elegir retención con el espacio del hosting; no borrar la última versión recuperable al publicar un parche.

Rollback = restaurar mundo/datos + mods + configuraciones de la misma copia. Nunca abrir un mundo ya migrado con mods antiguos como supuesto rollback.

## Reinicio solicitado como preferencia previa

Preparar al contratar el hosting: diariamente 06:00 America/Bogota (11:00 UTC si el panel usa UTC), avisos a 10, 5 y 1 minutos; parada limpia con guardado y arranque. Verificar zona horaria real del programador y un ciclo completo. No está creado todavía, ni se ha creado una automatización de Codex en su lugar.

## Aprobación mínima

1. Instalación limpia y actualización real en Freesm: opciones/mapas/waypoints/skins intactos.
2. Sesión en PC físico de 8 GB. Xmx4G no limita memoria nativa/texturas/Windows.
3. Dos personas: login, muerte/reconexión, voz, skins/caras cercanas y lejanas, PC, crianza, Lootr personal, recompensas únicas y permisos sin OP.
4. Carga representativa de exploración y pasturas; registrar TPS, percentil 95 de tick, GC y RAM total. Una medición en vacío no aprueba esa carga.
5. Endpoint HTTPS privado, licencias revisadas, gate/marker desplegados juntos, cliente compatible y promoción stable al final.
6. Copia restaurada arranca antes de tocar producción.

No copiar automáticamente la lluvia/días largos o refresco semanal de Lootr de Migajaland: pueden modificar apariciones y economía Pokémon. El botín de Lootr no se reinicia por instalar el idioma o la guía.
