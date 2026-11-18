package local.puchamon.radar;
import java.util.*;
import java.lang.reflect.Method;
import com.google.gson.Gson;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
public final class ServerRadar {
 private static int ticks;
 private static final Gson JSON=new Gson();
 private static Method authMethod;
 private static boolean authResolved;
 public static boolean authenticated(class_3222 player){
  if(!FabricLoader.getInstance().isModLoaded("easyauth"))return FabricLoader.getInstance().getEnvironmentType().name().equals("CLIENT");
  try{
   if(!authResolved){authMethod=Class.forName("xyz.nikitacartes.easyauth.interfaces.PlayerAuth").getMethod("easyAuth$isAuthenticated");authResolved=true;}
   return (boolean)authMethod.invoke(player);
  }catch(ReflectiveOperationException e){return false;}
 }
 public static boolean eligible(PokemonEntity entity,class_3222 player){
  double dx=entity.method_23317()-player.method_23317(),dz=entity.method_23321()-player.method_23321();
  return entity.method_5805()&&!entity.method_31481()&&!entity.method_5756(player)&&dx*dx+dz*dz<=Radar.RADIUS*Radar.RADIUS;
 }
 public static Snapshot collect(class_3222 player){
  var world=player.method_51469();
  // World#getEntitiesByClass only queries entity sections already in memory. No getChunk, tickets or generation.
  var entities=world.method_8390(PokemonEntity.class,player.method_5829().method_1014(Radar.RADIUS),e->eligible(e,player));
  entities.sort(Comparator.comparingDouble(e->{double x=e.method_23317()-player.method_23317(),z=e.method_23321()-player.method_23321();return x*x+z*z;}));
  var entries=new ArrayList<Snapshot.Entry>(Math.min(Radar.MAX_ENTRIES,entities.size()));
  for(var entity:entities){if(entries.size()>=Radar.MAX_ENTRIES)break;var pokemon=entity.getPokemon();
   entries.add(new Snapshot.Entry(entity.method_5667(),entity.method_5628(),pokemon.getSpecies().getResourceIdentifier().toString(),pokemon.getForm().getName(),pokemon.getShiny(),List.copyOf(entity.getAspects()),entity.method_23317(),entity.method_23318(),entity.method_23321()));
  }
  return new Snapshot(world.method_27983().method_29177().toString(),entries);
 }
 public static void tick(MinecraftServer server){
  if(++ticks%Radar.INTERVAL_TICKS!=0)return;
  for(var player:server.method_3760().method_14571()){
   if(!ServerPlayNetworking.canSend(player,RadarPayload.ID))continue;
   Snapshot snapshot=authenticated(player)?collect(player):new Snapshot(player.method_51469().method_27983().method_29177().toString(),List.of());
   ServerPlayNetworking.send(player,new RadarPayload(JSON.toJson(snapshot)));
  }
 }
}
