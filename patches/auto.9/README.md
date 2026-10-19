# auto.9 · Tumbas en Puchamon

You’re in Grave Danger Fabric 2.4.18 sustituye Hardcore Revival y Lenient Death. La recuperación se realiza con clic derecho, con protección del propietario y sin caducidad automática. Los respaldos tienen el límite nativo de 100 por jugador. Sin recompensas por morir, teclas nuevas ni teletransporte gratuito a tumbas.

Se conserva la guía de 349 misiones y se sustituyen los seis textos de rescate por la explicación de tumbas, con los mismos identificadores. 271 claves de traducción revisadas, formatos comprobados y carga nativa en español verificada en el cliente.

Pruebas de servidor con jugadores sintéticos, usando los manejadores nativos: creación de tumba, protección frente a otro UUID, guardado/carga NBT y recuperación con una nueva instancia del mismo jugador, mochila con 17 esmeraldas, cinturón equipado con 64 Poké Balls, ranura cosmética, armadura, mano secundaria y 160 puntos de experiencia; no sobrescribe objetos actuales ni permite cobrar la misma tumba dos veces. No es una prueba de reconexión de dos clientes humanos. Se debe vigilar especialmente la sincronización visual de Accessories, por reportes de terceros con esa versión; usar clic derecho, no romper la tumba.

Puente 1.0.5: únicamente retira RevivalRetryMixin; conserva las correcciones de mapa, skins y autenticación de auto.8. Friends 1.0.2 retira su dependencia de Hardcore Revival, conserva autenticación y comprobaciones de vida para /tpa y /home. Nunca instalar ambas versiones activas de estos complementos.

Los cambios de esta versión no afectan mundos, cuentas, progreso de misiones, mapas, waypoints ni preferencias gráficas/audio. Los agentes de diagnóstico y las cuentas no se distribuyen. Las fuentes propias actualizadas se incluyen en compat/.

Fuente del mod: https://modrinth.com/mod/yigd/version/T3grMjgj (Fabric 1.21–1.21.1). Distribución mediante su CDN oficial. No se modifica ni recompila el JAR de terceros.
