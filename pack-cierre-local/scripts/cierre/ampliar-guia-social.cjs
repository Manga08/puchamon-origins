const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {parse}=require('./snbt.cjs');
const project=path.resolve(__dirname,'../..'),source=path.join(project,'prueba-social-r3/server/config/ftbquests/quests');
const out=path.join(project,'cierre-rc6/generado/quests'),ev=path.join(project,'cierre-rc6/evidencias');
fs.mkdirSync(out,{recursive:true});fs.mkdirSync(ev,{recursive:true});
const write=(p,v)=>{fs.mkdirSync(path.dirname(p),{recursive:true});fs.writeFileSync(p,JSON.stringify(v,null,2)+'\n');};
const load=p=>parse(fs.readFileSync(p,'utf8').replace(/^\uFEFF/,''));
const old=fs.readdirSync(source+'/chapters').filter(x=>x.endsWith('.snbt')&&!x.startsWith('7106')).map(x=>load(source+'/chapters/'+x));
for(const c of old){c.quests??=[];for(const q of c.quests){q.tasks??=[];q.rewards??=[];}}
const langs={};for(const locale of ['es_es','en_us'])langs[locale]=load(source+'/lang/'+locale+'.snbt');
const prefix='7106',hex=n=>prefix+n.toString(16).toUpperCase().padStart(12,'0');
const identityPath=project+'/guia-social-identidades.json';
const identities=fs.existsSync(identityPath)?JSON.parse(fs.readFileSync(identityPath,'utf8')):{schema:1,next:0,chapters:{},quests:{}};
// Adopt the IDs from the already-built first candidate, not new positional IDs.
if(!fs.existsSync(identityPath)&&fs.existsSync(out+'/chapters'))for(const file of fs.readdirSync(out+'/chapters')){
 const c=load(out+'/chapters/'+file);identities.chapters[langs.es_es['chapter.'+c.id+'.title']??JSON.parse(fs.readFileSync(out+'/lang/es_es.snbt','utf8'))['chapter.'+c.id+'.title']]=c.id;
 const l=JSON.parse(fs.readFileSync(out+'/lang/es_es.snbt','utf8'));
 for(const obj of [c,...c.quests,...c.quests.flatMap(q=>[...q.tasks,...q.rewards])])identities.next=Math.max(identities.next,parseInt(obj.id.slice(4),16));
 for(const q of c.quests)identities.quests[c.id+'::'+l['quest.'+q.id+'.title']]={id:q.id,taskId:q.tasks[0].id,rewards:q.rewards.map(r=>r.id)};
}
const nextId=()=>hex(++identities.next),chapters=[],quests=[];
function chapter(title,icon){const id=identities.chapters[title]??(identities.chapters[title]=nextId());const c={id,filename:id,group:'6111000000000003',order_index:3+chapters.length,icon:{id:icon},default_quest_shape:'rsquare',default_min_width:240,default_hide_dependency_lines:false,quests:[],quest_links:[],images:[]};chapters.push(c);for(const l of Object.values(langs))l['chapter.'+id+'.title']=title;return c;}
function q(c,title,lines,item,reward,x,y,dep){
 const identity=identities.quests[c.id+'::'+title]??(identities.quests[c.id+'::'+title]={id:nextId(),taskId:nextId(),rewards:[]});
 const id=identity.id,task={id:identity.taskId,type:item?'item':'checkmark'};
 if(item)Object.assign(task,{item:{id:item},count:1,consume_items:false,match_components:false});
 const rewards=reward?[{id:identity.rewards[0]??(identity.rewards[0]=nextId()),type:'item',item:{id:reward[0]},count:reward[1],team_reward:false,auto:'disabled'}]:[];
 const node={id,x,y,icon:{id:item||'minecraft:knowledge_book'},tasks:[task],rewards,dependencies:dep?[dep]:[],optional:true};
 if(!item)node.shape='gear';
 c.quests.push(node);quests.push({id,title,item,rewards,chapter:c.id,lines});
 for(const l of Object.values(langs)){l['quest.'+id+'.title']=title;l['quest.'+id+'.quest_desc']=[...lines,item?'Detección: tener el objeto indicado. No se consume; obtenerlo por intercambio también es válido.':'Explicación opcional: no da premio ni bloquea la aventura.',reward?'Premio único y personal. No se repite al reconectar.':'', 'No necesitas completar esta subguía para avanzar por los gimnasios.'].filter(Boolean).flatMap((s,i)=>i?['',s]:[s]);}
 return id;
}
const sport=chapter('Fútbol y fiestas','footblockultimate:football');
const ball=q(sport,'Footblock: empieza un partido',[
 'Busca @footblockultimate en REI. Obtén un balón y prepara un terreno plano, lejos de las pasturas, accesos y cofres públicos.',
 'Acércate al balón para conducirlo. Mantén clic izquierdo y suelta para tirar; clic derecho sirve para pasar. El balón especial World Cup añade su propia física: empieza por el normal.',
 'Prueba primero dos pases y un tiro corto. No confundas el balón con una Poké Ball ni dispares hacia un combate Pokémon.',
 'Usa el silbato para detener los balones cercanos. Deja libre el espacio de juego: las entidades y obstáculos pueden alterar el recorrido.'
],'footblockultimate:football',['minecraft:white_concrete',8],0,0);
q(sport,'Porterías y marcador, paso a paso',[
 'Obtén la línea de gol roja y consulta también la azul en REI. Coloca cada detector en su portería, siguiendo la flecha de orientación.',
 'Prueba un gol por cada lado antes de jugar. El marco sólido debe rodear la abertura, no tapar el detector.',
 'La consola de marcador permite gestionar el partido según los permisos del mod. No concede permisos de administrador de Minecraft.',
 'Al terminar, recoge balones sobrantes. Si no cuenta un gol, comprueba primero orientación y abertura, no coloques varias líneas encima.'
],'footblockultimate:red_goal_line',['minecraft:white_concrete',8],0,3,ball);
q(sport,'Confeti: celebrar y recoger',[
 'La búsqueda @confetti_stuff muestra el lanzador de mano, cañón, capa decorativa y escoba. El lanzador tiene retroceso; úsalo fuera de zonas de crianza.',
 'Un pulso breve de redstone activa un cañón. No conectes muchos a relojes rápidos: las partículas y proyectiles también consumen recursos.',
 'La escoba sirve para limpiar el confeti. Retira la decoración sobrante cuando termine la fiesta.',
 'No uses el empuje para mover Pokémon ajenos, sacar jugadores de zonas seguras o molestar a quien está iniciando sesión.'
],'confetti_stuff:party_popper',['minecraft:paper',8],4,0);
q(sport,'Beachparty: montar una zona de descanso',[
 'Empieza por una silla, sombrilla o toalla. Busca el objeto exacto en REI; que aparezca en creativo no significa que todos los artefactos tengan receta.',
 'Las palmeras, conchas y parte del contenido aparecen en playas de terreno nuevo. No hace falta borrar el mundo para buscar una playa no explorada.',
 'Coloca muebles dejando acceso al agua y al camino. Prueba la interacción con la silla antes de completar toda la terraza.',
 'Los accesorios y bebidas tienen efectos concretos; revisa su descripción. Decorar una playa no sustituye un centro Pokémon ni cura tu equipo.'
],'beachparty:beach_chair',['minecraft:oak_planks',12],-4,0);
q(sport,'Radio: música sin duplicaciones',[
 'Obtén una radio y un disco compatible. Colócala y usa sus controles; esta radio reproduce su contenido compatible, no enlaces arbitrarios de YouTube.',
 'Prueba volumen y alcance con otra persona. Para una base compartida es preferible una radio a varias superpuestas.',
 'Al parar, alejarte o retirar la radio comprueba que no quede audio duplicado. Si ocurre, informa del objeto y la acción exactos.',
 'Para tocar un instrumento portátil usa Immersive Melodies: es otro sistema distinto, no una función de la radio.'
],'beachparty:radio',['minecraft:copper_ingot',2],-4,3);
q(sport,'Convivencia durante eventos',[
 'Los partidos y fiestas son opcionales. No bloquees gimnasios, centros, cofres personales de Lootr o entradas de otras personas.',
 'Las minas no son confeti: aunque no destruyan terreno, pueden dañar jugadores y otras criaturas. La protección de Puchamon frena sus detonaciones cerca de Pokémon o jugadores sin autenticar, pero no sustituye una zona de pruebas segura.',
 'Para una celebración concurrida reduce los efectos si notas tirones. No actives shaders para todos ni cambies configuraciones ajenas.'
],null,null,4,3);

