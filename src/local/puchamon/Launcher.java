package local.puchamon;

import com.google.gson.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.net.http.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.security.*;
import java.time.Duration;
import java.util.*;
import javax.swing.JOptionPane;

/** Runs unsup before the real JVM, with a closed file policy. Never logs launcher arguments or credentials. */
public final class Launcher {
 static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
 static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NEVER).build();
 static final Set<String> SHARED=Set.of("config/migaja-controls.properties","config/securitycraft-server.json","config/immersive_paintings/common_config.toml","config/global_packs.toml");
 static Path root;
 static boolean allowLocal;
 static PrintWriter log;
 public static void main(String[] args){
  int result=1;
  try{
   root=Path.of(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().getParent().getParent().toRealPath();
   if(!Path.of("").toAbsolutePath().toRealPath().equals(root))throw new IOException("El launcher debe ejecutarse desde la carpeta minecraft de Puchamon.");
   Files.createDirectories(root.resolve("puchamon/logs"));
   log=new PrintWriter(Files.newBufferedWriter(root.resolve("puchamon/logs/launcher.log"),StandardOpenOption.CREATE,StandardOpenOption.APPEND),true);
   Properties settings=new Properties();try(var in=Files.newInputStream(root.resolve("puchamon/launcher.properties"))){settings.load(in);}
   if(!"puchamon-origins".equals(settings.getProperty("packId")))throw new IOException("Esta instancia no está identificada como Puchamon.");
   allowLocal=Boolean.parseBoolean(settings.getProperty("allowLocalHttp","false"));
   if(args.length==0)throw new IOException("Freesm no proporcionó el proceso Java a ejecutar.");
   try(var channel=FileChannel.open(root.resolve("puchamon/launch.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE)){
    FileLock lock=channel.tryLock();if(lock==null)throw new IOException("Puchamon ya está abierto o actualizándose.");
    try{
     String release=update(args[0],settings);
     int memory=memoryLimit(settings,totalMemoryMiB());
     var command=new ArrayList<String>();command.add(args[0]);
     command.add("-Xms512m");command.add("-Xmx"+memory+"m");
     for(int i=1;i<args.length;i++)if(!args[i].startsWith("-Xmx")&&!args[i].startsWith("-Xms")&&!args[i].startsWith("-XX:MaxRAMPercentage=")&&!args[i].startsWith("-XX:InitialRAMPercentage="))command.add(args[i]);
     info("Versión "+release+" verificada. Límite de RAM: "+memory+" MiB. Iniciando el juego.");
     Process child=new ProcessBuilder(command).directory(root.toFile()).inheritIO().start();
     Thread hook=new Thread(()->{if(child.isAlive())child.destroy();});Runtime.getRuntime().addShutdownHook(hook);
     try{result=child.waitFor();}finally{Runtime.getRuntime().removeShutdownHook(hook);}
    }finally{lock.release();}
   }
  }catch(Exception e){
   String message="No se inició Minecraft: "+safeMessage(e)+"\nConsulta minecraft/puchamon/logs/launcher.log. No borres mapas ni reinstales tu partida.";
   info(message);
   if(!java.awt.GraphicsEnvironment.isHeadless())JOptionPane.showMessageDialog(null,message,"Puchamon: Origins — actualización pendiente",JOptionPane.ERROR_MESSAGE);
  }finally{if(log!=null)log.close();}
  System.exit(result);
 }
 static int totalMemoryMiB(){return (int)(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize()/1048576L);}
 static int memoryLimit(Properties p,int total)throws IOException{
  if(total<7168)throw new IOException("Este perfil requiere un equipo con al menos 8 GB de RAM.");
  if("manual".equals(p.getProperty("ramMode","auto"))){
   int selected=Integer.parseInt(p.getProperty("manualMaxMiB","4096"));
   if(selected<2048||selected>Math.min(16384,total-2048))throw new IOException("Límite manual de RAM fuera del rango seguro.");
   return selected;
  }
  return total>=15360?6144:4096;
 }
 static String update(String javaExecutable,Properties settings)throws Exception{
  URI channelUri=validUri(URI.create(settings.getProperty("channelUrl")));
  JsonObject channel=parse(fetch(channelUri,1024*1024));
  identity(channel);
  URI manifestUri=validUri(channelUri.resolve(channel.get("manifest").getAsString()));
  byte[] bytes=fetch(manifestUri,8*1024*1024);verify(bytes,channel.get("manifestSha256").getAsString());
  JsonObject manifest=parse(bytes);identity(manifest);
  if(!settings.getProperty("minecraft").equals(manifest.get("minecraft").getAsString())||!settings.getProperty("fabric").equals(manifest.get("fabric").getAsString()))throw new IOException("Esta versión cambia Minecraft o Fabric. Requiere una migración de instancia aprobada.");
  String version=manifest.get("release").getAsString();
  if(!version.matches("[a-zA-Z0-9._-]{1,100}"))throw new IOException("Identificador de versión inválido.");
  JsonArray files=manifest.getAsJsonArray("files");Set<String> paths=new HashSet<>();
  for(JsonElement e:files){JsonObject f=e.getAsJsonObject();String path=f.get("path").getAsString();
   if(!paths.add(path)||!allowed(path))throw new IOException("La actualización contiene un archivo no administrable: "+path);
   safePath(path);if(!f.get("sha256").getAsString().matches("[0-9a-f]{64}"))throw new IOException("Hash inválido.");
  }
  JsonObject previous=null;Path installed=root.resolve("puchamon/installed-manifest.json");
  if(Files.exists(installed)){
   previous=parse(Files.readAllBytes(installed));identity(previous);
   if(previous.has("sequence")&&manifest.has("sequence")&&manifest.get("sequence").getAsLong()<previous.get("sequence").getAsLong())throw new IOException("Se intentó instalar una versión anterior. La recuperación debe aprobarse y publicarse como un nuevo parche.");
   for(JsonElement e:previous.getAsJsonArray("files"))if(!allowed(e.getAsJsonObject().get("path").getAsString()))throw new IOException("Estado anterior inválido; no se aplicarán eliminaciones.");
  }
  String compatibility=manifest.get("compatibility").getAsString();
  if(!compatibility.matches("puchamon-[a-zA-Z0-9._-]{1,100}"))throw new IOException("Compatibilidad inválida.");
  boolean change=previous==null||!version.equals(previous.get("release").getAsString())||!consistent(files);
  installDefaults(manifest,manifestUri);
  if(change){
   preflightConflicts(previous,files);
   backup(previous,version);
   URI pack=validUri(manifestUri.resolve(manifest.get("packwiz").getAsString()));
   byte[] packData=fetch(pack,1024*1024);verify(packData,manifest.get("packwizSha256").getAsString());
   byte[] indexData=fetch(validUri(pack.resolve("index.toml")),4*1024*1024);verify(indexData,manifest.get("indexSha256").getAsString());
   String ini="version=1\npreset=minecraft\nsource_format=packwiz\nsource="+pack+"\nforce_env=client\nbehavior=auto\nenforce_secure_hashes=true\nupdate_mmc_pack=false\n";
   Path config=root.resolve("unsup.ini");atomic(config,ini.getBytes(java.nio.charset.StandardCharsets.UTF_8));
   info("Comprobando y descargando la versión "+version+"...");
   String unsupHash=settings.getProperty("unsupSha256");verify(Files.readAllBytes(root.resolve("puchamon/unsup.jar")),unsupHash);
   Process updater=new ProcessBuilder(javaExecutable,"-Xmx256m","-Djava.awt.headless=true","-Dunsup.guiInStandalone=false","-jar",root.resolve("puchamon/unsup.jar").toString()).directory(root.toFile()).redirectOutput(ProcessBuilder.Redirect.appendTo(root.resolve("puchamon/logs/updater.log").toFile())).redirectErrorStream(true).start();
   Thread updaterHook=new Thread(()->{if(updater.isAlive())updater.destroy();});Runtime.getRuntime().addShutdownHook(updaterHook);
   try{if(!updater.waitFor(15,java.util.concurrent.TimeUnit.MINUTES)){updater.destroy();throw new IOException("La descarga tardó demasiado. Vuelve a pulsar Jugar.");}}
   finally{Runtime.getRuntime().removeShutdownHook(updaterHook);}
   redactLog(root.resolve("unsup.log"));redactLog(root.resolve("puchamon/logs/updater.log"));
   if(updater.exitValue()!=0)throw new IOException("La descarga no terminó correctamente. Vuelve a pulsar Jugar cuando tengas conexión.");
   if(!consistent(files))throw new IOException("Faltan archivos o no coinciden sus hashes. No se abrirá una versión incompleta.");
   atomic(installed,bytes);
  }
  // A version marker is written only after full consistency validation, never before unsup.
  JsonObject marker=new JsonObject();marker.addProperty("schema",1);marker.addProperty("packId","puchamon-origins");marker.addProperty("release",version);marker.addProperty("compatibility",compatibility);
  atomic(root.resolve("puchamon/release.json"),JSON.toJson(marker).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  return version;
 }
 static boolean defaultAllowed(String path){
  if(path.contains("\\")||path.contains(":")||path.startsWith("/")||Arrays.asList(path.split("/",-1)).stream().anyMatch(x->x.isBlank()||x.equals(".")||x.equals("..")))return false;
  if(path.matches("(?i).*(quickskin|account|password|\\.db|sodium-fingerprint|migaja-controls-ipn-v1.done).*"))return false;
  if(path.contains("xaero")&&!path.matches("config/xaero/(lib|minimap|world-map)/[a-zA-Z0-9_./-]+\\.(cfg|json|txt)"))return false;
  return path.equals("options.txt")||path.startsWith("config/")||path.startsWith("resourcepacks/")||path.startsWith("shaderpacks/")||path.startsWith("fancymenu_data/")||path.startsWith("licenses/")||path.startsWith("emotes/")||path.equals("COBBLEVERSE - Third-Party Licenses.pdf");
 }
 static void installDefaults(JsonObject manifest,URI base)throws Exception{
  if(!manifest.has("defaults"))return;
  Path marker=root.resolve("puchamon/defaults-installed.json");Set<String> seen=new TreeSet<>();
  if(Files.exists(marker))for(JsonElement x:JsonParser.parseString(Files.readString(marker)).getAsJsonArray())seen.add(x.getAsString());
  for(JsonElement e:manifest.getAsJsonArray("defaults")){
   JsonObject f=e.getAsJsonObject();String name=f.get("path").getAsString();
   if(!defaultAllowed(name)||allowed(name))throw new IOException("Valor inicial no permitido: "+name);
   Path target=safePath(name);
   if(!seen.contains(name)&&!Files.exists(target)){
    Files.createDirectories(target.getParent());Path temporary=target.resolveSibling(target.getFileName()+".puchamon-download");
    URI uri=validUri(base.resolve(f.get("url").getAsString()));
    var response=HTTP.send(HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(3)).GET().build(),HttpResponse.BodyHandlers.ofInputStream());
    try(var body=response.body()){
     if(response.statusCode()!=200)throw new IOException("No se pudo descargar un recurso inicial (HTTP "+response.statusCode()+").");
     try(var output=Files.newOutputStream(temporary)){byte[] b=new byte[65536];long count=0;for(int n;(n=body.read(b))!=-1;){count+=n;if(count>512L*1024*1024)throw new IOException("Recurso inicial demasiado grande.");output.write(b,0,n);}}
    }
    if(!hash(temporary).equals(f.get("sha256").getAsString()))throw new IOException("Un recurso inicial no coincide con su hash.");
    Files.move(temporary,target); // Never replace an existing preference.
   }
   seen.add(name);
  }
  atomic(marker,JSON.toJson(seen).getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 static boolean allowed(String path){
  if(path.contains("\\")||path.contains(":")||path.startsWith("/")||Arrays.asList(path.split("/",-1)).stream().anyMatch(x->x.isBlank()||x.equals(".")||x.equals("..")))return false;
  return (path.startsWith("mods/")&&path.endsWith(".jar")&&path.indexOf('/',5)<0)||path.startsWith("datapacks/")||path.startsWith("config/ftbquests/quests/")||SHARED.contains(path)||path.matches("resourcepacks/Puchamon-[^/]+\\.zip");
 }
 static void identity(JsonObject object)throws IOException{if(object.get("schema").getAsInt()!=1||!"puchamon-origins".equals(object.get("packId").getAsString()))throw new IOException("Manifiesto de otro pack o formato incompatible.");}
 static void preflightConflicts(JsonObject previous,JsonArray files)throws Exception{
  Set<String> known=new HashSet<>();if(previous!=null)for(JsonElement e:previous.getAsJsonArray("files"))known.add(e.getAsJsonObject().get("path").getAsString());
  for(JsonElement e:files){JsonObject f=e.getAsJsonObject();Path path=safePath(f.get("path").getAsString());if(Files.exists(path)&&!known.contains(f.get("path").getAsString())&&!hash(path).equals(f.get("sha256").getAsString()))throw new IOException("Hay un archivo ajeno al actualizador en "+f.get("path").getAsString()+". Se ha conservado; revisa la instancia.");}
 }
 static void backup(JsonObject previous,String next)throws Exception{
  if(previous==null)return;
  String name=previous.get("release").getAsString()+"-antes-"+next+"-"+System.currentTimeMillis();
  if(!name.matches("[a-zA-Z0-9._-]+"))throw new IOException("Nombre de respaldo inválido.");
  Path dest=root.resolve("puchamon/backups").resolve(name);Files.createDirectories(dest);
  for(JsonElement e:previous.getAsJsonArray("files")){String p=e.getAsJsonObject().get("path").getAsString();Path source=safePath(p);if(Files.isRegularFile(source)){Path target=dest.resolve(p);Files.createDirectories(target.getParent());Files.copy(source,target);if(!hash(source).equals(hash(target)))throw new IOException("No se pudo verificar el respaldo.");}}
  Files.copy(root.resolve("puchamon/installed-manifest.json"),dest.resolve("installed-manifest.json"));
 }
 static boolean consistent(JsonArray files)throws Exception{for(JsonElement e:files){JsonObject f=e.getAsJsonObject();Path path=safePath(f.get("path").getAsString());if(!Files.isRegularFile(path)||!hash(path).equals(f.get("sha256").getAsString()))return false;}return true;}
 static Path safePath(String relative)throws IOException{
  Path path=root.resolve(relative).normalize();if(!path.startsWith(root)||path.equals(root))throw new IOException("Ruta fuera de la instancia.");
  for(Path p=path;p!=null&&p.startsWith(root);p=p.getParent())if(Files.isSymbolicLink(p)||Files.exists(p)&&!p.toRealPath().startsWith(root))throw new IOException("Enlace fuera del alcance permitido.");
  return path;
 }
 static URI validUri(URI uri)throws IOException{
  if(uri.getUserInfo()!=null||uri.getFragment()!=null||uri.getHost()==null)throw new IOException("Dirección de actualización inválida.");
  if(!uri.getScheme().equals("https")&&!(allowLocal&&uri.getScheme().equals("http")&&Set.of("localhost","127.0.0.1","::1").contains(uri.getHost())))throw new IOException("Las actualizaciones deben usar HTTPS; HTTP solo sirve para pruebas locales.");
  return uri;
 }
 static byte[] fetch(URI uri,int limit)throws Exception{
  var response=HTTP.send(HttpRequest.newBuilder(validUri(uri)).timeout(Duration.ofSeconds(60)).header("User-Agent","Puchamon-Launcher/1").GET().build(),HttpResponse.BodyHandlers.ofInputStream());
  try(var body=response.body()){if(response.statusCode()!=200)throw new IOException("El servidor de actualizaciones no está disponible (HTTP "+response.statusCode()+").");byte[] data=body.readNBytes(limit+1);if(data.length>limit)throw new IOException("Manifiesto demasiado grande.");return data;}
 }
 static JsonObject parse(byte[] data){return JsonParser.parseString(new String(data,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();}
 static String hash(Path path)throws Exception{try(var in=Files.newInputStream(path)){var digest=MessageDigest.getInstance("SHA-256");byte[] b=new byte[65536];for(int n;(n=in.read(b))!=-1;)digest.update(b,0,n);return HexFormat.of().formatHex(digest.digest());}}
 static void verify(byte[] bytes,String expected)throws Exception{if(expected==null||!expected.matches("[0-9a-f]{64}")||!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).equals(expected))throw new IOException("El archivo descargado no coincide con su hash.");}
 static void atomic(Path path,byte[] data)throws IOException{Files.createDirectories(path.getParent());Path tmp=path.resolveSibling(path.getFileName()+".tmp");Files.write(tmp,data);try{Files.move(tmp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){if(Files.exists(path))Files.copy(path,path.resolveSibling(path.getFileName()+".bak"),StandardCopyOption.REPLACE_EXISTING);Files.move(tmp,path,StandardCopyOption.REPLACE_EXISTING);}}
 static String safeMessage(Exception error){return error instanceof IOException?error.getMessage():"Error de validación; revisa la instalación y el registro.";}
 static void redactLog(Path path)throws IOException{if(Files.isRegularFile(path)&&Files.size(path)<16*1024*1024){String text=new String(Files.readAllBytes(path),java.nio.charset.StandardCharsets.ISO_8859_1);String clean=text.replaceAll("/[0-9a-fA-F]{64}/","/[clave-oculta]/");if(!text.equals(clean))atomic(path,clean.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));}}
 static void info(String value){String line="[Puchamon] "+value;System.out.println(line);if(log!=null)log.println(java.time.Instant.now()+" "+line);}
}
