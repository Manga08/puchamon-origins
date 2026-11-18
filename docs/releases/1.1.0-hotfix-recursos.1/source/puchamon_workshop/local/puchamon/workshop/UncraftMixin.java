package local.puchamon.workshop.mixin;
import local.puchamon.workshop.RecipePolicy;
import net.minecraft.class_1799;import net.pitan76.uncraftingtable.InsertSlot;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=InsertSlot.class,remap=false)
public abstract class UncraftMixin {
 @Inject(method="updateOutSlot",at=@At("HEAD"),cancellable=true)
 private void guarded(class_1799 stack,CallbackInfo ci){RecipePolicy.show((InsertSlot)(Object)this,stack);ci.cancel();}
}
