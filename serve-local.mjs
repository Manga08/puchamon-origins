import http from 'node:http';import fs from 'node:fs';import path from 'node:path';import {fileURLToPath} from 'node:url';
const root=path.join(path.dirname(fileURLToPath(import.meta.url)),'site');
const port=Number(process.argv[2]||8765);
http.createServer((req,res)=>{try{const p=path.resolve(root,'.'+decodeURIComponent(new URL(req.url,'http://127.0.0.1').pathname));if(!p.startsWith(root+path.sep)||!fs.statSync(p).isFile()){res.writeHead(404);res.end();return;}res.writeHead(200,{'Content-Length':fs.statSync(p).size,'Cache-Control':'no-store'});fs.createReadStream(p).pipe(res);}catch{res.writeHead(404);res.end();}}).listen(port,'127.0.0.1',()=>console.log('Puchamon test feed bound to 127.0.0.1:'+port));
