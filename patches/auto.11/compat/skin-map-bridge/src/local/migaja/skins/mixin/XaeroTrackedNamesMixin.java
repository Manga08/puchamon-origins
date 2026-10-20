package local.migaja.skins.mixin;
import net.minecraft.*;
import xaero.hud.minimap.player.tracker.PlayerTrackerMinimapElement;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Label remote tracked heads too. Nearby entity labels remain Xaero's native radar. */
@Mixin(targets="xaero.hud.minimap.player.tracker.PlayerTrackerMinimapElementRenderer",remap=false)
public abstract class XaeroTrackedNamesMixin {
 @Inject(method="renderElement(Lxaero/hud/minimap/player/tracker/PlayerTrackerMinimapElement;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lnet/minecraft/class_332;Lnet/minecraft/class_4597$class_4598;)Z",at=@At("RETURN"),remap=false,require=1)
 private void puchamon$label(PlayerTrackerMinimapElement<?> element,boolean highlighted,boolean outOfBounds,double depth,float scale,double partialX,double partialY,MinimapElementRenderInfo info,class_332 graphics,class_4597.class_4598 buffers,CallbackInfoReturnable<Boolean> result){
  if(!result.getReturnValueZ()||info.location==MinimapElementRenderLocation.IN_WORLD)return;
  var client=class_310.method_1551();var connection=client.method_1562();if(connection==null)return;
  var player=connection.method_2871(element.getPlayerId());if(player==null)return;
  String name=player.method_2966().getName();var matrix=graphics.method_51448();
  matrix.method_22903();matrix.method_22904(0,-15,0.02);matrix.method_22905(0.7f,0.7f,1);
  graphics.method_51433(client.field_1772,name,-client.field_1772.method_1727(name)/2,0,0xFFFFFFFF,true);
  matrix.method_22909();
 }
}
