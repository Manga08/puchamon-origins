package local.puchamon.workshop.mixin;
import com.cobblemon_trainers_backpack.network.OpenPcPayload;import dev.architectury.networking.NetworkManager;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=OpenPcPayload.class,remap=false)
public abstract class PcPacketMixin {
 @Inject(method="lambda$register$0",at=@At("HEAD"),cancellable=true)
 private static void disable(NetworkManager.PacketContext context,CallbackInfo ci){ci.cancel();}
}
