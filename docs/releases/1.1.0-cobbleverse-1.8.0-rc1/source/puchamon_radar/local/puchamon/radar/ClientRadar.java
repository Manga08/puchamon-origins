package local.puchamon.radar;
import java.util.*;
import com.google.gson.Gson;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.*;
public final class ClientRadar implements ClientModInitializer {
 private record Cached(String appearance,PokemonEntity entity){}
 private static final Gson JSON=new Gson();
 private static final Map<UUID,Cached> CACHE=new HashMap<>();
 private static List<Snapshot.Entry> entries=List.of();
 private static String dimension="";
 private static long received;
 private static class_638 cacheWorld;
 private static int nextProxyId=-1000000;
 public void onInitializeClient(){
  ClientPlayNetworking.registerGlobalReceiver(RadarPayload.ID,(payload,context)->context.client().execute(()->accept(payload.json())));
  ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->clear());
 }
 public static void accept(String json){
  Snapshot packet=JSON.fromJson(json,Snapshot.class);
  if(packet==null||packet.entries()==null||packet.entries().size()>Radar.MAX_ENTRIES)return;
  entries=List.copyOf(packet.entries());dimension=packet.dimension();received=System.nanoTime();
 }
 public static void clear(){entries=List.of();CACHE.clear();dimension="";cacheWorld=null;received=0;nextProxyId=-1000000;}
 public static int cacheSize(){return CACHE.size();}
 public static Iterable<class_1297> augment(class_638 world,Iterable<class_1297> nativeEntities){
  var client=class_310.method_1551();if(client.field_1724==null||world!=client.field_1687)return nativeEntities;
  if(cacheWorld!=world){CACHE.clear();cacheWorld=world;}
  if(System.nanoTime()-received>3_000_000_000L||!world.method_27983().method_29177().toString().equals(dimension)){CACHE.clear();return nativeEntities;}
  var result=new ArrayList<class_1297>();var present=new HashSet<UUID>();
  for(var entity:nativeEntities){result.add(entity);present.add(entity.method_5667());}
  var retained=new HashSet<UUID>();
  for(var e:entries){
   if(e.uuid()==null||e.species()==null||present.contains(e.uuid()))continue;
   double dx=e.x()-client.field_1724.method_23317(),dz=e.z()-client.field_1724.method_23321();
   if(!Double.isFinite(e.x()+e.y()+e.z())||dx*dx+dz*dz>Radar.RADIUS*Radar.RADIUS)continue;
   var appearance=e.species()+"|"+e.form()+"|"+e.shiny()+"|"+e.aspects();var cached=CACHE.get(e.uuid());
   if(cached==null||!appearance.equals(cached.appearance())){
    var species=PokemonSpecies.getByIdentifier(class_2960.method_60654(e.species()));if(species==null)continue;
    var pokemon=new Pokemon();pokemon.setSpecies(species);pokemon.setLevel(1);pokemon.setShiny(e.shiny());
    if(e.aspects()!=null)pokemon.setForcedAspects(new HashSet<>(e.aspects()));pokemon.updateAspects();
    var form=species.getFormByName(e.form());if(form!=null)pokemon.setForm(form);
    var entity=new PokemonEntity(world,pokemon,CobblemonEntities.POKEMON);entity.method_5826(e.uuid());entity.method_5838(nextProxyId--);
    // These objects are not spawned into ClientWorld, never tick and are used solely by Xaero's radar iterator.
    cached=new Cached(appearance,entity);CACHE.put(e.uuid(),cached);
   }
   var proxy=cached.entity();proxy.method_5814(e.x(),e.y(),e.z());proxy.field_6014=e.x();proxy.field_6036=e.y();proxy.field_5969=e.z();
   result.add(proxy);retained.add(e.uuid());
  }
  CACHE.keySet().retainAll(retained);return result;
 }
}
