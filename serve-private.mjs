import http from 'node:http';import https from 'node:https';import fs from 'node:fs';import path from 'node:path';import crypto from 'node:crypto';import {fileURLToPath} from 'node:url';
export function makeServer({root,key,tls=null}){
 if(!/^[0-9a-f]{64}$/.test(key||''))throw Error('Use una clave aleatoria de lectura de 64 caracteres hexadecimales. No un token de GitHub.');
 root=fs.realpathSync(root);const expected=Buffer.from(key);
 const handler=(req,res)=>{
  if(!['GET','HEAD'].includes(req.method)){res.writeHead(405);res.end();return;}
  try{
   const raw=new URL(req.url,'http://localhost').pathname.split('/');const supplied=Buffer.from(raw[1]||'');
   if(supplied.length!==expected.length||!crypto.timingSafeEqual(supplied,expected)){res.writeHead(401,{'Cache-Control':'no-store'});res.end('Acceso de lectura no válido');return;}
   const relative=decodeURIComponent(raw.slice(2).join('/'));const candidate=path.resolve(root,relative);
   if(!candidate.startsWith(root+path.sep)||!fs.statSync(candidate).isFile()||!fs.realpathSync(candidate).startsWith(root+path.sep)){res.writeHead(404);res.end();return;}
   res.writeHead(200,{'Content-Length':fs.statSync(candidate).size,'Cache-Control':'private, no-store','X-Content-Type-Options':'nosniff'});
   if(req.method==='HEAD')res.end();else fs.createReadStream(candidate).pipe(res);
  }catch{res.writeHead(404);res.end();}
 };
 return tls?https.createServer(tls,handler):http.createServer(handler);
}
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){
 const root=process.env.PUCHAMON_DOWNLOAD_ROOT||path.join(path.dirname(fileURLToPath(import.meta.url)),'site');
 const host=process.env.PUCHAMON_DOWNLOAD_HOST||'127.0.0.1',port=Number(process.env.PUCHAMON_DOWNLOAD_PORT||8766);
 const tls=process.env.PUCHAMON_TLS_CERT&&process.env.PUCHAMON_TLS_KEY?{cert:fs.readFileSync(process.env.PUCHAMON_TLS_CERT),key:fs.readFileSync(process.env.PUCHAMON_TLS_KEY)}:null;
 if(host!=='127.0.0.1'&&host!=='::1'&&!tls)throw Error('No exponga HTTP sin TLS. Use HTTPS o un proxy HTTPS hacia localhost.');
 const server=makeServer({root,key:process.env.PUCHAMON_DOWNLOAD_KEY,tls});
 server.listen(port,host,()=>console.log(`Servidor de solo lectura iniciado en ${host}:${port}; no se registra la clave ni las URL solicitadas.`));
}
