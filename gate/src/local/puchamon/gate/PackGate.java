package local.puchamon.gate;
import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.class_2960;
import net.minecraft.class_2561;
import java.nio.file.*;

/** Compatibility gate during LOGIN, before joining the world or reaching EasyAuth. Not an anti-cheat. */
public final class PackGate implements ModInitializer {
 public static final class_2960 CHANNEL=class_2960.method_60655("puchamon","release_check");
 public static String compatibility(){
  try{
   var json=JsonParser.parseString(Files.readString(FabricLoader.getInstance().getGameDir().resolve("puchamon/release.json"))).getAsJsonObject();
   String value=json.get("compatibility").getAsString();
   return "puchamon-origins".equals(json.get("packId").getAsString())&&value.matches("puchamon-[a-zA-Z0-9._-]{1,100}")?value:"";
  }catch(Exception e){return "";}
 }
 public void onInitialize(){
  if(!FabricLoader.getInstance().getEnvironmentType().name().equals("SERVER"))return;
  String required=compatibility();if(required.isEmpty())throw new IllegalStateException("Puchamon requiere puchamon/release.json con la versión del servidor; no se abrirá sin ella.");
  ServerLoginConnectionEvents.QUERY_START.register((handler,server,sender,sync)->{
   var payload=PacketByteBufs.create();payload.method_10804(1);payload.method_10814(required);sender.sendPacket(CHANNEL,payload);
  });
  ServerLoginNetworking.registerGlobalReceiver(CHANNEL,(server,handler,understood,buffer,sync,sender)->{
   boolean match=false;
   try{match=understood&&buffer.method_10816()==1&&required.equals(buffer.method_10800(128))&&buffer.readableBytes()==0;}catch(Exception ignored){}
   if(!match){handler.method_14380(class_2561.method_43470("Tu versión de Puchamon: Origins no es compatible. Cierra el juego y pulsa Jugar en la instancia con actualizador. No borres tus mapas ni tu partida."));}
  });
  System.out.println("[Puchamon] Login requiere compatibilidad "+required);
 }
}
