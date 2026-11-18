package local.puchamon.workshop.mixin;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import local.puchamon.workshop.RewardFlags;
import com.necro.raid.dens.common.raids.rewards.RewardHandler;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidRewardPostEvent;
import net.minecraft.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
/** Raid Dens 0.13 uses async rewards. Complete state and post events on the server. */
@Mixin(value=RewardHandler.class,remap=false)
public abstract class RaidRewardMixin implements RewardFlags {
 @Shadow @Final private UUID playerUUID;
 @Unique private boolean puchamon$itemsGiven,puchamon$moneyGiven;
 @Unique private CompletableFuture<Boolean> puchamon$pendingBag;
 public boolean puchamon$items(){return puchamon$itemsGiven;}
 public boolean puchamon$money(){return puchamon$moneyGiven;}
 public void puchamon$restore(boolean i,boolean m){puchamon$itemsGiven=i;puchamon$moneyGiven=m;}
 @Inject(method="sendRewardMessage",at=@At("HEAD"),cancellable=true)
 private void immediateBag(class_3222 player,CallbackInfo ci){
  if(!playerUUID.equals(player.method_5667())){ci.cancel();return;}
  var self=(RewardHandler)(Object)this;var server=player.method_5682();
  self.giveItemToPlayer(player,false).whenCompleteAsync((success,error)->{
   if(error!=null||!Boolean.TRUE.equals(success)||puchamon$moneyGiven)return;
   self.raidBoss((Consumer<RaidBoss>)boss->{
    if(puchamon$moneyGiven)return;
    puchamon$moneyGiven=true;
    RaidEvents.RAID_REWARD_POST.emit(new RaidRewardPostEvent(boss,player));
   },server);
  },server);
 }
 @Inject(method="giveItemToPlayer",at=@At("HEAD"),cancellable=true)
 private void preventSecondBag(class_3222 player,boolean bonus,CallbackInfoReturnable<CompletableFuture<Boolean>> ci){
  if(!playerUUID.equals(player.method_5667()))ci.setReturnValue(CompletableFuture.completedFuture(false));
  else if(puchamon$itemsGiven)ci.setReturnValue(CompletableFuture.completedFuture(true));
  else if(puchamon$pendingBag!=null)ci.setReturnValue(puchamon$pendingBag);
 }
 @Inject(method="giveItemToPlayer",at=@At("RETURN"),cancellable=true)
 private void rememberBag(class_3222 player,boolean bonus,CallbackInfoReturnable<CompletableFuture<Boolean>> ci){
  if(puchamon$itemsGiven||!playerUUID.equals(player.method_5667())||ci.getReturnValue()==puchamon$pendingBag)return;
  var result=ci.getReturnValue().thenApply(success->{if(Boolean.TRUE.equals(success))puchamon$itemsGiven=true;return success;});
  puchamon$pendingBag=result;
  result.whenComplete((success,error)->{if(error!=null||!Boolean.TRUE.equals(success))puchamon$pendingBag=null;});
  ci.setReturnValue(result);
 }
 @Inject(method="serialize",at=@At("RETURN"))
 private void remember(class_7225.class_7874 provider,CallbackInfoReturnable<class_2487> ci){ci.getReturnValue().method_10556("puchamon_items_given",puchamon$itemsGiven);ci.getReturnValue().method_10556("puchamon_money_given",puchamon$moneyGiven);}
 @Inject(method="deserialize",at=@At("RETURN"))
 private static void restore(class_2487 tag,class_7225.class_7874 provider,CallbackInfoReturnable<RewardHandler> ci){((RewardFlags)ci.getReturnValue()).puchamon$restore(tag.method_10577("puchamon_items_given"),tag.method_10577("puchamon_money_given"));}
 @ModifyArg(method="deserialize",at=@At(value="INVOKE",target="Lnet/minecraft/class_2487;method_10562(Ljava/lang/String;)Lnet/minecraft/class_2487;"),index=0)
 private static String correctSavedPokemonKey(String key){return key.equals("cached_reward")?"pokemon_reward":key;}
}
