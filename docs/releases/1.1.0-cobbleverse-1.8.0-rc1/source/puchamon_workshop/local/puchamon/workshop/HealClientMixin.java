package local.puchamon.workshop.mixin;
import com.cobblemon_trainers_backpack.client.gui.heal.HealUpgradeTab;import com.cobblemon_trainers_backpack.network.HealPartyPayload;import dev.architectury.networking.NetworkManager;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.Shadow;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=HealUpgradeTab.class,remap=false)
public abstract class HealClientMixin {
 @Inject(method="onTabOpen",at=@At("HEAD"),cancellable=true)
 private void serverDecides(CallbackInfo ci){NetworkManager.sendToServer(new HealPartyPayload(0));ci.cancel();}
}
