package local.puchamon.perf;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;

/** Opt-in local fixture, not a distributable mod. Never changes authentication or key input. */
public final class PerfQa implements ModInitializer {
 private final List<Double> ticks=new ArrayList<>();
 private final List<Pokemon> created=new ArrayList<>();
 private final List<PokemonPastureBlockEntity> pastures=new ArrayList<>();
 private long started; private boolean active; private int elapsed; private UUID owner;
 private boolean allowed(MinecraftServer server){
  var expected=FabricLoader.getInstance().getGameDir().resolve("saves/Puchamon QA visual rc6 - 2026-10").toAbsolutePath().normalize();
  return server.method_27050(class_5218.field_24188).toAbsolutePath().normalize().equals(expected);
 }
 public void onInitialize(){
  CommandRegistrationCallback.EVENT.register((dispatcher,access,environment)->{
   dispatcher.register(class_2170.method_9247("puchamonqa")
    .requires(source->allowed(source.method_9211()))
    .then(class_2170.method_9247("pasturas").executes(ctx->{
     try{setup(ctx.getSource().method_9211(),ctx.getSource().method_9207());return 1;}
     catch(Exception ex){ex.printStackTrace();return 0;}
    }))
    .then(class_2170.method_9247("informe").executes(ctx->{save();return 1;}))
    .then(class_2170.method_9247("retirar").executes(ctx->{cleanup(ctx.getSource().method_9211(),ctx.getSource().method_9207());return 1;})));
  });
  ServerTickEvents.START_SERVER_TICK.register(s->{started=System.nanoTime();});
  ServerTickEvents.END_SERVER_TICK.register(s->{if(active&&allowed(s)){ticks.add((System.nanoTime()-started)/1e6);elapsed++;if(elapsed%1200==0)save();}});
 }
 private void setup(MinecraftServer server,class_3222 player)throws Exception{
  if(active||!created.isEmpty())throw new IllegalStateException("Use retirar first; never duplicate the fixture");
  var world=server.method_30002(); owner=player.method_5667();
  var pc=Cobblemon.INSTANCE.getStorage().getPC(player);
  // Only a high, isolated QA platform, far from the player's initial test position.
  for(int cx=125;cx<=131;cx++)for(int cz=125;cz<=131;cz++)world.method_8402(cx,cz,class_2806.field_12803,true);
  for(int x=2000;x<=2096;x++)for(int z=2000;z<=2096;z++)world.method_8652(new class_2338(x,120,z),class_2246.field_10340.method_9564(),3);
  var block=class_7923.field_41175.method_10223(class_2960.method_60654("cobblemon:pasture"));
  if(block==class_2246.field_10124)throw new IllegalStateException("Missing native pasture block");
  for(int i=0;i<4;i++){
   var pos=new class_2338(2024+(i%2)*48,121,2024+(i/2)*48);
   var state=block.method_9564();
   if(state.method_28498(ludichat.cobbreeding.CustomProperties.BREEDING_ACTIVATED))state=state.method_11657(ludichat.cobbreeding.CustomProperties.BREEDING_ACTIVATED,true);
   world.method_8652(pos,state,3);
   var pasture=(PokemonPastureBlockEntity)world.method_8321(pos);
   pasture.setOwnerId(owner);pasture.setOwnerName("QA local");
   pastures.add(pasture);
   for(int n=0;n<10;n++){
    Pokemon p=PokemonProperties.Companion.parse("eevee level=10 gender="+(n%2==0?"male":"female")).create();
    if(!pc.add(p))throw new IllegalStateException("PC full in QA world");
    created.add(p);
    if(!pasture.tether(player,p,class_2350.field_11043))throw new IllegalStateException("Native tether rejected");
   }
   if(pasture.getTetheredPokemon().size()!=10)throw new IllegalStateException("Wrong pasture population");
  }
  ticks.clear();elapsed=0;active=true;save();
  System.out.println("[PUCHAMON PERF QA] Four native breeding pastures, ten Eevee each. Platform 2000..2096, Y121. No production save touched.");
 }
 private void save(){try{
  var sorted=new ArrayList<>(ticks);Collections.sort(sorted);var map=new LinkedHashMap<String,Object>();
  map.put("scenario","Local integrated server, four native breeding pastures, 40 tethered Pokemon; not multiplayer certification");
  map.put("active",active);map.put("ticks",ticks.size());map.put("nativePastures",pastures.size());map.put("createdPokemon",created.size());
  map.put("tetheredCounts",pastures.stream().map(p->p.getTetheredPokemon().size()).toList());
  map.put("secondsAt20TPS",elapsed/20.0);
  if(!sorted.isEmpty()){map.put("workTickP50Ms",sorted.get(sorted.size()/2));map.put("workTickP95Ms",sorted.get((int)(sorted.size()*.95)));map.put("workTickP99Ms",sorted.get((int)(sorted.size()*.99)));map.put("workTickMaxMs",sorted.getLast());map.put("ticksOver50ms",sorted.stream().filter(t->t>50).count());}
  var path=Path.of("C:/Games/minecraft-mods/Cobblemon-Proyecto/pulido-rc7/evidencias/pasturas-nativas.json");
  Files.writeString(path,new GsonBuilder().setPrettyPrinting().create().toJson(map));
 }catch(Exception e){e.printStackTrace();}}
 private void cleanup(MinecraftServer server,class_3222 player){
  active=false;save();
  for(var pasture:pastures)pasture.releaseAllPokemon(owner);
  var pc=Cobblemon.INSTANCE.getStorage().getPC(player);for(var p:created)pc.remove(p);
  created.clear();pastures.clear();System.out.println("[PUCHAMON PERF QA] Temporary Pokemon removed from QA PC; native platform retained in the separate QA world.");
 }
}
