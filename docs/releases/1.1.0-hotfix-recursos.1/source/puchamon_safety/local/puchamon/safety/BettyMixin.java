package local.puchamon.safety.mixin;
import local.puchamon.safety.MineSafety;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="net.geforcemods.securitycraft.entity.BouncingBetty",remap=false)
public abstract class BettyMixin {
 @Inject(method="explode",at=@At("HEAD"),cancellable=true,require=1,remap=false)
 private void puchamon$guard(CallbackInfo ci){
  class_1297 e=(class_1297)(Object)this;
  if(MineSafety.blockedNear(e.method_37908(),e.method_23317(),e.method_23318(),e.method_23321())){e.method_31472();ci.cancel();}
 }
}
