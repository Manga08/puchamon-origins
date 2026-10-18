# Puchamon: Origins 1.0 — parche de mapa 1.0-auto.2

18 octubre 2026. Versión de juego y protocolo de acceso sin cambios.

- Recupera la flecha propia del mapa grande mediante el perfil del servidor (`display_player_as_arrow=true`).
- Conecta también el renderizador de jugadores lejanos del mapa grande con Quick Skin. Antes, el puente solo cubría el minimapa.
- Nombres permanentes encima de las cabezas rastreadas. Evita duplicar el nombre y ampliar la cabeza al pasar el cursor por el mapa grande.
- Normaliza caras HD/legacy y compone el sombrero cuando el jugador no tiene una entidad cargada cerca. La cara base no se vuelve transparente.
- Seguimiento global en la misma dimensión, sin forzar chunks remotos.

La única sustitución de archivo gestionado es `migaja-skin-bridge-1.0.1.jar` por `migaja-skin-bridge-1.0.2.jar` (SHA256 `94250668556e39290a4b315df834b3f6b6408be01ad46fe97a27de101035a8d6`). No se modifica el mundo, Pokémon, cuentas, casas, progreso, mapas explorados ni waypoints. No se añaden cambios a gráficos, sonido o teclas.

Pruebas: cliente y servidor aislados arrancados; mixins del mapa cargados; cuatro formatos de skin y cara opaca; getter nativo de World Map recibe la textura de Quick Skin sin entidad cercana; caché estable y cambio de skin invalida el icono; perfil efectivo de servidor con flecha y seguimiento activos; flecha roja vista dentro del mapa grande. Actualización HTTPS con unsup real y Windows PowerShell 5.1: 736 hashes, retiro del JAR viejo, 7 archivos personales de prueba conservados e idempotencia. No se presenta esto como una prueba humana simultánea de dos jugadores reales con skins distintas.

Quienes ya activaron el actualizador solo cierran Minecraft y pulsan **Jugar**. El paquete nuevo `Puchamon-Origins-1.0-Automatico.zip` sigue sirviendo: consulta la versión estable al arrancar. Quienes aún usan la antigua instancia autónoma deben activar el actualizador una sola vez, sin reinstalar ni borrar su instancia.