const images=chapter('Imágenes y decoración personal','immersive_paintings:painting');
const painting=q(images,'Immersive Paintings: tu primer cuadro',[
 'Busca @immersive_paintings en REI y obtén un cuadro. Colócalo en una pared despejada e interactúa para abrir su editor.',
 'Selecciona una imagen desde las opciones disponibles. Empieza por una imagen pequeña, sin datos privados y con permiso para compartirla.',
 'Ajusta el formato y el marco en el editor. Comprueba el resultado desde cierta distancia antes de llenar la pared.',
 'El servidor admite hasta 100 imágenes por jugador, importaciones de 2048×2048 y resolución máxima de cuadro 256. Son límites de protección, no una recomendación de llenarlos.'
],'immersive_paintings:painting',['minecraft:white_wool',4],0,0);
q(images,'Cuadros luminosos y grafitis',[
 'Consulta las variantes normales, luminosas y de grafiti en REI. Elige el formato según si buscas un cuadro enmarcado o una imagen integrada en una superficie.',
 'Usa las variantes luminosas con moderación y prueba el contraste de noche. El aspecto puede variar según el shader personal.',
 'No subas repetidamente la misma imagen para probar tamaños: reutiliza la entrada existente cuando el editor lo permita.',
 'Después de decorar, sal y vuelve a entrar. Si otra persona no ve la imagen, revisa que haya terminado la transferencia y conserva una captura del aviso.'
],'immersive_paintings:glow_painting',['minecraft:glow_ink_sac',1],0,3,painting);
q(images,'Compartir imágenes con cuidado',[
 'Las imágenes se comparten con quienes entren al servidor. No incluyas contraseñas, documentos personales ni fotografías privadas sin consentimiento.',
 'El acceso a subir imágenes está permitido a jugadores normales. El administrador puede limitarlo si se abusa del tamaño o del contenido.',
 'Los cuadros colocados y las imágenes almacenadas forman parte del respaldo del servidor. No borres sus carpetas para solucionar un problema visual.',
 'Las actualizaciones del pack no deben sustituir tus mapas, skins, gráficos ni audio. Informa de un fallo antes de reinstalar o borrar la instancia.'
],null,null,4,0);

