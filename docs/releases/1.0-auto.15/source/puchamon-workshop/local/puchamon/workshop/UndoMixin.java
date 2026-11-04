package local.puchamon.workshop.mixin;
import net.minecraft.class_1657;import sophisticated.building.systems.UndoRedo;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=UndoRedo.class,remap=false)
public abstract class UndoMixin {
 @Inject(method="isAllowedToUndo",at=@At("HEAD"),cancellable=true)
 private void noSurvivalUndo(class_1657 p,CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(false);}
}
