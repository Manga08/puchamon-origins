# Puchamon Mine Safety 1.0.0

Servidor Fabric 1.21.1; fijado a Cobblemon 1.7.3, SecurityCraft Fabric 0.6.1 y EasyAuth 3.4.4. Código propio, licencia MIT. No editar ni redistribuir un JAR de SecurityCraft parcheado.

Antes de detonar, revisa la zona de 16 bloques alrededor. Si hay una entidad PokemonEntity (propia o salvaje) o un ServerPlayerEntity que EasyAuth aún no considera autenticado, detiene la detonación. La mina de bloque queda colocada; una Bouncing Betty o IMS ya lanzada se retira sin explosionar. No borra Pokémon ni modifica sus combates. No hay comando, nueva tecla, manejador de tick permanente ni escaneo global del mundo. Una mina intentada repetidamente hace una consulta local en cada intento: no se certifica rendimiento con centenares de minas.

Intercepta los cinco puntos de entrada de explosivos de bloque (BaseFullMineBlock y sus variantes heredadas, MineBlock, ClaymoreBlock, BouncingBettyBlock, TrackMineBlock), la detonación de la entidad BouncingBetty y el impacto de IMSBomb. Mixins obligatorios con requisito 1: cambiar las versiones exige nueva prueba, no ignorar un fallo de mixin.

Es protección de **detonaciones de SecurityCraft**, no inmunidad general. No cambia TNT/creepers, daño ambiental, láseres, lava falsa ni permisos de propiedad. Tampoco sustituye moderación o protección de zonas públicas. En una zona vacía las minas conservan su función y el ajuste original sin destrucción de terreno.

Pruebas nativas y fuentes del ensayo: `scripts/cierre/qa/`. Ese mod QA solo puede arrancar en la copia `cierre-rc6/server`; **no distribuirlo**. Se verificaron cinco explosivos de bloque, las dos entidades, salud del Pokémon, predicado nativo de jugador sin autenticar y detonación normal sin romper terreno cercano. Eso no es una partida humana de dos jugadores.
