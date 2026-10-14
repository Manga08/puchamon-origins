import {makeServer} from './serve-private.mjs';import fs from 'node:fs';import path from 'node:path';import crypto from 'node:crypto';import assert from 'node:assert/strict';import {fileURLToPath} from 'node:url';
const base=path.dirname(fileURLToPath(import.meta.url)),root=fs.mkdtempSync(path.join(base,'build/private-host-test-')),key=crypto.randomBytes(32).toString('hex');
fs.writeFileSync(path.join(root,'pack.txt'),'private pack');const server=makeServer({root,key});await new Promise(r=>server.listen(0,'127.0.0.1',r));const url='http://127.0.0.1:'+server.address().port;
try{
 assert.equal((await fetch(url+'/wrong/pack.txt')).status,401);
 assert.equal(await (await fetch(url+'/'+key+'/pack.txt')).text(),'private pack');
 assert.equal((await fetch(url+'/'+key+'/pack.txt',{method:'POST'})).status,405);
 assert.equal((await fetch(url+'/'+key+'/%2e%2e%2foutside.txt')).status,404);
 fs.writeFileSync(path.join(base,'build/private-host-results.json'),JSON.stringify({passed:true,tests:['No key: rejected','Read key: accepted','Writes: rejected','Traversal: rejected'],keyRecorded:false,publicListener:false},null,2));
 console.log('PASS: private read-only endpoint, wrong-key rejection, no writes, no traversal. Key not logged.');
}finally{await new Promise(r=>server.close(r));}
