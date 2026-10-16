const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {parse}=require('../cierre/snbt.cjs');
const project=path.resolve(__dirname,'../..'),root=project+'/pulido-rc7/idiomas-20261016';
const read=p=>JSON.parse(fs.readFileSync(p,'utf8').replace(/^\uFEFF/,''));
const audit=read(root+'/detalle-idiomas.json'),dict=read(__dirname+'/es-contexto-20261016.json');
const out=root+'/generado';fs.mkdirSync(out,{recursive:true});
const tokens=s=>[...s.matchAll(/%(?:(\d+)\$)?([sd])|\$\{[^}]+\}|%%|§[0-9a-fklmnor]/gi)].map(m=>m[0]).sort();
const overlays={};
for(const [ns,translations]of Object.entries(dict)){
 const lang={};for(const [key,en]of Object.entries(audit[ns].english)){
  assert(Object.hasOwn(translations,en),'Falta traducción contextual '+ns+': '+en);
  lang[key]=translations[en];assert.deepEqual(tokens(lang[key]),tokens(en),'Parámetros '+key);
  assert(!/[A-Za-zÁÉÍÓÚáéíóúñÑ]\?[A-Za-zÁÉÍÓÚáéíóúñÑ]|\uFFFD/.test(lang[key]),'Texto dañado '+key);
 }overlays[ns]=lang;
}
// Reviewed fixes: native Spanish incorrectly lost or added printf arguments. Do not rename species/items.
const cobble=overlays.cobblemon=read(__dirname+'/cobblemon-correcciones-20261016.json');
for(const [key,es]of Object.entries(cobble))assert.deepEqual(tokens(es),tokens(audit.cobblemon.english[key]),key);
overlays.comforts={'comforts.networking.failed':'Error de conexión de Comforts: %s'};
overlays.ftbteams={'ftbteams.party_api_only':'¡En este modpack no puedes crear equipos de grupo directamente!\n%s'};
overlays.waystones={'container.waystones.waystone_settings':'Editar %s'};
for(const [ns,lang]of Object.entries(overlays)){
 const dir=out+'/assets/'+ns+'/lang';fs.mkdirSync(dir,{recursive:true});
 for(const locale of ['es_es','es_mx','es_ar','es_cl','es_ec','es_uy','es_ve'])fs.writeFileSync(dir+'/'+locale+'.json',JSON.stringify(lang,null,2)+'\n');
}
const tips={
 '2211000000000008':['Si empiezas de cero: un Pokémon es un compañero de combate, no una armadura para tu personaje. Su nivel, vida (PS), movimientos, tipos y habilidades pertenecen al Pokémon; tus corazones y tu experiencia de Minecraft son otros sistemas.','Consejo: elige el inicial que te guste y captura compañeros que cubran sus debilidades. No necesitas conocer IV ni criar un equipo perfecto para empezar la aventura.'],
 '221100000000000B':['Consejo de captura: lleva un ataque débil para bajar los PS sin debilitar al objetivo. Si llega a cero no podrás capturarlo en ese combate. Sueño o parálisis pueden ayudar, pero ninguna de estas medidas garantiza una captura.','Un variocolor (shiny) tiene un color poco habitual: no es automáticamente más fuerte ni exige formar parte de tu equipo principal.'],
 '221100000000000E':['Consejo: llena las seis plazas cuando te resulte útil, no por obligación antes del primer gimnasio. Prueba dos o tres compañeros con respuestas distintas; puedes cambiar tu equipo usando las cajas del PC.'],
 '221100000000001A':['Antes de una expedición: recupera PS y comprueba los PP (usos restantes) de tus ataques. Una poción no sustituye a un objeto para recuperar PP ni revive por sí sola a un Pokémon debilitado.'],
 '2211000000000023':['Consejo: si dejas de ganar niveles, consulta primero tu tarjeta y el siguiente líder. En esta progresión existe un límite personal; combatir más no siempre permite superarlo. No confundas ese límite con la experiencia de Minecraft.'],
 '2211000000000272':['Plan sencillo para tu primer gimnasio: prepara al menos una respuesta Agua o Planta, otra opción por si la primera cae y recursos de recuperación. Revisa también los tipos del movimiento: el color o el aspecto del Pokémon no determina qué ataque está usando.','Si pierdes: identifica qué Pokémon rival te frenó y por qué. Cambiar un movimiento o un compañero suele ayudar más que repetir exactamente la misma estrategia.'],
 '22110000000002B7':['Ejemplo práctico: un ataque Eléctrico no afecta normalmente a un Pokémon de tipo Tierra. Usar un Pokémon de mayor nivel no elimina esa inmunidad. Prueba un movimiento de otro tipo o cambia de compañero.','Cobertura significa llevar ataques que te permitan responder a distintos tipos; no necesitas memorizar la tabla entera. Consulta aquí la ficha del tipo del rival cuando tengas dudas.'],
 '22110000000002D3':['Ejemplo de turno: si cambias a un compañero resistente, puede recibir el ataque rival al entrar. Una buena resistencia o inmunidad puede compensar ese turno; cambiar sin mirar los tipos puede debilitar a otro compañero.','No confundas una habilidad con un movimiento: la habilidad es una regla propia del Pokémon, no un quinto botón de ataque. Lee su descripción, porque puede cambiar el resultado de una estrategia.'],
 '22110000000002D5':['Consejo: si tu Pokémon destaca en Ataque Especial, un movimiento especial suele aprovecharlo mejor que uno físico equivalente. Esto no significa que debas borrar toda cobertura física: compara la situación y el rival.','Un ataque con menos potencia pero buena precisión puede ser más fiable. Ser más rápido tampoco asegura actuar primero: existen movimientos con prioridad y otras reglas del combate.'],
 '22110000000002D7':['Al capturar un Pokémon raro, el daño continuado del veneno o la quemadura puede debilitarlo antes de que lo atrapes. Valora usar sueño o parálisis y un ataque controlado. Las inmunidades y habilidades pueden impedir un estado.'],
 '22110000000002D9':['Prioridad para principiantes: entender los tipos → elegir buenos movimientos → nivel y recuperación → funciones del equipo. Optimizar naturaleza, EV e IV viene después; no necesitas perfección competitiva para disfrutar el progreso.','Glosario: IV = potencial individual de cada estadística; EV = entrenamiento acumulado; naturaleza = modificador de estadísticas; habilidad = efecto propio; objeto equipado = apoyo que lleva el Pokémon. Un ejemplar variocolor no garantiza mejores valores.'],
 '2211000000000143':['Consejo: primero decide si usarás ataques físicos o especiales. Evita gastar una menta rara hasta comprender qué estadísticas beneficia y perjudica; no existe una naturaleza ideal para todos los Pokémon.']
};
const source=project+'/prueba-social-r3/server/config/ftbquests/quests';
const chapters=fs.readdirSync(source+'/chapters').filter(f=>f.endsWith('.snbt')).map(f=>parse(fs.readFileSync(source+'/chapters/'+f,'utf8')));
const quests=new Map(chapters.flatMap(c=>c.quests).map(q=>[q.id,q]));
for(const locale of ['es_es','en_us']){
 const lang=parse(fs.readFileSync(source+'/lang/'+locale+'.snbt','utf8'));
 for(const [id,paragraphs]of Object.entries(tips)){
  assert(quests.has(id),'Misión desconocida '+id);const key='quest.'+id+'.quest_desc';assert(Array.isArray(lang[key]),key);
  let lines=lang[key];let at=lines.findIndex(s=>/^Detección|^Lectura informativa/.test(s));if(at<0)at=lines.length;
  for(const text of paragraphs)if(!lines.includes(text)){lines.splice(at,0,text,'');at+=2;}
  lang[key]=lines;
 }
 const dir=out+'/quests/lang';fs.mkdirSync(dir,{recursive:true});fs.writeFileSync(dir+'/'+locale+'.snbt',JSON.stringify(lang,null,2)+'\n');
}
const result={namespaces:Object.fromEntries(Object.entries(overlays).map(([n,v])=>[n,Object.keys(v).length])),translatedKeys:Object.values(overlays).reduce((n,v)=>n+Object.keys(v).length,0),questDescriptions:Object.keys(tips).length,chapters:chapters.length,quests:quests.size,changedObjectives:0,changedRewards:0,changedPositions:0,validation:'Static JSON, formatting tokens and mission identity checked. Effective in-game language order and layout not re-tested in this round.'};
fs.writeFileSync(root+'/correcciones-contextuales.json',JSON.stringify(result,null,2)+'\n');console.log(JSON.stringify(result,null,2));
