package local.puchamon.workshop;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.cobblemon_trainers_backpack.config.CTBConfig;
import sophisticated.building.*;
public final class Workshop implements ModInitializer {
 public void onInitialize(){
  tune();
  ServerLifecycleEvents.SERVER_STARTED.register(s->{tune();RecipePolicy.validate(s);Healing.load(s);});
  ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((s,r,ok)->{if(ok)RecipePolicy.validate(s);});
 }
 public static void tune(){
  CTBConfig.enablePcUpgrade=false;CTBConfig.enableCampfirePotUpgrade=true;CTBConfig.enableHealUpgrade=true;
  CTBConfig.enableHealCooldown=true;CTBConfig.healCooldownSeconds=300;CTBConfig.enableUpgradeTiers=true;
  CTBConfig.reduceTimeToHealTier1=10;CTBConfig.reduceTimeToHealTier2=20;CTBConfig.reduceTimeToHealTier3=30;CTBConfig.reduceTimeToHealTier4=40;
  ServerConfig.validation.maxBlocksPlacedAtOnce.set(256);ServerConfig.memory.undoStackSize.set(10);
  CommonConfig.maxBlocksPlacedAtOnce.level0.set(32);CommonConfig.maxBlocksPlacedAtOnce.level1.set(64);CommonConfig.maxBlocksPlacedAtOnce.level2.set(128);CommonConfig.maxBlocksPlacedAtOnce.level3.set(256);
  CommonConfig.maxBlocksPerAxis.level0.set(8);CommonConfig.maxBlocksPerAxis.level1.set(12);CommonConfig.maxBlocksPerAxis.level2.set(16);CommonConfig.maxBlocksPerAxis.level3.set(24);
  CommonConfig.reach.level1.set(8);CommonConfig.reach.level2.set(12);CommonConfig.reach.level3.set(16);
  ClientConfig.visuals.maxBlockPreviews.set(512);ClientConfig.performance.maxMiniBlockPreviews.set(512);
 }
}
