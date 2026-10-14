package local.puchamon.qa;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import local.puchamon.safety.MineSafety;
import com.fibermc.essentialcommands.text.ECText;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import java.lang.management.ManagementFactory;

/** Test-only fixture. Refuses every directory except our isolated closure copy. */
public final class LocalQa implements ModInitializer {
 private static final Map<String,Object> results=new LinkedHashMap<>();
 private static final List<Double> tickMs=new ArrayList<>();
 private static long tickStart;private static int ticks;private static boolean ready;
 public void onInitialize(){
  Path dir=FabricLoader.getInstance().getGameDir().toAbsolutePath().normalize();
  if(!dir.toString().replace('\\','/').equalsIgnoreCase("C:/Games/minecraft-mods/Cobblemon-Proyecto/cierre-rc6/server"))throw new IllegalStateException("QA is never a production mod");
  ServerLifecycleEvents.SERVER_STARTED.register(server->{ready=true;});
  ServerTickEvents.START_SERVER_TICK.register(server->{tickStart=System.nanoTime();});
  ServerTickEvents.END_SERVER_TICK.register(server->{if(!ready)return;ticks++;
   if(ticks>100&&ticks<1300)tickMs.add((System.nanoTime()-tickStart)/1e6);
   if(ticks==100)try{check(server);}catch(Throwable e){results.put("failure",e.toString());e.printStackTrace();save();}
   if(ticks==1300){var sorted=new ArrayList<>(tickMs);Collections.sort(sorted);results.put("idleTickSamples",sorted.size());results.put("idleTickP50Ms",sorted.get(sorted.size()/2));results.put("idleTickP95Ms",sorted.get((int)(sorted.size()*.95)));results.put("heapUsedMiB",ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()/1048576);results.put("maxHeapMiB",Runtime.getRuntime().maxMemory()/1048576);results.put("players",server.method_3760().method_14571().size());save();System.out.println("[PUCHAMON QA] completed; report saved (idle, not multiplayer benchmark).");}
   if(ticks==1350)try{LootAudit.start(server);}catch(Exception e){e.printStackTrace();}
   if(ticks>1350)LootAudit.tick();
  });
 }
 private static void require(boolean ok,String name){results.put(name,ok);if(!ok)throw new AssertionError(name);}
 private static void save(){try{Files.writeString(FabricLoader.getInstance().getGameDir().resolve("../evidencias/qa-runtime.json"),new GsonBuilder().setPrettyPrinting().create().toJson(results));}catch(Exception e){throw new IllegalStateException(e);}}
 private static void check(MinecraftServer server)throws Exception {
  results.put("actualServer",true);results.put("timezone",java.time.ZoneId.systemDefault().getId());
  int missing=0,refs=0;for(String line:Files.readAllLines(FabricLoader.getInstance().getGameDir().resolve("../evidencias/referencias.tsv"))){
   var parts=line.split("\t");var id=class_2960.method_60654(parts[1]);refs++;
   if(parts[0].equals("item")?!class_7923.field_41178.method_10250(id):server.method_3851().method_12896(id)==null){System.out.println("[PUCHAMON QA] Missing reference "+line);missing++;}
  }
  results.put("references",refs);results.put("missingReferences",missing);require(missing==0,"allGuideReferencesExist");
  Object questFile=Class.forName("dev.ftb.mods.ftbquests.quest.ServerQuestFile").getField("INSTANCE").get(null);
  var loadedTasks=(java.util.List<?>)questFile.getClass().getMethod("getAllTasks").invoke(questFile);int emptyTasks=0;
  for(Object task:loadedTasks)if(task.getClass().getSimpleName().equals("ItemTask")){Object stack=task.getClass().getMethod("getItemStack").invoke(task);if((boolean)stack.getClass().getMethod("method_7960").invoke(stack))emptyTasks++;}
  results.put("loadedQuestTasks",loadedTasks.size());require(loadedTasks.size()==229&&emptyTasks==0,"nativeQuestLoadNoEmptyItemTasks");
  require(ECText.getInstance().getString("teleport.queued").startsWith("Teletransporte"),"essentialCommandsSpanishLoaded");
  var world=server.method_30002();var pos=new class_2338(40,250,40);world.method_8402(2,2,class_2806.field_12803,true);
  var pokemon=PokemonProperties.Companion.parse("pikachu level=10").createEntity(world);pokemon.method_5814(42.5,250,40.5);world.method_8649(pokemon);
  float hp=pokemon.method_6032();var kinds=List.of("dirt_mine","mine","claymore","bouncing_betty","track_mine");
  try{
   require(MineSafety.shouldProtect(pokemon),"pokemonProtectionPredicate");
   for(String name:kinds){
    var block=class_7923.field_41175.method_10223(class_2960.method_60654("securitycraft:"+name));
    world.method_8652(pos.method_10074(),class_2246.field_10340.method_9564(),3);
    world.method_8652(pos,block.method_9564(),3);
    long before=MineSafety.blocked;block.getClass().getMethod("explode",class_1937.class,class_2338.class).invoke(block,world,pos);
    require(MineSafety.blocked>before&&world.method_8320(pos).method_26204()==block,"guardBlock_"+name);
    world.method_8652(pos,class_2246.field_10124.method_9564(),3);
   }
   Class<?> betty=Class.forName("net.geforcemods.securitycraft.entity.BouncingBetty");
   Object b=betty.getConstructor(class_1937.class,double.class,double.class,double.class).newInstance(world,40.5,250.0,40.5);
   var explode=betty.getDeclaredMethod("explode");explode.setAccessible(true);long count=MineSafety.blocked;explode.invoke(b);require(MineSafety.blocked>count,"guardBouncingEntity");
   Class<?> ims=Class.forName("net.geforcemods.securitycraft.entity.IMSBomb");
   var type=(class_1299<?>)class_7923.field_41177.method_10223(class_2960.method_60654("securitycraft:ims_bomb"));
   Object bomb=ims.getConstructor(class_1299.class,class_1937.class).newInstance(type,world);((class_1297)bomb).method_5814(40.5,250,40.5);
   var impact=ims.getDeclaredMethod("method_7488",class_239.class);impact.setAccessible(true);count=MineSafety.blocked;
   impact.invoke(bomb,new class_3965(new class_243(40.5,250,40.5),class_2350.field_11036,pos,false));require(MineSafety.blocked>count,"guardImsEntity");
   var fake=new class_3222(server,world,new com.mojang.authlib.GameProfile(UUID.nameUUIDFromBytes("OfflinePlayer:QaAuthFixture".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"QaAuthFixture"),class_8791.method_53821());
   require(!((xyz.nikitacartes.easyauth.interfaces.PlayerAuth)fake).easyAuth$isAuthenticated()&&MineSafety.shouldProtect(fake),"nativeUnauthenticatedPlayerProtected");
   require(pokemon.method_6032()==hp,"pokemonHealthUnchanged");
  }finally{pokemon.method_31472();world.method_8652(pos,class_2246.field_10124.method_9564(),3);}
  require(!MineSafety.blockedNear(world,1000000,250,1000000),"noGlobalExplosionBan");
  // An actual ordinary mine still detonates in the empty arena; nearby terrain survives.
  var mine=class_7923.field_41175.method_10223(class_2960.method_60654("securitycraft:dirt_mine"));
  world.method_8652(pos,mine.method_9564(),3);world.method_8652(pos.method_10069(2,0,0),class_2246.field_10340.method_9564(),3);
  mine.getClass().getMethod("explode",class_1937.class,class_2338.class).invoke(mine,world,pos);
  require(world.method_8320(pos).method_26215(),"mineStillDetonatesWithoutProtectedEntities");
  require(world.method_8320(pos.method_10069(2,0,0)).method_26204()==class_2246.field_10340,"mineDoesNotBreakNearbyTerrain");
  results.put("blockedDetonations",MineSafety.blocked);save();
 }
}
