import {execFileSync} from 'node:child_process';
import fs from 'node:fs';import path from 'node:path';
const repo=process.argv[2];if(!repo)throw Error('Repository path required');
const git=(...args)=>execFileSync('git',['-C',repo,...args],{encoding:'utf8',maxBuffer:32*1024*1024});
const objects=git('rev-list','--objects','--all').trim().split('\n');const findings=[];let blobs=0;
for(const line of objects){const m=line.match(/^([a-f0-9]+) (.+)$/);if(!m)continue;const [,sha,name]=m;
 if(/\.(jar|zip|db|sqlite|sqlite3|log|pem|key|nbt|mca|dat)$/i.test(name)||/(^|\/)(world|saves|accounts|playerdata|backups|\.env)(\/|$)/i.test(name))findings.push({file:name,kind:'private-or-binary-path'});
 if(git('cat-file','-t',sha).trim()!=='blob')continue;blobs++;const text=git('cat-file','-p',sha);
 for(const [kind,re] of Object.entries({githubToken:/\b(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{40,})\b/,privateKey:/-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----/,realIP:/\b(?:181\.54|190\.69|206\.62)\.\d+\.\d+\b/,credentialURL:/https?:\/\/[^\s/:]+:[^\s/@]+@/}))if(re.test(text))findings.push({file:name,kind});
}
const result={repository:'Manga08/puchamon-origins',commits:Number(git('rev-list','--all','--count').trim()),blobsChecked:blobs,findings,passed:findings.length===0,note:'Heuristic review of all reachable Git objects. Does not certify distribution rights or rule out every secret format.'};
console.log(JSON.stringify(result,null,2));
if(findings.length)process.exitCode=1;
