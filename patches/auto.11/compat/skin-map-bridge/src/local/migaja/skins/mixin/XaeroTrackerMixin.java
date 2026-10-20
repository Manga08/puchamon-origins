package local.migaja.skins.mixin;
import local.migaja.skins.SkinFaces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.*;

@Mixin(targets="xaero.hud.minimap.player.tracker.PlayerTrackerMinimapElementRenderer",remap=false)
public abstract class XaeroTrackerMixin {
 @Inject(method="getPlayerSkin",at=@At("RETURN"),cancellable=true,remap=false,require=1)
 private void migaja$head(class_1657 entity,class_640 info,CallbackInfoReturnable<class_2960> result){
  var id=entity!=null?entity.method_5667():info!=null?info.method_2966().getId():null;
  if(id==null)return;var atlas=SkinFaces.atlas(id);if(atlas!=null)result.setReturnValue(atlas);
 }
}
