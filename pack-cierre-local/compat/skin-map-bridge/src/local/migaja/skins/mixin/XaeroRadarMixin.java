package local.migaja.skins.mixin;
import local.migaja.skins.SkinFaces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.*;

@Mixin(targets="xaero.hud.minimap.radar.icon.definition.BuiltInRadarIconDefinitions",remap=false)
public abstract class XaeroRadarMixin {
 @Inject(method="getVariant",at=@At("RETURN"),cancellable=true,remap=false,require=1)
 private static void migaja$variant(class_2960 texture,class_897<?> renderer,class_1297 entity,CallbackInfoReturnable<Object> result){
  if(!(entity instanceof class_1657))return;
  var source=SkinFaces.source(entity.method_5667());
  if(source!=null)result.setReturnValue(result.getReturnValue()+"|quickskin:"+source);
 }
}
