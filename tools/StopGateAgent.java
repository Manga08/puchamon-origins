import java.lang.instrument.Instrumentation;import java.util.concurrent.Executor;import java.nio.file.Path;import com.sun.tools.attach.VirtualMachine;
public final class StopGateAgent {
 public static void main(String[] args)throws Exception{var vm=VirtualMachine.attach(args[0]);try{vm.loadAgent(args[1]);}finally{vm.detach();}}
 public static void agentmain(String args,Instrumentation instrumentation)throws Exception{
  for(var type:instrumentation.getAllLoadedClasses())if(type.getName().equals("net.minecraft.server.MinecraftServer")){
   var loader=type.getClassLoader();var fabric=Class.forName("net.fabricmc.loader.api.FabricLoader",true,loader);var api=fabric.getMethod("getInstance").invoke(null);var dir=(Path)fabric.getMethod("getGameDir").invoke(api);
   if(!dir.toAbsolutePath().toString().replace('\\','/').endsWith("/Cobblemon-Proyecto/distribucion/build/gate-server"))throw new IllegalStateException("Only the isolated gate test server can be stopped");
   var server=fabric.getMethod("getGameInstance").invoke(api);((Executor)server).execute(()->{try{type.getMethod("method_3747",boolean.class).invoke(server,false);}catch(Exception e){throw new RuntimeException(e);}});return;
  }
  throw new IllegalStateException("Not a Minecraft server");
 }
}
