package local.puchamon.workshop.mixin;
import net.pitan76.uncraftingtable.UncraftingScreenHandler;import net.pitan76.mcpitanlib.api.gui.args.SlotClickEvent;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=UncraftingScreenHandler.class,remap=false)
public abstract class UncraftClicksMixin {
 @Inject(method="onSlotClick",at=@At("HEAD"),cancellable=true)
 private void atomicOutput(SlotClickEvent event,CallbackInfo ci){if(event.getSlot()>=1&&event.getSlot()<=9&&!event.isPickupAction()&&!event.isQuickMoveAction())ci.cancel();}
}
