package local.puchamon.safety;

import net.fabricmc.api.ModInitializer;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_3222;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import xyz.nikitacartes.easyauth.interfaces.PlayerAuth;

/** No periodic scans. Called only when a SecurityCraft explosive would detonate. */
public final class MineSafety implements ModInitializer {
 public static long blocked=0;
 public void onInitialize(){System.out.println("[Puchamon Safety] SecurityCraft detonation guard active (16 blocks; Pokemon/login).");}
 public static boolean blockedNear(class_1937 world,double x,double y,double z){
  if(world.field_9236)return false;
  for(class_1297 e:world.method_8335(null,new class_238(x-16,y-16,z-16,x+16,y+16,z+16))){
   if(!e.method_5805())continue;
   if(shouldProtect(e)){blocked++;return true;}
  }
  return false;
 }
 public static boolean shouldProtect(class_1297 e){
  return e instanceof PokemonEntity
   ||e instanceof class_3222 p&&!((PlayerAuth)p).easyAuth$isAuthenticated();
 }
}
