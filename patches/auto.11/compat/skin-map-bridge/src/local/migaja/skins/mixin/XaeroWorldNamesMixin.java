package local.migaja.skins.mixin;
import net.minecraft.*;
import xaero.map.radar.tracker.PlayerTrackerMapElement;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Names for distant tracker icons; nearby radar already supplies its own labels. */
@Mixin(targets="xaero.map.radar.tracker.PlayerTrackerMapElementRenderer",remap=false)
public abstract class XaeroWorldNamesMixin {
 @ModifyVariable(method="renderElement(Lxaero/map/radar/tracker/PlayerTrackerMapElement;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/class_332;Lnet/minecraft/class_4597$class_4598;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",at=@At("HEAD"),argsOnly=true,ordinal=0,remap=false,require=1)
 private boolean puchamon$stableLabel(boolean hovered){return false;}
 @Inject(method="renderElement(Lxaero/map/radar/tracker/PlayerTrackerMapElement;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/class_332;Lnet/minecraft/class_4597$class_4598;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",at=@At("RETURN"),remap=false,require=1)
 private void puchamon$worldLabel(PlayerTrackerMapElement<?> element,boolean hovered,double depth,float scale,double partialX,double partialY,ElementRenderInfo info,class_332 graphics,class_4597.class_4598 buffers,MultiTextureRenderTypeRendererProvider provider,CallbackInfoReturnable<Boolean> result){
  if(element.wasRenderedOnRadar())return;
  var client=class_310.method_1551();var connection=client.method_1562();if(connection==null)return;
  var player=connection.method_2871(element.getPlayerId());if(player==null)return;
  String name=player.method_2966().getName();var matrix=graphics.method_51448();
  matrix.method_22903();matrix.method_22904(partialX,partialY-20,0.02);matrix.method_22905(0.85f,0.85f,1);
  graphics.method_51433(client.field_1772,name,-client.field_1772.method_1727(name)/2,0,0xFFFFFFFF,true);
  matrix.method_22909();
 }
}
