package local.migaja.skins;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.*;

/** Authentication integration is server-only; no blanket custom-packet exception. */
public final class SkinBridge implements ModInitializer {
 public void onInitialize(){
  String dependency=FabricLoader.getInstance().getEnvironmentType().name().equals("SERVER")?"easyauth":"xaerominimap";
  String expected=dependency.equals("easyauth")?"3.4.4":"26.6.0";
  String actual=FabricLoader.getInstance().getModContainer(dependency).orElseThrow().getMetadata().getVersion().getFriendlyString();
  if(!expected.equals(actual))throw new IllegalStateException("Bridge requires validated "+dependency+" "+expected+", not "+actual);
  if(FabricLoader.getInstance().getEnvironmentType().name().equals("SERVER")){
   try{
    ClassLoader l=getClass().getClassLoader();
    Class<?> events=Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents",true,l);
    Class<?> callback=Class.forName(events.getName()+"$EndTick",true,l);
    Object listener=Proxy.newProxyInstance(l,new Class<?>[]{callback},(proxy,method,args)->{
     if(method.getName().equals("onEndTick"))AuthHelloQueue.tick(args[0]);
     if(method.getName().equals("toString"))return "MigajaSkinAuthTick";
     return null;
    });
    Class.forName("net.fabricmc.fabric.api.event.Event",true,l).getMethod("register",Object.class).invoke(events.getField("END_SERVER_TICK").get(null),listener);
   }catch(Exception e){throw new IllegalStateException("Unable to initialize authenticated skins bridge",e);}
  }
 }
}
