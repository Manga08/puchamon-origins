const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {parse}=require('../cierre/snbt.cjs');
const project=path.resolve(__dirname,'../..');
const source=project+'/prueba-social-r3/server/config/ftbquests/quests';
const out=project+'/pulido-rc7/generado/quests';
const id='2211000000000017';
const chapter=parse(fs.readFileSync(source+'/chapters/2111000000000001.snbt','utf8'));
const q=chapter.quests.find(q=>q.id===id);
assert.equal(q.tasks[0].advancement,'cobblemon:obtain_berry');
assert.equal(q.tasks[0].id,'3111000000000018');
for(const locale of ['es_es','en_us']){
 const lang=parse(fs.readFileSync(source+'/lang/'+locale+'.snbt','utf8'));
 assert(['Cultiva bayas','Tus primeras bayas'].includes(lang[`quest.${id}.title`]));
 lang[`quest.${id}.title`]='Tus primeras bayas';
 lang[`quest.${id}.quest_desc`]=[
  'Consigue tu primera baya. Valen las del kit inicial, una baya silvestre o una recibida por intercambio. Este objetivo comprueba obtenerla, no plantarla ni cosecharla tú.',
  '',
  'Guarda algunas para cultivar: planta una baya en un suelo compatible y deja espacio para el arbusto. Consulta en REI los usos y la información de cada especie de baya.',
  '',
  'Oran ayuda a recuperar PS; no todas las bayas curan. Algunas reducen EV o afectan otros sistemas. Lee el efecto antes de dársela a un Pokémon.',
  '',
  'Detección automática: avance cobblemon:obtain_berry. No hay casilla manual ni se exige fabricar o plantar una baya.',
  '',
  'Recompensa única y personal: 8 unidades de polvo de hueso. No se repite al reconectar.'
 ];
 fs.mkdirSync(out+'/lang',{recursive:true});
 fs.writeFileSync(out+'/lang/'+locale+'.snbt',JSON.stringify(lang,null,2)+'\n');
}
fs.mkdirSync(project+'/pulido-rc7/evidencias',{recursive:true});
fs.writeFileSync(project+'/pulido-rc7/evidencias/guia-pulido.json',JSON.stringify({questId:id,taskId:q.tasks[0].id,rewardId:q.rewards[0].id,advancement:q.tasks[0].advancement,chapterFilesChanged:0,title:'Tus primeras bayas'},null,2));
console.log('Guía: título y explicación corregidos, sin cambiar IDs, tareas, premios ni capítulos.');
