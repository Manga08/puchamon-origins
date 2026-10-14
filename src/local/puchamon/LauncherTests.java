package local.puchamon;
import java.util.*;
public final class LauncherTests {
 public static void main(String[] args)throws Exception{
  var p=new Properties();
  for(int[] scenario:new int[][]{{7680,4096},{8192,4096},{12288,4096},{15360,6144},{16384,6144},{32768,6144}})if(Launcher.memoryLimit(p,scenario[0])!=scenario[1])throw new AssertionError("RAM");
  for(String path:List.of("mods/a.jar","datapacks/a.zip","config/ftbquests/quests/a.snbt","config/securitycraft-server.json"))if(!Launcher.allowed(path))throw new AssertionError(path);
  for(String path:List.of("options.txt","../options.txt","mods/../options.txt","config/quickskin-client.json","xaero/map","saves/test","C:/Users/test","mods/sub/a.jar","mods\\a.jar","puchamon/launcher.properties"))if(Launcher.allowed(path))throw new AssertionError("Protected "+path);
  p.setProperty("ramMode","manual");p.setProperty("manualMaxMiB","4096");if(Launcher.memoryLimit(p,16384)!=4096)throw new AssertionError("Manual");
  System.out.println("PASS: physical RAM thresholds, manual RAM and closed protected-file policy.");
 }
}
