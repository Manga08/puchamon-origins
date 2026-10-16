import fs from 'node:fs';import os from 'node:os';import path from 'node:path';import assert from 'node:assert/strict';
import {approvedPath,prepareApprovedUpdates} from './approved-updates.mjs';
for(const p of ['options.txt','config/soundcontroller.json','config/xaero/minimap/main.cfg','shaderpacks/Example.zip'])assert(approvedPath(p),p);
for(const p of ['../options.txt','config/../a','config/easyauth/main.conf','config/quickskin.json','saves/a','puchamon/launcher.properties','config/token.key'])assert(!approvedPath(p),p);
const temp=fs.mkdtempSync(path.join(os.tmpdir(),'puchamon-approved-test-'));fs.writeFileSync(path.join(temp,'patch.json'),'{"renderDistance":6}');
const plan=path.join(temp,'plan.json'),entry={id:'distance-1',path:'options.txt',mode:'options-keys',source:'patch.json',administratorRequest:'Test request: update distance only'};
const build=x=>{fs.writeFileSync(plan,JSON.stringify(x));return prepareApprovedUpdates(plan,{managedAllowed:()=>false,blob:()=>({url:'https://example.invalid/a',sha256:'a'.repeat(64)})});};
assert.equal(build([entry])[0].path,'options.txt');assert.throws(()=>build([{...entry,administratorRequest:''}]));assert.throws(()=>build([{...entry,mode:'replace'}]));assert.throws(()=>build([entry,entry]));
console.log('PASS: explicit publication plans reject unauthorized paths, missing requests, duplicate IDs and full options replacement.');
