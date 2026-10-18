package local.puchamon;
import com.google.gson.*;
import java.io.*;import java.net.*;import java.net.http.*;import java.nio.file.*;
import java.security.*;import java.time.Duration;import java.util.*;import java.util.zip.*;

/** Obtain unchanged upstream overrides from the author's archive, not a rehost. */
final class ArchiveFiles {
 static void install(JsonObject manifest,JsonArray files,URI base)throws Exception{
  if(!manifest.has("archives"))return;
  Map<String,String> wanted=new HashMap<>();for(JsonElement e:files){var f=e.getAsJsonObject();wanted.put(f.get("path").getAsString(),f.get("sha256").getAsString());}
  Set<String> paths=new HashSet<>();
  for(JsonElement e:manifest.getAsJsonArray("archives")){
   JsonObject archive=e.getAsJsonObject();String checksum=archive.get("sha256").getAsString();
   if(!checksum.matches("[a-f0-9]{64}"))throw new IOException("Hash de origen inválido.");
   List<JsonObject> missing=new ArrayList<>();
   for(JsonElement item:archive.getAsJsonArray("entries")){
    JsonObject f=item.getAsJsonObject();String p=f.get("path").getAsString(),entry=f.get("entry").getAsString();
    if(!Launcher.allowed(p)||!paths.add(p)||!Objects.equals(wanted.get(p),f.get("sha256").getAsString())||!entry.startsWith("overrides/")||entry.contains("\\")||Arrays.asList(entry.split("/",-1)).stream().anyMatch(x->x.isBlank()||x.equals(".")||x.equals("..")))throw new IOException("Entrada de origen no autorizada.");
    Path target=Launcher.safePath(p);if(!Files.isRegularFile(target)||!Launcher.hash(target).equals(wanted.get(p)))missing.add(f);
   }
   if(missing.isEmpty())continue;
   Path cache=Launcher.safePath("puchamon/downloads/"+checksum+".mrpack");Files.createDirectories(cache.getParent());
   if(!Files.isRegularFile(cache)||!Launcher.hash(cache).equals(checksum)){
    URI uri=Launcher.validUri(base.resolve(archive.get("url").getAsString()));
    if(!"cdn.modrinth.com".equals(uri.getHost())&&!Launcher.allowLocal)throw new IOException("Origen de la base no aprobado.");
    var response=Launcher.HTTP.send(HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(10)).GET().build(),HttpResponse.BodyHandlers.ofInputStream());
    Path temporary=cache.resolveSibling(cache.getFileName()+".part");
    try(var in=response.body()){
     if(response.statusCode()!=200)throw new IOException("La base oficial no está disponible (HTTP "+response.statusCode()+").");
     try(var out=Files.newOutputStream(temporary)){byte[] buffer=new byte[65536];long count=0;for(int n;(n=in.read(buffer))!=-1;){count+=n;if(count>1024L*1024*1024)throw new IOException("Archivo de base demasiado grande.");out.write(buffer,0,n);}}
    }
    if(!Launcher.hash(temporary).equals(checksum))throw new IOException("La base oficial no coincide con su hash.");
    Files.move(temporary,cache,StandardCopyOption.REPLACE_EXISTING);
   }
   // Stage and verify every selected entry before touching game files. No extraction
   // of unrelated paths, no trust in archive-provided destinations or sizes.
   Map<Path,Path> staged=new LinkedHashMap<>();
   try(var zip=new ZipFile(cache.toFile())){
    for(JsonObject f:missing){String p=f.get("path").getAsString();ZipEntry entry=zip.getEntry(f.get("entry").getAsString());if(entry==null||entry.isDirectory())throw new IOException("Falta un recurso en la base oficial.");
     Path target=Launcher.safePath(p),stage=Launcher.safePath("puchamon/downloads/staged/"+wanted.get(p));Files.createDirectories(stage.getParent());
     try(var in=zip.getInputStream(entry);var out=Files.newOutputStream(stage)){byte[] buffer=new byte[65536];long count=0;for(int n;(n=in.read(buffer))!=-1;){count+=n;if(count>512L*1024*1024)throw new IOException("Recurso de base demasiado grande.");out.write(buffer,0,n);}}
     if(!Launcher.hash(stage).equals(wanted.get(p)))throw new IOException("Recurso de base con hash incorrecto.");staged.put(target,stage);
    }
   }
   for(var pair:staged.entrySet()){Files.createDirectories(pair.getKey().getParent());Files.move(pair.getValue(),pair.getKey(),StandardCopyOption.REPLACE_EXISTING);}
   Launcher.info("Recursos de la base oficial verificados: "+staged.size()+".");
  }
 }
}
