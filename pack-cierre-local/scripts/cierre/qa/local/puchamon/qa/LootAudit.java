package local.puchamon.qa;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/** Reads heavy resource/NBT work off tick; checks registries on tick, in small batches. */
public final class LootAudit {
 private static final Set<String> refs=new TreeSet<>();
 private static final Map<String,Integer> containers=new TreeMap<>();
 private static List<String> checks;private static int offset;private static Set<String> tables;
 private static Object itemKey;private static int resourcesCount,structureCount,structureReferences;
 private static final List<String> failures=new ArrayList<>();
 private static final List<Map<String,Object>> conversions=new ArrayList<>();
 private static long started;
 public static void start(MinecraftServer server)throws Exception{
  started=System.nanoTime();Object lootKey=null;
  for(Field f:class_7924.class.getFields()){Object v=f.get(null);if(v.toString().equals("ResourceKey[minecraft:root / minecraft:loot_table]"))lootKey=v;if(v.toString().equals("ResourceKey[minecraft:root / minecraft:item]"))itemKey=v;}
  if(lootKey==null||itemKey==null)throw new IllegalStateException("No registry keys");
  tables=new TreeSet<>();for(Object id:server.method_58576().method_58290((class_5321)lootKey))tables.add(id.toString());
  var resourceManager=server.method_34864();var loot=resourceManager.method_14488("loot_table",id->id.toString().endsWith(".json"));var structures=resourceManager.method_14488("structure",id->id.toString().endsWith(".nbt"));resourcesCount=loot.size();structureCount=structures.size();
  java.util.concurrent.CompletableFuture.runAsync(()->{try{
   for(var entry:loot.entrySet()){
    String name=entry.getKey().toString().replace(":loot_table/",":");name=name.substring(0,name.length()-5);if(!tables.contains(name))continue;
    try(var reader=entry.getValue().method_43039()){scan(JsonParser.parseReader(reader));}
   }
   for(var entry:structures.entrySet())try(var stream=entry.getValue().method_14482()){
    byte[] bytes=stream.readAllBytes();class_2487 nbt;
    if(bytes.length>2&&(bytes[0]&255)==31&&(bytes[1]&255)==139)nbt=class_2507.method_10629(new ByteArrayInputStream(bytes),class_2505.method_53899(256_000_000L));
    else nbt=class_2507.method_10625(new DataInputStream(new ByteArrayInputStream(bytes)),class_2505.method_53899(256_000_000L));
    var palette=nbt.method_10554("palette",10);if(palette.isEmpty()){var palettes=nbt.method_10554("palettes",9);if(!palettes.isEmpty())palette=palettes.method_10603(0);}
    var blocks=nbt.method_10554("blocks",10);
    for(int i=0;i<blocks.size();i++){var b=blocks.method_10602(i);var data=b.method_10562("nbt");if(!data.method_10573("LootTable",8))continue;
     structureReferences++;refs.add("table\t"+data.method_10558("LootTable"));int index=b.method_10550("state");if(index>=0&&index<palette.size())containers.merge(palette.method_10602(index).method_10558("Name"),1,Integer::sum);
    }
   }
   server.execute(()->{checks=new ArrayList<>(refs);});
  }catch(Throwable e){server.execute(()->{failures.add("resourceAuditException:"+e.getClass().getSimpleName());write(false);});}});
 }
 private static void scan(JsonElement e){
  if(e.isJsonArray()){for(var child:e.getAsJsonArray())scan(child);return;}if(!e.isJsonObject())return;var o=e.getAsJsonObject();String type=o.has("type")?o.get("type").getAsString():"";String f=type.endsWith("loot_table")&&o.has("value")?"value":"name";
  if(o.has(f)&&o.get(f).isJsonPrimitive()){String id=o.get(f).getAsString();if(type.equals("item")||type.equals("minecraft:item"))refs.add("item\t"+id);if(type.equals("tag")||type.equals("minecraft:tag"))refs.add("tag\t"+id);if(type.equals("loot_table")||type.equals("minecraft:loot_table"))refs.add("table\t"+id);}
  for(var child:o.entrySet())scan(child.getValue());
 }
 public static void tick(){
  if(checks==null)return;
  for(int end=Math.min(offset+128,checks.size());offset<end;offset++){
   String line=checks.get(offset);var p=line.split("\t");var id=class_2960.method_60654(p[1]);boolean ok=switch(p[0]){
    case "item"->class_7923.field_41178.method_10250(id);
    case "table"->tables.contains(p[1]);
    case "tag"->class_7923.field_41178.method_40266(class_6862.method_40092((class_5321)itemKey,id)).isPresent();
    default->false;
   };if(!ok)failures.add(line);
  }
  if(offset!=checks.size())return;
  try{
   var api=Class.forName("noobanidus.mods.lootr.common.api.LootrAPI");
   for(var e:containers.entrySet()){
    var block=class_7923.field_41175.method_10223(class_2960.method_60654(e.getKey()));var state=block.method_9564();
    Object replacement=api.getMethod("replacementBlockState",class_2680.class).invoke(null,state);
    Object be=block instanceof class_2343 provider?provider.method_10123(new class_2338(0,200,0),state):null;
    Object adapter=be==null?null:api.getMethod("getAdapter",Object.class).invoke(null,be);
    boolean excluded=e.getKey().equals("minecraft:dispenser"),ok=replacement!=null&&adapter!=null;
    conversions.add(Map.of("block",e.getKey(),"count",e.getValue(),"convertible",ok,"intentionalTrapExclusion",excluded));
    if(!ok&&!excluded)failures.add("container\t"+e.getKey());
   }
   write(failures.isEmpty());
  }catch(Throwable e){failures.add("adapterException:"+e.getClass().getSimpleName());write(false);}
  checks=null;
 }
 private static void write(boolean success){try{
  var data=new LinkedHashMap<String,Object>();data.put("passed",success);data.put("activeTables",tables.size());data.put("tableResources",resourcesCount);data.put("structureFiles",structureCount);data.put("containersWithLootTable",structureReferences);data.put("uniqueReferencesChecked",offset);data.put("containerTypes",conversions);data.put("errors",failures);data.put("elapsedSeconds",(System.nanoTime()-started)/1e9);data.put("heavyReadsOffThread",true);data.put("registryBatchSize",128);data.put("noWorldLootReset",true);
  Files.writeString(FabricLoader.getInstance().getGameDir().resolve("../evidencias/lootr-r6-runtime.json"),new GsonBuilder().setPrettyPrinting().create().toJson(data));System.out.println("[PUCHAMON QA] Lootr audit passed="+success+" errors="+failures.size());
 }catch(Exception e){throw new IllegalStateException(e);}}
}
