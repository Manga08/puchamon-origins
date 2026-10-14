# Puchamon: Origins — instalación y actualización

Estado: **implementado y probado localmente; no activado por internet**. Repositorio privado: https://github.com/Manga08/puchamon-origins . Base Minecraft 1.21.1, Fabric 0.19.5, Cobblemon 1.7.3. Esta herramienta no modifica Migajaland ni la instancia social jugada.

## Qué hace el jugador

Cuando tengamos el endpoint privado HTTPS operativo: importar una instancia ZIP preparada en Freesm y pulsar **Jugar**. Freesm puede pedir confirmar el comando personalizado del paquete: revisar su origen y aceptar solo el ZIP entregado por el administrador. No se pide desactivar la seguridad del launcher.

La instancia nueva no contiene cuentas, contraseñas, mundos ni preferencias de nuestro ensayo. No se reparte un backup de una instancia usada. No requiere BAT de activación. El ZIP actual con sufijo **PRUEBA-LOCAL** apunta a 127.0.0.1: solo sirve en el equipo que ejecuta el servicio de pruebas; **no enviarlo aún a amigos**. El icono sigue siendo el de la base; no se ha creado un logo nuevo.

## Arranque

1. Un lanzador Java pequeño, incluido en el ZIP, consulta el canal aprobado y verifica el manifiesto, pack.toml e index.toml por SHA256.
2. Los valores iniciales se descargan solo cuando faltan y aún no se han entregado. Los borrados o cambios personales posteriores se respetan. Shaders apagados inicialmente; recursos de la base y controles curados incluidos. No se copian opciones personales de la instancia jugada.
3. **unsup 1.1.6 estable** aplica el pack en formato packwiz, con hashes seguros y versiones exactas. No se hace un `update all` contra las últimas versiones de internet.
4. El lanzador verifica todos los archivos administrados antes de registrar éxito y arrancar el JVM real. Si falla la red o un hash, se detiene; volver a Jugar permite reintentar. No se abre una mezcla de versiones a propósito.
5. RAM del proceso real: equipos de 8/12 GB → 4096 MiB; de 16 GB o más → 6144 MiB. Se tolera la RAM utilizable ligeramente menor a la nominal. Menos de aproximadamente 8 GB: se rechaza este perfil. Son límites máximos, no consumo constante. La cifra base mostrada por Freesm puede seguir en 4096: el wrapper ajusta los argumentos del JVM real y lo registra.
6. Opción avanzada en `minecraft/puchamon/launcher.properties`: `ramMode=manual` y `manualMaxMiB=4096`. No la sobrescribe el feed. En modo automático, cambiar el máximo en Freesm no vence el límite del wrapper.

El wrapper conserva stdin/stdout para el protocolo del launcher y no registra sus argumentos, que pueden contener credenciales. El updater tiene su propio proceso y finaliza antes de abrir Minecraft. El conjunto usa Java 21 ya gestionado por el launcher; no reparte un ejecutable de Minecraft o una cuenta.

## Archivos protegidos

Lista cerrada de archivos administrados: JAR directamente dentro de `mods`, datapacks, definiciones de FTB Quests y cuatro ajustes compartidos explícitos. Nada de `options.txt`, mapas/waypoints, skins/cachés, cuentas, capturas o partidas es un archivo administrado. Las preferencias se entregan únicamente como valores iniciales y no se sincronizan después.

Los JAR añadidos manualmente por un jugador no se eliminan silenciosamente. Si uno ocupa un nombre que necesita un archivo nuevo del pack y su contenido difiere, se bloquea el arranque con un aviso. No es soporte garantizado para mods ajenos ni un sistema antitrampas.

Antes de actualizar se respaldan los archivos administrados anteriores en `minecraft/puchamon/backups`, con hashes verificados. Son respaldos de software, **no del mundo**. Actualmente no se purgan automáticamente: el administrador debe vigilar espacio y retirar versiones antiguas de forma explícita. En el servidor siempre se respalda y restaura el mundo junto al software correspondiente; el updater cliente no toca mundos ni puede hacer ese rollback por sí solo.

## Versiones y servidor

- Releases inmutables; `test` y `stable` separados. La creación de un draft no publica una versión estable.
- `promover-stable.mjs` exige instalación nueva, actualización, servidor preparado y revisión de derechos de distribución aprobados explícitamente. Rechaza promover una URL local/sin HTTPS.
- Una secuencia evita retroceder silenciosamente a un manifiesto anterior. Una corrección se publica como parche nuevo, no sobrescribiendo una versión existente.
- `puchamon_gate` comprueba la compatibilidad durante LOGIN, antes de entrar al mundo o llegar al registro de EasyAuth. Falta de mod o ID antiguo: desconexión con instrucción en español. No revisa la identidad de la cuenta ni sustituye EasyAuth. No es una protección contra clientes deliberadamente falsificados.
- `Servidor-Puchamon-<release>` es un **overlay de software**, no un servidor nuevo completo. Incluye solo JAR de raíz, datapacks/guía, ajustes compartidos y marcador. No incluye LuckPerms DB, cuentas, whitelist, OP, mundo o librerías. El gate y `puchamon/release.json` se despliegan juntos durante mantenimiento, antes de promover clientes.
- Cambiar Minecraft o Fabric requiere migración de instancia aprobada: el wrapper no intenta modificar componentes del launcher después de haber calculado su classpath. Actualizar la base Cobbleverse no debe borrar nuestros añadidos/guía/correcciones.
- La dirección del servidor de amigos no está preconfigurada todavía porque ese hosting no se ha elegido. No se distribuye 127.0.0.1 como si fuera su servidor.

