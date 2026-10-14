import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {fileURLToPath} from 'node:url';
import {parseArgs} from 'node:util';
const base=path.dirname(fileURLToPath(import.meta.url)),project=process.env.PUCHAMON_PROJECT||path.dirname(base);
const {values}=parseArgs({options:{release:{type:'string',default:'0.1.0-rc1'},'base-url':{type:'string',default:'http://127.0.0.1:8765/'},channel:{type:'string',default:'test'}}});
const release=values.release,url=new URL(values['base-url']);
if(!/^[a-z0-9][a-z0-9._-]+$/.test(release)||!['test','stable'].includes(values.channel))throw Error('Invalid release/channel');
if(url.protocol!=='https:'&&!['localhost','127.0.0.1'].includes(url.hostname))throw Error('HTTPS required outside localhost');
if(!url.pathname.endsWith('/'))url.pathname+='/';
const client=process.env.PUCHAMON_CLIENT||path.join(process.env.APPDATA||'', 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft');
const server=process.env.PUCHAMON_SERVER||path.join(project,'prueba-social-r3/server'),site=path.join(base,'site'),dest=path.join(site,'releases',release);
if(fs.existsSync(dest))throw Error('Immutable release already exists: '+release);
const sha=b=>crypto.createHash('sha256').update(b).digest('hex');
const load=p=>JSON.parse(fs.readFileSync(p,'utf8').replace(/^\uFEFF/,''));
const write=(p,b)=>{fs.mkdirSync(path.dirname(p),{recursive:true});fs.writeFileSync(p,b);};
const walk=d=>!fs.existsSync(d)?[]:fs.readdirSync(d,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(d,e.name)):e.isFile()?[path.join(d,e.name)]:[]);
const relative=(root,file)=>path.relative(root,file).replaceAll('\\','/');
const sources=new Map();
for(const f of load(path.join(base,'build/upstream-index.json')).files)if(f.hashes.sha256&&f.downloads?.[0]?.startsWith('https://'))sources.set(f.hashes.sha256,f.downloads[0]);
for(const v of Object.values(load(path.join(base,'cache/modrinth-hashes.json'))))for(const f of v.files||[])if(f.hashes?.sha512){
 const current=fs.existsSync(path.join(client,'mods',f.filename))?path.join(client,'mods',f.filename):null;
 if(current){const b=fs.readFileSync(current);if(crypto.createHash('sha512').update(b).digest('hex')===f.hashes.sha512)sources.set(sha(b),f.url);}
}
const licenseReview=[];
function blob(file){const b=fs.readFileSync(file),hash=sha(b),name=hash+path.extname(file),target=path.join(site,'blobs',name);if(!fs.existsSync(target))write(target,b);return {url:new URL('blobs/'+name,url).href,sha256:hash};}
function origin(file){const b=fs.readFileSync(file),hash=sha(b);if(sources.has(hash))return {url:sources.get(hash),sha256:hash};licenseReview.push(relative(project,file));return blob(file);}
const shared=['config/migaja-controls.properties','config/securitycraft-server.json','config/immersive_paintings/common_config.toml','config/global_packs.toml'];
function allowed(p){return /^mods\/[^/]+\.jar$/.test(p)||p.startsWith('datapacks/')||p.startsWith('config/ftbquests/quests/')||shared.includes(p);}
const managed=[...fs.readdirSync(path.join(client,'mods'),{withFileTypes:true}).filter(e=>e.isFile()&&e.name.endsWith('.jar')).map(e=>path.join(client,'mods',e.name)),...walk(path.join(client,'datapacks')),...walk(path.join(client,'config/ftbquests/quests')),...shared.filter(p=>fs.existsSync(path.join(client,p))).map(p=>path.join(client,p))];
const gate=path.join(base,'runtime/puchamon-gate-1.0.0.jar');
const files=[],index=[];
function add(file,p){
 if(!allowed(p))throw Error('Protected path: '+p);
 const bytes=fs.readFileSync(file),hash=sha(bytes);files.push({path:p,sha256:hash});
 if(p.endsWith('.jar')&&p.startsWith('mods/')){
  const source=origin(file),meta=p+'.pw.toml';
  const text=`name = ${JSON.stringify(path.basename(p))}\nfilename = ${JSON.stringify(path.basename(p))}\nside = "client"\n\n[download]\nurl = ${JSON.stringify(source.url)}\nhash-format = "sha256"\nhash = "${hash}"\n`;
  write(path.join(dest,'client',meta),text);index.push({file:meta,hash:sha(text),metafile:true});
 }else{write(path.join(dest,'client',p),bytes);index.push({file:p,hash,metafile:false});}
}
for(const f of managed)add(f,relative(client,f));add(gate,'mods/puchamon-gate-1.0.0.jar');
const indexText='hash-format = "sha256"\n'+index.map(e=>`\n[[files]]\nfile = ${JSON.stringify(e.file)}\nhash = "${e.hash}"\nmetafile = ${e.metafile}\n`).join('');
write(path.join(dest,'client/index.toml'),indexText);
const pack=`name = "Puchamon: Origins"\nauthor = "Puchamon"\nversion = "${release}"\npack-format = "packwiz:1.1.0"\n\n[index]\nfile = "index.toml"\nhash-format = "sha256"\nhash = "${sha(indexText)}"\n\n[versions]\nminecraft = "1.21.1"\nfabric = "0.19.5"\n`;
write(path.join(dest,'client/pack.toml'),pack);
const defaultsMap=new Map();
for(const file of walk(path.join(base,'build/upstream-defaults')))defaultsMap.set(relative(path.join(base,'build/upstream-defaults'),file),file);
for(const folder of ['resourcepacks','shaderpacks','emotes'])for(const f of walk(path.join(client,folder)))if(!f.endsWith('.txt')&&!f.endsWith('.rpo'))defaultsMap.set(relative(client,f),f);
// Deliberately selected settings only. Never clone config/** from a played instance.
for(const folder of ['defaultoptions','fancymenu','inventoryprofilesnext'])for(const f of walk(path.join(client,'config',folder)))if(!/backup|\.done$|\.log$|profiles|bookmarks|last/i.test(f))defaultsMap.set(relative(client,f),f);
for(const name of ['ftbteams-server.snbt','ftbultimine-server.snbt','lootr.json','fightorflight.json5','fightorflight_moves.json5','sophisticatedbackpacks-common.toml','sophisticatedstorage-common.toml','trashslot-common.toml','global_packs.toml']){
 const file=path.join(client,'config',name);if(fs.existsSync(file))defaultsMap.set('config/'+name,file);
}
const defaults=[];
for(const [p,file] of defaultsMap){
 if(allowed(p)||/quickskin|account|password|\.db|fingerprint|\.done$/i.test(p))continue;
 if(p.includes('xaero')&&!/^config\/xaero\/(lib|minimap|world-map)\/[a-zA-Z0-9_./-]+\.(cfg|json|txt)$/.test(p))continue;
 const b=fs.readFileSync(file);if(b.length<3e6&&/C:[/\\]Users[/\\]|181\.54\.|Manga08|SkinTest10/i.test(b.toString('utf8')))throw Error('Potential personal data in initial default: '+p);
 defaults.push({path:p,...origin(file)});
}
const keyDefaults=fs.readFileSync(path.join(client,'config/defaultoptions/keybindings.txt'),'utf8').split(/\r?\n/).filter(x=>/^key_[^:]+:key\.(keyboard|mouse)\./.test(x)).join('\n');
const packSelection=fs.readFileSync(path.join(client,'config/defaultoptions/options.txt'),'utf8').split(/\r?\n/).find(x=>x.startsWith('resourcePacks:'))||'resourcePacks:[]';
JSON.parse(packSelection.slice('resourcePacks:'.length));
const options=`version:3955\nlang:es_es\nrenderDistance:8\nsimulationDistance:6\ngraphicsMode:0\nparticles:1\nnarrator:0\n${packSelection}\n${keyDefaults}\n`;
const optionsFile=path.join(base,'build/initial-options.txt');write(optionsFile,options);defaults.push({path:'options.txt',...blob(optionsFile)});
// Upstream enables shaders: switch them OFF only for new installs, never change an existing player's choice.
const irisFile=path.join(base,'build/initial-iris.properties');write(irisFile,'enableShaders=false\ndisableUpdateMessage=true\nshaderPack=COBBLEVERSE - Shaders\n');
const irisDefault=defaults.findIndex(x=>x.path==='config/iris.properties');if(irisDefault>=0)defaults.splice(irisDefault,1);
defaults.push({path:'config/iris.properties',...blob(irisFile)});
const compatibility='puchamon-'+release;
const manifest={schema:1,packId:'puchamon-origins',sequence:Date.now(),release,compatibility,minecraft:'1.21.1',fabric:'0.19.5',packwiz:'client/pack.toml',packwizSha256:sha(pack),indexSha256:sha(indexText),files,defaults};
const manifestBytes=JSON.stringify(manifest,null,2)+'\n';write(path.join(dest,'manifest.json'),manifestBytes);
const channel={schema:1,packId:'puchamon-origins',manifest:`../releases/${release}/manifest.json`,manifestSha256:sha(manifestBytes)};
const seed=path.join(base,'output',`Puchamon-Origins-${release}-${values.channel}`),game=path.join(seed,'minecraft');
fs.mkdirSync(path.join(game,'puchamon'),{recursive:true});
for(const f of ['launch.jar','gson.jar','unsup.jar'])fs.copyFileSync(path.join(base,'runtime',f),path.join(game,'puchamon',f));
write(path.join(game,'puchamon/launcher.properties'),`packId=puchamon-origins\nchannelUrl=${new URL('channels/'+values.channel+'.json',url)}\nminecraft=1.21.1\nfabric=0.19.5\nallowLocalHttp=${url.protocol==='http:'}\nramMode=auto\nmanualMaxMiB=4096\nunsupSha256=${sha(fs.readFileSync(path.join(base,'runtime/unsup.jar')))}\n`);
write(path.join(seed,'instance.cfg'),'[General]\nInstanceType=OneSix\nname=Puchamon: Origins\niconKey=default\nManagedPack=false\nOverrideMemory=true\nMinMemAlloc=512\nMaxMemAlloc=4096\nOverrideCommands=true\nPreLaunchCommand=\nPostExitCommand=\nWrapperCommand="$INST_JAVA" -Xmx128m -jar puchamon/launch.jar\nOverrideJavaArgs=false\nJoinServerOnLaunch=false\n');
write(path.join(seed,'mmc-pack.json'),JSON.stringify({formatVersion:1,components:[{uid:'net.minecraft',version:'1.21.1',important:true},{uid:'net.fabricmc.fabric-loader',version:'0.19.5',important:true}]},null,2));
if(fs.existsSync(path.join(client,'icon.png')))fs.copyFileSync(path.join(client,'icon.png'),path.join(seed,'icon.png'));
// Offline admin-side server payload: selected software only, never world/auth/operator data.
const serverStage=path.join(base,'output',`Servidor-Puchamon-${release}`);
for(const sub of ['mods','datapacks','config/ftbquests/quests'])for(const file of walk(path.join(server,sub))){const p=relative(server,file);if(sub==='mods'&&!/^mods\/[^/]+\.jar$/.test(p))continue;write(path.join(serverStage,p),fs.readFileSync(file));}
write(path.join(serverStage,'mods/puchamon-gate-1.0.0.jar'),fs.readFileSync(gate));
for(const p of shared)if(fs.existsSync(path.join(server,p)))write(path.join(serverStage,p),fs.readFileSync(path.join(server,p)));
write(path.join(serverStage,'puchamon/release.json'),JSON.stringify({schema:1,packId:'puchamon-origins',release,compatibility},null,2));
write(path.join(base,'build',`release-${release}.json`),JSON.stringify({release,seed,serverStage,clientFiles:files.length,initialDefaults:defaults.length,channelUrl:new URL('channels/'+values.channel+'.json',url).href,localOnly:url.protocol==='http:',licenseReview:[...new Set(licenseReview)],publicationApproved:false},null,2));
// Publish the test pointer last, after every artifact was built. Stable promotion is separate.
if(values.channel==='test')write(path.join(site,'channels','test.json'),JSON.stringify(channel,null,2)+'\n');
console.log(JSON.stringify({release,seed,clientFiles:files.length,initialDefaults:defaults.length,localOnly:url.protocol==='http:',publicationApproved:false}));
