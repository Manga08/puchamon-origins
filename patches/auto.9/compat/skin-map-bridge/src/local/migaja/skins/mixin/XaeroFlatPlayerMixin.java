package local.migaja.skins.mixin;
import local.migaja.skins.SkinFaces;import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xaero.hud.minimap.radar.icon.definition.form.sprite.RadarIconSpriteForm;
import xaero.hud.minimap.radar.icon.definition.form.type.RadarIconFormTypes;
/** Nearby players use a flat face, avoiding third-party player-model prerender failures. */
@Mixin(targets="xaero.hud.minimap.radar.icon.RadarIconManager",remap=false)
public abstract class XaeroFlatPlayerMixin{
 @ModifyArgs(method="get",at=@At(value="INVOKE",target="Lxaero/hud/minimap/radar/icon/creator/RadarIconCreator$Parameters;<init>(Ljava/lang/Object;Lxaero/hud/minimap/radar/icon/definition/form/model/config/RadarIconModelConfig;Lxaero/hud/minimap/radar/icon/definition/form/RadarIconForm;FZ)V"),remap=false,require=1)
 private void puchamon$flatFace(Args args,class_1297 entity,float scale,boolean debug,boolean variants,class_332 graphics,class_276 framebuffer){
  if(!(entity instanceof class_1657))return;var texture=SkinFaces.radarSprite(entity.method_5667());
  if(texture!=null){
   args.set(2,new RadarIconSpriteForm(RadarIconFormTypes.NORMAL_SPRITE,texture));
   // Xaero's sprite prerenderer always draws a 64x64 canvas, independently of
   // texture resolution. Render the face at 8 logical pixels, not a 64px square.
   args.set(3,Math.min(1.0f,Math.max(0.0f,scale))*0.125f);
  }
 }
}
