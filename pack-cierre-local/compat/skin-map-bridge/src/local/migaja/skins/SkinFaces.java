package local.migaja.skins;

import java.util.*;
import net.minecraft.*;
import com.quickskin.mod.client.services.PlayerAppearanceService;

/** Owns only bounded map textures. Never writes Quick Skin images, maps or waypoints. */
public final class SkinFaces {
 private record Entry(class_2960 source,class_1043 texture,class_1011 pixels,long fingerprint,class_2960 output){}
 private static final Map<UUID,Entry> CACHE=new HashMap<>();
 private static boolean warned;
 private static int builds;
 public static class_2960 source(UUID id){
  var service=PlayerAppearanceService.getInstance();
  return service!=null&&id!=null&&service.hasActiveSkin(id)?service.getSkinLocation(id):null;
 }
 public static class_2960 atlas(UUID id){
  var mc=class_310.method_1551();if(!mc.method_18854())return null;
  class_2960 source=source(id);if(source==null)return null;
  try{
   if(!(mc.method_1531().method_4619(source) instanceof class_1043 texture))return null;
   var pixels=texture.method_4525();if(pixels==null)return null;
   int w=pixels.method_4307(),h=pixels.method_4323();
   if(w<64||w%64!=0||(h!=w&&h!=w/2))return null;
   int unit=w/64;long fingerprint=1;
   for(int y=0;y<8;y++)for(int x=0;x<8;x++)for(int layer=0;layer<2;layer++)fingerprint=31*fingerprint+pixels.method_4315(((layer==0?8:40)+x)*unit,(8+y)*unit);
   Entry old=CACHE.get(id);
   if(old!=null&&old.source().equals(source)&&old.texture()==texture&&old.pixels()==pixels&&old.fingerprint()==fingerprint)return old.output();
   if(old==null&&CACHE.size()>=128)return null;
   var image=normalize(pixels);
   class_2960 output=class_2960.method_60655("migaja_skin_bridge","heads/"+id);
   mc.method_1531().method_4616(output,new class_1043(image));
   CACHE.put(id,new Entry(source,texture,pixels,fingerprint,output));builds++;
   return output;
  }catch(RuntimeException e){if(!warned){warned=true;System.err.println("[Migaja Skin Bridge] Native map fallback: "+e);}return null;}
 }
 public static class_1011 normalize(class_1011 source){
  int w=source.method_4307(),h=source.method_4323();
  if(w<64||w%64!=0||(h!=w&&h!=w/2))throw new IllegalArgumentException("Invalid skin dimensions");
  int unit=w/64;var image=new class_1011(64,64,true);
  // Xaero tracked-player UVs are always a 64-pixel atlas, even if the source skin is HD.
  for(int y=0;y<64;y++)for(int x=0;x<64;x++)image.method_4305(x,y,0);
  for(int y=0;y<8;y++)for(int x=0;x<8;x++)for(int layer=0;layer<2;layer++){int u=layer==0?8:40;image.method_4305(u+x,8+y,source.method_4315((u+x)*unit,(8+y)*unit));}
  return image;
 }
 public static void clear(){
  var mc=class_310.method_1551();for(Entry e:CACHE.values())mc.method_1531().method_4615(e.output());CACHE.clear();
 }
 public static int cacheSize(){return CACHE.size();}
 public static int builds(){return builds;}
}
