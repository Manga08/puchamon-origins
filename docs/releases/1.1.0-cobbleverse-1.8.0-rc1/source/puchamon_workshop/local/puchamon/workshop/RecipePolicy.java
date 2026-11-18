package local.puchamon.workshop;
import net.minecraft.*;import net.minecraft.server.MinecraftServer;import com.google.gson.*;import com.mojang.serialization.JsonOps;import java.util.*;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import net.pitan76.uncraftingtable.*;
public final class RecipePolicy {
 public static volatile boolean valid=false;public static String observedFingerprint="";public static JsonObject rules=new JsonObject();
 public static JsonElement sorted(JsonElement j){if(j.isJsonObject()){var out=new JsonObject();new TreeMap<>(j.getAsJsonObject().asMap()).forEach((k,v)->out.add(k,sorted(v)));return out;}if(j.isJsonArray()){var a=new JsonArray();j.getAsJsonArray().forEach(v->a.add(sorted(v)));return a;}return j;}
 public static String fingerprint(MinecraftServer s)throws Exception{
  var gson=new GsonBuilder().disableHtmlEscaping().create();var rows=new TreeMap<String,String>();
  for(var e:s.method_3772().method_8126()){var json=class_1860.field_47319.encodeStart(s.method_30611().method_57093(JsonOps.INSTANCE),e.comp_1933()).getOrThrow();rows.put(e.comp_1932().toString(),gson.toJson(sorted(json)));}
  var join=new StringBuilder();rows.forEach((k,v)->{if(!join.isEmpty())join.append('\n');join.append(k).append('|').append(v);});
  return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(join.toString().getBytes(StandardCharsets.UTF_8)));
 }
 public static void validate(MinecraftServer s){valid=false;try(var in=RecipePolicy.class.getResourceAsStream("/puchamon-uncrafting-policy.json")){
   var kept=s.method_3772().method_8126().stream().filter(e->!e.comp_1932().toString().equals("cobblemon_sophisticated_backpacks:pc_upgrade")).toList();s.method_3772().method_20702(kept);
   if(in==null)throw new IllegalStateException("Missing embedded audited recipes");
   var p=JsonParser.parseString(new String(in.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();observedFingerprint=fingerprint(s);
   if(!observedFingerprint.equals(p.get("recipeSetSha256").getAsString()))throw new IllegalStateException("Recipe set changed: "+observedFingerprint);
   rules=p.getAsJsonObject("rules");valid=true;System.out.println("[Puchamon Workshop] Audited uncrafting enabled: "+rules.size()+" outputs; recipe set "+observedFingerprint);
  }catch(Exception e){rules=new JsonObject();System.err.println("[Puchamon Workshop] Uncrafting disabled safely: "+e);}}
 public static void show(InsertSlot slot,class_1799 input){
  if(slot.player.isClient())return;
  if(!(slot.player.getCurrentScreenHandler() instanceof UncraftingScreenHandler menu))return;
  slot.canGet=false;slot.latestOutputCount=0;slot.latestOutRecipes=List.of();slot.recipeIndex=0;slot.tagItemIndex=0;
  for(int i=1;i<=9;i++)((OutSlot)menu.callGetSlot(i)).superSetStack(class_1799.field_8037);
  if(!valid||input.method_7960()||!class_1799.method_31577(input,new class_1799(input.method_7909())))return;
  String id=class_7923.field_41178.method_10221(input.method_7909()).toString();if(!rules.has(id))return;
  JsonObject rule=rules.getAsJsonObject(id);int batch=rule.get("consume").getAsInt();if(input.method_7947()<batch)return;
  var values=rule.getAsJsonArray("slots");for(int i=0;i<values.size();i++){String item=values.get(i).getAsString();if(item.equals("minecraft:air"))continue;var actual=class_7923.field_41178.method_10223(class_2960.method_60654(item));((OutSlot)menu.callGetSlot(i+1)).superSetStack(new class_1799(actual));}
  slot.latestOutputCount=batch;slot.latestItemStack=input.method_7972();slot.canGet=true;
 }
}
