package local.puchamon.workshop.mixin;
import local.puchamon.workshop.BuildGuard;
import net.minecraft.class_1657;import sophisticated.building.systems.ServerBlockPlacer;import sophisticated.building.utilities.*;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=ServerBlockPlacer.class,remap=false)
public abstract class BuildMixin {
 @org.spongepowered.asm.mixin.Shadow public abstract void applyBlockSet(class_1657 p,BlockSet b);
 @Inject(method="placeBlocksDelayed",at=@At("HEAD"),cancellable=true)
 private void rate(class_1657 p,BlockSet b,long time,CallbackInfo ci){if(BuildGuard.request(p,b))applyBlockSet(p,b);ci.cancel();}
 @Inject(method="validateBlockSet",at=@At("HEAD"),cancellable=true)
 private void limit(class_1657 p,BlockSet b,CallbackInfoReturnable<Boolean> ci){if(b.size()>BuildGuard.limit(p))ci.setReturnValue(false);}
 @Inject(method="validateBlockEntry",at=@At("HEAD"),cancellable=true)
 private void safe(class_1657 p,BlockEntry b,boolean breaking,CallbackInfoReturnable<Boolean> ci){if(!BuildGuard.valid(p,b,breaking))ci.setReturnValue(false);}
}
