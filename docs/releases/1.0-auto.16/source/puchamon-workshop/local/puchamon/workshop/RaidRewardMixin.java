package local.puchamon.workshop.mixin;
import java.util.UUID;import local.puchamon.workshop.RewardFlags;import com.necro.raid.dens.common.raids.rewards.RewardHandler;import net.minecraft.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=RewardHandler.class,remap=false)
public abstract class RaidRewardMixin implements RewardFlags {
 @Shadow @Final private UUID playerUUID;@Unique private boolean puchamon$itemsGiven;@Unique private boolean puchamon$moneyGiven;
 public boolean puchamon$items(){return puchamon$itemsGiven;}public boolean puchamon$money(){return puchamon$moneyGiven;}public void puchamon$restore(boolean i,boolean m){puchamon$itemsGiven=i;puchamon$moneyGiven=m;}
 @Inject(method="sendRewardMessage",at=@At("HEAD"),cancellable=true)
 private void immediateBag(class_3222 player,CallbackInfo ci){if(!playerUUID.equals(player.method_5667())){ci.cancel();return;}var self=(RewardHandler)(Object)this;if(self.giveItemToPlayer(player,false))self.giveCurrency(player);}
 @Inject(method="giveItemToPlayer",at=@At("HEAD"),cancellable=true)
 private void preventSecondBag(class_3222 player,boolean bonus,CallbackInfoReturnable<Boolean> ci){if(!playerUUID.equals(player.method_5667()))ci.setReturnValue(false);else if(puchamon$itemsGiven)ci.setReturnValue(true);}
 @Inject(method="giveItemToPlayer",at=@At("RETURN"))
 private void bagGiven(class_3222 player,boolean bonus,CallbackInfoReturnable<Boolean> ci){if(Boolean.TRUE.equals(ci.getReturnValue())&&playerUUID.equals(player.method_5667()))puchamon$itemsGiven=true;}
 @Inject(method="giveCurrency",at=@At("HEAD"),cancellable=true)
 private void preventSecondMoney(class_3222 player,CallbackInfo ci){if(puchamon$moneyGiven||!playerUUID.equals(player.method_5667())){ci.cancel();return;}puchamon$moneyGiven=true;}
 @Inject(method="serialize",at=@At("RETURN"))
 private void remember(class_7225.class_7874 provider,CallbackInfoReturnable<class_2487> ci){ci.getReturnValue().method_10556("puchamon_items_given",puchamon$itemsGiven);ci.getReturnValue().method_10556("puchamon_money_given",puchamon$moneyGiven);}
 @Inject(method="deserialize",at=@At("RETURN"))
 private static void restore(class_2487 tag,class_7225.class_7874 provider,CallbackInfoReturnable<RewardHandler> ci){((RewardFlags)ci.getReturnValue()).puchamon$restore(tag.method_10577("puchamon_items_given"),tag.method_10577("puchamon_money_given"));}
 @ModifyArg(method="deserialize",at=@At(value="INVOKE",target="Lnet/minecraft/class_2487;method_10562(Ljava/lang/String;)Lnet/minecraft/class_2487;"),index=0)
 private static String correctSavedPokemonKey(String key){return key.equals("cached_reward")?"pokemon_reward":key;}
}
