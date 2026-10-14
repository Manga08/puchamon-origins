import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import com.sun.tools.attach.VirtualMachine;

/** Stops only our loopback social test server, and only with no players. */
public final class StopSocial {
 public static void main(String[] a)throws Exception {var vm=VirtualMachine.attach(a[0]);try{vm.loadAgent(a[1]);}finally{vm.detach();}}
 public static void agentmain(String arg, Instrumentation inst)throws Exception {
  ClassLoader l=null;
  for(Class<?> c:inst.getAllLoadedClasses())if(c.getName().equals("com.cobblemon.mod.common.Cobblemon")){l=c.getClassLoader();break;}
  if(l==null)throw new IllegalStateException("Not Cobblemon");
  Class<?> api=Class.forName("net.fabricmc.loader.api.FabricLoader",true,l);
  Object loader=api.getMethod("getInstance").invoke(null);
  Path dir=((Path)api.getMethod("getGameDir").invoke(loader)).toAbsolutePath().normalize();
  String normalized=dir.toString().replace('\\','/');
  if(!normalized.equalsIgnoreCase("C:/Games/minecraft-mods/Cobblemon-Proyecto/prueba-social-r3/server")&&!normalized.equalsIgnoreCase("C:/Games/minecraft-mods/Cobblemon-Proyecto/cierre-rc6/server")&&!normalized.equalsIgnoreCase("C:/Games/minecraft-mods/Cobblemon-Proyecto/cierre-rc6/restauracion-verificada"))throw new IllegalStateException("Wrong test directory");
  Object server=api.getMethod("getGameInstance").invoke(loader);
  ((Executor)server).execute(()->{try{
   Object players=server.getClass().getMethod("method_3760").invoke(server);
   if(((java.util.List<?>)players.getClass().getMethod("method_14571").invoke(players)).size()!=0)throw new IllegalStateException("Players online; refusing stop");
   server.getClass().getMethod("method_3747",boolean.class).invoke(server,false);
  }catch(Exception e){throw new IllegalStateException(e);}});
 }
}
