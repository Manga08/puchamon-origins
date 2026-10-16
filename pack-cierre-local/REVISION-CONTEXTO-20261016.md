# Puchamon: Origins — idiomas, guía y publicación

## Hecho el 16 de octubre

- Repositorio existente convertido de privado a público, conservando el historial. No se eliminó información ni se creó una segunda copia privada. Se inspeccionaron todos los objetos alcanzables de los dos commits anteriores; no se detectaron credenciales ni archivos de partidas. Esa comprobación heurística no certifica todos los posibles formatos de secreto ni derechos sobre contenido de terceros.
- GitHub Pages habilitado en `main:/docs`, HTTPS forzado: https://manga08.github.io/puchamon-origins/ . La página informa que aún no hay instalador final/canal estable; no se publica un feed local como si funcionara para amigos.
- Actualizador ampliado mediante planes explícitos por release. Permite reemplazar archivos concretos aprobados o mezclar claves específicas de `options.txt`; mantiene respaldo y registro de aplicación única. No sincroniza carpetas enteras ni vuelve a resetear preferencias cada arranque. Retirar un parche no borra la configuración. No se añadieron configuraciones personales reales a un plan.
- Se conserva el gate de compatibilidad de cada release. El marcador exacto `puchamon/launcher-protocol-v2.json` hace que wrappers antiguos rechacen los nuevos manifiestos, en lugar de ignorar silenciosamente los parches de configuración. El instalador final debe incorporar el wrapper nuevo; el ZIP rc6 sigue siendo un ensayo inmutable, no la entrega para amigos.

## Idiomas: auditoría y correcciones

Se inspeccionaron los 164 JAR raíz del cliente, sus dependencias embebidas y 33 ZIP de recursos seleccionados: 136 espacios de nombres con archivos de idioma detectados. Se revisaron claves inglesas/españolas, formatos, posibles caracteres dañados y cadenas idénticas. Una cadena idéntica no demuestra error: puede ser un nombre propio. La revisión estática no reconstruye perfectamente el orden de todos los recursos internos opcionales ni sustituye comprobar el texto efectivo en el juego.

Aplicado localmente, con la versión anterior respaldada: `puchamon-translations-1.0.1.jar`, tanto cliente como servidor de prueba. Incorpora 501 entradas añadidas o corregidas:

| Área | Entradas |
| --- | ---: |
| Cobbreeding | 395 (329 son variantes del mismo nombre «Huevo Pokémon») |
| Footblock Ultimate | 45 |
| Sound Controller | 8 |
| TrashSlot | 24 |
| Mensajes de combate y títulos de Cobblemon | 26 |
| Comforts / FTB Teams / Waystones | 1 por mod |

Las 26 discrepancias de parámetros localizadas en Cobblemon ya no aparecen en la pasada estática posterior. Footblock ya no presenta las 12 cadenas detectadas con interrogaciones dentro de palabras. Los formatos `%1$s`, los porcentajes y los identificadores se preservaron; los nombres de mods, Pokémon, métodos con nombre propio y artistas no se sustituyeron por traducciones literales arbitrarias. Los nombres comunes de objetos usan su contexto Pokémon; PS y PP se explican en la guía.

**No está todo el modpack traducido.** El inventario encontró 6.663 claves de diálogo de RCT sin es_es, además de textos de navegación, opciones avanzadas y construcción. Parte de las ausencias de es_es tiene traducción nativa es_mx (por ejemplo Handcrafted y EMF/ETF); requiere revisar y aprovechar esa traducción antes de escribir otra. No se deben presentar todos los nombres de entrenadores/especies como texto pendiente ni traducirlos automáticamente.

Prioridad siguiente: diálogos funcionales y tarjeta del entrenador/RCT; interfaz de navegación; instrucciones Pokémon de añadidos; nombres/descripciones de construcción; opciones avanzadas. Los valores de Cobblemon presentes solo en un resource pack o generados dinámicamente deben verificarse en contexto. El detector aún señala el texto de apoyo económico de REI; el caso de Battle Extras cambia `%s` implícitos a `%1$s/%2$s` y necesita normalización semántica del detector, no corrección ciega.

No se lanzó Minecraft para verificar visualmente esta ronda, ni se declara aprobada la precedencia de cada overlay. Los JSON, parámetros, hashes de copia y archivos de capítulos sí se comprobaron. Preferencias gráficas, audio, controles, mapas, mundos y cuentas no cambiaron.

## Guía para quien nunca ha jugado Pokémon

Se conservaron 18 capítulos y 229 misiones. Se reforzaron 12 descripciones existentes, sin mover nodos ni cambiar IDs, detectores, recompensas o progreso. No se añadieron casillas informativas para inflar el número de misiones.

Contenido añadido: diferencia entre experiencia de Minecraft y nivel Pokémon; qué son PS, PP y habilidades; equipo y cajas de PC; capturar sin debilitar; utilidad de sueño/parálisis frente a daño continuado; variocolor no significa más fuerte; cobertura e inmunidades con un ejemplo; preparación y análisis de una derrota; elegir físico/especial; prioridad de movimientos; límite de nivel y tarjeta; orden de aprendizaje antes de EV/IV y crianza competitiva.

La guía anterior ya tenía la tabla de 18 tipos, STAB, turnos, estados, entrenamiento y cuatro circuitos regionales. El refuerzo hace esas ideas más prácticas para principiantes, no constituye una certificación de haber jugado cada liga o capturado cada legendario. La página actual de la wiki puede describir Cobblemon 1.8: no se trasladaron sus novedades al pack 1.7.3.

## Skins realmente instaladas

- Quick Skin Fabric 3.1.1: selección y sincronización de skins.
- `migaja-skin-bridge-1.0.0-prueba.jar`: puente propio Fabric; negociación de Quick Skin después de EasyAuth y caras en Xaero. Mismo SHA256 en cliente, servidor y build comprobado.
- Xaero Minimap 26.6.0 y World Map 1.47.0.
- Chat Heads 0.15.7: caras en el chat; no es el puente de mapa.
- EMF 3.2.4 y ETF 7.1, con Fresh Moves seleccionado, para los recursos visuales correspondientes; Not Enough Animations 1.12.4 para movimientos visuales.

**3D Skin Layers no está instalado** en esta copia. No se añadió por inferencia. Sigue pendiente la prueba de dos personas con cambio de skin, caras próximas/lejos y reconexión; los ensayos técnicos anteriores del puente no sustituyen esa prueba.

## Lo que falta para entregar

El sitio HTTPS ya existe, pero no hay canal estable publicado. Los blobs locales ocupan unos 564 MB e incluyen ZIP de 179 y 124 MB: no se pueden subir como archivos Git normales; además hay que revisar licencias y preferir descargas de origen de terceros. Se necesita decidir técnicamente cómo servir esos recursos grandes, terminar las verificaciones del candidato y construir/importar el instalador nuevo. No publicar cuentas, copias de mundos o credenciales en Pages/Releases.

El servidor de juego aún no está desplegado en el hosting comprado. Al desplegar habrá que configurar IP/UDP, gate/marker, permisos, mundo/spawn, backups, zona horaria y pregeneración. No se cambió Migajaland ni se prometió soporte probado para un PC físico de 8 GB.
