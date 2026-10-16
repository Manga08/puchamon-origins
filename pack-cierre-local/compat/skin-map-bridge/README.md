# Migaja Skin / Map Bridge — Fabric 1.21.1

Puente original: cliente (caras de Xaero) y servidor (inicio diferido de Quick Skin después de EasyAuth). No contiene código copiado de Quick Skin, ni modifica sus archivos/imágenes. No es el JAR NeoForge/JourneyMap de Migajaland.

## Versiones probadas

MC 1.21.1, Fabric Loader 0.18.4, Quick Skin 3.1.1, EasyAuth 3.4.4, Xaero Minimap 26.6.0 y World Map 1.47.0. El puente comprueba las versiones de las APIs específicas y falla explícitamente ante otra versión no validada. Cambiar estas dependencias exige recompilar/probar; no se promete compatibilidad futura sin revisión.

## Seguridad del login

EasyAuth sigue rechazando los paquetes previos al login. Solo se conserva el último `quickskin:protocol_hello` de cada conexión (incluido el envoltorio Fabric de Architectury), hasta 64 jugadores, 64 bytes por saludo y 180 segundos. No se guardan subidas de texturas, acciones Pokémon, comandos, recompensas ni movimiento. Tras autenticarse y mientras sea la MISMA conexión, se reproduce una vez el saludo en el manejador original de Quick Skin: conserva sus comprobaciones de versión, nonce y límites. Conexiones cerradas/caducadas se descartan. No se habilita `allow-all-packets` ni `architectury:network` genéricamente.

## Cabezas y memoria

El renderizador de jugadores rastreados recibe la skin del UUID de Quick Skin, incluso sin entidad cercana. Se normalizan exclusivamente los dos cuadrados de cara/sombrero al atlas de 64 píxeles que Xaero usa; se conservan alfa y proporciones. El radar diferencia en su caché la textura de Quick Skin para actualizar la cabeza al cambiar de skin.

La caché propia admite como máximo 128 atlas pequeños, reutiliza entradas y libera sus texturas al desconectarse. No altera tamaños/opacidad/visibilidad del radar, no revela jugadores fuera de los permisos del mapa y no escribe `xaero`, mapas ni waypoints. Si aún no está disponible la skin, usa el camino nativo de Xaero y reintenta después.

No sustituye la autenticación ni la cuenta: el servidor offline sigue identificando nombres con mayúsculas exactas. Una skin no verifica la identidad de una cuenta.

## Compilar y probar

`scripts/compilar-puente-skins.ps1` produce el JAR; `-Install` lo copia solo en las dos rutas locales de prueba. Requiere cerrar las instancias antes de instalar. Las clases Minecraft están en namespace intermediary y se compilan contra los JAR exactos; no se empaquetan dependencias.

Pruebas locales: saludo bloqueado antes y negociado después del login; textura Quick Skin = PlayerInfo; getter real de Xaero con entidad y sin ella; reutilización de caché; UV/alfa de 64×64, 64×32, 128×128 y 256×128. Antes de una publicación definitiva, probar también dos clientes reales con skins diferentes y cambio de skin durante la sesión.

El mod se distribuye a clientes y servidor como añadido propio. El servidor no carga las clases gráficas. No distribuir los agentes de diagnóstico como mods.
