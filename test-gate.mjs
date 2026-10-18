import net from 'node:net';import crypto from 'node:crypto';import zlib from 'node:zlib';import fs from 'node:fs';import path from 'node:path';import assert from 'node:assert/strict';import {fileURLToPath} from 'node:url';
const base=path.dirname(fileURLToPath(import.meta.url)),release=process.argv[2]||'0.1.0-rc2';
const targetPort=Number(process.env.PUCHAMON_TEST_PORT||25578);
if(!Number.isInteger(targetPort)||![25578,25579].includes(targetPort))throw Error('Only isolated test ports allowed');
const vi=n=>{const b=[];do{let v=n&127;n>>>=7;if(n)v|=128;b.push(v);}while(n);return Buffer.from(b);};
function readVi(b,offset=0){let value=0;for(let i=0;i<5;i++){if(offset+i>=b.length)return null;const x=b[offset+i];value|=(x&127)<<(i*7);if(!(x&128))return {value,next:offset+i+1};}throw Error('VarInt too long');}
const str=s=>Buffer.concat([vi(Buffer.byteLength(s)),Buffer.from(s)]);
const frame=b=>Buffer.concat([vi(b.length),b]);
async function attempt(mode){return new Promise((resolve,reject)=>{
 const logPath=path.join(base,'build/gate-server/logs/latest.log'),logOffset=fs.existsSync(logPath)?fs.statSync(logPath).size:0;
 const socket=net.connect(targetPort,'127.0.0.1');let buffer=Buffer.alloc(0),compression=-1,querySeen=false,done=false;
 const finish=result=>{if(done)return;done=true;socket.destroy();resolve(result);};socket.setTimeout(15000,()=>{socket.destroy();reject(Error('Gate timeout '+mode));});socket.on('error',error=>{const delta=fs.existsSync(logPath)?fs.readFileSync(logPath).subarray(logOffset).toString('utf8'):'';const denial=delta.split('\n').find(line=>line.includes('Disconnecting Gate'+mode+' ')&&line.includes('Tu versión de Puchamon'));if(error.code==='ECONNRESET'&&querySeen&&mode!=='Good'&&denial)finish({mode,querySeen,rejected:true,message:denial,transportReset:true,serverDenialVerified:true});else reject(error);});
 socket.on('connect',()=>{
  const name='Gate'+mode;const uuid=crypto.createHash('md5').update('OfflinePlayer:'+name).digest();uuid[6]=(uuid[6]&15)|48;uuid[8]=(uuid[8]&63)|128;
  socket.write(frame(Buffer.concat([vi(0),vi(767),str('127.0.0.1'),Buffer.from([targetPort>>8,targetPort&255]),vi(2)])));
  socket.write(frame(Buffer.concat([vi(0),str(name),uuid])));
 });
 socket.on('data',data=>{
  buffer=Buffer.concat([buffer,data]);
  try{while(true){const length=readVi(buffer);if(!length||buffer.length<length.next+length.value)return;let packet=buffer.subarray(length.next,length.next+length.value);buffer=buffer.subarray(length.next+length.value);
   if(compression>=0){const size=readVi(packet);packet=size.value?zlib.inflateSync(packet.subarray(size.next)):packet.subarray(size.next);}
   const id=readVi(packet);let i=id.next;
   if(id.value===0){finish({mode,querySeen,rejected:true,message:packet.subarray(i).toString('utf8')});return;}
   if(id.value===3){compression=readVi(packet,i).value;continue;}
   if(id.value===2){finish({mode,querySeen,rejected:false,loginSucceeded:true});return;}
   if(id.value!==4)continue;
   const message=readVi(packet,i);i=message.next;const nameLength=readVi(packet,i);i=nameLength.next;const channel=packet.subarray(i,i+nameLength.value).toString('utf8');i+=nameLength.value;
   let payload=Buffer.concat([vi(2),vi(message.value),Buffer.from([0])]);
   if(channel==='puchamon:release_check'){
    querySeen=true;
    if(mode!=='Missing')payload=Buffer.concat([vi(2),vi(message.value),Buffer.from([1]),vi(1),str(mode==='Good'?'puchamon-'+release:mode==='Previous'?'puchamon-1.0-auto.6':mode==='Legacy'?'puchamon-0.1.0-rc7.1-amigos':'puchamon-outdated')]);
   }
   if(compression>=0)payload=Buffer.concat([vi(0),payload]);socket.write(frame(payload));
  }}catch(e){socket.destroy();reject(e);}
 });
 socket.on('close',()=>{if(!done)reject(Error('Connection closed before result '+mode));});
 });}
const results=[];
for(const mode of ['Missing','Wrong','Previous','Legacy','Good']){const r=await attempt(mode);assert(r.querySeen);assert.equal(r.rejected,mode!=='Good');if(mode!=='Good')assert(r.message.includes('Puchamon'));results.push(r);}
fs.writeFileSync(path.join(base,'build',`gate-results-${release}.json`),JSON.stringify({passed:true,results,enteredWorld:false,usedActualServer:true},null,2));console.log(JSON.stringify({passed:true,results},null,2));
