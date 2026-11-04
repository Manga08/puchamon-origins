package local.puchamon.workshop;
import net.minecraft.*;import net.minecraft.server.MinecraftServer;import java.util.*;import java.nio.file.*;import com.google.gson.*;
import com.cobblemon.mod.common.Cobblemon;import com.cobblemon.mod.common.battles.BattleRegistry;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import com.cobblemon_trainers_backpack.items.Class.heal.HealUpgradeWrapper;
import io.wispforest.accessories.api.AccessoriesCapability;
public final class Healing {
 public static Map<UUID,Long> until=new HashMap<>();private static Path file;
 public static void load(MinecraftServer s){
  file=s.method_27050(class_5218.field_24188).resolve("data/puchamon-healing-cooldowns.json");until.clear();
  if(!Files.exists(file))return;
  try{var j=JsonParser.parseString(Files.readString(file)).getAsJsonObject();for(var e:j.entrySet()){long v=e.getValue().getAsLong();if(v>System.currentTimeMillis())until.put(UUID.fromString(e.getKey()),v);}}
  catch(Exception e){throw new IllegalStateException("Cannot read persistent healing cooldowns",e);}
 }
 private static void save()throws Exception{var j=new JsonObject();until.forEach((id,time)->j.addProperty(id.toString(),time));Files.createDirectories(file.getParent());var tmp=file.resolveSibling(file.getFileName()+".tmp");Files.writeString(tmp,j.toString());Files.move(tmp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
 public static int secondsForTier(int tier){return new int[]{300,270,240,210,180}[Math.max(0,Math.min(4,tier))];}
 public static void say(class_3222 p,String t){p.method_43502(class_2561.method_43470(t),true);}
 public static boolean ready(class_3222 p){
  if(!p.method_5805()||p.method_7325())return false;
  var loader=net.fabricmc.loader.api.FabricLoader.getInstance();
  if(!loader.isModLoaded("easyauth"))return loader.getEnvironmentType()==net.fabricmc.api.EnvType.CLIENT;
  try{return (boolean)p.getClass().getMethod("easyAuth$isAuthenticated").invoke(p);}catch(Exception e){return false;}
 }
 public static int carriedTier(class_3222 p){
  if(!(p.field_7512 instanceof BackpackContainer menu)||!menu.method_7597(p)||menu.getBlockPosition().isPresent())return -1;
  var target=menu.getStorageWrapper().getContentsUuid();if(target.isEmpty())return -1;
  var carried=new ArrayList<class_1799>();for(int i=0;i<p.method_31548().method_5439();i++)carried.add(p.method_31548().method_5438(i));
  var cap=AccessoriesCapability.get(p);if(cap!=null)for(var ref:cap.getAllEquipped())carried.add(ref.stack());
  boolean own=false;for(var stack:carried){if(!class_7923.field_41178.method_10221(stack.method_7909()).toString().startsWith("sophisticatedbackpacks:"))continue;if(BackpackWrapper.fromStack(stack).getContentsUuid().equals(target)){own=true;break;}}
  if(!own)return -1;
  int result=-1;for(var wrapper:menu.getStorageWrapper().getUpgradeHandler().getWrappersThatImplement(HealUpgradeWrapper.class)){
   if(!wrapper.isEnabled())continue;String id=class_7923.field_41178.method_10221(wrapper.getUpgradeStack().method_7909()).toString();
   if(id.equals("cobblemon_trainers_backpack:heal_upgrade"))result=Math.max(0,result);
   else if(id.matches("cobblemon_trainers_backpack:heal_upgrade_tier_[1-4]"))result=Math.max(result,Integer.parseInt(id.substring(id.length()-1)));
  }return result;
 }
 public static void handle(class_3222 p){
  if(!ready(p))return;
  if(BattleRegistry.getBattleByParticipatingPlayer(p)!=null){say(p,"No puedes curar al equipo durante un combate.");return;}
  int tier=carriedTier(p);if(tier<0){say(p,"Abre tu mochila y equipa una mejora de curación para usarla.");return;}
  long now=System.currentTimeMillis(),remaining=until.getOrDefault(p.method_5667(),0L)-now;
  if(remaining>0){say(p,"La curación estará lista en "+((remaining+999)/1000)+" s.");return;}
  var party=Cobblemon.INSTANCE.getStorage().getParty(p);boolean needs=false;for(var pokemon:party){if(pokemon.getEntity()!=null&&pokemon.getEntity().isBusy()){say(p,"Espera a que tu Pokémon termine su acción.");return;}if(pokemon.getCurrentHealth()<pokemon.getMaxHealth()||pokemon.getStatus()!=null)needs=true;}
  if(!needs){say(p,"Tu equipo ya está curado o no tienes Pokémon.");return;}
  until.put(p.method_5667(),now+1000L*secondsForTier(tier));try{save();}catch(Exception e){until.remove(p.method_5667());say(p,"No se pudo guardar la curación. Inténtalo de nuevo.");return;}
  party.heal();say(p,"Equipo curado. Próxima curación en "+secondsForTier(tier)+" s.");
 }
}