const safe=chapter('Seguridad y convivencia','securitycraft:keypad_door');
const manual=q(safe,'SecurityCraft: propiedad y herramientas',[
 'Esta versión es un port Fabric parcial. Usa el manual y REI para ver lo que realmente existe; no asumas que un tutorial del SecurityCraft de Forge trae las mismas cámaras o torretas.',
 'Colocar un bloque protegido establece su propiedad según el mod. Prueba primero en una construcción tuya y confirma el acceso con otra cuenta.',
 'No refuerces ni reclames cofres Lootr, PC públicos, gimnasios ni bloques ajenos. Las protecciones son para tu base, no para impedir la progresión de otros.',
 'El reforzador modifica bloques compatibles y el removedor permite revertir los tuyos. Comprueba primero una pieza y conserva una salida de tu habitación.',
 'El mod incluye un Codebreaker para intentos de intrusión. Una cerradura no es invulnerabilidad absoluta: no uses las herramientas de intrusión contra bases ajenas sin consentimiento.'
],'securitycraft:universal_block_reinforcer_lvl1',['minecraft:iron_ingot',2],0,0);
q(safe,'Puerta con código: acceso sin quedar encerrado',[
 'Obtén una puerta con código. Colócala en una pared de prueba, configura su código y comprueba que abre y cierra antes de usarla como única entrada.',
 'Comparte el código únicamente con quien quieras autorizar. No uses la misma contraseña de tu cuenta ni la escribas en un cartel público.',
 'Pide a otra persona que pruebe el código correcto e incorrecto. El código no cambia quién es propietario del bloque.',
 'Usa las herramientas de cambio del mod para modificar el acceso. No concedas OP para abrir una puerta y no protejas una salida pública.'
],'securitycraft:keypad_door',['minecraft:redstone',4],0,3,manual);
q(safe,'Contenedor protegido no es Lootr',[
 'Un cofre con código guarda objetos de una base privada. Un cofre natural convertido por Lootr da un inventario personal a cada jugador: no son el mismo sistema.',
 'Construye tu contenedor privado desde cero y prueba el acceso antes de guardar objetos importantes.',
 'No intentes convertir, reforzar ni reclamar cofres de estructuras. Si encuentras un contenedor sin botín, informa de la estructura y posición antes de romperlo.',
 'Las correcciones de botín no vuelven a sortear automáticamente un cofre que ya abriste. No borres datos de Lootr ni objetos para forzar otro premio.'
],'securitycraft:keypad_chest',['minecraft:iron_ingot',2],-4,3,manual);
q(safe,'Minas: práctica solo en zona controlada',[
 'Consulta mina, minas camufladas, control remoto y herramienta de desactivación en REI y el manual. No coloques una mina hasta saber retirarla.',
 'La configuración reduce la explosión y desactiva fuego y destrucción de terreno. Eso no convierte los explosivos en decoración inofensiva.',
 'La protección de Puchamon frena las detonaciones de SecurityCraft si detecta Pokémon o jugadores sin autenticar en la zona de 16 bloques alrededor. No modifica explosiones vanilla, combates Pokémon ni otros peligros como láseres o lava falsa.',
 'No confíes en creativo para probar daño: el comportamiento al romper en creativo es distinto. Usa una zona vacía y una cuenta de prueba.',
 'Practica de todos modos lejos de pasturas, caminos y cofres públicos. No se regalan explosivos como premio. No uses bombas para acosar ni bloquear un inicio de sesión.'
],'securitycraft:wire_cutters',['minecraft:iron_ingot',2],4,3,manual);
q(safe,'Tu cuenta y tus regresos',[
 'Regístrate o inicia sesión antes de usar Pokémon, inventarios y comandos. No compartas la contraseña ni la repitas en el chat normal.',
 'Guarda una casa con /home set nombre; vuelve con /home tp nombre. Hay dos casas y preparación de 2 segundos en este perfil.',
 '/tpa nombre solicita ir hacia otra persona. /tpaccept y /tpdeny gestionan la solicitud; no tienes permiso para arrastrar a otro jugador con /tpahere.',
 'Un waypoint marca un destino; no concede teletransporte de administrador. Si mueres y al reconectar apareces en un lugar incorrecto, comunica la hora y no borres tu perfil.'
],null,null,-4,0);