## GitHub privado no es un hosting anónimo

Los amigos no necesitan cuenta de GitHub para este diseño. El repositorio guarda fuentes, herramientas y evidencias resumidas; no se incrusta un token personal de GitHub en el instalador.

Se preparó `serve-private.mjs`: servicio de archivos **solo GET/HEAD**, protegido por una clave aleatoria de lectura de 256 bits. Esa clave es de grupo: quien tiene el enlace puede descargar y compartirlo; **no ofrece permisos individuales ni secreto frente a los destinatarios**. Permite revocar/rotar el acceso al grupo, no administrar GitHub ni escribir archivos. No se debe publicar el ZIP privado ni su launcher.properties. Los logs del servicio no guardan URL/clave; los logs del updater se depuran de segmentos de clave antes de compartirlos.

Por defecto escucha en localhost, detrás de un proxy HTTPS. También admite certificados TLS explícitos. Rechaza exponer HTTP directamente fuera de loopback. Variables del servicio: `PUCHAMON_DOWNLOAD_ROOT`, `PUCHAMON_DOWNLOAD_KEY`, `PUCHAMON_DOWNLOAD_HOST`, `PUCHAMON_DOWNLOAD_PORT`, opcionalmente `PUCHAMON_TLS_CERT` y `PUCHAMON_TLS_KEY`. La clave se configura en el hosting, nunca se confirma en Git ni se imprime. No hay dominio, certificado, túnel o servicio público creado en este trabajo; falta elegir el alojamiento.

Una vez elegido el hosting: generar una clave de grupo, preparar el feed con su URL HTTPS, revisar licencias de los archivos sin origen externo directo, validar un import real de Freesm, preparar servidor y recién promover `stable`. No copiar el directorio `site` completo a un repositorio público sin esa revisión.

## Construir y probar (administrador)

Herramientas: PowerShell, Java 21/JDK, Node.js y una copia local validada del pack. `compilar.ps1` acepta `-Jdk`/`-Gson`; `compilar-gate.ps1` acepta `-Client`/`-Libraries`/`-Jdk`. Variables de construcción opcionales: `PUCHAMON_PROJECT`, `PUCHAMON_CLIENT`, `PUCHAMON_SERVER`, `PUCHAMON_JAVA`. Los valores por defecto corresponden al proyecto local actual.

1. Obtener los binarios externos exactos de `DEPENDENCIAS.md` y verificar sus hashes.
2. `compilar.ps1`, `compilar-gate.ps1`, `preparar-origenes.ps1`.
3. `node test-updater.mjs` y `node test-private.mjs`: no abren Minecraft; usan unsup real y un JVM de diagnóstico.
4. `node build-release.mjs --release <ID> --base-url <URL-HTTPS> --channel stable`: prepara draft e instalador apuntando a stable, pero **no lo promociona**.
5. Importar el ZIP limpio en Freesm y verificar el arranque visual. Antes de compartir, configurar la dirección real del servidor sin reemplazar listas personales existentes.
6. Probar gate en un servidor clonado; `test-gate.mjs` habla el protocolo LOGIN y no entra al mundo. No reutilizar puerto/servidor de producción.
7. Registrar aprobación local en `approvals/<ID>.json`: `release`, `freshInstallVerified`, `updateVerified`, `serverReady`, `distributionRightsReviewed`. Ejecutar `node promover-stable.mjs <ID>` después del despliegue compatible del servidor. Subir primero los archivos de la release y al final el puntero de canal.

`exportar-instalador.ps1` exporta el draft de prueba mediante lista cerrada. El export final de un canal estable debe usar el mismo mecanismo y su URL HTTPS validada, no comprimir el directorio usado en pruebas.

## Verificación efectuada y límites

Pruebas automatizadas: RAM física 8/12/16/32 GB simulada en la selección; heaps reales de 4096 y 6144 MiB; instalación limpia con espacios/tildes; actualización y retirada de un mod; preferencias intactas; rechazo de ruta protegida; hash erróneo; descarga truncada y recuperación; servicio caído; endpoint privado con clave correcta/incorrecta, método de escritura y traversal. Gate probado en servidor real: sin identificación, antiguo y correcto. Instalación del pack completo realizada mediante unsup con 163 JAR y verificación posterior; se ejecutó un JVM diagnóstico, **no se afirmó que fuera Minecraft**.

Pendiente: importación visual del ZIP por Freesm, arranque del juego desde ese ZIP, prueba en un PC físico de 8 GB y acceso desde otro PC al hosting definitivo. No se reintentó por otra vía el lanzamiento de Freesm previamente bloqueado por la herramienta. No se activó el gate en la instancia social anterior ni se desplegó en un hosting de Minecraft.

Fuentes oficiales: [unsup](https://modrinth.com/mod/unsup), [packwiz](https://packwiz.infra.link/tutorials/installing/packwiz-installer/), [código de Freesm](https://github.com/FreesmTeam/FreesmLauncher), [Fabric networking](https://github.com/FabricMC/fabric/tree/1.21.1/fabric-networking-api-v1).
