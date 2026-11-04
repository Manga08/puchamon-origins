package local.puchamon.workshop.mixin;
import com.necro.raid.dens.common.raids.RaidInstance;import com.necro.raid.dens.common.data.raid.RaidBoss;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=RaidInstance.class,remap=false)
public abstract class RaidVictoryMixin {
 @Shadow public abstract boolean isFinished();@Unique private boolean puchamon$settling;
 @Inject(method="stopRaid",at=@At("HEAD"),cancellable=true)
 private void settleOnce(boolean success,CallbackInfo ci){if(puchamon$settling||isFinished()){ci.cancel();return;}puchamon$settling=true;}
 @Redirect(method="handleSuccess",at=@At(value="INVOKE",target="Lcom/necro/raid/dens/common/data/raid/RaidBoss;getRequiredDamage()Ljava/lang/Float;"))
 private Float noDamageGate(RaidBoss boss){return 0f;}
}
