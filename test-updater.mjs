import fs from 'node:fs';import path from 'node:path';import http from 'node:http';import crypto from 'node:crypto';import {spawn} from 'node:child_process';import {fileURLToPath} from 'node:url';import assert from 'node:assert/strict';
const base=path.dirname(fileURLToPath(import.meta.url)),runtime=path.join(base,'runtime'),test=fs.mkdtempSync(path.join(base,'build','updater-test-'));
const java=process.env.PUCHAMON_JAVA||(process.env.JAVA_HOME?path.join(process.env.JAVA_HOME,'bin/java'):path.join(process.env.APPDATA||'', 'FreesmLauncher/java/java-runtime-delta/bin/java.exe'));
const sha=b=>crypto.createHash('sha256').update(b).digest('hex'),write=(p,b)=>{fs.mkdirSync(path.dirname(p),{recursive:true});fs.writeFileSync(p,b);};
let active,broken=false;const responses=new Map(),results=[];
const server=http.createServer((req,res)=>{let value=req.url==='/channel.json'?active:responses.get(req.url);if(!value){res.writeHead(404);res.end();return;}if(broken&&req.url.includes('artifact')){res.writeHead(200,{'Content-Length':99999});res.write('truncated');res.socket.destroy();return;}res.writeHead(200);res.end(value);});
await new Promise(r=>server.listen(0,'127.0.0.1',r));const origin='http://127.0.0.1:'+server.address().port;
function release(version,entries,defaults=[],approvedUpdates=[]){
 const prefix='/releases/'+version;let index='hash-format = "sha256"\n';const files=[];
 for(const [p,data] of Object.entries(entries)){
  const content=Buffer.from(data);files.push({path:p,sha256:sha(content)});
  // Direct files exercise the exact packwiz/unsup transaction engine, without loading game mods.
  responses.set(prefix+'/client/'+p,content);index+=`\n[[files]]\nfile = ${JSON.stringify(p)}\nhash = "${sha(content)}"\nmetafile = false\n`;
 }
 const pack=`name = "Puchamon: Origins"\nversion = "${version}"\npack-format = "packwiz:1.1.0"\n[index]\nfile = "index.toml"\nhash-format = "sha256"\nhash = "${sha(index)}"\n[versions]\nminecraft = "1.21.1"\nfabric = "0.19.5"\n`;
 responses.set(prefix+'/client/index.toml',index);responses.set(prefix+'/client/pack.toml',pack);
 const manifest={schema:1,packId:'puchamon-origins',release:version,compatibility:'puchamon-'+version,minecraft:'1.21.1',fabric:'0.19.5',packwiz:'client/pack.toml',packwizSha256:sha(pack),indexSha256:sha(index),files,defaults,approvedUpdates};
 return publish(manifest,prefix);
}
function publish(manifest,prefix){const b=JSON.stringify(manifest);responses.set(prefix+'/manifest.json',b);return JSON.stringify({schema:1,packId:'puchamon-origins',manifest:prefix+'/manifest.json',manifestSha256:sha(b)});}
function setup(name){const game=path.join(test,name,'minecraft');for(const f of ['launch.jar','unsup.jar','gson.jar'])write(path.join(game,'puchamon',f),fs.readFileSync(path.join(runtime,f)));write(path.join(game,'puchamon/launcher.properties'),`packId=puchamon-origins\nchannelUrl=${origin}/channel.json\nminecraft=1.21.1\nfabric=0.19.5\nallowLocalHttp=true\nramMode=auto\nunsupSha256=${sha(fs.readFileSync(path.join(runtime,'unsup.jar')))}\n`);return game;}
function run(game,args=['--memory']){return new Promise((resolve,reject)=>{const child=spawn(java,['-Xmx128m','-Djava.awt.headless=true','-jar','puchamon/launch.jar',java,'-Xmx8192m','-Xms1024m','-cp','puchamon/launch.jar;puchamon/gson.jar','local.puchamon.Probe',...args],{cwd:game,windowsHide:true});let output='';child.stdout.on('data',b=>output+=b);child.stderr.on('data',b=>output+=b);const timer=setTimeout(()=>{child.kill();reject(Error('Test timeout'));},120000);child.on('error',reject);child.on('exit',code=>{clearTimeout(timer);resolve({code,output});});});}
try{
 const game=setup('friend with spaces á');
 const sentinel={'options.txt':'MY GRAPHICS AND AUDIO','xaero/map.dat':'MAP','quickskin/skin.json':'MY SKIN','config/soundcontroller.json':'MY VOLUME','saves/world/level.dat':'MY WORLD','config/inventoryprofilesnext/inventoryprofiles.json':'MY IPN PREFERENCES','config/inventoryprofilesnext/integrationHints/personal.json':'MY OWN HINT'};
 for(const [p,b]of Object.entries(sentinel))write(path.join(game,p),b);
 responses.set('/defaults/options','SHOULD NOT REPLACE');responses.set('/defaults/client','INITIAL-ONLY');
 const defaults=[{path:'options.txt',url:origin+'/defaults/options',sha256:sha('SHOULD NOT REPLACE')},{path:'config/new-default.toml',url:origin+'/defaults/client',sha256:sha('INITIAL-ONLY')}];
 const hint='config/inventoryprofilesnext/integrationHints/puchamon-accessories.json';
 active=release('test1',{'mods/old.jar':'version one','config/securitycraft-server.json':'one',[hint]:'{"qa":"one"}'},defaults);
 let r=await run(game);write(path.join(test,'01-first.log'),r.output);assert.equal(r.code,0,r.output);assert.match(r.output,/ACTUAL_MAX_MIB=6144/);results.push('Fresh install through real unsup; real child heap 6144 MiB; spaces/accent path');
 fs.unlinkSync(path.join(game,'config/new-default.toml'));write(path.join(game,'mods/user-added.jar'),'NOT OUR FILE');
 active=release('test2',{'mods/new.jar':'version two','config/securitycraft-server.json':'two',[hint]:'{"qa":"two"}'},defaults);
 r=await run(game);write(path.join(test,'02-update.log'),r.output);assert.equal(r.code,0,r.output);assert(!fs.existsSync(path.join(game,'mods/old.jar')));assert.equal(fs.readFileSync(path.join(game,'mods/new.jar'),'utf8'),'version two');assert(fs.existsSync(path.join(game,'mods/user-added.jar')));assert(!fs.existsSync(path.join(game,'config/new-default.toml')));results.push('Update/removal only of tracked files; untracked mods preserved; deleted preferences not recreated');
 for(const [p,b]of Object.entries(sentinel))assert.equal(fs.readFileSync(path.join(game,p),'utf8'),b);assert.equal(fs.readFileSync(path.join(game,hint),'utf8'),'{"qa":"two"}');results.push('Graphics/audio/maps/skins/world/IPN preferences survive byte-for-byte; only our Accessories hint updates');
 const before=fs.readFileSync(path.join(game,'mods/new.jar'));
 active=release('badpath',{'options.txt':'EVIL'});r=await run(game);assert.notEqual(r.code,0);assert(!r.output.includes('ACTUAL_MAX_MIB'));assert.deepEqual(fs.readFileSync(path.join(game,'mods/new.jar')),before);results.push('Protected path rejected before updater/game');
 active=release('hasherror',{'mods/artifact.jar':'expected bytes'});responses.set('/releases/hasherror/client/mods/artifact.jar','wrong bytes');r=await run(game);write(path.join(test,'03-bad-hash.log'),r.output);assert.notEqual(r.code,0);assert(!r.output.includes('ACTUAL_MAX_MIB'));assert.equal(JSON.parse(fs.readFileSync(path.join(game,'puchamon/release.json'))).release,'test2');results.push('Bad download hash blocks launch; no false successful version marker');
 active=release('test2',{'mods/new.jar':'version two','config/securitycraft-server.json':'two',[hint]:'{"qa":"two"}'},defaults);
 r=await run(game);assert.equal(r.code,0,r.output);results.push('Retry returns to approved version after failed update');
 let properties=fs.readFileSync(path.join(game,'puchamon/launcher.properties'),'utf8');write(path.join(game,'puchamon/launcher.properties'),properties+'manualMaxMiB=4096\nramMode=manual\n');r=await run(game);assert.equal(r.code,0,r.output);assert.match(r.output,/ACTUAL_MAX_MIB=4096/);results.push('Manual opt-out preserved, actual child heap 4096 MiB');
 active=release('cut',{'mods/artifact.jar':'COMPLETE AFTER RETRY'});broken=true;r=await run(game);assert.notEqual(r.code,0);assert(!r.output.includes('ACTUAL_MAX_MIB'));broken=false;r=await run(game);assert.equal(r.code,0,r.output);assert.equal(fs.readFileSync(path.join(game,'mods/artifact.jar'),'utf8'),'COMPLETE AFTER RETRY');results.push('Truncated/interrupted transfer blocks launch; successful retry repairs it');
 // Explicit configuration requests apply once and never replace the entire preferences file.
 write(path.join(game,'options.txt'),'renderDistance:8\nsoundCategory_master:0.6\nkey_key.jump:key.keyboard.space\n');
 responses.set('/approved/options','{"renderDistance":6}');responses.set('/approved/sound','APPROVED VOLUME');
 const patches=[{id:'chunks-20261016',path:'options.txt',mode:'options-keys',administratorRequest:'Synthetic administrator request: change only render distance',url:origin+'/approved/options',sha256:sha('{"renderDistance":6}')},{id:'sound-20261016',path:'config/soundcontroller.json',mode:'replace',administratorRequest:'Synthetic administrator request: replace selected configuration',url:origin+'/approved/sound',sha256:sha('APPROVED VOLUME')}];
 const settingsFiles={'mods/new.jar':'SETTINGS TEST'};
 active=release('settings1',settingsFiles,[],patches);r=await run(game);assert.equal(r.code,0,r.output);
 assert.match(fs.readFileSync(path.join(game,'options.txt'),'utf8'),/renderDistance:6/);assert.match(fs.readFileSync(path.join(game,'options.txt'),'utf8'),/soundCategory_master:0.6/);assert.match(fs.readFileSync(path.join(game,'options.txt'),'utf8'),/key_key.jump:key.keyboard.space/);assert.equal(fs.readFileSync(path.join(game,'config/soundcontroller.json'),'utf8'),'APPROVED VOLUME');
 const approvedBackups=fs.readdirSync(path.join(game,'puchamon/backups')).filter(n=>n.startsWith('approved-settings1-'));assert.equal(approvedBackups.length,1);assert.equal(fs.readFileSync(path.join(game,'puchamon/backups',approvedBackups[0],'config/soundcontroller.json'),'utf8'),'MY VOLUME');
 results.push('Explicit config patch + selective options preserve unrelated graphics/audio/keys; verified prior file retained in backup');
 write(path.join(game,'options.txt'),'renderDistance:10\nsoundCategory_master:0.6\nkey_key.jump:key.keyboard.space\n');r=await run(game);assert.equal(r.code,0,r.output);assert.match(fs.readFileSync(path.join(game,'options.txt'),'utf8'),/renderDistance:10/);results.push('Approved patches apply once; later personal adjustments are not reset every launch');
 active=release('settings2',settingsFiles);r=await run(game);assert.equal(r.code,0,r.output);assert.equal(fs.readFileSync(path.join(game,'config/soundcontroller.json'),'utf8'),'APPROVED VOLUME');results.push('Withdrawing a config patch does not delete the configuration');
 active=release('settingsbad',settingsFiles,[],[{...patches[0],id:'new-bad',sha256:'a'.repeat(64)}]);r=await run(game);assert.notEqual(r.code,0);assert(!r.output.includes('ACTUAL_MAX_MIB'));assert.match(fs.readFileSync(path.join(game,'options.txt'),'utf8'),/renderDistance:10/);assert.equal(JSON.parse(fs.readFileSync(path.join(game,'puchamon/release.json'))).release,'settings2');results.push('Bad approved-config hash blocks game without changing preferences or successful version marker');
 active=release('settingsprivate',settingsFiles,[],[{...patches[1],id:'private',path:'config/easyauth/main.conf'}]);r=await run(game);assert.notEqual(r.code,0);assert(!r.output.includes('ACTUAL_MAX_MIB'));results.push('Even explicit config plans cannot overwrite authentication data');
 await new Promise(r=>server.close(r));r=await run(game);assert.notEqual(r.code,0);assert(!r.output.includes('ACTUAL_MAX_MIB'));results.push('Unavailable update service fails closed, without starting game');
 write(path.join(test,'results.json'),JSON.stringify({passed:true,results,game,syntheticGameOnly:true,realUnsup:true,freemUiImported:false},null,2));console.log(JSON.stringify({passed:true,results,test},null,2));
}finally{server.close();}
