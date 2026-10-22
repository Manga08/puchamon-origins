import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

// A proof is generated only after loading/saving with the installed FTB version.
// This does NOT relax the launcher's SHA-256 checks or rewrite player progress.
export function assertNativeQuestFormat(releaseRoot) {
  const read=p=>JSON.parse(fs.readFileSync(p,'utf8'));
  const manifest=read(path.join(releaseRoot,'manifest.json'));
  const chapters=manifest.files.filter(f=>/^config\/ftbquests\/quests\/chapters\/[^/]+\.snbt$/.test(f.path));
  if(!chapters.length)return;
  const proof=read(path.join(releaseRoot,'quest-format-proof.json'));
  if(proof.schema!==1||proof.nativeSaveReloadSave!==true||proof.minecraft!==manifest.minecraft||proof.fabric!==manifest.fabric)throw Error('Falta una prueba nativa de guardado/recarga/guardado de FTB Quests.');
  if(Object.keys(proof.chapters||{}).length!==chapters.length)throw Error('Prueba incompleta de capítulos.');
  for(const prefix of ['mods/ftb-quests-','mods/ftb-library-']){
    const found=manifest.files.filter(f=>f.path.startsWith(prefix));
    if(found.length!==1||proof.writerFiles?.[found[0].path]!==found[0].sha256)throw Error('FTB cambió de versión: repetir la prueba nativa antes de publicar.');
  }
  for(const file of chapters){
    const actual=crypto.createHash('sha256').update(fs.readFileSync(path.join(releaseRoot,'client',file.path))).digest('hex');
    if(actual!==file.sha256||proof.chapters[file.path]!==actual)throw Error('Capítulo editado sin repetir el guardado nativo: '+file.path);
  }
  return {chapters:chapters.length,nativeSaveReloadSave:true};
}
