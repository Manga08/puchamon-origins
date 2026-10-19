# Skin Map Bridge 1.0.4 · auto.8

Fuentes del puente Fabric 1.21.1 publicado con auto.8. Reemplaza 1.0.3 en cliente y servidor; no instalar ambas versiones activas.

## Cambios

- Conserva únicamente el saludo tipado de Xaero Minimap (canal `xaerominimap:main`, paquete 1, protocolo 3), hasta autenticar con EasyAuth. Cola acotada a 64 conexiones y 180 segundos; se reproduce una vez en el manejador nativo, en la misma conexión. No permite acciones ni otros paquetes antes del login.
- Conserva la negociación diferida de Quick Skin y Simple Voice Chat.
- Cara cercana 8×8 y escala 1/8 del lienzo lógico fijo de Xaero. No cambia los iconos Pokémon ni el atlas de seguimiento lejano.
- Conserva nombres, flecha propia, mapas, waypoints y preferencias.

Seguimiento nativo dentro de la misma dimensión, sin cargar chunks remotos. La prueba a 10.000 bloques es una distancia de ensayo, no un radio de detección Pokémon ni un límite configurado.

## Validación de esta compilación

Minecraft 1.21.1, Fabric 0.19.5, Quick Skin 3.1.1, EasyAuth 3.4.4, Xaero Minimap 26.6.0, World Map 1.47.0 y XaeroLib 1.7.3. Actualizar esas APIs exige revisar y volver a probar.

Pruebas locales nativas: rechazo previo al login, reproducción única posterior, rechazo de otro protocolo y de paquetes que no son el saludo; emisión nativa de seguimiento de un jugador sintético a 10.000 bloques. Render real de Xaero en GPU y lectura del atlas: cara 8×8. Pruebas de formatos de skin, caché, ausencia de entidad cercana, Tab/V y reintento de rescate.

La prueba a distancia no fue una sesión de dos jugadores humanos. No distribuir agentes ni fixtures de diagnóstico. Solo se empaqueta código propio, sin dependencias ni cuentas.

JAR SHA-256: `ad3ee2d24a5f1821828383a7d3f56b7d5862c0b2bebc90d57ee0cdb26e6c4588`.
