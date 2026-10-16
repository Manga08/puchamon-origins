import assert from 'node:assert/strict';
import {applyProfile} from './performance-profiles.mjs';
const original='lang:es_es\nrenderDistance:18\nsoundCategory_music:0.27\nkey_key.cobblemon.summary:key.keyboard.c\nresourcePacks:["vanilla","file/COBBLEVERSE RP.zip"]\n';
for(const profile of ['balanced','ligero']){
 const text=applyProfile(original,profile);
 for(const line of original.split('\n').filter(x=>x&&!x.startsWith('renderDistance:')))assert(text.includes(line+'\n'));
 assert(text.includes('renderDistance:'+(profile==='ligero'?6:8)+'\n'));
 assert(text.includes('maxFps:60\n')); assert.equal(applyProfile(text,profile),text);
 assert.equal(JSON.parse(text.match(/^renderClouds:(.*)$/m)[1]),'false');
 assert.equal(text.match(/^renderDistance:/gm).length,1);
}
assert.throws(()=>applyProfile(original,'bad'));
console.log('PASS: initial graphics profiles preserve language, controls, audio and Pokemon resource packs; idempotent.');
