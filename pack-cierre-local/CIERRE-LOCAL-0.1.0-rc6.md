# Puchamon: Origins — cierre local 0.1.0-rc6

14 de octubre de 2026. Candidata local aplicada a la misma instancia social, **no publicación stable ni servidor público**. Migajaland permanece fuera del alcance. El usuario indicó que el hosting se comprará después; no se contrató alojamiento ni dominio.

Actualización posterior de evidencia: el control de ventanas volvió a estar disponible. Se arrancó Minecraft desde Freesm, se entró al mundo local sin autenticar y se revisaron las tres subguías nuevas, mapa y skin local. También hubo ensayo individual, ordenación real y dos capturas nativas de fotogramas. Detalles y límites en `PRUEBA-VISUAL-rc6-2026-10-14.md`; esto no equivale a probar autenticación, dos jugadores o la importación del ZIP limpio.

## Implementado

- 18 capítulos y 229 misiones: 198 premiadas y 31 informativas. Se conservan las 215 misiones anteriores y sus tareas/premios; no se cambian sus archivos de capítulo. Tres subguías nuevas: Fútbol y fiestas, Imágenes y decoración personal, Seguridad y convivencia. El nodo de bienvenida indica dónde abrirlas. 14 misiones nuevas, 11 premiadas; informativas sin premios. Sin bloqueo adicional en la ruta de gimnasios.
- Premios sociales únicos: total de 16 hormigones blancos, 8 papeles, 12 tablones, 2 cobres, 4 lanas, 1 saco de tinta luminosa, 6 hierros y 4 redstones al completar los 11 objetivos. Sin dinero, legendarios, IV perfectos, explosivos ni llaves. Las tareas de objetos detectan posesión, no se presentan como prueba de fabricación propia; el intercambio es válido.
- Identidades nuevas persistentes en `guia-social-identidades.json`, separadas del registro de las misiones originales. El generador adopta los IDs del primer borrador y los conserva al repetirse; no reasigna IDs por posición.
- Complemento de idioma propio `puchamon-translations-1.0.0.jar`: 1.353 entradas añadidas/revisadas. Confetti 8/8, Beachparty 183/183, SecurityCraft 1.057/1.057, Essential Commands 101/101 y cuatro claves que faltaban en Immersive Paintings. Se conservan sus otras 141 traducciones nativas y las 45/45 de Footblock. No equivale a certificar cada frase dinámica de todos los mods del pack. No se cambian idioma seleccionado, gráficos, audio o teclas del jugador.
- SecurityCraft usa su traducción upstream MIT filtrada a las claves realmente instaladas, con commit/hash y licencia; se completan los textos específicos del port. Se corrigieron dos textos upstream cuyo número de parámetros no coincidía con el port. Validación automática de parámetros, JSON y ausencia de caracteres de reemplazo. Artistas, canciones, nombres de mods y nombres propios conservados.
- `puchamon-safety-1.0.0.jar`, solo servidor: frena detonaciones de SecurityCraft si detecta Pokémon propios/salvajes o jugadores sin autenticar en la zona de 16 bloques. Conserva la función de las minas en zonas vacías y el ajuste sin destruir terreno. No cambia TNT/creepers, combate Pokémon, láseres o lava falsa; no sustituye convivencia/moderación.
- Datapack `Puchamon-Correcciones-Sociales`: el armario natural `beachparty:palm_cabinet` con LootTable entra en conversión de Lootr a barril personal. Los muebles construidos sin tabla no se convierten. No se borraron inventarios Lootr ni se programó regeneración de botines.
- Herramientas de respaldo completo, restauración a carpeta NUEVA y cálculo de UUID offline, más procedimiento de operación/actualización en `operacion/`. El ayudante UUID no escribe whitelist ni concede permisos.
- Candidata congelada: `actualizaciones/puchamon-origins-social-r6/release-lock.json`, 601 archivos administrados. Instalador local 0.1.0-rc6 y overlay de servidor preparados. El gate no se activa en la instancia social todavía para no bloquear al cliente usado para pruebas manuales; se prueba en copia separada.