// Use native quest links to existing new quests, without duplicating tasks/rewards.
for(const [index,c] of chapters.entries())c.autofocus_id=c.quests[0].id;
const main=old.find(c=>c.group==='6111000000000001');assert(main);
const welcome=main.quests.find(q=>langs.es_es['quest.'+q.id+'.title']==='Bienvenido a Puchamon: Origins');assert(welcome);
const text=langs.es_es['quest.'+welcome.id+'.quest_desc'];
const extra='También tienes subguías opcionales de Fútbol y fiestas, Imágenes y decoración personal, y Seguridad y convivencia en «Tu base y comodidad».';
if(!text.includes(extra)){for(const l of Object.values(langs)){l['quest.'+welcome.id+'.quest_desc'].push('',extra);}}
const all=[...old,...chapters];const ids=new Set();let total=0,rewarded=0,manualRewards=0;const budget={};const refs=new Set();
for(const c of all){
 const nodes=[...c.quests,...(c.quest_links||[])];
 for(let i=0;i<nodes.length;i++)for(let j=i+1;j<nodes.length;j++){const a=nodes[i],b=nodes[j],bound=((a.size||1)+(b.size||1))/2+0.3;assert(!(Math.abs(a.x-b.x)<bound&&Math.abs(a.y-b.y)<bound),'Layout overlap '+c.id);}
 for(const obj of [c,...c.quests,...(c.quest_links||[]),...c.quests.flatMap(q=>[...q.tasks,...q.rewards])]){assert(!ids.has(obj.id),'Duplicate ID '+obj.id);ids.add(obj.id);}
 refs.add('item\t'+c.icon.id);
 for(const q of c.quests){total++;if(q.rewards.length)rewarded++;if(q.tasks.some(t=>t.type==='checkmark')&&q.rewards.length)manualRewards++;
  refs.add('item\t'+q.icon.id);for(const t of q.tasks){if(t.item)refs.add('item\t'+t.item.id);if(t.advancement)refs.add('adv\t'+t.advancement);}
  for(const r of q.rewards){assert(!q.repeatable);assert.equal(r.team_reward,false);refs.add('item\t'+r.item.id);if(chapters.includes(c))budget[r.item.id]=(budget[r.item.id]||0)+r.count;}
 }
}
assert.equal(manualRewards,0);
for(const c of chapters)write(out+'/chapters/'+c.id+'.snbt',c);
for(const [locale,data] of Object.entries(langs))write(out+'/lang/'+locale+'.snbt',data);
write(ev+'/guia-social-validacion.json',{chapters:all.length,quests:total,rewarded,informative:total-rewarded,addedQuests:quests.length,existingQuestsPreserved:old.reduce((s,c)=>s+c.quests.length,0),oldChapterFilesChanged:0,manualRewards,overlaps:0,uniqueRewardBudget:budget});
write(ev+'/guia-social-catalogo.json',quests);fs.writeFileSync(ev+'/referencias.tsv',[...refs].sort().join('\n')+'\n');
write(identityPath,identities);
console.log(fs.readFileSync(ev+'/guia-social-validacion.json','utf8'));
