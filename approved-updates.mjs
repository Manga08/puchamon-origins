import fs from 'node:fs';import path from 'node:path';
export function approvedPath(p){
 if(typeof p!=='string'||p.includes('\\')||p.includes(':')||p.startsWith('/')||p.split('/').some(x=>!x||x==='.'||x==='..'))return false;
 if(/easyauth|quickskin|account|password|credential|fingerprint|\.db($|[./])|\.sqlite|\.pem$|\.key$/i.test(p))return false;
 return p==='options.txt'||p.startsWith('config/')||/^(resourcepacks|shaderpacks|emotes)\/[^/]+\.(zip|json|emotecraft)$/.test(p);
}
export function prepareApprovedUpdates(planPath,{blob,managedAllowed}){
 if(!planPath)return [];
 const plan=JSON.parse(fs.readFileSync(planPath,'utf8').replace(/^\uFEFF/,''));
 if(!Array.isArray(plan))throw Error('Approved updates must be an explicit array');
 const ids=new Set(),paths=new Set();return plan.map(entry=>{
  const {id,path:p,mode,source,administratorRequest}=entry;
  if(!approvedPath(p)||managedAllowed(p)||!['replace','options-keys'].includes(mode)||!id?.match(/^[a-zA-Z0-9._-]{1,100}$/)||ids.has(id)||paths.has(p.toLowerCase())||typeof administratorRequest!=='string'||!administratorRequest.trim()||typeof source!=='string')throw Error('Invalid explicit patch request: '+(id??'unknown'));
  if((p==='options.txt')!==(mode==='options-keys'))throw Error('options.txt requires selective keys');
  ids.add(id);paths.add(p.toLowerCase());const file=path.resolve(path.dirname(planPath),source),bytes=fs.readFileSync(file);
  if(bytes.length>64*1024*1024)throw Error('Approved file exceeds 64 MiB');
  if(mode==='options-keys'){
   const keys=JSON.parse(bytes.toString('utf8'));if(!keys||Array.isArray(keys)||typeof keys!=='object'||!Object.keys(keys).length||Object.entries(keys).some(([k,v])=>!k.match(/^[a-zA-Z0-9_.-]+$/)||!['string','number','boolean'].includes(typeof v)||/[\r\n]/.test(String(v))))throw Error('Invalid options keys');
  }
  // Prevent accidental public publication of common credential assignments. Never print their values.
  if(/\.json5?$|\.toml$|\.properties$|\.conf$|\.txt$|\.cfg$/i.test(p)){
   const text=bytes.toString('utf8');if(/(?:gh[pousr]_[a-zA-Z0-9]{20,}|github_pat_[a-zA-Z0-9_]{40,}|BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY)/.test(text)||/(?:password|api[_-]?key|access[_-]?token|secret)\s*["']?\s*[:=]\s*["'][^"'\s]{8,}["']/i.test(text))throw Error('Possible credential in approved public file: '+p);
  }
  return {id,path:p,mode,administratorRequest,...blob(file)};
 });
}
