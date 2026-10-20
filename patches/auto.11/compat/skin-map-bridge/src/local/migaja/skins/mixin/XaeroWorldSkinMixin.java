package local.migaja.skins.mixin;
import local.migaja.skins.SkinFaces;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The fullscreen map has its own icon manager; the minimap hook cannot reach it. */
@Mixin(targets="xaero.map.radar.tracker.TrackedPlayerIconManager",remap=false)
public abstract class XaeroWorldSkinMixin {
 @Inject(method="getPlayerSkin",at=@At("RETURN"),cancellable=true,remap=false,require=1)
 private void puchamon$worldHead(class_1657 entity,class_640 info,CallbackInfoReturnable<class_2960> result){
  var id=entity!=null?entity.method_5667():info!=null?info.method_2966().getId():null;
  if(id==null)return;var atlas=SkinFaces.worldMapAtlas(id);if(atlas!=null)result.setReturnValue(atlas);
 }
}
