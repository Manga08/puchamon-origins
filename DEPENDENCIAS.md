# Dependencias externas

- unsup **1.1.6 estable**, versión Modrinth `tx2qzgzv`: https://modrinth.com/mod/unsup/version/tx2qzgzv . Se descarga de la API/CDN oficial y compara SHA512 publicado antes de usar. LGPL-3.0-or-later; copyright Una Kearney; fuente enlazada por el autor: https://git.sleeping.town/unascribed/unsup . No se modifica su JAR. El JSON de procedencia queda local en `runtime/unsup-origin.json`.
- Gson **2.10.1**: https://github.com/google/gson ; Apache-2.0. La copia local usada proviene de las librerías del launcher. No se modifica su JAR; en una reconstrucción obtenerlo de Maven Central y verificar el checksum editorial antes de empaquetar.
- Fabric API **0.116.14+1.21.1**, Loader **0.19.5** y las clases de Minecraft **1.21.1** solo se usan como classpath de compilación del gate; no se incluyen dentro de su JAR. Sus entradas de compilación deben provenir de la instalación validada.

El lanzador pequeño y el gate son código propio. Los mods, recursos y datapacks del pack conservan sus licencias: este repositorio no concede permisos sobre contenido de terceros ni redistribuye Minecraft. La revisión de una release con blobs locales es obligatoria antes de distribuirla por internet.
