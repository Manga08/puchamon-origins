package local.migaja.skins.mixin;
import local.migaja.skins.AuthHelloQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.class_3222;
import net.minecraft.class_2596;

@Mixin(targets="xyz.nikitacartes.easyauth.event.AuthEventHandler",remap=false)
public abstract class AuthPacketMixin {
 @Inject(method="isAllowedPacket",at=@At("RETURN"),remap=false,require=1)
 private static void migaja$deferHello(class_3222 player,class_2596<?> packet,CallbackInfoReturnable<Boolean> result){
  if(!result.getReturnValue())AuthHelloQueue.remember(player,packet);
 }
}
