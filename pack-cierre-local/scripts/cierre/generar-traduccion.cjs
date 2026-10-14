const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const project=path.resolve(__dirname,'../..'),root=path.join(project,'cierre-rc6');
const read=p=>JSON.parse(fs.readFileSync(p,'utf8').replace(/^\uFEFF/,''));
const resources=read(root+'/evidencias/lenguajes-sociales.json'),own=read(__dirname+'/traducciones-propias.json'),official=read(root+'/fuentes-securitycraft/es_es.json');
const out=root+'/generado/traduccion';fs.mkdirSync(out,{recursive:true});
const result=[],missing=[],tokens=s=>[...s.matchAll(/\$\{[^}]+\}|%(?:\d+\$)?[sd]/g)].map(m=>m[0]).sort();
const untranslatedSongs=new Set(['Kevin MacLeod - Beachparty','Pecan Pie - Caribbean Beach','Andey Rossi - Pridelands','Pecan Pie - Vocalista','Adams - Wild Veins','MissLilitu - Over the Rainbow']);
for(const r of resources.filter(r=>own[r.ns])){
 const lang={};for(const [key,english] of Object.entries(r.english)){
  let spanish=own[r.ns][english]??(r.ns==='securitycraft'?official[key]:undefined);
  if(spanish===undefined&&untranslatedSongs.has(english))spanish=english;
  if(spanish===undefined&&r.spanish&&Object.hasOwn(r.spanish,key))continue;
  if(spanish===undefined){missing.push({ns:r.ns,key,english});continue;}
  assert.deepEqual(tokens(spanish),tokens(english),'Placeholder mismatch '+key);
  assert(!spanish.includes('\uFFFD'),'Replacement glyph '+key);lang[key]=spanish;
 }
 const folder=out+'/assets/'+r.ns+'/lang';fs.mkdirSync(folder,{recursive:true});for(const locale of ['es_es','es_mx','es_ar','es_cl','es_ec','es_uy','es_ve'])fs.writeFileSync(folder+'/'+locale+'.json',JSON.stringify(lang,null,2)+'\n');
 const effective={...(r.spanish||{}),...lang};assert(Object.keys(r.english).every(k=>Object.hasOwn(effective,k)),'Incomplete effective Spanish '+r.ns);
 result.push({namespace:r.ns,englishKeys:Object.keys(r.english).length,translatedKeys:Object.keys(lang).length,nativeSpanishKeysPreserved:Object.keys(r.spanish||{}).length});
}
if(missing.length){console.log(JSON.stringify(missing,null,2));throw Error('Untranslated keys '+missing.length);}
fs.writeFileSync(out+'/fabric.mod.json',JSON.stringify({schemaVersion:1,id:'puchamon_translations',version:'1.0.0',name:'Puchamon: Origins - Español',description:'Traducciones y complemento de idioma. Sin controles ni cambios de gráficos.',environment:'*',license:'MIT',depends:{fabricloader:'>=0.19.5',minecraft:'1.21.1'}},null,2));
fs.copyFileSync(root+'/fuentes-securitycraft/LICENSE',out+'/LICENSE-SecurityCraft');
fs.copyFileSync(root+'/fuentes-securitycraft/origen.json',out+'/ATRIBUCION-SecurityCraft.json');
fs.writeFileSync(out+'/CREDITOS.txt','Traducción SecurityCraft: proyecto SecurityCraft (MIT), origen/commit en ATRIBUCION-SecurityCraft.json. Solo claves presentes en el port instalado. Resto: traducción propia Puchamon. Nombres de mods, artistas y canciones conservados.\n');
fs.writeFileSync(root+'/evidencias/traduccion-validacion.json',JSON.stringify({coverage:result,missingKeys:0,placeholderChecks:'pass',nativeSpanishPreserved:['footblockultimate','immersive_paintings']},null,2)+'\n');console.log(JSON.stringify(result));
