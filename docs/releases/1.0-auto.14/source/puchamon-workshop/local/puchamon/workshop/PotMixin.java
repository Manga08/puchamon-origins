package local.puchamon.workshop.mixin;
import com.cobblemon_trainers_backpack.items.Class.campfire_pot.CampfirePotUpgradeWrapper;import net.minecraft.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=CampfirePotUpgradeWrapper.class,remap=false)
public abstract class PotMixin {
 @Shadow @Final private class_1277 inventory;
 @Inject(method="tick",at=@At("HEAD"),cancellable=true)
 private void preserveStackedContainers(class_1297 entity,class_1937 world,class_2338 pos,CallbackInfo ci){for(int i=0;i<9;i++){var s=inventory.method_5438(i);if(s.method_7947()>1&&s.method_7909().method_7857()){ci.cancel();return;}}}
}
