package local.puchamon.radar.mixin;
import local.puchamon.radar.ClientRadar;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Augment only Xaero's cosmetic iterator. ClientWorld's actual entities are never modified. */
@Mixin(targets="xaero.hud.minimap.radar.state.RadarStateUpdater",remap=false)
public abstract class RadarEntitiesMixin {
 @Redirect(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/class_638;method_18112()Ljava/lang/Iterable;"),remap=false,require=1)
 private Iterable<class_1297> puchamon$radarOnly(class_638 world){return ClientRadar.augment(world,world.method_18112());}
}