## Pruebas realizadas

1. Respaldo offline COMPLETO: 3.414 archivos verificados por SHA256, incluyendo mundo, cuentas, LuckPerms fuera de la carpeta de mundo, configuraciones, software y motores auxiliares. Restauración a otra carpeta: 3.414/3.414 coincidentes. Esa copia arrancó con las versiones originales y se detuvo limpiamente. Copia privada, nunca dentro del instalador/feed.
2. Servidor dedicado real en una copia aislada. Cargó 229 tareas nativas de FTB, cero ItemTasks vacías y las 310 referencias de items/avances/iconos existen. Validación estática: sin IDs duplicados ni solapamientos en los capítulos; los nuevos nodos no se insertan encima de la ruta principal. No se afirma haber revisado visualmente las nuevas pantallas dentro de Minecraft.
3. Explosivos: dirt mine, mine, claymore, bouncing betty y track mine frenadas; entidades BouncingBetty e IMS frenadas; salud de Pikachu intacta. Predicado nativo de ServerPlayerEntity sin autenticar comprobado. En arena vacía, la mina normal detona y el terreno vecino permanece. Es una prueba técnica de las entradas nativas, no una partida de dos humanos ni inmunidad a todos los peligros.
4. Essential Commands cargó realmente el texto español de la cola de teletransporte. Zona Java del servidor: America/Bogota. EasyAuth conserva restricciones y cuentas; solo cambia su idioma de respaldo a es_es.
5. Lootr repetido con TODOS los recursos activos: 8.774 tablas, 5.338 estructuras, 2.291 referencias de contenedores con LootTable y 9.785 referencias únicas comprobadas. Se encontró el armario de Beachparty sin conversión; tras añadir la etiqueta, **cero errores**. 21 tipos de contenedor revisados; los dispensadores de trampas quedan excluidos deliberadamente. NBT/JSON pesados fuera del hilo del juego, comprobación de registro en lotes de 128. No se retocan probabilidades ni se asegura que un objeto raro salga en pocos cofres.
6. Cliente: preflight estático de 163 JAR raíz, cero IDs duplicados y cero requisitos/breaks/conflicts incumplidos con el parser de Fabric (1.109 predicados del lado cliente). No sustituye un arranque visual del cliente. Las adiciones propias no registran teclas.
7. Servidor sin jugadores, Xmx4G: 1.199 muestras después de las comprobaciones; mediana de tick 0,4381 ms, P95 1,9258 ms; heap usado puntual 1.181 MiB. El proceso total ronda 2,3 GiB en esta máquina de 32 GB. **No es un benchmark de varios jugadores ni demuestra que el cliente vaya bien en un PC físico de 8 GB.** El registro de entrenadores sigue produciendo un retraso de arranque de unos ocho segundos también en la copia original; no se oculta como una mejora de rendimiento.
8. Actualizador: nueve escenarios de regresión con unsup real, preservación de preferencias, archivos no administrados, hash incorrecto, corte/reintento y servicio caído; endpoint local privado de solo lectura probado sin publicar clave. Además instalación limpia COMPLETA y actualización rc5→rc6 del pack: 320 archivos administrados/164 JAR verificados, cinco archivos personales de ensayo intactos y heap real del proceso diagnóstico de 6.144 MiB. Se ejecutó un JVM diagnóstico, **no Minecraft ni una importación de Freesm**.
9. Gate rc6 en servidor real de copia: cliente sin identificación y antiguo rechazados; compatible completa LOGIN. No entra al mundo ni suplanta una prueba de sesión humana; no es un sistema antitrampas.

## Conservación

Antes del siguiente arranque de la fuente social se cotejan los 33 archivos personales de cliente registrados: cero cambios. También se compararon los 291 archivos de configuración preexistentes de cliente ajenos al idioma de la guía: cero cambios. Mundo original, base EasyAuth y datos LuckPerms: 196 archivos cotejados contra el respaldo completo, sin cambios durante la aplicación. Evidencia: `cierre-rc6/evidencias/conservacion-final.json`. El arranque normal posterior puede guardar metadatos; no es un borrado de progreso.

