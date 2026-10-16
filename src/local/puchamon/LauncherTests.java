package local.puchamon;
import java.util.*;
public final class LauncherTests {
 public static void main(String[] args)throws Exception{
  var p=new Properties();
  for(int[] scenario:new int[][]{{7680,4096},{8192,4096},{12288,4096},{15360,6144},{16384,6144},{32768,6144}})if(Launcher.memoryLimit(p,scenario[0])!=scenario[1])throw new AssertionError("RAM");
  for(String path:List.of("mods/a.jar","datapacks/a.zip","config/ftbquests/quests/a.snbt","config/securitycraft-server.json","config/inventoryprofilesnext/integrationHints/puchamon-accessories.json"))if(!Launcher.allowed(path))throw new AssertionError(path);
  for(String path:List.of("options.txt","../options.txt","mods/../options.txt","config/quickskin-client.json","xaero/map","saves/test","C:/Users/test","mods/sub/a.jar","mods\\a.jar","puchamon/launcher.properties","config/inventoryprofilesnext/inventoryprofiles.json","config/inventoryprofilesnext/integrationHints/personal.json","config/inventoryprofilesnext/integrationHints/../options.txt"))if(Launcher.allowed(path))throw new AssertionError("Protected "+path);
  p.setProperty("ramMode","manual");p.setProperty("manualMaxMiB","4096");if(Launcher.memoryLimit(p,16384)!=4096)throw new AssertionError("Manual");
  for(String path:List.of("config/a.json","config/xaero/minimap/main.cfg","options.txt","resourcepacks/Example.zip"))if(!ApprovedUpdates.allowed(path))throw new AssertionError(path);
  for(String path:List.of("../config/a.json","config/../options.txt","config/easyauth/main.conf","config/quickskin-client.json","saves/world/level.dat","XaeroWaypoints/a.txt","puchamon/launcher.properties"))if(ApprovedUpdates.allowed(path))throw new AssertionError(path);
  String merged=new String(ApprovedUpdates.mergeOptions("renderDistance:8\nsoundCategory_master:0.7\nkey_key.jump:key.keyboard.space\n".getBytes(),"{\"renderDistance\":6}".getBytes()));
  if(!merged.contains("renderDistance:6")||!merged.contains("soundCategory_master:0.7")||!merged.contains("key_key.jump:key.keyboard.space"))throw new AssertionError("Selective options");
  System.out.println("PASS: RAM, protected paths and explicitly approved selective options.");
 }
}
