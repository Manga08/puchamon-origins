package local.migaja.skins;
import net.fabricmc.api.ClientModInitializer;
import java.lang.reflect.*;
public final class SkinClient implements ClientModInitializer {
 public void onInitializeClient(){
  try{
   var l=getClass().getClassLoader();var events=Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents",true,l);
   var callback=Class.forName(events.getName()+"$Disconnect",true,l);
   var listener=Proxy.newProxyInstance(l,new Class<?>[]{callback},(p,m,a)->{if(m.getName().equals("onPlayDisconnect"))SkinFaces.clear();if(m.getName().equals("toString"))return "MigajaSkinDisconnect";return null;});
   Class.forName("net.fabricmc.fabric.api.event.Event",true,l).getMethod("register",Object.class).invoke(events.getField("DISCONNECT").get(null),listener);
  }catch(Exception e){throw new IllegalStateException(e);}
 }
}
