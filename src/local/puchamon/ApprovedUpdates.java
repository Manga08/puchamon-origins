package local.puchamon;
import com.google.gson.*;
import java.io.*;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.net.URI;import java.util.*;

/** Exact, release-scoped administrator requests. Never a config/** directory synchronizer. */
final class ApprovedUpdates {
 static boolean allowed(String p){
  if(p.contains("\\")||p.contains(":")||p.startsWith("/")||Arrays.stream(p.split("/",-1)).anyMatch(s->s.isBlank()||s.equals(".")||s.equals("..")))return false;
  String lower=p.toLowerCase(Locale.ROOT);
  if(lower.matches(".*(easyauth|quickskin|account|password|credential|fingerprint|\\.db($|[./])|\\.sqlite|\\.pem$|\\.key$).*"))return false;
  return p.equals("options.txt")||p.startsWith("config/")||p.matches("(resourcepacks|shaderpacks|emotes)/[^/]+\\.(zip|json|emotecraft)");
 }
 static byte[] mergeOptions(byte[] current,byte[] patch)throws IOException{
  JsonObject keys=Launcher.parse(patch);if(keys.size()==0)throw new IOException("El parche de opciones está vacío.");
  var values=new LinkedHashMap<String,String>();
  for(var e:keys.entrySet()){
   if(!e.getKey().matches("[a-zA-Z0-9_.-]+")||!e.getValue().isJsonPrimitive())throw new IOException("Clave de opciones inválida.");
   String value=e.getValue().getAsString();if(value.contains("\n")||value.contains("\r"))throw new IOException("Valor de opciones inválido.");values.put(e.getKey(),value);
  }
  String text=new String(current,StandardCharsets.UTF_8),newline=text.contains("\r\n")?"\r\n":"\n";
  var lines=new ArrayList<String>();Set<String> changed=new HashSet<>();
  for(String line:text.split("\\r?\\n",-1)){
   int colon=line.indexOf(':');String key=colon<0?"":line.substring(0,colon);
   if(values.containsKey(key)){if(!changed.add(key))throw new IOException("Opciones duplicadas: "+key);lines.add(key+":"+values.get(key));}else lines.add(line);
  }
  if(!lines.isEmpty()&&lines.getLast().isEmpty())lines.removeLast();
  for(var e:values.entrySet())if(!changed.contains(e.getKey()))lines.add(e.getKey()+":"+e.getValue());
  return (String.join(newline,lines)+newline).getBytes(StandardCharsets.UTF_8);
 }
 static void apply(JsonObject manifest,URI base)throws Exception{
  if(!manifest.has("approvedUpdates"))return;
  Path statePath=Launcher.safePath("puchamon/approved-updates.json");
  JsonObject state=Files.exists(statePath)?Launcher.parse(Files.readAllBytes(statePath)):new JsonObject();
  var staged=new LinkedHashMap<Path,byte[]>();var ids=new LinkedHashMap<String,String>();Set<String> paths=new HashSet<>();
  for(JsonElement item:manifest.getAsJsonArray("approvedUpdates")){
   JsonObject f=item.getAsJsonObject();String p=f.get("path").getAsString(),id=f.get("id").getAsString(),mode=f.get("mode").getAsString(),hash=f.get("sha256").getAsString();
   if(!allowed(p)||Launcher.allowed(p)||!paths.add(p.toLowerCase(Locale.ROOT))||!id.matches("[a-zA-Z0-9._-]{1,100}")||ids.containsKey(id)||!hash.matches("[a-f0-9]{64}")||!f.has("administratorRequest")||f.get("administratorRequest").getAsString().isBlank())throw new IOException("Parche sin autorización explícita, ruta válida o separado de los archivos administrados.");
   if(!mode.equals("replace")&&!mode.equals("options-keys"))throw new IOException("Modo de parche desconocido.");
   if(p.equals("options.txt")!=mode.equals("options-keys"))throw new IOException("options.txt solo admite claves explícitas, nunca sustitución completa.");
   String fingerprint=hash+"|"+p+"|"+mode;Path target=Launcher.safePath(p);ids.put(id,fingerprint);
   if(state.has(id)){if(!state.get(id).getAsString().equals(fingerprint))throw new IOException("Se reutilizó un ID de parche con otro contenido o destino.");continue;}
   byte[] payload=Launcher.fetch(Launcher.validUri(base.resolve(f.get("url").getAsString())),64*1024*1024);Launcher.verify(payload,hash);
   if(Files.exists(target)&&!Files.isRegularFile(target))throw new IOException("El destino de configuración no es un archivo.");
   staged.put(target,mode.equals("options-keys")?mergeOptions(Files.exists(target)?Files.readAllBytes(target):new byte[0],payload):payload);
  }
  if(staged.isEmpty())return;
  Path backup=Launcher.safePath("puchamon/backups/approved-"+manifest.get("release").getAsString()+"-"+System.currentTimeMillis());Files.createDirectories(backup);
  var originals=new LinkedHashMap<Path,byte[]>();var applied=new ArrayList<Path>();
  for(Path target:staged.keySet()){
   byte[] before=Files.exists(target)?Files.readAllBytes(target):null;originals.put(target,before);
   if(before!=null){Path copy=backup.resolve(Launcher.root.relativize(target));Files.createDirectories(copy.getParent());Files.write(copy,before);if(!Arrays.equals(before,Files.readAllBytes(copy)))throw new IOException("No se verificó el respaldo de configuración.");}
  }
  try{
   for(var e:staged.entrySet()){applied.add(e.getKey());Launcher.atomic(e.getKey(),e.getValue());}
   for(var e:ids.entrySet())state.addProperty(e.getKey(),e.getValue());Launcher.atomic(statePath,Launcher.JSON.toJson(state).getBytes(StandardCharsets.UTF_8));
  }catch(Exception failure){for(Path target:applied){byte[] before=originals.get(target);try{if(before==null)Files.deleteIfExists(target);else Launcher.atomic(target,before);}catch(Exception rollback){failure.addSuppressed(rollback);}}throw failure;}
  Launcher.info("Ajustes expresamente aprobados aplicados: "+staged.size()+". Respaldo local conservado.");
 }
}
