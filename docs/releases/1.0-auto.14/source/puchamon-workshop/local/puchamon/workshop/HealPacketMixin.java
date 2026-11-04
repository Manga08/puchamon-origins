package local.puchamon.workshop.mixin;
import local.puchamon.workshop.Healing;
import com.cobblemon_trainers_backpack.network.HealPartyPayload;import dev.architectury.networking.NetworkManager;import net.minecraft.class_3222;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=HealPartyPayload.class,remap=false)
public abstract class HealPacketMixin {
 @Inject(method="lambda$register$2",at=@At("HEAD"),cancellable=true)
 private static void authoritative(NetworkManager.PacketContext context,HealPartyPayload payload,CallbackInfo ci){if(context.getPlayer() instanceof class_3222 p)Healing.handle(p);ci.cancel();}
}
