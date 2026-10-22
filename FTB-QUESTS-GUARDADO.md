# Evitar bloqueos del actualizador al guardar FTB Quests

El incidente de auto.11 se reprodujo con los 23 capítulos que FTB Quests reescribió. Los JAR y otros 717 archivos administrados coincidían; no faltaba ningún archivo. Unsup conservaba su estado «actualizado» y no reparaba esas diferencias locales, mientras que el launcher mantenía correctamente el bloqueo por SHA-256.

## Corrección auto.12

Distribuir los capítulos en el formato del escritor real de FTB Quests 2101.1.36 y FTB Library 2101.1.36, en Minecraft 1.21.1/Fabric 0.19.5. No basta convertir JSON a SNBT ni cambiar los saltos de línea: el modelo de FTB normaliza defaults, tipos numéricos e ItemStacks.

Se cargó la guía publicada en un servidor Fabric local de laboratorio, se guardó con `ServerQuestFile.saveNow()`, se recargó y se guardó nuevamente. Los hashes de los 23 capítulos permanecieron idénticos. Una auditoría adicional confirmó las mismas 349 misiones/tareas y 245 recompensas, incluidos IDs, requisitos, cantidades y posición de nodos; las diferencias son defaults nativos y numeración de orden de capítulos. Esta comparación es una prueba de publicación, **no una excepción de validación en el cliente**.

El actualizador existente recuperó la instancia con las 23 diferencias, usando unsup real y Windows PowerShell 5.1, sin cambiar su `launch.jar`. Validó los 740 archivos, arrancó el JVM de diagnóstico y repitió el inicio correctamente. No se presentó este JVM como un arranque visual de Minecraft. Diez archivos testigo de preferencias, mapas, waypoints, skins y partidas quedaron intactos. No se modificó ningún dato de jugadores en producción durante esas pruebas.

## Para las próximas guías

1. Conservar todos los IDs existentes. Trabajar siempre en un clon de laboratorio, nunca en un mundo personal ni en producción.
2. Después de editar la guía o actualizar FTB Quests/FTB Library, cargarla y ejecutar guardado → recarga → guardado con esas versiones reales. Exigir los mismos hashes entre los dos guardados.
3. Exportar los capítulos resultantes y auditar requisitos, IDs y recompensas. Una prueba de sintaxis SNBT sola no sustituye esta prueba.
4. Crear `quest-format-proof.json` en la raíz de la release con el esquema de auto.12: versiones Minecraft/Fabric, hashes de los dos JAR escritores y de cada capítulo. No renovar los hashes del comprobante sin repetir la prueba nativa.
5. Ejecutar `assertNativeQuestFormat(releaseRoot)` de `quest-format-policy.mjs`, además de las pruebas de actualización e instalación. `promover-stable.mjs` ya exige ese comprobante; los scripts manuales de parche también deben llamarlo.
6. Publicar una release nueva e inmutable y coordinar su marcador exacto de servidor. No modificar retrospectivamente auto.11 ni reutilizar su compatibilidad.

La corrección evita este falso positivo concreto. No constituye una reparación genérica de cualquier archivo local alterado: se siguen rechazando JAR o datos administrados que no coincidan. Tampoco modifica las reglas de recompensas ni reinicia el progreso.

El launcher distribuido es Windows. Si se incorporan clientes de otros sistemas, repetir la validación del formato/saltos de línea en esos sistemas antes de darles soporte.