No se cambiaron ruta de instancia, dirección local, nombre de mundo, UUID de jugadores ni IDs antiguos de misiones. El ZIP limpio no incluye opciones usadas, cuentas, mapas, cachés de skins, partidas, logs o bases de datos. El mod QA no va en cliente ni overlay de servidor; el constructor rechaza expresamente su nombre.

## Instalador para esta máquina, no para amigos

`distribucion/output/Puchamon-Origins-0.1.0-rc6-PRUEBA-LOCAL.zip`

Es una instancia nativa ZIP para importar en Freesm y actualizar antes de iniciar. Requiere el servicio local de descargas en 127.0.0.1:8765. No requiere BAT de activación. La instancia social existente también recibe la guía/idioma/correcciones; no hay que borrarla para probar esos cambios.

La distribución lleva 164 JAR cliente y el overlay 131 servidor (incluido gate; sin QA ni bases). El cliente social de trabajo lleva 163 y servidor social 130 porque su gate aún no se activa. RAM automática: equipos de 8/12 GB → 4 GB; de 16 GB o más → 6 GB. Shaders apagados solo como valor inicial, nunca sobrescribiendo una elección personal.

## Pendientes reales antes de stable

- Importación visual del ZIP limpio. El arranque de la instancia existente desde Freesm ya se realizó en la prueba visual posterior; no se confunde con la importación del instalador.
- PC físico de 8 GB, sesión prolongada y carga multijugador representativa: exploración, varios criaderos/pasturas, partidos, cuadros y partículas. No hay usuarios artificiales presentados como amigos reales.
- Dos personas para voz desde redes distintas, skins/caras cercanas y lejanas, cambio de skin, PC Plus, muerte/reconexión y login lento, recompensas personales y accesos de SecurityCraft/Lootr. Instalación o metadatos correctos no certifican esas interacciones.
- Comprar/configurar hosting con UDP, endpoint privado HTTPS, dirección real en instalador, límites y spawn final. Revisar derechos de los archivos alojados, desplegar gate/marker conjuntamente y solo entonces promover stable. No se subió el pack completo al GitHub privado ni se compartió un token GitHub con jugadores.
- Backups automáticos y reinicio 06:00 Colombia con avisos se crearán en el proveedor elegido; no se programaron en Migajaland ni en Windows. Retención/espacio de backups de software cliente sigue explícita, sin purga silenciosa.
- Logo propio opcional: se conserva el arte acreditado de la base; no se presenta como un logo original de Puchamon.

## Fuentes y criterio

- [SecurityCraft Fabric](https://github.com/VerkoSK/SecurityCraftFabric): port parcial; no prometer cámaras/torretas no implementadas. La implementación instalada y sus métodos mandan sobre vídeos de otra edición.
- [Traducción upstream fijada](https://raw.githubusercontent.com/Geforce132/SecurityCraft/c4d35b0c8bf85c2a94c37a1f8ce37ef5ce1a6aa8/src/main/resources/assets/securitycraft/lang/es_es.json), licencia MIT conservada.
- [Configuración oficial Cobblemon](https://wiki.cobblemon.com/index.php/Config): autoUpdateShowdown actualiza a la versión incluida en el Cobblemon instalado, no significa tomar sin control cualquier versión futura de internet. Se conserva true y el motor guardado en los respaldos; no se desactivó a ciegas ni se cambiaron reglas de aparición/clima.
- La habilidad de rendimiento guio a separar medición en vacío de la carga objetivo y a no tocar distancias/spawns sin un perfil representativo. La de balanceo orientó premios únicos de materiales ligados a la actividad, sin monedas ni ventaja Pokémon. Su referencia auxiliar `docs/game-design-theory.md` no está instalada; no se atribuye a ella una validación teórica o de economía completa.

Fallos de desarrollo de esta ronda: un primer borrador puso el inicializador dentro del paquete de mixins y no arrancó. Se corrigió y repitieron las pruebas. Un ID de herramienta de la guía se corrigió antes de instalar. Evidencias privadas conservadas, no supresión de logs ni cambios en el mundo original.
