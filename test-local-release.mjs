import fs from 'node:fs';import path from 'node:path';import crypto from 'node:crypto';import assert from 'node:assert/strict';import {spawn} from 'node:child_process';import {fileURLToPath} from 'node:url';
const base=path.dirname(fileURLToPath(import.meta.url)),release=process.argv[2]||'0.1.0-rc6';
if(!/^[0-9a-z.-]+$/.test(release))throw Error('Invalid release');
const test=fs.mkdtempSync(path.join(base,'build/full-release-test-'));
const old=path.join(base,'output/Puchamon-Origins-0.1.0-rc5-test/minecraft'),seed=path.join(base,`output/Puchamon-Origins-${release}-test/minecraft`);
const java=path.join(process.env.APPDATA,'FreesmLauncher/java/java-runtime-delta/bin/java.exe');
const sha=b=>crypto.createHash('sha256').update(b).digest('hex');
const channel=JSON.parse(fs.readFileSync(path.join(base,'site/channels/test.json')));assert(channel.manifest.includes(release));
const run=game=>new Promise((resolve,reject)=>{
 const child=spawn(java,['-Xmx128m','-Djava.awt.headless=true','-jar','puchamon/launch.jar',java,'-Xmx8192m','-cp','puchamon/launch.jar;puchamon/gson.jar','local.puchamon.Probe','--memory'],{cwd:game,windowsHide:true});let output='';
 child.stdout.on('data',b=>output+=b);child.stderr.on('data',b=>output+=b);child.on('error',reject);
 const timer=setTimeout(()=>{child.kill();reject(Error('Release download timed out'));},300000);
 child.on('exit',code=>{clearTimeout(timer);fs.writeFileSync(path.join(test,path.basename(game)+'.log'),output);if(code!==0)return reject(Error('Updater exited '+code+'; see private test log'));resolve(output);});
});
const game=path.join(test,'actualizacion-con-preferencias');fs.cpSync(old,game,{recursive:true});
const protectedFiles=['options.txt','xaero/qa-map-preservation.sentinel','xaero/qa-waypoint-preservation.sentinel','quickskin/qa-skin-preservation.sentinel','config/qa-personal-volume.sentinel'];
let options=fs.readFileSync(path.join(game,'options.txt'),'utf8').replace(/^renderDistance:.*$/m,'renderDistance:13');options+='\nsoundCategory_music:0.19\n';fs.writeFileSync(path.join(game,'options.txt'),options);
for(const name of protectedFiles.slice(1)){const p=path.join(game,name);fs.mkdirSync(path.dirname(p),{recursive:true});fs.writeFileSync(p,'USER-CHOICE-DO-NOT-REPLACE');}
const before=Object.fromEntries(protectedFiles.map(p=>[p,sha(fs.readFileSync(path.join(game,p)))]));
const output=await run(game);assert.match(output,/ACTUAL_MAX_MIB=6144/);
for(const p of protectedFiles)assert.equal(sha(fs.readFileSync(path.join(game,p))),before[p]);
assert.equal(JSON.parse(fs.readFileSync(path.join(game,'puchamon/release.json'))).release,release);
const fresh=path.join(test,'instalacion-limpia');fs.cpSync(seed,fresh,{recursive:true});const freshOutput=await run(fresh);assert.match(freshOutput,/ACTUAL_MAX_MIB=6144/);
const manifest=JSON.parse(fs.readFileSync(path.join(base,`site/releases/${release}/manifest.json`)));
for(const root of [game,fresh])for(const file of manifest.files)assert.equal(sha(fs.readFileSync(path.join(root,file.path))),file.sha256);
const result={passed:true,release,upgradedFrom:'0.1.0-rc5',managedFilesVerified:manifest.files.length,rootJars:manifest.files.filter(f=>f.path.startsWith('mods/')).length,preservedUserFiles:protectedFiles.length,actualMaxHeapMiB:6144,realUnsup:true,realFullPackDownloaded:true,minecraftStarted:false,freemUiImported:false,test};
fs.writeFileSync(path.join(test,'results.json'),JSON.stringify(result,null,2));console.log(JSON.stringify(result,null,2));
