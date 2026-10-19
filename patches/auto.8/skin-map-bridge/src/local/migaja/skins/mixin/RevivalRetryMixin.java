package local.migaja.skins.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.Shadow;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** The upstream abort handler clears the target, but leaves the local rescue latch stuck. */
@Mixin(targets="net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient",remap=false)
public abstract class RevivalRetryMixin{
 @Shadow private static boolean isRescuing;
 @Inject(method="setRevivalProgress",at=@At("HEAD"),remap=false,require=1)
 private static void puchamon$clearAbortedRescue(int entityId,float progress,CallbackInfo ci){if(progress<0)isRescuing=false;}
}
